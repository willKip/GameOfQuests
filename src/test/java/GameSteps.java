import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("CucumberJavaStepDefClassInDefaultPackage")
public class GameSteps {
    private Game game;

    // Interpret a word as a player ID (e.g. P1, P2)
    @ParameterType("P[0-9]+") // Match on "P" followed by any length of digits, until a whitespace is encountered
    public Player player(String id) {
        return game.getPlayerByID(id);
    }

    // Interpret a word as a card ID or name (e.g. F5, Horse)
    @ParameterType("([^\\s]+)") // Match until next whitespace encountered
    public Card card(String id) {
        return new Card(id);
    }

    // Interpret space-separated card IDs or names between square brackets (e.g. [F5, Horse])
    @ParameterType("\\[(.*?)\\]") // Match on the text between square brackets
    public List<Card> cardList(String ids) {
        return Card.stringToCards(ids);
    }

    @Given("a new game")
    public void new_game() {
        game = new Game(new PrintWriter(System.out));
        game.initGame();
        game.enableInputEcho();
    }

    @Given("a deck rigged for scenario {int}")
    public void a_rigged_deck_for_scenario(int scenario) throws IllegalArgumentException {
        switch (scenario) {
            case 1: // A1_scenario
                // Rig initial hands of each player
                game.getPlayerByID("P1")
                    .overwriteHand(Card.stringToCards("F5 F5 F15 F15 D5 S10 S10 H10 H10 B15 B15 L20"));
                game.getPlayerByID("P2")
                    .overwriteHand(Card.stringToCards("F5 F5 F15 F15 F40 D5 S10 H10 H10 B15 B15 E30"));
                game.getPlayerByID("P3")
                    .overwriteHand(Card.stringToCards("F5 F5 F5 F15 D5 S10 S10 S10 H10 H10 B15 L20"));
                game.getPlayerByID("P4")
                    .overwriteHand(Card.stringToCards("F5 F15 F15 F40 D5 D5 S10 H10 H10 B15 L20 E30"));

                // Rig adventure deck; cards added first should be drawn last
                ArrayList<Card> rigAdvDeck = new ArrayList<>();
                rigAdvDeck.addAll(Card.stringToCards("F30 Sword Battle-axe"));  // Stage 1
                rigAdvDeck.addAll(Card.stringToCards("F10 Lance Lance"));       // Stage 2
                rigAdvDeck.addAll(Card.stringToCards("Battle-axe Sword"));      // Stage 3
                rigAdvDeck.addAll(Card.stringToCards("F30 Lance"));             // Stage 4
                // 13 Sponsor reward cards
                rigAdvDeck.addAll(Card.stringToCards("F5 F5 F5 F10 F15 F20 F40 F70 D5 D5 S10 H10 L20"));
                game.getAdventureDeck().addToDrawPile(rigAdvDeck.reversed());

                // Rig event deck with one Q4 on top
                game.getEventDeck().addToDrawPile(new Card("Q4"));
                break;
            case 2: // 2winner_game_2winner_quest
                break;
            case 3: // 1winner_game_with_events
                break;
            case 4: // 0_winner_quest
                break;
            default:
                throw new IllegalArgumentException("Invalid scenario number (" + scenario + ")!");
        }
    }

    @When("{player} draws a quest of {int} stages")
    public void player_draws_a_quest_of_n_stages(Player p, int stages) {
        game.setCurrentPlayer(p);
        game.setCurrentEvent(game.drawEventCard());
        assertEquals(new Card(Card.CardType.QUEST, "Quest", 'Q', stages), game.getCurrentEventCard());
    }

    @Then("{player} refuses to sponsor")
    public void player_sponsor_refuses(Player p) {
        game.addInput("n\n\n"); // Decline, end turn
        game.promptToSponsor(p);
        assertNotEquals(p, game.getSponsor());
    }

    @Then("{player} agrees to sponsor")
    public void player_sponsor_agrees(Player p) {
        game.addInput("y\n"); // Accept
        game.promptToSponsor(p);
        assertEquals(p, game.getSponsor());
    }

    @Then("{player} builds stage {int} with {cardList}")
    public void p_builds_stage_with(Player p, int stage, List<Card> stageCards) {
        assertEquals("Correct player should be the sponsor", p, game.getSponsor());
        assertEquals("Stage number should be correct", game.viewQuestStages().size() + 1, stage);

        game.addInput(Game.buildDiscardString(game.viewEffectiveSponsorHand(), stageCards) + "quit\n");
        game.buildAndAddStage();
    }

