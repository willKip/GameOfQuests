import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Player {
    private final int number; // Identifying player number
    private final List<Card> hand; // Sorted list of cards in hand

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
    }

    // Overwrite the player's hand with the cards in the collection given.
    // Existing cards will be wiped. If the new hand has too many cards, a trim dialogue will be triggered afterwards.
    public void rigHand(final Collection<Card> cards) {
        hand.clear();
        addToHand(cards);
    }
}
