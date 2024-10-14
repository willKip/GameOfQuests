import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

public class MainTest {
    @Test
    @DisplayName("Decks are set up with the correct number of cards")
    void RESP_01_TEST_01() {
        Game game = new Game();
        game.initDecks();

        int adventureDeckSize = game.getAdventureDeck().totalSize();
        int eventDeckSize = game.getEventDeck().totalSize();

        // Adventure deck should have 100x cards, Event deck should have 17x cards.
        assertAll("Deck size", () -> assertEquals(100, adventureDeckSize, "Adventure deck size"),
                  () -> assertEquals(17, eventDeckSize, "Event deck size"));
    }

    @Test
    @DisplayName("Can draw cards of the correct type from each deck, which are updated accordingly")
    void RESP_01_TEST_02() {
        Game game = new Game();
        game.initDecks();

        Deck adventureDeck = game.getAdventureDeck();
        Deck eventDeck = game.getEventDeck();

        final int CARDS_TO_DRAW = 10;

        final int INIT_DECK_SIZE_ADVENTURE = adventureDeck.drawPileSize();
        final int INIT_DECK_SIZE_EVENT = eventDeck.drawPileSize();

        final int DRAWN_DECK_SIZE_ADVENTURE = INIT_DECK_SIZE_ADVENTURE - CARDS_TO_DRAW;
        final int DRAWN_DECK_SIZE_EVENT = INIT_DECK_SIZE_EVENT - CARDS_TO_DRAW;

        final List<Card.CardType> ADVENTURE_DECK_CARD_TYPES = Arrays.asList(Card.CardType.FOE, Card.CardType.WEAPON);
        final List<Card.CardType> EVENT_DECK_CARD_TYPES = Arrays.asList(Card.CardType.QUEST, Card.CardType.EVENT);

        // NOTE: Junit 5 with no added dependencies does not support a more verbose way to assert that a condition
        // matches 'one of' several valid states. We are prioritising minimal dependencies for the assignment, so the
        // below asserts to check valid card type cannot display what the given type was.

        // Draw Adventure cards and ensure only the Adventure deck is affected
        for (int i = 0; i < CARDS_TO_DRAW; i++) {
            Card drawnAdventure = game.drawAdventureCard();
            Card.CardType drawnAdventureCardType = drawnAdventure.getCardType();
            assertNotNull(drawnAdventureCardType, "Check that drawn Adventure card doesn't return null for its type");
            assertTrue(ADVENTURE_DECK_CARD_TYPES.contains(drawnAdventureCardType),
                       "Drawn Adventure card is a valid type (Foe/Weapon)");
        }
        assertEquals(DRAWN_DECK_SIZE_ADVENTURE, adventureDeck.totalSize(),
                     "Drawn Adventure cards are removed from Adventure deck");
        assertEquals(INIT_DECK_SIZE_EVENT, eventDeck.totalSize(),
                     "Event deck is not affected by draws from the Adventure deck");

        // Draw Event Cards and ensure only the Event deck is affected
        for (int i = 0; i < CARDS_TO_DRAW; i++) {
            Card drawnEvent = game.drawEventCard();
            Card.CardType drawnEventCardType = drawnEvent.getCardType();
            assertNotNull(drawnEventCardType, "Check that drawn Event card doesn't return null for its type");
            assertTrue(EVENT_DECK_CARD_TYPES.contains(drawnEvent.getCardType()),
                       "Drawn Event card is a valid type (Quest/Event)");
        }
        assertEquals(DRAWN_DECK_SIZE_ADVENTURE, adventureDeck.totalSize(),
                     "Adventure deck is not affected by draws from the Event deck");
        assertEquals(DRAWN_DECK_SIZE_EVENT, eventDeck.totalSize(), "Drawn Event cards are removed from Event deck");
    }

