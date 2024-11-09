import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CustomTest {
    @ParameterizedTest
    @ValueSource(strings = {"D", "d", "_", ""})
    void card_by_id_too_short(String id) {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new Card(id));
        assertEquals("Card ID string '" + id + "' is too short!", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"d5", "510"})
    void card_by_id_invalid_first_char(String id) {
        // First char must be an uppercase alphabet
        Exception e = assertThrows(IllegalArgumentException.class, () -> new Card(id));
        assertEquals("Card ID string '" + id + "' is not a valid format!", e.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"X5", "Z40"})
    void card_by_id_invalid_symbol(String id) {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new Card(id));
        assertEquals("Invalid card symbol '" + id.charAt(0) + "'!", e.getMessage());
    }

    @ParameterizedTest(name = "Throws that {1} value should be {2}, not {3}")
    @CsvSource({"D10,Dagger,5,10", "H11,Horse,10,11", "E70,Excalibur,30,70"})
    void card_by_id_invalid_value(String id, String name, int trueValue, int wrongValue) {
        Exception e = assertThrows(IllegalArgumentException.class, () -> new Card(id));
        assertEquals("Card '" + name + "' should have value '" + trueValue + "'! (Given: '" + wrongValue + "')",
                     e.getMessage());
    }

    @Test
    void card_by_id_creates_correctly() {
        assertAll("Foes", () -> assertEquals(new Card(Card.CardType.FOE, "Foe", 'F', 5), new Card("F5")),
                  () -> assertEquals(new Card(Card.CardType.FOE, "Foe", 'F', 10), new Card("F10")),
                  () -> assertEquals(new Card(Card.CardType.FOE, "Foe", 'F', 70), new Card("F70")));

        assertAll("Quests", () -> assertEquals(new Card(Card.CardType.QUEST, "Quest", 'Q', 2), new Card("Q2")),
                  () -> assertEquals(new Card(Card.CardType.QUEST, "Quest", 'Q', 3), new Card("Q3")),
                  () -> assertEquals(new Card(Card.CardType.QUEST, "Quest", 'Q', 4), new Card("Q4")),
                  () -> assertEquals(new Card(Card.CardType.QUEST, "Quest", 'Q', 5), new Card("Q5")));

        assertAll("Weapons by ID", () -> assertEquals(new Card(Card.CardType.WEAPON, "Dagger", 'D', 5), new Card("D5")),
                  () -> assertEquals(new Card(Card.CardType.WEAPON, "Horse", 'H', 10), new Card("H10")),
                  () -> assertEquals(new Card(Card.CardType.WEAPON, "Sword", 'S', 10), new Card("S10")),
                  () -> assertEquals(new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 15), new Card("B15")),
                  () -> assertEquals(new Card(Card.CardType.WEAPON, "Lance", 'L', 20), new Card("L20")),
                  () -> assertEquals(new Card(Card.CardType.WEAPON, "Excalibur", 'E', 30), new Card("E30")));

        assertAll("Weapons by Alias",
                  () -> assertEquals(new Card(Card.CardType.WEAPON, "Dagger", 'D', 5), new Card("Dagger")),
                  () -> assertEquals(new Card(Card.CardType.WEAPON, "Horse", 'H', 10), new Card("Horse")),
                  () -> assertEquals(new Card(Card.CardType.WEAPON, "Sword", 'S', 10), new Card("Sword")),
                  () -> assertEquals(new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 15), new Card("Battle-axe")),
                  () -> assertEquals(new Card(Card.CardType.WEAPON, "Lance", 'L', 20), new Card("Lance")),
                  () -> assertEquals(new Card(Card.CardType.WEAPON, "Excalibur", 'E', 30), new Card("Excalibur")));

        assertAll("Events by Alias",
                  () -> assertEquals(new Card(Card.CardType.EVENT, "Plague", 'E', 1), new Card("Plague")),
                  () -> assertEquals(new Card(Card.CardType.EVENT, "Queen's Favor", 'E', 2), new Card("Queen's Favor")),
                  () -> assertEquals(new Card(Card.CardType.EVENT, "Prosperity", 'E', 2), new Card("Prosperity")));
    }

    @Test
    void string_to_cards() {
        List<Card> cards =
                Arrays.asList(new Card(Card.CardType.FOE, "Foe", 'F', 5), new Card(Card.CardType.FOE, "Foe", 'F', 5),
                              new Card(Card.CardType.FOE, "Foe", 'F', 15), new Card(Card.CardType.FOE, "Foe", 'F', 15),
                              new Card(Card.CardType.WEAPON, "Dagger", 'D', 5),
                              new Card(Card.CardType.WEAPON, "Sword", 'S', 10),
                              new Card(Card.CardType.WEAPON, "Sword", 'S', 10),
                              new Card(Card.CardType.WEAPON, "Horse", 'H', 10),
                              new Card(Card.CardType.WEAPON, "Horse", 'H', 10),
                              new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 15),
                              new Card(Card.CardType.WEAPON, "Battle-axe", 'B', 15),
                              new Card(Card.CardType.WEAPON, "Lance", 'L', 20));

        assertEquals(cards, Card.stringToCards("F5 F5 F15 F15 D5 S10 S10 H10 H10 B15 B15 L20"));

        assertEquals(Collections.emptyList(), Card.stringToCards(""), "Empty string gives empty list");
    }

    @Test
    void find_card_select_indices() {
        assertEquals("1\n7\nquit\n",
                     Game.buildDiscardString(Card.stringToCards("F5 F5 F15 F15 F40 D5 S10 H10 H10 B15 B15 E30"),
                                             Card.stringToCards("F5 H10")) + "quit\n");
    }

    @Test
    void build_quest_using_specified_cardIDs() {
        StringWriter output = new StringWriter();
        Game game = new Game(new PrintWriter(output));
        game.initGame();

        Player p = game.getCurrentPlayer();
        game.setSponsor(p);

        p.overwriteHand(Card.stringToCards("F5 F5 F15 F15 F40 D5 S10 H10 H10 B15 B15 E30"));
        game.setCurrentEvent(new Card("Q4"));

        for (String s : Arrays.asList("F5 H10", "F15 S10", "F15 D5 B15", "F40 B15")) {
            game.addInput(Game.buildDiscardString(game.viewEffectiveSponsorHand(), Card.stringToCards(s)) + "quit\n");
            game.buildAndAddStage();
        }

        assertEquals(4, game.viewQuestStages().size(), "4 stages");

        List<Card> stage1 = game.viewQuestStages().get(0);
        List<Card> stage2 = game.viewQuestStages().get(1);
        List<Card> stage3 = game.viewQuestStages().get(2);
        List<Card> stage4 = game.viewQuestStages().get(3);

        assertEquals(15, Game.cardSum(stage1), "Stage 1 value 15");
        assertEquals(25, Game.cardSum(stage2), "Stage 2 value 25");
        assertEquals(35, Game.cardSum(stage3), "Stage 3 value 35");
        assertEquals(55, Game.cardSum(stage4), "Stage 4 value 55");

        final String outputString = output.toString();

        assertTrue(outputString.contains("Stage Completed: F5 H10"), "Stage 1 completed");
        assertTrue(outputString.contains("Stage Completed: F15 S10"), "Stage 2 completed");
        assertTrue(outputString.contains("Stage Completed: F15 D5 B15"), "Stage 3 completed");
        assertTrue(outputString.contains("Stage Completed: F40 B15"), "Stage 4 completed");
    }
}