    @Then("stage {int} of the quest begins")
    public void stage_n_of_the_quest_begins(int stage) {
        game.startNewStage();

        assertEquals("Correct stage number", game.getStageNum(), stage);
    }

    @Then("{player} decides to participate in the stage, drawing {card}")
    public void player_participates_draw(Player p, Card drawn) {
        player_participates_draw_and_discard(p, drawn, null); // Discarding nothing because no trim needed
    }

    @Then("{player} decides to participate in the stage, drawing {card} and trimming {card}")
    public void player_participates_draw_and_discard(Player p, Card drawn, Card toDiscard) {
        List<Card> expectedHand = new ArrayList<>(p.viewHand()); // Expected hand to test against
        expectedHand.add(drawn);        // Add card that should be drawn
        Collections.sort(expectedHand); // Sort hand

        // Agree to participate (by refusing to withdraw), discard card from new over-capacity hand, end turn
        game.addInput("n\n" + Game.buildDiscardString(expectedHand, toDiscard) + "\n");
        game.promptWithdraw(p);

        // Verify player discards the card they should
        if (toDiscard != null) {
            expectedHand.remove(toDiscard);
        }

        assertEquals("Hand modified as expected after participation draw", expectedHand, p.getHand());
    }

    @Then("{player} attacks with {cardList}")
    public void player_builds_an_attack_with(Player p, List<Card> attackCards) {
        List<Card> initialHand = new ArrayList<>(p.viewHand());

        // Pick cards for attack, end turn
        game.addInput(Game.buildDiscardString(initialHand, attackCards) + "quit\n" + "\n");
        game.doAttack(p);

        // Verify cards used in attack are removed correctly
        for (final Card c : attackCards) {
            initialHand.remove(c);
        }
        assertEquals("Cards discarded after attack", initialHand, p.getHand());
    }

    @Then("{player} won the stage")
    public void player_stage_assert_won(Player p) {
        assertTrue(game.viewEligible().contains(p));
    }

    @Then("{player} lost the stage")
    public void player_stage_assert_lost(Player p) {
        assertFalse(game.viewEligible().contains(p));
    }

    @Then("{player} has {int} shields")
    public void player_has_n_shields(Player p, int shields) {
        assertEquals(shields, p.getShields());
    }

    @Then("{player} has {int} cards")
    public void player_has_n_cards(Player p, int cards) {
        assertEquals(cards, p.getHandSize());
    }

    @Then("the quest is finished")
    public void quest_finished_assert() {
        assertEquals("Stage number should be equal to total number of stages", game.questLength(), game.getStageNum());
    }

    @Then("the sponsor updates their hand, drawing {cardList} and trimming {cardList}")
    public void quest_finished_sponsor_updates_hand_with_discard(List<Card> toDraw, List<Card> toDiscard) {
        Player sponsor = game.getSponsor();

        // Sponsor will discard all cards used to build the quest and draw as many, and additionally draw
        // cards equal to the number of stages in the quest.
        assertEquals("Must draw correct amount of cards in step definition",
                     sponsor.getHandSize() - game.viewEffectiveSponsorHand().size() + game.questLength(),
                     toDraw.size());
        assertEquals("Must discard correct amount of cards in step definition",
                     Math.max(0, (sponsor.getHandSize() + game.questLength()) - 12), toDiscard.size());

        List<Card> expectedHand = new ArrayList<>(game.viewEffectiveSponsorHand());
        expectedHand.addAll(toDraw);
        Collections.sort(expectedHand);

        // Trim cards if sponsor rewards would make their hand exceed the max size.
        game.addInput(Game.buildDiscardString(expectedHand, toDiscard));
        game.updateSponsorCardsAfterQuest();

        // Verify sponsor hand is correct
        for (final Card c : toDiscard) {
            expectedHand.remove(c);
        }
        assertEquals("Sponsor hand is updated correctly after quest ends", expectedHand, sponsor.getHand());
    }

    @Then("{player} won the game")
    public void player_is_winner(Player p) {
        assertTrue(game.getWinners().contains(p));
    }

    @Then("the sponsor's hand is {string}")
    public void sponsor_hand_is(String cardIDs) {
        player_hand_is(game.getSponsor(), cardIDs);
    }

    @Then("{player}'s hand is {string}")
    public void player_hand_is(Player p, String cardIDs) {
        assertEquals(Card.stringToCards(cardIDs), p.viewHand());
    }

    @Then("{player} is the sponsor")
    public void player_is_sponsor(Player p) {
        assertEquals(p, game.getSponsor());
    }
}
