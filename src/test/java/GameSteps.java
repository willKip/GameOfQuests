import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

@SuppressWarnings("CucumberJavaStepDefClassInDefaultPackage")
public class GameSteps {
    private Game game;

    // Helper method to be called for step definitions that require a certain player to be in action
    private void setPlayer(int playerNum) {
        game.setCurrentPlayer("P" + playerNum);
    }

    @Given("a new game")
    public void new_game() {
        game = new Game();
        game.initGame();
    }

    @Given("a rigged deck for scenario {int}")
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
                game.getAdventureDeck().addToDrawPile(rigAdvDeck.reversed());

                // Rig event deck with one Q4 on top
                game.getEventDeck().addToDrawPile(Card.newCard("Q4"));
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

    @When("P{int} draws a quest of {int} stages")
    public void player_draws_a_quest_of_n_stages(int playerNum, int stages) {
        this.setPlayer(playerNum);
        Card drawnCard = game.drawEventCard();
        assertEquals(new Card(Card.CardType.QUEST, "Quest", 'Q', stages), drawnCard);
        game.setCurrentEvent(drawnCard);
    }

    @Then("P{int} refuses to sponsor")
    public void player_refuses_to_sponsor(int playerNum) {
        Player p = game.getPlayerByNumber(playerNum);

        game.overrideInput("n\n\n"); // Decline, end turn
        assertFalse(game.promptToSponsor(p, game.getCurrentEvent().getValue()));
    }

    @Then("P{int} decides to sponsor")
    public void player_decides_to_sponsor(int playerNum) {
        Player p = game.getPlayerByNumber(playerNum);

        game.overrideInput("y\n"); // Accept; no turn end happens
        assertTrue(game.promptToSponsor(p, game.getCurrentEvent().getValue()));

        assertEquals(p, game.getSponsor());
    }

    @Then("P{int} builds stage {int} with {string}")
    public void p_builds_stage_with(int playerNum, int stage, String cardString) {
        Player p = game.getPlayerByNumber(playerNum);
        assertEquals(p, game.getSponsor());

        assertTrue("Should not build more stages than the current quest allows",
                   game.viewQuestStages().size() < game.getCurrentEvent().getValue());
        assertEquals("Stage number should be valid", game.viewQuestStages().size() + 1, stage);

        // Get the sponsor's hand minus the cards already used to build stages in the quest
        List<Card> handForStage = new ArrayList<>(p.viewHand());
        for (final Card used : game.viewCardsOfQuest()) {
            handForStage.remove(used);
        }

        game.overrideInput(Game.buildDiscardInput(Card.stringToCards(cardString), handForStage));
        game.buildAndAddStage();
    }

    @Then("P{int} decides to participate in the stage, drawing {string}")
    public void player_participates_draw(int playerNum, String drawingCards) {
        player_participates_draw_and_discard(playerNum, drawingCards, "");
    }

    @Then("P{int} decides to participate in the stage, drawing {string} and discarding {string}")
    public void player_participates_draw_and_discard(int playerNum, String drawingCardID, String discardingCardID) {
        Player p = game.getPlayerByNumber(playerNum);

        Card drawing = Card.newCard(drawingCardID);
        Card discarding = Objects.equals(discardingCardID, "") ? null : Card.newCard(discardingCardID);

        // Expected hand to test against
        List<Card> expectedHand = new ArrayList<>(p.getHand());

        // Add expected drawn card
        expectedHand.add(drawing);
        Collections.sort(expectedHand);

        String discardStr = discarding == null ? "" : Game.buildDiscardInput(List.of(discarding), expectedHand);

        // Agree to participate (refuse to withdraw), discard card from new over-capacity hand, end turn
        String inputStr = "n\n" + discardStr + "\n";

        game.overrideInput(inputStr);
        game.promptWithdraw(p);

        if (discarding != null) {
            // Remove expected discarded card
            expectedHand.remove(discarding);
            Collections.sort(expectedHand);
        }

        assertEquals("Hand modified as expected after participation draw", expectedHand, p.getHand());
    }

    @Then("stage {int} of the quest begins")
    public void stage_n_of_the_quest_begins(int stage) {
        game.setStageNum(game.getStageNum() + 1);
        assertTrue(game.eligibleRemaining());
        assertEquals(game.getStageNum(), stage);
    }

    @Then("P{int} attacks with {string}")
    public void player_builds_an_attack_with(int playerNum, String cardIDs) {
        Player p = game.getPlayerByNumber(playerNum);

        List<Card> attackCards = Card.stringToCards(cardIDs);
        List<Card> initialHand = new ArrayList<>(p.viewHand());

        // Pick cards for attack + end turn
        String inputStr = Game.buildDiscardInput(attackCards, initialHand) + "\n";

        game.overrideInput(inputStr);
        game.doAttack(p);

        for (final Card c : attackCards) {
            initialHand.remove(c);
        }

        assertEquals("Cards discarded after attack", initialHand, p.getHand());
    }

    @Then("P{int} {word} the stage")
    public void player_stage_assert_won_or_lost(int playerNum, String result) {
        Player p = game.getPlayerByNumber(playerNum);
        boolean playerStillEligible = game.viewEligible().contains(p);

        switch (result) {
            case "won" -> assertTrue(playerStillEligible);
            case "lost" -> assertFalse(playerStillEligible);
            default -> throw new IllegalArgumentException("'" + result + "' is not a valid result word!");
        }
    }

    @Then("P{int} has {int} shields")
    public void player_has_n_shields(int playerNum, int shields) {
        assertEquals(shields, game.getPlayerByNumber(playerNum).getShields());
    }

    @Then("P{int} has {int} cards")
    public void player_has_n_cards(int playerNum, int cards) {
        assertEquals(cards, game.getPlayerByNumber(playerNum).getHandSize());
    }

    @Then("the quest is finished, sponsor draws cards")
    public void quest_finished_sponsor_updates_hand() {
        quest_finished_sponsor_updates_hand_with_discard("");
    }

    @Then("the quest is finished, sponsor draws cards, discarding {string}")
    public void quest_finished_sponsor_updates_hand_with_discard(String discardIDs) {
        Player sponsor = game.getSponsor();

        // Sponsor will discard all cards used to build the quest and draw as many, and additionally draw
        // cards equal to the number of stages in the quest, trimming to the max hand size of 12 if necessary.
        int sponsorHandSizeExpected = Math.min(12, sponsor.viewHand().size() + game.getCurrentEvent().getValue());

        if (!discardIDs.isEmpty()) {
            Game.buildDiscardInput(Card.stringToCards(discardIDs), sponsor.viewHand());
        }

        assertEquals("sponsor hand size updated correctly", sponsorHandSizeExpected, sponsor.viewHand().size());
    }

    @Then("P{int} won the game")
    public void player_is_winner(int playerNum) {
        assertTrue(game.getWinners().contains(game.getPlayerByNumber(playerNum)));
    }

    @Then("the sponsor's hand is {string}")
    public void sponsor_hand_is(String cardIDs) {
        player_hand_is(game.getSponsor().getNumber(), cardIDs);
    }

    @Then("P{int}'s hand is {string}")
    public void player_hand_is(int playerNum, String cardIDs) {
        assertEquals(Card.stringToCards(cardIDs), game.getPlayerByNumber(playerNum).viewHand());
    }

    @Then("P{int} is the sponsor")
    public void player_is_sponsor(int playerNum) {
        assertEquals(game.getPlayerByNumber(playerNum), game.getSponsor());
    }
}
