import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Map.entry;

public final class Game {
    private final Deck adventureDeck;
    private final Deck eventDeck;

    private final List<Player> playerList; // Ordered list of players, representing turn order as well
    private int currPlayerIndex;     // Index of player list denoting whose turn it is in the game.

    public Game() {
        this.adventureDeck = new Deck();
        this.eventDeck = new Deck();
        this.playerList = new ArrayList<>();
        this.currPlayerIndex = 0; // Game starts with the first player in the list
    }

    // Return the corresponding player from the index, wrapping around if
    // an index larger than the player list size is given.
    private Player getPlayerFromIndex(final int i) {
        return playerList.get(i % playerList.size());
    }

    // Set up the decks of a standard game, clearing the current decks (acting as a reset).
    public void initDecks() {
        adventureDeck.clearDeck();
        eventDeck.clearDeck();

        // Map of Foe Cards to initialise: <CardValue, Count>. (e.g., (5, 8) means there are 8 F5 cards)
        final Map<Integer, Integer> FOE_CARDS = Map.ofEntries(entry(5, 8), entry(10, 7), entry(15, 8), entry(20, 7),
                                                              entry(25, 7), entry(30, 4), entry(35, 4), entry(40, 2),
                                                              entry(50, 2), entry(70, 1));

        // Add Foe Cards
        for (var entry : FOE_CARDS.entrySet()) {
            int copies = entry.getValue();
            int foeValue = entry.getKey();

            // Add the specified amount of copies of a certain Foe card
            adventureDeck.addToDrawPile(copies, new Card(Card.CardType.FOE, "Foe", "F", foeValue));
        }

        // Add Weapon Cards
        adventureDeck.addToDrawPile(6, new Card(Card.CardType.WEAPON, "Dagger", "D", 5));
        adventureDeck.addToDrawPile(12, new Card(Card.CardType.WEAPON, "Horse", "H", 10));
        adventureDeck.addToDrawPile(16, new Card(Card.CardType.WEAPON, "Sword", "S", 10));
        adventureDeck.addToDrawPile(8, new Card(Card.CardType.WEAPON, "Battle-axe", "B", 15));
        adventureDeck.addToDrawPile(6, new Card(Card.CardType.WEAPON, "Lance", "L", 20));
        adventureDeck.addToDrawPile(2, new Card(Card.CardType.WEAPON, "Excalibur", "E", 30));

        // Map of Quest Cards to initialise: <CardValue, Count>. (e.g., (3, 2) means there are 2 Q3 cards)
        final Map<Integer, Integer> questCards = Map.ofEntries(entry(2, 3), entry(3, 4), entry(4, 3), entry(5, 2));

        // Add Quest Cards
        for (var entry : questCards.entrySet()) {
            int copies = entry.getValue();
            int questLength = entry.getKey();
            eventDeck.addToDrawPile(copies, new Card(Card.CardType.QUEST, "Quest", "Q", questLength));
        }

        // Add Event (E) Cards
        eventDeck.addToDrawPile(1, new Card(Card.CardType.EVENT, "Plague", "E", 2));
        eventDeck.addToDrawPile(2, new Card(Card.CardType.EVENT, "Queen's Favor", "E", 2));
        eventDeck.addToDrawPile(2, new Card(Card.CardType.EVENT, "Prosperity", "E", 2));

        adventureDeck.shuffleDeck();
        eventDeck.shuffleDeck();
    }

    // Set up and add 4 players to the game, in the order P1 > P2 > P3 > P4 > P1 > ...
    public void initPlayers() {
        playerList.clear();

        initDecks(); // Decks must be initialised for players to be able to draw cards

        currPlayerIndex = 0;

        final int NUM_PLAYERS = 4;
        final int DRAW_COUNT = 12;

        for (int i = 0; i < NUM_PLAYERS; i++) {
            int playerNumber = i + 1;
            Player newPlayer = new Player(playerNumber);
            newPlayer.addToHand(drawAdventureCards(DRAW_COUNT));
            playerList.add(newPlayer);
        }
    }

    public int getPlayerCount() {
        return playerList.size();
    }

    public Deck getEventDeck() {
        return eventDeck;
    }

    public Deck getAdventureDeck() {
        return adventureDeck;
    }

    public Card drawAdventureCard() {
        return adventureDeck.draw();
    }

    // Draw n cards from the Adventure deck and return them in a list, maintaining draw order.
    public List<Card> drawAdventureCards(final int n) {
        ArrayList<Card> drawnCards = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            drawnCards.add(drawAdventureCard());
        }

        return drawnCards;
    }

    public Card drawEventCard() {
        return eventDeck.draw();
    }

    public void discard(final Card c) {
        switch (c.getCardType()) {
            case Card.CardType.FOE:
            case Card.CardType.WEAPON:
                adventureDeck.addToDiscardPile(c);
                break;
            case Card.CardType.QUEST:
            case Card.CardType.EVENT:
                eventDeck.addToDiscardPile(c);
                break;
        }
    }

    // Return the player matching the given ID string in the format "P1", "P2"...
    public Player getPlayerByID(final String id) {
        for (final Player p : playerList) {
            if (Objects.equals(p.getID(), id)) {
                return p;
            }
        }

        return null; // Player not found
    }

    // Return the current Player in the game's turn order.
    public Player getCurrentPlayer() {
        return playerList.get(currPlayerIndex);
    }

    // Set the current player to the supplied player.
    public void setCurrentPlayer(final Player p) {
        currPlayerIndex = playerList.indexOf(p);
    }

    // Return the player who will play a turn after the current player
    public Player getNextPlayer(final Player p) {
        return getPlayerFromIndex(playerList.indexOf(p) + 1);
    }

    // Return a list of Players in the game, ordered in turn order and starting with the current player.
    // Use for iterating through the Players in order just once.
    public List<Player> getPlayersStartingCurrent() {
        ArrayList<Player> orderedPlayers = new ArrayList<>();

        for (int i = 0; i < getPlayerCount(); i++) {
            orderedPlayers.add(getPlayerFromIndex(currPlayerIndex + i));
        }
        return orderedPlayers;
    }

    // Print whose turn it is, and display that player's hand.
    public void printPlayerTurnStart(final PrintWriter output) {
        Player currPlayer = getCurrentPlayer();

        output.println("[" + currPlayer.getID() + "]'s Turn:");
        output.println(currPlayer.getHandString());

        output.flush();
    }

    // Return a list of players who have met the victory condition (7 or more shields).
    // The returned list is empty if no players are eligible.
    public List<Player> getWinners() {
        // TODO: implement
        return null;
    }

    // Print that the game has ended, and list the players given as the winners.
    public void printGameEnd(final PrintWriter output, final List<Player> players) {
        // TODO: implement
    }
}
