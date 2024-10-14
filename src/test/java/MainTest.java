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

        Game game = new Game();
        game.initPlayers();

        // First player in turn order
        Player currPlayer = game.getCurrentPlayer();

        game.printPlayerTurnStart(new PrintWriter(output));
        String outputString = output.toString();

        boolean initPlayerTurnDisplayed = outputString.contains(currPlayer.getID());

        // Next player in turn order
        currPlayer = game.getNextPlayer(currPlayer);
        game.setCurrentPlayer(currPlayer);

        game.printPlayerTurnStart(new PrintWriter(output));
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

        Game game = new Game();
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

        game.printPlayerTurnStart(new PrintWriter(output));

        final String outputString = output.toString();

        assertAll("Turn start printed information",
                  () -> assertTrue(outputString.contains(currentPlayer.getID()), "Prints current player"),
                  () -> assertTrue(outputString.contains(correctHandOrder), "Prints current player's hand"));
    }
}