    @Test
    @DisplayName("Drawn cards can be discarded to the correct discard piles")
    void RESP_01_TEST_03() {
        Game game = new Game();
        game.initDecks();

        Deck adventureDeck = game.getAdventureDeck();
        Deck eventDeck = game.getEventDeck();

        // Ensure discard piles start out empty
        assertEquals(0, adventureDeck.discardPileSize(), "Adventure deck is set up with empty discard pile");
        assertEquals(0, eventDeck.discardPileSize(), "Event deck is set up with empty discard pile");

        final int CARDS_TO_DRAW = 5;

        List<Card> drawnAdventureCards = new ArrayList<>();
        List<Card> drawnEventCards = new ArrayList<>();

        // Draw cards from each deck
        for (int i = 0; i < CARDS_TO_DRAW; i++) {
            drawnAdventureCards.add(game.drawAdventureCard());
            drawnEventCards.add(game.drawEventCard());
        }

        // Check that discard piles of each deck are updated correctly from discard operations
        for (Card c : drawnAdventureCards) {
            game.discard(c);
        }
        assertEquals(CARDS_TO_DRAW, adventureDeck.discardPileSize(),
                     "Discard pile of Adventure deck updated after Adventure cards discarded");
        assertEquals(0, eventDeck.discardPileSize(),
                     "Discard pile of Event deck unaffected after Adventure cards discarded");

        for (Card c : drawnEventCards) {
            game.discard(c);
        }
        assertEquals(CARDS_TO_DRAW, adventureDeck.discardPileSize(),
                     "Discard pile of Adventure deck unaffected after Event cards discarded");
        assertEquals(CARDS_TO_DRAW, eventDeck.discardPileSize(),
                     "Discard pile of Event deck updated after Event cards discarded");
    }

    @Test
    @DisplayName("When decks run out of cards, they refresh, shuffling the discard pile and using it as the deck")
    void RESP_01_TEST_04() {
        Game game = new Game();
        game.initDecks();

        Card lastCard; // Temporary variable to hold cards

        /* General case: Deck refreshing when out of cards (but there are some in discard pile) */
        Deck adventureDeck = game.getAdventureDeck();
        Deck eventDeck = game.getEventDeck();

        final int INIT_DRAWPILE_SIZE_ADVENTURE = adventureDeck.drawPileSize();

        // Draw, then immediately discard, all but 1 card from the Adventure deck
        for (int i = 0; i < INIT_DRAWPILE_SIZE_ADVENTURE - 1; i++) {
            game.discard(game.drawAdventureCard());
        }

        assertEquals(1, adventureDeck.drawPileSize(), "One card remaining in Adventure deck draw pile");
        assertEquals(INIT_DRAWPILE_SIZE_ADVENTURE - 1, adventureDeck.discardPileSize(),
                     "All but one card in Adventure deck discard pile");

        // Trigger refresh by drawing last card from deck (note that this card is separate from the deck when it
        // refreshes; it is not included when the discard pile is shuffled back into the draw pile.)
        lastCard = game.drawAdventureCard();
        assertEquals(0, adventureDeck.discardPileSize(), "Discard pile empty after refresh");
        assertEquals(INIT_DRAWPILE_SIZE_ADVENTURE - 1, adventureDeck.drawPileSize(),
                     "All but the one drawn card are in Adventure deck draw pile after refresh");

        /* Edge case: Deck not attempting to refresh if out of cards, refreshing as soon as discard pile gains a card */
        final int INIT_DRAWPILE_SIZE_EVENT = eventDeck.drawPileSize();

        // Draw every card (but do not discard them) from the Event deck
        for (int i = 0; i < INIT_DRAWPILE_SIZE_EVENT; i++) {
            lastCard = game.drawEventCard();
        }

        assertEquals(0, eventDeck.totalSize(), "Event deck entirely empty");

        // Discard the last drawn Event card, triggering a refresh from it.
        game.discard(lastCard);
        assertEquals(0, eventDeck.discardPileSize(), "Event deck discard pile is empty after refresh");
        assertEquals(1, eventDeck.drawPileSize(),
                     "Event deck draw pile received the single card in discard pile after refresh");
    }

