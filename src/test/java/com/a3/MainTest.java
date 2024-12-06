package com.a3;

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
        assertAll("Deck size",
                  () -> assertEquals(100, adventureDeckSize, "Adventure deck size"),
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
        assertEquals(DRAWN_DECK_SIZE_ADVENTURE,
                     adventureDeck.totalSize(),
                     "Drawn Adventure cards are removed from Adventure deck");
        assertEquals(INIT_DECK_SIZE_EVENT,
                     eventDeck.totalSize(),
                     "Event deck is not affected by draws from the Adventure deck");

        // Draw Event Cards and ensure only the Event deck is affected
        for (int i = 0; i < CARDS_TO_DRAW; i++) {
            Card drawnEvent = game.drawEventCard();
            Card.CardType drawnEventCardType = drawnEvent.getCardType();
            assertNotNull(drawnEventCardType, "Check that drawn Event card doesn't return null for its type");
            assertTrue(EVENT_DECK_CARD_TYPES.contains(drawnEvent.getCardType()),
                       "Drawn Event card is a valid type (Quest/Event)");
        }
        assertEquals(DRAWN_DECK_SIZE_ADVENTURE,
                     adventureDeck.totalSize(),
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
        assertEquals(CARDS_TO_DRAW,
                     adventureDeck.discardPileSize(),
                     "Discard pile of Adventure deck updated after Adventure cards discarded");
        assertEquals(0,
                     eventDeck.discardPileSize(),
                     "Discard pile of Event deck unaffected after Adventure cards discarded");

        for (Card c : drawnEventCards) {
            game.discard(c);
        }
        assertEquals(CARDS_TO_DRAW,
                     adventureDeck.discardPileSize(),
                     "Discard pile of Adventure deck unaffected after Event cards discarded");
        assertEquals(CARDS_TO_DRAW,
                     eventDeck.discardPileSize(),
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
        assertEquals(INIT_DRAWPILE_SIZE_ADVENTURE - 1,
                     adventureDeck.discardPileSize(),
                     "All but one card in Adventure deck discard pile");

        // Trigger refresh by drawing last card from deck (note that this card is separate from the deck when it
        // refreshes; it is not included when the discard pile is shuffled back into the draw pile.)
        lastCard = game.drawAdventureCard();
        assertEquals(0, adventureDeck.discardPileSize(), "Discard pile empty after refresh");
        assertEquals(INIT_DRAWPILE_SIZE_ADVENTURE - 1,
                     adventureDeck.drawPileSize(),
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
        assertEquals(1,
                     eventDeck.drawPileSize(),
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
        final Map<Integer, Integer> FOE_MAP = Map.ofEntries(entry(5, 8),
                                                            entry(10, 7),
                                                            entry(15, 8),
                                                            entry(20, 7),
                                                            entry(25, 7),
                                                            entry(30, 4),
                                                            entry(35, 4),
                                                            entry(40, 2),
                                                            entry(50, 2),
                                                            entry(70, 1));
        final Map<String, Integer> WEAPON_MAP = Map.ofEntries(entry("Dagger", 6),
                                                              entry("Horse", 12),
                                                              entry("Sword", 16),
                                                              entry("Battle-axe", 8),
                                                              entry("Lance", 6),
                                                              entry("Excalibur", 2));

        final Map<Integer, Integer> QUEST_MAP = Map.ofEntries(entry(2, 3), entry(3, 4), entry(4, 3), entry(5, 2));
        final Map<String, Integer> EVENT_MAP = Map.ofEntries(entry("Plague", 1),
                                                             entry("Queen's Favor", 2),
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
    @DisplayName("Game can rig the draws of the decks")
    void RESP_01_TEST_06() {
        Game game = new Game();
        game.initDecks();

        ArrayList<Card> rigAdvDeck = new ArrayList<>();
        rigAdvDeck.addFirst(new Card(Card.CardType.FOE, "Foe", 'F', 30));
        rigAdvDeck.addFirst(new Card(Card.CardType.WEAPON, "Sword", 'S', 10));
        rigAdvDeck.addFirst(new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 15));
        game.getAdventureDeck().addToDrawPile(rigAdvDeck);

        ArrayList<Card> rigEvDeck = new ArrayList<>();
        rigEvDeck.addFirst(new Card(Card.CardType.QUEST, "Quest", 'Q', 4));
        rigEvDeck.addFirst(new Card(Card.CardType.EVENT, "Queen's Favor", 'E', 2));
        game.getEventDeck().addToDrawPile(rigEvDeck);

        // Draw order will be reversed, since decks are Last In First Out
        List<Card> riggedAdvDraws = game.drawAdventureCards(rigAdvDeck.size());
        assertEquals(rigAdvDeck, riggedAdvDraws.reversed());

        List<Card> riggedEvDraws = new ArrayList<>(List.of(game.drawEventCard(), game.drawEventCard()));
        assertEquals(rigEvDeck, riggedEvDraws.reversed());
    }

    @Test
    @DisplayName("Game sets up exactly 4 players")
    void RESP_02_TEST_01() {
        Game game = new Game();
        game.initGame();
        assertEquals(4, game.getPlayerCount());
    }

    @Test
    @DisplayName("P1 is the first player in the turn order")
    void RESP_02_TEST_02() {
        Game game = new Game();
        game.initGame();

        Player firstPlayer = game.getCurrentPlayer();
        String firstPlayerID = firstPlayer.getID();

        assertEquals("P1", firstPlayerID);
    }

    @ParameterizedTest
    @DisplayName("Game can provide the next Player in the turn order [P1>P2>P3>P4>P1>...]")
    @ValueSource(strings = {"P1", "P2", "P3", "P4"})
    void RESP_02_TEST_03(String startingPlayerID) {
        Game game = new Game();
        game.initGame();

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
        game.initGame();

        Player currPlayer = game.getPlayerByID(currPlayerID);

        game.setCurrentPlayer(currPlayer);

        List<Player> orderedPlayerList = game.getPlayersStartingCurrent();
        assertEquals(orderedPlayerList.size(),
                     game.getPlayerCount(),
                     "Ordered Player list should include all players in the game, once each");
        assertNotEquals(0, orderedPlayerList.size(), "Player list should not be empty");

        // Iterate through list save for the last element, checking that the ordering of players is consistent
        for (int i = 0; i < orderedPlayerList.size() - 1; i++) {
            Player thisPlayerInList = orderedPlayerList.get(i);
            Player nextPlayerInList = orderedPlayerList.get(i + 1);

            Player trueNextPlayer = game.getNextPlayer(thisPlayerInList);

            assertEquals(trueNextPlayer.getID(),
                         nextPlayerInList.getID(),
                         trueNextPlayer.getID() + "Player after " + thisPlayerInList.getID());
        }
    }

    @Test
    @DisplayName("Each player is set up with 12 cards")
    void RESP_02_TEST_05() {
        Game game = new Game();
        game.initGame();

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
        game.initGame();

        Player player = game.getCurrentPlayer();

        // Create a rigged hand, adding the cards in proper sorted order
        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 15));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 15));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Dagger", 'D', 5));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Sword", 'S', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Sword", 'S', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", 'H', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", 'H', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 15));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 15));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Lance", 'L', 20));

        player.overwriteHand(riggedCards);

        assertEquals(riggedCards, player.getHand());
    }

    @Test
    @DisplayName("A player's hand can be returned as a string with correct card ordering")
    void RESP_03_TEST_01() {
        Game game = new Game();
        game.initGame();

        Player player = game.getCurrentPlayer();

        // Create a rigged hand with cards added in the wrong order.
        // Hand card ordering rules:
        //   Foes always come before Weapons.
        //   Swords must come before Horses despite the values being the same.
        //   Ordered by value ascending otherwise.
        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.WEAPON, "Excalibur", 'E', 30));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", 'H', 10));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 5));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Sword", 'S', 10));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", 'H', 10));
        player.overwriteHand(riggedCards);

        final String correctHandOrder = "F5 F10 S10 H10 H10 E30";

        assertEquals(correctHandOrder, player.getHandString());
    }

    @Test
    @DisplayName("Game can accurately indicate the current turn player after a turn change occurs")
    void RESP_03_TEST_02() {
        StringWriter output = new StringWriter();

        Game game = new Game(new PrintWriter(output));
        game.initGame();

        // First player in turn order
        Player currPlayer = game.getCurrentPlayer();

        game.printCurrentPlayerTurnStart();
        String outputString = output.toString();

        boolean initPlayerTurnDisplayed = outputString.contains(currPlayer.getID());

        // Next player in turn order
        currPlayer = game.getNextPlayer(currPlayer);
        game.setCurrentPlayer(currPlayer);

        game.printCurrentPlayerTurnStart();
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
        game.initGame();

        Player currentPlayer = game.getCurrentPlayer();

        // Create a rigged hand with cards added in a random order.
        ArrayList<Card> riggedCards = new ArrayList<>();

        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 15));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 15));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 40));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Dagger", 'D', 5));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Sword", 'S', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", 'H', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", 'H', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 15));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 15));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Excalibur", 'E', 30));

        Collections.shuffle(riggedCards);
        currentPlayer.overwriteHand(riggedCards);

        final String correctHandOrder = "F5 F5 F15 F15 F40 D5 S10 H10 H10 B15 B15 E30";

        game.printCurrentPlayerTurnStart();

        final String outputString = output.toString();

        assertAll("Turn start printed information",
                  () -> assertTrue(outputString.contains(currentPlayer.getID()), "Prints current player"),
                  () -> assertTrue(outputString.contains(correctHandOrder), "Prints current player's hand"));
    }

    @Test
    @DisplayName("Each player is set up with 0 shields")
    void RESP_04_TEST_01() {
        Game game = new Game();
        game.initGame();

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
        game.initGame();

        Player p2 = game.getPlayerByID("P2");
        Player p3 = game.getPlayerByID("P3");
        Player p4 = game.getPlayerByID("P4");

        // P1 remains at 0 shields
        p2.addShields(1);
        p3.addShields(2);
        p4.addShields(6);

        List<Player> winners = game.getWinners();
        assertEquals(0, winners.size(), "Number of winners should be 0");
    }

    @ParameterizedTest
    @DisplayName("When there is exactly one player that has 7 or more shields, only they are evaluated as the winner")
    @ValueSource(strings = {"P1", "P2", "P3", "P4"})
    void RESP_04_TEST_03(String winningPlayerID) {
        Game game = new Game();
        game.initGame();

        Player winningPlayer = game.getPlayerByID(winningPlayerID);

        winningPlayer.addShields(7);

        List<Player> winners = game.getWinners();

        // Assert that there is only one winner, and it is the player that should be winning
        assertTrue(winners.size() == 1 && Objects.equals(winners.getLast().getID(), winningPlayerID));
    }

    @Test
    @DisplayName("When there are multiple players that have 7 or more shields, they are all evaluated as winners")
    void RESP_04_TEST_04() {
        Game game = new Game();
        game.initGame();

        Player p2 = game.getPlayerByID("P2");
        Player p3 = game.getPlayerByID("P3");
        Player p4 = game.getPlayerByID("P4");

        // P2 and P4 should win
        // P1 remains at 0 shields
        p2.addShields(7);
        p3.addShields(6);
        p4.addShields(50);

        List<Player> winners = game.getWinners();
        assertTrue(winners.size() == 2 && winners.containsAll(Arrays.asList(p2, p4)));
    }

    @Test
    @DisplayName("Game can display a list of players as winners in console")
    void RESP_04_TEST_05() {
        // Note: Termination does not need to be tested for Assignment 1.

        StringWriter output = new StringWriter();

        Game game = new Game(new PrintWriter(output));
        game.initGame();

        ArrayList<Player> winners = new ArrayList<>();
        winners.add(game.getPlayerByID("P1"));
        winners.add(game.getPlayerByID("P3"));

        game.printGameEnd(winners);

        final String outputString = output.toString();

        assertAll("Victory output",
                  () -> assertTrue(outputString.contains("Winner"), "Has 'Winner' string"),
                  () -> assertTrue(outputString.contains("P1"), "Has all winners"),
                  () -> assertTrue(outputString.contains("P3"), "Has all winners"));
    }

    @Test
    @DisplayName("Player can trim their hand when there is 1 over the max")
    void RESP_05_TEST_01() {
        // List of 13 cards (in order), 1 over the max of 12
        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 1));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 2));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 3));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 4));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 6));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 7));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Dagger", 'D', 8));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Sword", 'S', 9));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", 'H', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 11));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Lance", 'L', 12));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Excalibur", 'E', 13));

        String expectedHand = "F1 F2 F3 F5 F6 F7 D8 S9 H10 B11 L12 E13"; // Foe value 4 card removed

        String input = "4\n"; // Input to remove the 'F4' card
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player player = game.getCurrentPlayer();
        player.overwriteHand(Collections.emptyList()); // Empty the player's existing hand
        player.overwriteHand(riggedCards);

        assertAll("Player trims 1 card",
                  () -> assertEquals(12, player.getHandSize(), "Player hand size trimmed to 12"),
                  () -> assertEquals(expectedHand, player.getHandString(), "Correct card removed"));
    }

    @Test
    @DisplayName("Player can trim their hand when there are 3 over the max")
    void RESP_05_TEST_02() {
        // List of 15 cards (in order), 3 over the max of 12
        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 1));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 2));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 3));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 4));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 6));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 7));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 8));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 9));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 10));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 11));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 12));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 13));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 14));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 15));

        String expectedHand = "F3 F4 F5 F6 F7 F8 F9 F10 F11 F12 F13 F14";

        String input = "1\n1\n13\n"; // Input to remove 'F1', 'F2', 'F15', in order.
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player player = game.getCurrentPlayer();
        player.overwriteHand(Collections.emptyList()); // Empty the player's existing hand
        player.overwriteHand(riggedCards);

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
        game.initGame();

        game.printTurnEndOf(game.getPlayerByID(playerID));

        final String outputString = output.toString();

        assertAll("Player " + playerID + "'s turn ends",
                  () -> assertTrue(outputString.contains(playerID), "Indicates correct player's end of turn"),
                  () -> assertTrue(outputString.contains(
                                           "Press <return> to continue... > " + "\n".repeat(Game.FLUSH_LINES)),
                                   "Indicates to press <return> to clear the display, followed by the defined number "
                                   + "of newlines to achieve this"));
    }

    @Test
    @DisplayName("Game can draw and display a Q card from the Event deck")
    void RESP_07_TEST_01() {
        StringWriter output = new StringWriter();

        Game game = new Game(new PrintWriter(output));
        game.initGame();

        game.setCurrentEvent(new Card("Q5"));
        game.printCurrentEventCard();

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
        game.initGame();

        game.setCurrentEvent(new Card(eCardName));
        game.printCurrentEventCard();

        String eventDesc = null;
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
        game.initGame();

        Player p1 = game.getPlayerByID("P1");
        Player p2 = game.getPlayerByID("P2");
        Player p3 = game.getPlayerByID("P3");
        Player p4 = game.getPlayerByID("P4");

        // P1 remains at 0 shields
        p2.addShields(1);
        p3.addShields(2);
        p4.addShields(6);

        Card plagueCard = new Card(Card.CardType.EVENT, "Plague", 'E', 2);

        game.setCurrentEvent(plagueCard);
        game.setCurrentPlayer(p2);
        game.addInput("\n");
        game.runEvent();

        game.setCurrentEvent(plagueCard);
        game.setCurrentPlayer(p4);
        game.addInput("\n");
        game.runEvent();

        assertAll("Plague event triggers",
                  () -> assertEquals(0, p1.getShields(), "P1 didn't draw Plague, shields should stay at 0"),
                  () -> assertEquals(0, p2.getShields(), "P2 drew Plague, shields should be 1 -> 0"),
                  () -> assertEquals(2, p3.getShields(), "P3 didn't draw Plague, shields should stay at 2"),
                  () -> assertEquals(4, p4.getShields(), "P2 drew Plague, shields should be 6 -> 4"));
    }

    @Test
    @DisplayName("Event: Queen's Favor will make the drawing player draw 2 cards, possibly trimming")
    void RESP_08_TEST_02() {
        String input = "1\n\n"; // Trim one card after drawing 2 to reach 13 cards in hand
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player player = game.getPlayerByID("P2"); // Arbitrary player choice
        assertNotNull(player);

        // List of 11 cards (in order), 1 under the max of 11
        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 1));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 2));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 3));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 4));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 5));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 6));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 7));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 8));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 9));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 10));
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 11));

        player.overwriteHand(riggedCards);

        Card queenFavorCard = new Card(Card.CardType.EVENT, "Queen's Favor", 'E', 2);

        game.setCurrentPlayer(player);
        game.setCurrentEvent(queenFavorCard);
        game.runEvent();

        assertEquals(12,
                     player.getHandSize(),
                     "Player drew 2 cards from Queen's Favour while having 11 cards in hand, trimmed to 12");
    }

    @Test
    @DisplayName("Event: Prosperity will make every player draw 2 cards, possibly trimming")
    void RESP_08_TEST_03() {
        // P2 next, P3 trim 1, P3 next, P4 trim 1, P4 trim 1, P4 next, P1 next, end turn
        String input = "\n1\n\n1\n1\n\n\n\n";
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p1 = game.getPlayerByID("P1");
        Player p2 = game.getPlayerByID("P2");
        Player p3 = game.getPlayerByID("P3");
        Player p4 = game.getPlayerByID("P4");

        // Empty P1
        p1.overwriteHand(Collections.emptyList());

        // P2 gets 3 cards
        p2.overwriteHand(Card.stringToCards("F1 F2 F3"));

        // P3 gets 11 cards
        p3.overwriteHand(Card.stringToCards("F1 F2 F3 F4 F5 F6 F7 F8 F9 F10 F11"));

        // P4 gets 12 cards
        p4.overwriteHand(Card.stringToCards("F1 F2 F3 F4 F5 F6 F7 F8 F9 F10 F11 F12"));

        // All players get 2 cards each
        game.setCurrentPlayer(p2);
        game.setCurrentEvent(new Card("Prosperity"));
        game.runEvent();

        assertAll("Prosperity event triggers",
                  () -> assertEquals(2, p1.getHandSize(), "P1 gains 2 cards from 0"),
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
        game.initGame();

        game.setCurrentEvent(new Card("Q5"));
        game.promptPlayersToSponsor();

        Player sponsor = game.getSponsor();

        assertTrue(output.toString().contains("Would you like to sponsor this Quest of 5 stages?"), "Quest prompt");
        assertNotNull(game.getSponsor(), "A sponsor should be found");
        assertEquals("P3", sponsor.getID(), "The correct sponsor is returned");
    }

    @Test
    @DisplayName("Game prompts players to sponsor, all decline")
    void RESP_09_TEST_02() {
        String input = "n\n\nn\n\nn\n\nn\n\n"; // P1 decline, P2 decline, P3 decline, P4 decline, all with transitions
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        game.setCurrentEvent(new Card("Q10"));
        game.promptPlayersToSponsor();

        assertNull(game.getSponsor(), "A sponsor should not be found");
    }

    @Test
    @DisplayName("A sponsor can construct a valid quest with 1 Foe and 2 Weapons.")
    void RESP_10_TEST_01() {
        String input = "3\n2\n1\nquit\n"; // [3] B15, [2] H10, [1] F10, quit
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();

        // Empty hand does not need to be handled specially because Assignment 1 specifies that we may assume players
        // will only sponsor if they can construct a full quest; they can simply type quit with their empty hand, and
        // it will be sufficient to finish construction of the whole quest.
        p.overwriteHand(Card.stringToCards("F10 F10 H10 B15 L20"));

        // Make player build a valid stage; previous stage had value 30
        game.setQuestStages(List.of(Card.stringToCards("F10 L20")));
        game.setSponsor(p);
        game.setCurrentEvent(new Card("Q2"));
        game.buildAndAddStage();

        final String outputString = output.toString();

        assertEquals(Card.stringToCards("F10 H10 B15"),
                     game.viewQuestStages().getLast(),
                     "Chosen stage cards are returned");

        assertAll("Stage cards correctly displayed",
                  () -> assertTrue(outputString.contains("Stage Cards: (empty)"), "Empty stage"),
                  () -> assertTrue(outputString.contains("Stage Cards: B15"), "B15 added first"),
                  () -> assertTrue(outputString.contains("Stage Cards: H10 B15"), "H10 added second"),
                  () -> assertTrue(outputString.contains("Stage Cards: F10 H10 B15"), "F10 added third"));

        assertAll("Stage values correctly displayed",
                  () -> assertTrue(outputString.contains("Stage Value: 0"), "Stage value starts at 0"),
                  () -> assertTrue(outputString.contains("Stage Value: 15"), "B15 added 15"),
                  () -> assertTrue(outputString.contains("Stage Value: 25"), "H10 added 10"),
                  () -> assertTrue(outputString.contains("Stage Value: 35"), "F10 added 10"));

        assertTrue(outputString.contains("Stage Completed: F10 H10 B15"), "Final stage cards displayed");
    }

    @Test
    @DisplayName("A sponsor can construct a valid quest with 1 Foe and no Weapons.")
    void RESP_10_TEST_02() {
        String input = "1\nquit\n"; // [1] F25, quit
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();

        p.overwriteHand(Card.stringToCards("F20 F25"));

        // Make player build a valid stage; previous stage had value 20, reachable with one F25
        game.setSponsor(p);
        game.setQuestStages(List.of(Card.stringToCards("F20")));
        game.setCurrentEvent(new Card("Q2"));
        game.buildAndAddStage();

        final String outputString = output.toString();

        assertTrue(outputString.contains("Stage Completed: F25"), "Stage completed with F25");
    }

    @Test
    @DisplayName("Game will indicate no more than one foe may be used to build a stage")
    void RESP_10_TEST_03() {
        String input = "2\n1\nquit\n"; // [2] F20, [1] F10, quit
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();

        // F10, F20; use F20, F10 will be denied for being a second Foe card
        p.overwriteHand(Card.stringToCards("F5 F10 F20"));

        game.setSponsor(p);
        game.setQuestStages(List.of(Card.stringToCards("F5"))); // Stage value target: 5
        game.setCurrentEvent(new Card("Q2"));
        game.buildAndAddStage();

        final String outputString = output.toString();

        assertEquals(Card.stringToCards("F20"), game.viewQuestStages().getLast(), "Chosen F20 returned");
        assertTrue(outputString.contains("Cannot add more than one Foe card to a stage"),
                   "Indicate only one Foe is allowed for a stage");
        assertTrue(outputString.contains("Stage Completed: F20"), "Stage completed with F20");
    }

    @Test
    @DisplayName("Game will indicate no repeated weapons for building a stage")
    void RESP_10_TEST_04() {
        String input = "1\n1\n1\nquit\n"; // [1] F10, [1] D5, [1] D5, quit
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();

        p.overwriteHand(Card.stringToCards("F10 D5 D5 F13"));

        game.setSponsor(p);
        game.setQuestStages(List.of(Card.stringToCards("F13"))); // Stage value target: 13
        game.setCurrentEvent(new Card("Q2"));
        game.buildAndAddStage();

        final String outputString = output.toString();

        assertEquals(Card.stringToCards("F10 D5"), game.viewQuestStages().getLast(), "Chosen F10, D5 returned");
        assertTrue(outputString.contains("Cannot add a repeat Weapon card to a stage."),
                   "Indicate Weapons must be unique");
        assertTrue(outputString.contains("Stage Completed: F10 D5"), "Stage completed with F10 and D5");
    }

    @Test
    @DisplayName("Game will indicate a stage cannot be empty to finalise")
    void RESP_10_TEST_05() {
        String input = "quit\n1\nquit\n"; // quit, [1] F10, quit
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();

        List<Card> riggedHand = Card.stringToCards("F5 F10");
        p.overwriteHand(riggedHand);

        game.setSponsor(p);
        game.setQuestStages(List.of(Card.stringToCards("F5"))); // Stage value target: 5
        game.setCurrentEvent(new Card("Q2"));
        game.buildAndAddStage();

        final String outputString = output.toString();

        assertEquals(List.of(new Card("F10")), game.viewQuestStages().getLast(), "Chosen card returned");
        assertTrue(outputString.contains("A stage cannot be empty"), "Indicate a stage cannot be empty");
        assertTrue(outputString.contains("Stage Completed: F10"), "Stage completed");
    }

    @Test
    @DisplayName("Game will indicate a stage must have a Foe to finalise")
    void RESP_10_TEST_06() {
        String input = "2\nquit\n1\nquit\n"; // [2] B15, quit, [1] F10, quit
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();

        // F10, B15; B15 is sufficient value-wise for the target 15, but 1 Foe card is required still
        p.overwriteHand(Card.stringToCards("F10 B15 F15"));

        game.setSponsor(p);
        game.setQuestStages(List.of(Card.stringToCards("F15"))); // Stage value target: 15
        game.setCurrentEvent(new Card("Q2"));
        game.buildAndAddStage();

        final String outputString = output.toString();

        assertEquals(Card.stringToCards("F10 B15"), game.viewQuestStages().getLast(), "Chosen cards returned");
        assertTrue(outputString.contains("A stage must have a Foe card"), "Indicate a stage must have a Foe card");
        assertTrue(outputString.contains("Stage Completed: F10 B15"), "Stage completed");
    }

    @Test
    @DisplayName("Game will indicate sufficient value is needed for a stage")
    void RESP_10_TEST_07() {
        String input = "1\nquit\n1\nquit\n1\nquit\n"; // [1] F5, quit, [1] S10, quit, [1] B15, quit
        StringWriter output = new StringWriter();

        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();

        // Target 15 must be exceeded by using all 3 cards (exclude F15 which is there as the previous stage)
        List<Card> riggedCards = Card.stringToCards("F5 S10 B15 F15");
        p.overwriteHand(riggedCards);

        game.setSponsor(p);
        game.setCurrentEvent(new Card("Q2"));
        game.setQuestStages(List.of(Card.stringToCards("F15"))); // Stage value target: 15
        game.buildAndAddStage();

        List<Card> stageCards = game.viewQuestStages().getLast();
        final String outputString = output.toString();

        assertEquals(Card.stringToCards("F5 S10 B15"), stageCards, "Chosen cards returned");
        assertTrue(outputString.contains("Insufficient value for this stage, need strictly greater than "),
                   "Indicate stage value requirement");
        assertEquals(30, Game.cardSum(stageCards), "Expected stage value returned");
        assertTrue(outputString.contains("Stage Completed: F5 S10 B15"), "Stage completed");
    }

    @Test
    @DisplayName("A player can construct an attack, only choosing valid cards")
    void RESP_11_TEST_01() {
        String input = "2\n1\nquit\n"; // [2] B15, [1] H10, quit

        StringWriter output = new StringWriter();
        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();

        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", 'H', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 15));
        p.overwriteHand(riggedCards);

        // Make player build an attack, return its cards
        List<Card> attackCards = game.buildAttack(p);

        final String outputString = output.toString();

        assertAll("Cards properly moved",
                  () -> assertEquals(riggedCards, attackCards, "Chosen attack cards are returned"),
                  () -> assertEquals(0, p.getHandSize(), "Cards used for attack removed from player's hand"));

        assertEquals(25, Game.cardSum(attackCards), "Correct value in returned attack cards");

        assertAll("Attack cards correctly displayed",
                  () -> assertTrue(outputString.contains("Attack Cards: (empty)"), "Empty attack"),
                  () -> assertTrue(outputString.contains("Attack Cards: B15"), "B15 added first"),
                  () -> assertTrue(outputString.contains("Attack Cards: H10 B15"), "H10 added second"));

        assertAll("Attack values correctly displayed",
                  () -> assertTrue(outputString.contains("Attack Value: 0"), "Attack value starts at 0"),
                  () -> assertTrue(outputString.contains("Attack Value: 15"), "B15 added 15"),
                  () -> assertTrue(outputString.contains("Attack Value: 25"), "H10 added 10"));

        assertTrue(outputString.contains("Attack Built (Value 25): H10 B15"), "Final attack cards displayed");
    }

    @Test
    @DisplayName("Game indicates that attacks cannot use Foe cards")
    void RESP_11_TEST_02() {
        String input = "1\n2\nquit\n"; // [1] F10, [2] H10, quit

        StringWriter output = new StringWriter();
        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();

        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.FOE, "Foe", 'F', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", 'H', 10));
        p.overwriteHand(riggedCards);

        // Make player build an attack, return its cards
        List<Card> attackCards = game.buildAttack(p);

        final String outputString = output.toString();

        ArrayList<Card> listWithF10 = new ArrayList<>(List.of(new Card(Card.CardType.FOE, "Foe", 'F', 10)));
        ArrayList<Card> listWithH10 = new ArrayList<>(List.of(new Card(Card.CardType.WEAPON, "Horse", 'H', 10)));

        assertAll("Cards properly moved",
                  () -> assertEquals(listWithH10, attackCards, "H10 chosen and returned"),
                  () -> assertEquals(listWithF10, p.getHand(), "F10 remains in hand"));

        assertEquals(10, Game.cardSum(attackCards), "Correct value in returned attack card");

        assertTrue(outputString.contains("Attacks can only use Weapons"), "Indicate Attacks can use Weapon cards only");

        assertAll("Attack cards correctly displayed",
                  () -> assertTrue(outputString.contains("Attack Cards: (empty)"), "Empty attack"),
                  () -> assertTrue(outputString.contains("Attack Cards: H10"), "H10 added"));

        assertAll("Attack values correctly displayed",
                  () -> assertTrue(outputString.contains("Attack Value: 0"), "Attack value starts at 0"),
                  () -> assertTrue(outputString.contains("Attack Value: 10"), "H10 added 10"));

        assertTrue(outputString.contains("Attack Built (Value 10): H10"), "Final attack card displayed");
    }

    @Test
    @DisplayName("Game indicates that attacks cannot use repeat Weapon cards")
    void RESP_11_TEST_03() {
        String input = "1\n1\n2\nquit\n"; // [1] H10, [1] H10, [2] B15, quit

        StringWriter output = new StringWriter();
        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();

        ArrayList<Card> riggedCards = new ArrayList<>();
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", 'H', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Horse", 'H', 10));
        riggedCards.add(new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 15));
        p.overwriteHand(riggedCards);

        List<Card> attackCards = game.buildAttack(p); // Make player build an attack, return its cards

        final String outputString = output.toString();

        ArrayList<Card> expectedAttack = new ArrayList<>(List.of(new Card(Card.CardType.WEAPON, "Horse", 'H', 10),
                                                                 new Card(Card.CardType.WEAPON,
                                                                          "Battle-axe",
                                                                          'B',
                                                                          15)));
        ArrayList<Card> expectedHand = new ArrayList<>(List.of(new Card(Card.CardType.WEAPON, "Horse", 'H', 10)));

        assertAll("Cards properly moved",
                  () -> assertEquals(expectedAttack, attackCards, "H10 B15 chosen and returned"),
                  () -> assertEquals(expectedHand, p.getHand(), "H10 remains in hand"));

        assertEquals(25, Game.cardSum(attackCards), "Correct value in returned attack cards");

        assertTrue(outputString.contains("Cannot add a repeat Weapon card to an attack"),
                   "Indicate Attacks cannot use repeated Weapon cards");

        assertAll("Attack cards correctly displayed",
                  () -> assertTrue(outputString.contains("Attack Cards: (empty)"), "Empty attack"),
                  () -> assertTrue(outputString.contains("Attack Cards: H10"), "H10 added"),
                  () -> assertTrue(outputString.contains("Attack Cards: H10 B15"), "B15 added"));

        assertAll("Attack values correctly displayed",
                  () -> assertTrue(outputString.contains("Attack Value: 0"), "Attack value starts at 0"),
                  () -> assertTrue(outputString.contains("Attack Value: 10"), "H10 added 10"),
                  () -> assertTrue(outputString.contains("Attack Value: 25"), "B15 added 15"));

        assertTrue(outputString.contains("Attack Built (Value 25): H10 B15"), "Final attack cards displayed");
    }

    @Test
    @DisplayName("Can construct a valid quest from several stages")
    void RESP_12_TEST_01() {
        String input = "1\n7\nquit\n2\n5\nquit\n2\n3\n4\nquit\n2\n3\nquit\n";

        StringWriter output = new StringWriter();
        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();
        game.setSponsor(p);

        p.overwriteHand(Card.stringToCards("F5 F5 F15 F15 F40 D5 S10 H10 H10 B15 B15 E30"));
        game.setCurrentEvent(new Card("Q4"));

        for (int i = 0; i < 4; i++) {
            game.buildAndAddStage();
        }

        assertEquals(4, game.viewQuestStages().size(), "4 stages");

        List<Card> stage1 = game.viewQuestStages().get(0);
        List<Card> stage2 = game.viewQuestStages().get(1);
        List<Card> stage3 = game.viewQuestStages().get(2);
        List<Card> stage4 = game.viewQuestStages().get(3);

        assertEquals(15, Game.cardSum(stage1), "Stage 1 value 15");
        assertEquals(25, Game.cardSum(stage2), "Stage 2 value 25");
        assertEquals(35, Game.cardSum(stage3), "Stage 3 value 35");
        assertEquals(55, Game.cardSum(stage4), "Stage 4 value 55");

        final String outputString = output.toString();

        assertTrue(outputString.contains("Stage Completed: F5 H10"), "Stage 1 completed");
        assertTrue(outputString.contains("Stage Completed: F15 S10"), "Stage 2 completed");
        assertTrue(outputString.contains("Stage Completed: F15 D5 B15"), "Stage 3 completed");
        assertTrue(outputString.contains("Stage Completed: F40 B15"), "Stage 4 completed");
    }

    @Test
    @DisplayName("Can remove players from a list after prompting each for withdrawal")
    void RESP_13_TEST_01() {
        String input = "n\n1\n\ny\n";

        StringWriter output = new StringWriter();
        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();

        Player p1 = game.getPlayerByID("P1"), p2 = game.getPlayerByID("P2");

        game.setEligible(Arrays.asList(p1, p2));

        for (final Player p : game.viewEligible()) {
            game.promptWithdraw(p);
        }

        final String outputString = output.toString();

        assertTrue(outputString.contains("P1: Would you like to withdraw from this quest?"), "P1 withdrawing");
        assertTrue(outputString.contains("P2: Would you like to withdraw from this quest?"), "P2 withdrawing");

        assertEquals(List.of(p1), game.viewEligible(), "P2 removed from list");
        assertEquals(12, p1.getHandSize(), "P1 hand size trimmed to 12");
    }

    @Test
    @DisplayName("Can run a stage for players")
    void RESP_14_TEST_01() {
        String input = "n\n\nn\n\n2\nquit\n\n2\nquit\n\n";

        StringWriter output = new StringWriter();
        Game game = new Game(new Scanner(input), new PrintWriter(output));
        game.initGame();
        game.getAdventureDeck().addToDrawPile(Card.stringToCards("F5 F5"));

        // Will use excalibur and win, 30 > 20
        Player winner = game.getPlayerByID("P1");

        // Will use dagger and lose, 5 < 20
        Player loser = game.getPlayerByID("P2");

        winner.overwriteHand(Card.stringToCards("E30"));
        loser.overwriteHand(Card.stringToCards("D5"));

        game.setEligible(Arrays.asList(winner, loser));

        // Stage value 20; final stage of 4 stage quest.
        game.setQuestStages(Arrays.asList(Card.stringToCards("F1"),
                                          Card.stringToCards("F2"),
                                          Card.stringToCards("F3"),
                                          Card.stringToCards("F20")));
        game.setCurrentEvent(new Card("Q4"));
        game.setStageNum(3); // Will increment to 4 when new stage runs
        game.runStage();

        final String outputString = output.toString();

        assertTrue(outputString.contains("P1: Build an attack for stage 4"), "P1 attack building");
        assertTrue(outputString.contains("P2: Build an attack for stage 4"), "P2 attack building");

        assertEquals(4, winner.getShields(), "P1 gets [stage count] shields from winning");
        assertEquals(0, loser.getShields(), "P2 stays at 0 shields");
        assertTrue(game.viewEligible().contains(winner), "P1 remains eligible since they won");
        assertFalse(game.viewEligible().contains(loser), "P2 removed from list for losing");
    }

    @SuppressWarnings("ExtractMethodRecommender")
    @Test
    @DisplayName("A-TEST JP-Scenario")
    void A_TEST_JP_SCENARIO() {
        // noinspection TextBlockMigration
        String inputStr = "n\n\ny\n1\n7\nquit\n2\n5\nquit\n2\n3\n4\nquit\n2\n3\nquit\n\nn\n1\n\nn\n1\n\nn\n1\n\n5\n5"
                          + "\nquit\n\n5\n4\nquit\n\n4\n6\nquit\n\nn\n\nn\n\nn\n\n7\n6\nquit\n\n9\n4\nquit\n\n6\n6"
                          + "\nquit\n\nn\n\nn\n\n9\n6\n5\nquit\n\n7\n5\n6\nquit\n\nn\n\nn\n\n7\n6\n6\nquit\n\n4\n4\n4"
                          + "\n5\nquit\n\n1\n1\n1\n1\n\n";

        Game game = new Game(new Scanner(inputStr));
        game.initGame(); // Initialises decks and players, sets up player hands

        Player p1 = game.getPlayerByID("P1"), p2 = game.getPlayerByID("P2"), p3 = game.getPlayerByID("P3"), p4 =
                game.getPlayerByID("P4");

        // Rig initial hands of each player
        p1.overwriteHand(Card.stringToCards("F5 F5 F15 F15 D5 S10 S10 H10 H10 B15 B15 L20"));
        p2.overwriteHand(Card.stringToCards("F5 F5 F15 F15 F40 D5 S10 H10 H10 B15 B15 E30"));
        p3.overwriteHand(Card.stringToCards("F5 F5 F5 F15 D5 S10 S10 S10 H10 H10 B15 L20"));
        p4.overwriteHand(Card.stringToCards("F5 F15 F15 F40 D5 D5 S10 H10 H10 B15 L20 E30"));

        // Rig adventure deck; cards added first should be drawn last
        ArrayList<Card> rigAdvDeck = new ArrayList<>();
        rigAdvDeck.addAll(Card.stringToCards("F30 Sword Battle-axe"));  // Stage 1
        rigAdvDeck.addAll(Card.stringToCards("F10 Lance Lance"));       // Stage 2
        rigAdvDeck.addAll(Card.stringToCards("Battle-axe Sword"));      // Stage 3
        rigAdvDeck.addAll(Card.stringToCards("F30 Lance"));             // Stage 4
        game.getAdventureDeck().addToDrawPile(rigAdvDeck.reversed());

        game.getEventDeck().addToDrawPile(new Card("Q4")); // Rig event deck with one Q4 on top

        game.runTurn(); // Do one turn

        /* Asserts */
        assertEquals(0, p1.getShields(), "P1 has no shields");
        assertEquals("F5 F10 F15 F15 F30 H10 B15 B15 L20", p1.getHandString(), "P1 hand correct");

        assertEquals(0, p3.getShields(), "P3 has no shields");
        assertEquals("F5 F5 F15 F30 S10", p3.getHandString(), "P3 hand correct");

        assertEquals(4, p4.getShields(), "P4 has 4 shields");
        assertEquals("F15 F15 F40 L20", p4.getHandString(), "P4 hand correct");

        assertEquals(12, p2.getHandSize(), "P2 has 12 cards in hand");
    }
}
