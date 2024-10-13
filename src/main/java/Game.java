import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;

public final class Game {
    private final Deck adventureDeck;
    private final Deck eventDeck;

    public Game() {
        this.adventureDeck = new Deck();
        this.eventDeck = new Deck();
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

    public void initPlayers() {
    }

    public int getPlayerCount() {
        return 0;
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

    public Player getPlayerByID(final String id) {
        return new Player();
    }

    // Return the current Player in the game's turn order.
    public Player getCurrentPlayer() {
        return new Player();
    }

    // Set the current player to the one provided.
    public void setCurrentPlayer(final Player p) {
    }

    // Return the Player who will play a turn after the current one.
    public Player getNextPlayer(final Player p) {
        return new Player();
    }

    // Return a list of Players in the game, ordered in turn order and starting with the current player.
    // Use for iterating through the Players in order just once.
    public List<Player> getPlayersStartingCurrent() {
        return new ArrayList<>();
    }
}
