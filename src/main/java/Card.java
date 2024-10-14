import java.util.Objects;

public class Card implements Comparable<Card> {
    private final CardType cardType;
    private final String cardName;
    private final String cardSymbol;
    private final int value;

    public Card(final CardType cardType, final String cardName, final String cardSymbol, final int value) {
        this.cardType = cardType;
        this.cardName = cardName;
        this.cardSymbol = cardSymbol;
        this.value = value;
    }

    public CardType getCardType() {
        return cardType;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return cardName;
    }

    // Return the alphabet + value representation of a card. (e.g. F5, E30)
    public String getCardID() {
        return cardSymbol + value;
    }

    @Override
    public int compareTo(Card c) {
        // Sort based on type (ascending) first
        int ord = this.cardType.compareTo(c.cardType);

        if (ord == 0) {
            // Same type, break ties with values (ascending)
            ord = this.value - c.value;

            // Special case: Swords and Horses have the same card value, but Swords come first.
            if (this.cardType == CardType.WEAPON && ord == 0) {
                if (Objects.equals(this.cardName, "Sword") && Objects.equals(c.cardName, "Horse")) {
                    ord = -1;
                } else if (Objects.equals(this.cardName, "Horse") && Objects.equals(c.cardName, "Sword")) {
                    ord = 1;
                }
            }
        }

        return ord;
    }

    // All possible card types; the order here also defines sorting order.
    public enum CardType {FOE, WEAPON, QUEST, EVENT}
}
