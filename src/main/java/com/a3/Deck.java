package com.a3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    // The 'deck' cards are drawn from is referred to as the 'draw pile';
    // the 'Deck' class refers to the linked pair of draw and discard piles.
    private final List<Card> drawPile;
    private final List<Card> discardPile;

    public Deck() {
        this.drawPile = new ArrayList<>();
        this.discardPile = new ArrayList<>();
    }

    // Empties the deck of all cards.
    public void clearDeck() {
        drawPile.clear();
        discardPile.clear();
    }

    // Takes one card from the top of the draw pile.
    public Card draw() {
        Card drawn = drawPile.removeLast();
        refresh();
        return drawn;
    }

    // Adds the given card to the draw pile.
    public void addToDrawPile(final Card card) {
        addToDrawPile(card, 1);
    }

    // Adds n copies of the given card to the draw pile.
    public void addToDrawPile(final Card card, final int n) {
        drawPile.addAll(Collections.nCopies(n, card));
    }

    public void addToDiscardPile(final Card c) {
        discardPile.addLast(c);
        refresh();
    }

    public void addToDrawPile(final List<Card> cards) {
        drawPile.addAll(cards);
    }

    // Returns the total number of cards in the deck and its discard pile.
    // NOTE: will NOT count any cards that are currently drawn from the deck (e.g. in a player's
    // hand).
    public int totalSize() {
        return drawPileSize() + discardPileSize();
    }

    // Returns the number of cards in the deck's draw pile.
    public int drawPileSize() {
        return drawPile.size();
    }

    // Returns the number of cards in the deck's discard pile.
    public int discardPileSize() {
        return discardPile.size();
    }

    // Shuffles the draw pile (but not the discard pile).
    public void shuffleDeck() {
        Collections.shuffle(drawPile);
    }

    // Call whenever the draw pile may run out of cards after an operation;
    // if so, and there are cards in the discard pile, it will be shuffled and used as the new draw
    // pile.
    private void refresh() {
        if (drawPileSize() == 0 && discardPileSize() > 0) {
            drawPile.addAll(discardPile); // Copy over all cards in discard pile to draw pile
            discardPile.clear(); // Clear discard pile
            shuffleDeck();
        }
    }
}