    @Test
    @DisplayName("Decks set up with correct card distributions")
    void RESP_01_TEST_05() {
        Game game = new Game();
        game.initDecks();

        // Maps for each card type, detailing the correct number of occurrences for each specific card of a type.
        // (e.g. entry("Dagger", 6) means there should be 6 Daggers in the adventure deck;
        //       entry(2, 3) means there should be 3 Q2 in the event deck.)
        final Map<Integer, Integer> FOE_MAP = Map.ofEntries(entry(5, 8), entry(10, 7), entry(15, 8), entry(20, 7),
                                                            entry(25, 7), entry(30, 4), entry(35, 4), entry(40, 2),
                                                            entry(50, 2), entry(70, 1));
        final Map<String, Integer> WEAPON_MAP = Map.ofEntries(entry("Dagger", 6), entry("Horse", 12),
                                                              entry("Sword", 16), entry("Battle-axe", 8),
                                                              entry("Lance", 6), entry("Excalibur", 2));

        final Map<Integer, Integer> QUEST_MAP = Map.ofEntries(entry(2, 3), entry(3, 4), entry(4, 3), entry(5, 2));
        final Map<String, Integer> EVENT_MAP = Map.ofEntries(entry("Plague", 1), entry("Queen's Favor", 2),
                                                             entry("Prosperity", 2));

        // Maps for each card type that will store the number of occurrences for each specific card of a type.
        Map<Integer, Integer> foeOccurrences = new HashMap<>();
        Map<String, Integer> weaponOccurrences = new HashMap<>();
        Map<Integer, Integer> questOccurrences = new HashMap<>();
        Map<String, Integer> eventOccurrences = new HashMap<>();

        // Initialise occurrence count maps
        for (Map.Entry<Integer, Integer> entry : FOE_MAP.entrySet()) {
            foeOccurrences.put(entry.getKey(), 0);
        }
        for (Map.Entry<String, Integer> entry : WEAPON_MAP.entrySet()) {
            weaponOccurrences.put(entry.getKey(), 0);
        }
        for (Map.Entry<Integer, Integer> entry : QUEST_MAP.entrySet()) {
            questOccurrences.put(entry.getKey(), 0);
        }
        for (Map.Entry<String, Integer> entry : EVENT_MAP.entrySet()) {
            eventOccurrences.put(entry.getKey(), 0);
        }

        // Adventure deck
        final int ADVENTURE_DECK_SIZE = game.getAdventureDeck().totalSize();
        for (int i = 0; i < ADVENTURE_DECK_SIZE; i++) {

            Card drawnCard = game.drawAdventureCard();

            Card.CardType drawnCardType = drawnCard.getCardType();

            if (drawnCardType == Card.CardType.FOE) {
                int drawnCardValue = drawnCard.getValue();
                Integer prevCount = foeOccurrences.get(drawnCardValue);

                // If there is no matching key in occurrences map, this card's key does not exist in a standard game
                assertNotNull(prevCount, "Invalid Foe card");

                foeOccurrences.put(drawnCardValue, prevCount + 1);
            } else if (drawnCardType == Card.CardType.WEAPON) {
                String drawnCardName = drawnCard.getName();
                Integer prevCount = weaponOccurrences.get(drawnCardName);

                // If there is no matching key in occurrences map, this card's key does not exist in a standard game
                assertNotNull(prevCount, "Invalid Weapon card");

                weaponOccurrences.put(drawnCardName, prevCount + 1);
            }
        }

        final int EVENT_DECK_SIZE = game.getEventDeck().totalSize();

        // Event deck
        for (int i = 0; i < EVENT_DECK_SIZE; i++) {
            Card drawnCard = game.drawEventCard();

            Card.CardType drawnCardType = drawnCard.getCardType();

            if (drawnCardType == Card.CardType.QUEST) {
                int drawnCardValue = drawnCard.getValue();
                Integer prevCount = questOccurrences.get(drawnCardValue);

                // If there is no matching key in occurrences map, this card's key does not exist in a standard game
                assertNotNull(prevCount, "Invalid Quest card");

                questOccurrences.put(drawnCardValue, prevCount + 1);
            } else if (drawnCardType == Card.CardType.EVENT) {
                String drawnCardName = drawnCard.getName();
                Integer prevCount = eventOccurrences.get(drawnCardName);

                // If there is no matching key in occurrences map, this card's key does not exist in a standard game
                assertNotNull(prevCount, "Invalid Event (E) card");

                eventOccurrences.put(drawnCardName, prevCount + 1);
            }
        }

        // Confirm correct distributions
        for (Map.Entry<Integer, Integer> entry : FOE_MAP.entrySet()) {
            final int FOE_VALUE = entry.getKey();
            final int FOE_COUNT_TARGET = entry.getValue();
            final int actualFoeCount = foeOccurrences.get(FOE_VALUE);

            assertEquals(FOE_COUNT_TARGET, actualFoeCount, " 'F" + FOE_VALUE + "' card count");
        }
        for (Map.Entry<String, Integer> entry : WEAPON_MAP.entrySet()) {
            final String WEAPON_NAME = entry.getKey();
            final int WEAPON_COUNT_TARGET = entry.getValue();
            final int actualWeaponCount = weaponOccurrences.get(WEAPON_NAME);

            assertEquals(WEAPON_COUNT_TARGET, actualWeaponCount, " '" + WEAPON_NAME + "' card count");
        }
        for (Map.Entry<Integer, Integer> entry : QUEST_MAP.entrySet()) {
            final int QUEST_VALUE = entry.getKey();
            final int QUEST_COUNT_TARGET = entry.getValue();
            final int actualQuestCount = questOccurrences.get(QUEST_VALUE);

            assertEquals(QUEST_COUNT_TARGET, actualQuestCount, " 'Q" + QUEST_VALUE + "' card count");
        }
        for (Map.Entry<String, Integer> entry : EVENT_MAP.entrySet()) {
            final String EVENT_NAME = entry.getKey();
            final int EVENT_COUNT_TARGET = entry.getValue();
            final int actualEventCount = eventOccurrences.get(EVENT_NAME);

            assertEquals(EVENT_COUNT_TARGET, actualEventCount, " '" + EVENT_NAME + "' card count");
        }
    }

