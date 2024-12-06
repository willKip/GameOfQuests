package com.a3;

import java.util.*;

public class Player {
    private final int number; // Identifying player number
    private final List<Card> hand; // Sorted list of cards in hand

    private int shields;

    public Player(final int num) {
        this.number = num;
        this.hand   = new ArrayList<>();
    }

    // From the given list of players, return a comma-separated list of their IDs as a string.
    public static String playersToString(final List<Player> players) {
        StringJoiner sj = new StringJoiner(", ");
        for (final Player p : players) {
            sj.add(p.toString());
        }
        return sj.toString();
    }

    public String getID() {
        return "P" + number;
    }

    public int getHandSize() {
        return hand.size();
    }

    public List<Card> getHand() {
        return hand;
    }

    // Return an immutable view of the current hand
    public List<Card> viewHand() {
        return List.copyOf(hand);
    }

    public void addToHand(final Collection<Card> cards) {
        hand.addAll(cards);
        Collections.sort(hand);
        trim();
    }

    public void addToHand(final Card card) {
        hand.add(card);
        Collections.sort(hand);
        trim();
    }

    // Overwrite the player's hand with the cards in the collection given.
    // Existing cards will be wiped. If the new hand has too many cards, a trim dialogue will be triggered afterwards.
    public void overwriteHand(final Collection<Card> cards) {
        hand.clear();
        addToHand(cards);
    }

    // Return a space-separated, ordered string of the cards in the player's hand.
    public String getHandString() {
        StringJoiner sj = new StringJoiner(" ");

        for (Card c : hand) {
            sj.add(c.getCardID());
        }

        return sj.toString();
    }

    public int getShields() {
        return shields;
    }

    // Remove shields, to a minimum of 0
    public void removeShields(int n) {
        shields = Math.max(0, shields - n);
    }

    // Give n shields to this player.
    public void addShields(final int n) {
        shields += n;
    }

    private void trim() {
        while (getHandSize() > 12) {
            String prompt = "You have too many cards in your hand. (" + getHandSize() + "/12)"
                            + "\nPlease enter a card position and hit enter to discard it:";

            String userInput = Game.cardSelection(prompt, hand);

            boolean isInteger = !userInput.isBlank() && userInput.chars().allMatch(Character::isDigit);
            if (isInteger) {
                int selected = Integer.parseInt(userInput) - 1; // Adjust for 0-index
                if (selected >= 0 && selected < getHandSize()) {
                    hand.remove(selected);
                }
            }
        }
    }

    @Override
    public String toString() {
        return getID(); // Print ID; e.g. "P2", "P30"
    }
}
