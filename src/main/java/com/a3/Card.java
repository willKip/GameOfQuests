package com.a3;

import java.util.*;

import static java.util.Map.entry;

public class Card implements Comparable<Card> {
    private final CardType cardType;
    private final String cardName;
    private final char cardSymbol;
    private final int value;

    public Card(final CardType cardType, final String cardName, final char cardSymbol, final int value) {
        if (!Character.isUpperCase(cardSymbol)) {
            throw new RuntimeException("Invalid card symbol '" + cardSymbol + "'!");
        }
        this.cardType = cardType;
        this.cardName = cardName;
        this.cardSymbol = cardSymbol;
        this.value = value;
    }

    // Construct and return a card based on the ID given (e.g. Q2, S10), OR pre-defined alias (e.g. "Sword" for S10)
    public Card(final String s) throws IllegalArgumentException {
        if (s.length() < 2) {
            throw new IllegalArgumentException("Card ID string '" + s + "' is too short!");
        }

        String remainder = s.substring(1); // Part of cardID after first character
        char symbol = s.charAt(0);

        Card.CardType type;
        String name;
        Integer value;

        value = getValue(s);
        if (value != null) {
            // String is a valid card alias
            if (s.matches("Plague|Queen's Favor|Prosperity")) {
                type = CardType.EVENT;
                symbol = 'E';
            } else {
                type = CardType.WEAPON;
            }
            name = s;
        } else if (Character.isUpperCase(symbol) && remainder.chars().allMatch(Character::isDigit)) {
            // Given string is a valid card ID format (e.g. D5, H10, Q4) for Foe/Weapon/Quest
            value = Integer.valueOf(remainder);
            switch (symbol) {
                case 'F':
                    type = CardType.FOE;
                    name = "Foe";
                    break;
                case 'Q':
                    type = CardType.QUEST;
                    name = "Quest";
                    break;
                default:
                    name = weaponSymbolToName(symbol); // See if a weapon is defined for this symbol
                    if (name != null) { // It is a weapon
                        type = CardType.WEAPON;
                    } else { // Invalid symbol
                        throw new IllegalArgumentException("Invalid card symbol '" + symbol + "'!");
                    }
            }
        } else {
            throw new IllegalArgumentException("Card ID string '" + s + "' is not a valid format!");
        }

        // Validate weapon value
        if (type == CardType.WEAPON) {
            Integer trueValue = getValue(name);
            if (!Objects.equals(value, trueValue)) {
                throw new IllegalArgumentException(
                        "Card '" + name + "' should have value '" + trueValue + "'! (Given: '" + value + "')");
            }
        }

        this.cardType = type;
        this.cardName = name;
        this.cardSymbol = symbol;
        this.value = value;
    }

    // From the given list of cards, return a space-separated list of their IDs as a string.
    public static String cardsToString(final List<Card> cards) {
        StringJoiner sj = new StringJoiner(" ");
        for (final Card c : cards) {
            sj.add(c.getCardID());
        }
        return sj.toString();
    }

    // Turn a space-separated string of card IDs (or names) into a list of cards.
    // Returns an empty list if empty string is given.
    public static List<Card> stringToCards(final String s) {
        if (Objects.equals(s, "")) {
            return Collections.emptyList();
        }

        List<Card> cardObjects = new ArrayList<>();

        for (String id : s.split(" ")) {
            cardObjects.add(new Card(id));
        }

        return cardObjects;
    }

    private static String weaponSymbolToName(final char c) {
        final Map<Character, String> SYMBOL_TO_NAME =
                Map.ofEntries(entry('D', "Dagger"), entry('S', "Sword"), entry('H', "Horse"), entry('B', "Battle-axe"),
                              entry('L', "Lance"), entry('E', "Excalibur"), entry('F', "Foe"), entry('Q', "Quest"));
        return SYMBOL_TO_NAME.get(c);
    }

    // Retrieve value of Weapon or Event card.
    private static Integer getValue(final String s) {
        final Map<String, Integer> NAME_TO_VALUE =
                Map.ofEntries(entry("Dagger", 5), entry("Sword", 10), entry("Horse", 10), entry("Battle-axe", 15),
                              entry("Lance", 20), entry("Excalibur", 30), entry("Plague", 2), entry("Queen's Favor", 2),
                              entry("Prosperity", 2));
        return NAME_TO_VALUE.get(s);
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
        return "" + cardSymbol + value;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Card that)) {
            return false; // Only fellow Card classes could be equal
        }

        // Type, name, symbol, value must all be equal
        return this.cardType == that.cardType && Objects.equals(this.cardName, that.cardName) && Objects.equals(
                this.cardSymbol, that.cardSymbol) && this.value == that.value;
    }

    @Override
    public String toString() {
        // e.g. B15 "Battle-axe"
        return getCardID() + ' ' + '"' + cardName + '"';
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