    @Test
    @DisplayName("Game sets up exactly 4 players")
    void RESP_02_TEST_01() {
        Game game = new Game();
        game.initPlayers();
        assertEquals(4, game.getPlayerCount());
    }

    @Test
    @DisplayName("P1 is the first player in the turn order")
    void RESP_02_TEST_02() {
        Game game = new Game();
        game.initPlayers();

        Player firstPlayer = game.getCurrentPlayer();
        String firstPlayerID = firstPlayer.getID();

        assertEquals("P1", firstPlayerID);
    }

    @ParameterizedTest
    @DisplayName("Game can provide the next Player in the turn order [P1>P2>P3>P4>P1>...]")
    @ValueSource(strings = {"P1", "P2", "P3", "P4"})
    void RESP_02_TEST_03(String startingPlayerID) {
        Game game = new Game();
        game.initPlayers();

        String nextPlayerID = "";

        switch (startingPlayerID) {
            case "P1" -> nextPlayerID = "P2";
            case "P2" -> nextPlayerID = "P3";
            case "P3" -> nextPlayerID = "P4";
            case "P4" -> nextPlayerID = "P1";
        }

        Player startingPlayer = game.getPlayerByID(startingPlayerID);
        Player nextPlayer = game.getNextPlayer(startingPlayer);

        assertEquals(nextPlayerID, nextPlayer.getID());
    }

    @ParameterizedTest
    @DisplayName("Game can provide an ordered list of Players starting from the current Player")
    @ValueSource(strings = {"P1", "P2", "P3", "P4"})
    void RESP_02_TEST_04(String currPlayerID) {
        Game game = new Game();
        game.initPlayers();

        Player currPlayer = game.getPlayerByID(currPlayerID);

        game.setCurrentPlayer(currPlayer);

        List<Player> orderedPlayerList = game.getPlayersStartingCurrent();
        assertEquals(orderedPlayerList.size(), game.getPlayerCount(),
                     "Ordered Player list should include all players in the game, once each");
        assertNotEquals(0, orderedPlayerList.size(), "Player list should not be empty");

        // Iterate through list save for the last element, checking that the ordering of players is consistent
        for (int i = 0; i < orderedPlayerList.size() - 1; i++) {
            Player thisPlayerInList = orderedPlayerList.get(i);
            Player nextPlayerInList = orderedPlayerList.get(i + 1);

            Player trueNextPlayer = game.getNextPlayer(thisPlayerInList);

            assertEquals(trueNextPlayer.getID(), nextPlayerInList.getID(),
                         trueNextPlayer.getID() + "Player after " + thisPlayerInList.getID());
        }
    }

    @Test
    @DisplayName("Each player is set up with 12 cards")
    void RESP_02_TEST_05() {
        Game game = new Game();
        game.initPlayers();

        List<Player> orderedPlayerList = game.getPlayersStartingCurrent();
        assertNotEquals(0, orderedPlayerList.size(), "Player list should not be empty");

        for (final Player p : orderedPlayerList) {
            assertEquals(12, p.getHandSize(), p.getID() + " should start with 0 shields");
        }
    }

