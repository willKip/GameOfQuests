import java.util.*;

public class Player {
    private final int number; // Identifying player number
    private final List<Card> hand; // Sorted list of cards in hand

    private int shields;

    public Player(final int num) {
        this.number = num;
        this.hand = new ArrayList<>();
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

    public void addToHand(final Collection<Card> cards) {
        hand.addAll(cards);
        Collections.sort(hand);
    }

    // Overwrite the player's hand with the cards in the collection given.
    // Existing cards will be wiped. If the new hand has too many cards, a trim dialogue will be triggered afterwards.
    public void rigHand(final Collection<Card> cards) {
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

    // Give n shields to this player.
    public void addShields(final int n) {
        shields += n;
    }
}
