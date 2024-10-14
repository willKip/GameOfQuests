import java.io.PrintWriter;
import java.util.*;

public class Player {
    private final Scanner input;
    private final PrintWriter output;

    private final int number; // Identifying player number
    private final List<Card> hand; // Sorted list of cards in hand

    private int shields;

    public Player(final int num, Scanner input, PrintWriter output) {
        this.input = input;
        this.output = output;

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
        trim();
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

    // Remove shields, to a minimum of 0
    public void removeShields(int n) {
        shields = Math.max(0, shields - n);
    }

    // Give n shields to this player.
    public void addShields(final int n) {
        shields += n;
    }

    private void trim() {
        // Assignment 1 Note: Can assume selection made by user is valid

        while (getHandSize() > 12) {
            int cardIndex = 0; // Human-friendly 1-index for card list

            output.println("You have too many cards in your hand. (" + getHandSize() + "/12)");
            output.println("Please enter a card position and hit enter to discard it:");

            for (final Card c : getHand()) {
                output.println("[" + (cardIndex + 1) + "] " + c.getCardID());
                cardIndex++;
            }

            output.print("> ");

            output.flush();

            int selected = Integer.parseInt(input.nextLine());

            hand.remove(selected - 1);
        }
    }
}