    @Test
    @DisplayName("Game can 'rig' player hands")
    void RESP_02_TEST_06() {
        Game game = new Game();
        game.initPlayers();

        Player player = game.getCurrentPlayer();

        // Create a rigged hand, adding the cards in proper sorted order
        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 15));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 15));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Dagger", "D", 5));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Sword", "S", 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Sword", "S", 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", "H", 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", "H", 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", "B", 15));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", "B", 15));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Lance", "L", 20));

        player.rigHand(riggedCards);

        assertEquals(riggedCards, player.getHand());
    }

    @Test
    @DisplayName("A player's hand can be returned as a string with correct card ordering")
    void RESP_03_TEST_01() {
        Game game = new Game();
        game.initPlayers();

        Player player = game.getCurrentPlayer();

        // Create a rigged hand with cards added in the wrong order.
        // Hand card ordering rules:
        //   Foes always come before Weapons.
        //   Swords must come before Horses despite the values being the same.
        //   Ordered by value ascending otherwise.
        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.WEAPON, "Excalibur", "E", 30));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", "H", 10));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 5));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Sword", "S", 10));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", "H", 10));
        player.rigHand(riggedCards);

        final String correctHandOrder = "F5 F10 S10 H10 H10 E30";

        assertEquals(correctHandOrder, player.getHandString());
    }

    @Test
    @DisplayName("Game can accurately indicate the current turn player after a turn change occurs")
    void RESP_03_TEST_02() {
        StringWriter output = new StringWriter();

        Game game = new Game(new PrintWriter(output));
        game.initPlayers();

        // First player in turn order
        Player currPlayer = game.getCurrentPlayer();

        game.printPlayerTurnStart();
        String outputString = output.toString();

        boolean initPlayerTurnDisplayed = outputString.contains(currPlayer.getID());

        // Next player in turn order
        currPlayer = game.getNextPlayer(currPlayer);
        game.setCurrentPlayer(currPlayer);

        game.printPlayerTurnStart();
        outputString = output.toString();

        boolean nextPlayerTurnDisplayed = outputString.contains(currPlayer.getID());

        assertAll("Player indicator at turn start",
                  () -> assertTrue(initPlayerTurnDisplayed, "Initial player displayed at turn start"),
                  () -> assertTrue(nextPlayerTurnDisplayed,
                                   "Next player displayed at turn start after it becomes their turn"));
    }

    @Test
    @DisplayName("Game can indicate the player of the current turn AND display that player's hand")
    void RESP_03_TEST_03() {
        StringWriter output = new StringWriter();

        Game game = new Game(new PrintWriter(output));
        game.initPlayers();

        Player currentPlayer = game.getCurrentPlayer();

        // Create a rigged hand with cards added in a random order.
        ArrayList<Card> riggedCards = new ArrayList<>();

        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 15));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 15));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 40));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Dagger", "D", 5));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Sword", "S", 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", "H", 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", "H", 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", "B", 15));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", "B", 15));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Excalibur", "E", 30));

        Collections.shuffle(riggedCards);
        currentPlayer.rigHand(riggedCards);

        final String correctHandOrder = "F5 F5 F15 F15 F40 D5 S10 H10 H10 B15 B15 E30";

        game.printPlayerTurnStart();

        final String outputString = output.toString();

        assertAll("Turn start printed information",
                  () -> assertTrue(outputString.contains(currentPlayer.getID()), "Prints current player"),
                  () -> assertTrue(outputString.contains(correctHandOrder), "Prints current player's hand"));
    }

    @Test
    @DisplayName("Each player is set up with 0 shields")
    void RESP_04_TEST_01() {
        Game game = new Game();
        game.initPlayers();

        List<Player> orderedPlayerList = game.getPlayersStartingCurrent();
        assertNotEquals(0, orderedPlayerList.size(), "Player list should not be empty");

        for (final Player p : orderedPlayerList) {
            assertEquals(0, p.getShields(), p.getID() + " should start with 0 shields");
        }
    }

    @Test
    @DisplayName("When no players have 7 or more shields, no players are evaluated as winners")
    void RESP_04_TEST_02() {
        Game game = new Game();
        game.initPlayers();

        Player p1 = game.getPlayerByID("P1");
        Player p2 = game.getPlayerByID("P2");
        Player p3 = game.getPlayerByID("P3");
        Player p4 = game.getPlayerByID("P4");

        assertNotNull(p1);
        assertNotNull(p2);
        assertNotNull(p3);
        assertNotNull(p4);

        // P1 remains at 0 shields
        p2.addShields(1);
        p3.addShields(2);
        p4.addShields(6);

        List<Player> winners = game.getWinners();
        assertNotNull(winners);
        assertEquals(0, winners.size(), "Number of winners should be 0");
    }

    @ParameterizedTest
    @DisplayName("When there is exactly one player that has 7 or more shields, only they are evaluated as the winner")
    @ValueSource(strings = {"P1", "P2", "P3", "P4"})
    void RESP_04_TEST_03(String winningPlayerID) {
        Game game = new Game();
        game.initPlayers();

        Player winningPlayer = game.getPlayerByID(winningPlayerID);
        assertNotNull(winningPlayer, "Winning player should not be null");

        winningPlayer.addShields(7);

        List<Player> winners = game.getWinners();

        // Assert that there is only one winner, and it is the player that should be winning
        assertNotNull(winners);
        assertTrue(winners.size() == 1 && Objects.equals(winners.getLast().getID(), winningPlayerID));
    }

    @Test
    @DisplayName("When there are multiple players that have 7 or more shields, they are all evaluated as winners")
    void RESP_04_TEST_04() {
        Game game = new Game();
        game.initPlayers();

        Player p1 = game.getPlayerByID("P1");
        Player p2 = game.getPlayerByID("P2");
        Player p3 = game.getPlayerByID("P3");
        Player p4 = game.getPlayerByID("P4");

        assertNotNull(p1);
        assertNotNull(p2);
        assertNotNull(p3);
        assertNotNull(p4);

        // P2 and P4 should win
        // P1 remains at 0 shields
        p2.addShields(7);
        p3.addShields(6);
        p4.addShields(50);

        List<Player> winners = game.getWinners();
        assertNotNull(winners);
        assertTrue(winners.size() == 2 && winners.containsAll(Arrays.asList(p2, p4)));
    }

    @Test
    @DisplayName("Game can display a list of players as winners in console")
    void RESP_04_TEST_05() {
        // Note: Termination does not need to be tested for Assignment 1.

        StringWriter output = new StringWriter();

        Game game = new Game(new PrintWriter(output));
        game.initPlayers();

        ArrayList<Player> winners = new ArrayList<>();
        winners.add(game.getPlayerByID("P1"));
        winners.add(game.getPlayerByID("P3"));

        game.printGameEnd(winners);

        final String outputString = output.toString();

        assertAll("Victory output", () -> assertTrue(outputString.contains("Winner"), "Has 'Winner' string"),
                  () -> assertTrue(outputString.contains("P1"), "Has all winners"),
                  () -> assertTrue(outputString.contains("P3"), "Has all winners"));
    }

    @Test
    @DisplayName("Player can trim their hand when there is 1 over the max")
    void RESP_05_TEST_01() {
        // List of 13 cards (in order), 1 over the max of 12
        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 1));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 2));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 3));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 4));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 6));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 7));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Dagger", "D", 8));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Sword", "S", 9));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", "H", 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", "B", 11));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Lance", "L", 12));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Excalibur", "E", 13));

        String expectedHand = "F1 F2 F3 F5 F6 F7 D8 S9 H10 B11 L12 E13"; // Foe value 4 card removed

        String input = "4\n"; // Input to remove the 'F4' card
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initPlayers();

        Player player = game.getCurrentPlayer();
        player.rigHand(Collections.emptyList()); // Empty the player's existing hand
        player.rigHand(riggedCards);

        assertAll("Player trims 1 card", () -> assertEquals(12, player.getHandSize(), "Player hand size trimmed to 12"),
                  () -> assertEquals(expectedHand, player.getHandString(), "Correct card removed"));
    }

    @Test
    @DisplayName("Player can trim their hand when there are 3 over the max")
    void RESP_05_TEST_02() {
        // List of 15 cards (in order), 3 over the max of 12
        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 1));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 2));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 3));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 4));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 6));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 7));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 8));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 9));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 10));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 11));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 12));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 13));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 14));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 15));

        String expectedHand = "F3 F4 F5 F6 F7 F8 F9 F10 F11 F12 F13 F14";

        String input = "1\n1\n13\n"; // Input to remove 'F1', 'F2', 'F15', in order.
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initPlayers();

        Player player = game.getCurrentPlayer();
        player.rigHand(Collections.emptyList()); // Empty the player's existing hand
        player.rigHand(riggedCards);

        assertAll("Player trims 3 cards",
                  () -> assertEquals(12, player.getHandSize(), "Player hand size trimmed to 12"),
                  () -> assertEquals(expectedHand, player.getHandString(), "Correct cards removed"));
    }

    @ParameterizedTest
    @DisplayName("Game can indicate the end of a player's turn and clear the display when <return> key is pressed.")
    @ValueSource(strings = {"P1", "P2", "P3", "P4"})
    void RESP_06_TEST_01(String playerID) {
        String input = "\n"; // Input to remove the 'F4' card
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initPlayers();

        Player player = game.getPlayerByID(playerID);
        assertNotNull(player);

        game.printTurnEndOf(player);

        final String outputString = output.toString();

        assertAll("Player " + playerID + "'s turn ends",
                  () -> assertTrue(outputString.contains(playerID), "Indicates correct player's end of turn"),
                  () -> assertTrue(outputString.contains("Press <return> to continue... > " + "\n".repeat(30)),
                                   "Indicates to press <return> to clear the display, followed by at least 30 " +
                                           "newlines to achieve this"));
    }

    @Test
    @DisplayName("Game can draw and display a Q card from the Event deck")
    void RESP_07_TEST_01() {
        StringWriter output = new StringWriter();

        Game game = new Game(new PrintWriter(output));
        game.initPlayers();

        Card eventCard = game.drawEventCard();
        assertNotNull(eventCard);

        // Overwrite event card with our own Quest card
        eventCard = new Card(Card.CardType.QUEST, "Quest", "Q", 5);

        game.printEventCard(eventCard);

        final String outputString = output.toString();

        assertAll("Display a Q card draw",
                  () -> assertTrue(outputString.contains("Drawing an Event card..."), "Drawing Event card dialogue"),
                  () -> assertTrue(outputString.contains("A Quest of 5 stages!"), "Q card displayed"));
    }

    @ParameterizedTest
    @DisplayName("Game can draw and display each E card from the Event deck")
    @ValueSource(strings = {"Plague", "Queen's Favor", "Prosperity"})
    void RESP_07_TEST_02(String eCardName) {
        StringWriter output = new StringWriter();

        Game game = new Game(new PrintWriter(output));
        game.initPlayers();

        Card eventCard = game.drawEventCard();
        assertNotNull(eventCard);

        // Overwrite event card with our own Quest card
        eventCard = new Card(Card.CardType.EVENT, eCardName, "E", 2);

        game.printEventCard(eventCard);

        String eventDesc = "";
        switch (eCardName) {
            case "Plague" -> eventDesc = "Current player loses 2 Shields";
            case "Queen's Favor" -> eventDesc = "Current player draws 2 Adventure cards";
            case "Prosperity" -> eventDesc = "All players draw 2 Adventure cards";
        }
        final String finalEventDesc = eventDesc;

        final String outputString = output.toString();

        assertAll("Display an E card draw",
                  () -> assertTrue(outputString.contains("Drawing an Event card..."), "Drawing Event card dialogue"),
                  () -> assertTrue(outputString.contains("Event: " + eCardName + " - " + finalEventDesc),
                                   "Event " + eCardName + " displayed"));
    }

    @Test
    @DisplayName("Event: Plague will make the drawing player lose 2 shields, to a minimum of 0")
    void RESP_08_TEST_01() {
        StringWriter output = new StringWriter();

        Game game = new Game(new PrintWriter(output));
        game.initPlayers();

        Player p1 = game.getPlayerByID("P1");
        Player p2 = game.getPlayerByID("P2");
        Player p3 = game.getPlayerByID("P3");
        Player p4 = game.getPlayerByID("P4");

        assertNotNull(p1);
        assertNotNull(p2);
        assertNotNull(p3);
        assertNotNull(p4);

        // P1 remains at 0 shields
        p2.addShields(1);
        p3.addShields(2);
        p4.addShields(6);

        Card plagueCard = new Card(Card.CardType.EVENT, "Plague", "E", 2);

        game.setCurrentPlayer(p2);
        game.doEvent(plagueCard);

        game.setCurrentPlayer(p4);
        game.doEvent(plagueCard);

        assertAll("Plague event triggers",
                  () -> assertEquals(0, p1.getShields(), "P1 didn't draw Plague, shields should stay at 0"),
                  () -> assertEquals(0, p2.getShields(), "P2 drew Plague, shields should be 1 -> 0"),
                  () -> assertEquals(2, p3.getShields(), "P3 didn't draw Plague, shields should stay at 2"),
                  () -> assertEquals(4, p4.getShields(), "P2 drew Plague, shields should be 6 -> 4"));
    }

    @Test
    @DisplayName("Event: Queen's Favor will make the drawing player draw 2 cards, possibly trimming")
    void RESP_08_TEST_02() {
        String input = "1\n"; // Trim one card after drawing 2 to reach 13 cards in hand
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initPlayers();

        Player player = game.getPlayerByID("P2"); // Arbitrary player choice
        assertNotNull(player);

        // List of 11 cards (in order), 1 under the max of 11
        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 1));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 2));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 3));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 4));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 6));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 7));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 8));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 9));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 10));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 11));

        player.rigHand(riggedCards);

        Card queenFavorCard = new Card(Card.CardType.EVENT, "Queen's Favor", "E", 2);

        game.setCurrentPlayer(player);
        game.doEvent(queenFavorCard);

        assertEquals(12, player.getHandSize(),
                     "Player drew 2 cards from Queen's Favour while having 11 cards in hand, trimmed to 12");
    }

    @Test
    @DisplayName("Event: Prosperity will make every player draw 2 cards, possibly trimming")
    void RESP_08_TEST_03() {
        String input = "\n1\n\n1\n1\n\n\n"; // P2 next, P3 trim 1, P3 next, P4 trim 1, P4 trim 1, P4 next, P1 next
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initPlayers();

        Player p1 = game.getPlayerByID("P1");
        Player p2 = game.getPlayerByID("P2");
        Player p3 = game.getPlayerByID("P3");
        Player p4 = game.getPlayerByID("P4");

        assertNotNull(p1);
        assertNotNull(p2);
        assertNotNull(p3);
        assertNotNull(p4);

        ArrayList<Card> riggedCards;

        // Empty P1
        p1.rigHand(Collections.emptyList());

        // P2 gets 3 cards
        riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 1));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 2));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 3));
        p2.rigHand(riggedCards);

        // P3 gets 11 cards
        riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 1));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 2));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 3));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 4));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 6));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 7));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 8));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 9));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 10));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 11));
        p3.rigHand(riggedCards);

        // P4 gets 12 cards
        riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 1));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 2));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 3));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 4));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 6));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 7));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 8));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 9));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 10));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 11));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", "F", 12));
        p4.rigHand(riggedCards);

        // All players get 2 cards each
        Card prosperityCard = new Card(Card.CardType.EVENT, "Prosperity", "E", 2);

        game.setCurrentPlayer(p2);
        game.doEvent(prosperityCard);

        assertAll("Prosperity event triggers", () -> assertEquals(2, p1.getHandSize(), "P1 gains 2 cards from 0"),
                  () -> assertEquals(5, p2.getHandSize(), "P2 gains 2 cards from 3"),
                  () -> assertEquals(12, p3.getHandSize(), "P3 gains 2 cards from 11, trims 1"),
                  () -> assertEquals(12, p4.getHandSize(), "P4 gains 2 cards from 12, trims 2"));
    }

    @Test
    @DisplayName("Game prompts players to sponsor, gets a sponsor")
    void RESP_09_TEST_01() {
        String input = "n\n\nn\n\ny\n"; // P1 decline, P2 decline, P3 accept
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initPlayers();

        Player sponsor = game.findSponsor(5);

        assertTrue(output.toString().contains("Would you like to sponsor this Quest of 5 stages?"), "Quest prompt");
        assertNotNull(sponsor, "A sponsor should be found");
        assertEquals("P3", sponsor.getID(), "The correct sponsor is returned");
    }

    @Test
    @DisplayName("Game prompts players to sponsor, all decline")
    void RESP_09_TEST_02() {
        String input = "n\n\nn\n\nn\n\nn\n\n"; // P1 decline, P2 decline, P3 decline, P4 decline, all with transitions
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initPlayers();

        Player sponsor = game.findSponsor(10);

        assertNull(sponsor, "A sponsor should not be found");
    }
}
