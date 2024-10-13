public final class Game {
    public void initDecks() {
    }

    public Deck getEventDeck() {
        return new Deck();
    }

    public Deck getAdventureDeck() {
        return new Deck();
    }

    public Card drawAdventureCard() {
        return new Card();
    }

    public Card drawEventCard() {
        return new Card();
    }

    public void discard(final Card c) {
    }
}
