public class Card {
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

    public enum CardType {FOE, WEAPON, QUEST, EVENT}
}
