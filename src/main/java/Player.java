import java.util.Collection;
import java.util.List;

public class Player {
    public String getID() {
        return "";
    }

    public int getHandSize() {
        return 0;
    }

    public List<Card> getHand() {
        return null;
    }

    // Overwrite the player's hand with the cards in the collection given.
    // Existing cards will be wiped. If the new hand has too many cards, a trim dialogue will be triggered afterwards.
    public void rigHand(Collection<Card> cards) {
    }
}
