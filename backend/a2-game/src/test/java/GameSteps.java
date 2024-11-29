import io.cucumber.datatable.DataTable;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings("CucumberJavaStepDefClassInDefaultPackage")
public class GameSteps {
    private Game game;

    @ParameterType("Plague|Queen's Favor|Prosperity")
    public Card eventCard(String name) {
        return new Card(name);
    }

    // Interpret a word as a player ID (e.g. P1, P20)
    @ParameterType("P[0-9]+") // Match on "P" followed by any length of digits, until a whitespace is encountered
    public Player player(String id) {
        return game.getPlayerByID(id);
    }

    // Interpret a word as a card ID or name (e.g. F5, Horse)
    @ParameterType("([^\\s]+)") // Match until next whitespace encountered
    public Card card(String id) {
        return new Card(id);
    }

    // Interpret space-separated card IDs (or names) between square brackets (e.g. [F5 Horse]) as cards
    @ParameterType("\\[(.*?)\\]") // Match on the text between square brackets
    public List<Card> cardList(String ids) {
        return Card.stringToCards(ids);
    }

    @Given("a new game")
    public void new_game() {
        game = new Game();
        game.initGame(); // Set up a new game with a standard deck and random hands per player.
    }

    // Rigging happens after game is set up normally.
    @Given("rigged decks and hands for scenario {int}")
    public void rig_deck_for_scenario(int scenario) throws IllegalArgumentException {
        ArrayList<Card> rigDeck;
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
                rigDeck = new ArrayList<>();
                rigDeck.addAll(Card.stringToCards("F30 Sword Battle-axe"));  // Stage 1
                rigDeck.addAll(Card.stringToCards("F10 Lance Lance"));       // Stage 2
                rigDeck.addAll(Card.stringToCards("Battle-axe Sword"));      // Stage 3
                rigDeck.addAll(Card.stringToCards("F30 Lance"));             // Stage 4
                // 13 Sponsor reward cards
                rigDeck.addAll(Card.stringToCards("F5 F5 F5 F10 F15 F20 F40 F70 D5 D5 S10 H10 L20"));
                game.getAdventureDeck().addToDrawPile(rigDeck.reversed());

                // Rig event deck
                game.getEventDeck().addToDrawPile(new Card("Q4"));
                break;
            case 2: // 2winner_game_2winner_quest
                // Rig initial hands of each player
                game.getPlayerByID("P1")
                    .overwriteHand(Card.stringToCards("F5 F5 F15 F15 F40 D5 S10 H10 H10 B15 B15 E30"));
                game.getPlayerByID("P2")
                    .overwriteHand(Card.stringToCards("F5 F5 F15 F15 D5 S10 B15 H10 H10 B15 B15 L20"));
                game.getPlayerByID("P3")
                    .overwriteHand(Card.stringToCards("F5 F5 F5 F15 D5 S10 S10 S10 H10 H10 B15 L20"));
                game.getPlayerByID("P4")
                    .overwriteHand(Card.stringToCards("F5 F15 F15 F40 D5 D5 S10 H10 H10 B15 L20 E30"));

                /* Rig adventure deck */
                rigDeck = new ArrayList<>();
                // Quest 1
                rigDeck.addAll(Card.stringToCards("Horse Sword Battle-axe"));   // Stage 1
                rigDeck.addAll(Card.stringToCards("Horse Lance"));              // Stage 2
                rigDeck.addAll(Card.stringToCards("Lance Sword"));              // Stage 3
                rigDeck.addAll(Card.stringToCards("F5 Lance"));                 // Stage 4
                // 10 Sponsor reward cards
                rigDeck.addAll(Card.stringToCards("F5 F10 F15 F20 F40 F70 D5 S10 H10 L20"));

                // Quest 2
                rigDeck.addAll(Card.stringToCards("Dagger Horse"));         // Stage 1
                rigDeck.addAll(Card.stringToCards("Sword Lance"));          // Stage 2
                rigDeck.addAll(Card.stringToCards("Battle-axe Excalibur")); // Stage 3
                // 7 Sponsor reward cards
                rigDeck.addAll(Card.stringToCards("F5 F40 F70 D5 D5 S10 H10"));

                game.getAdventureDeck().addToDrawPile(rigDeck.reversed());

                /* Rig event deck */
                rigDeck = new ArrayList<>(Card.stringToCards("Q4 Q3"));
                game.getEventDeck().addToDrawPile(rigDeck.reversed());
                break;
            case 3: // 1winner_game_with_events
                // Rig initial hands of each player
                game.getPlayerByID("P1")
                    .overwriteHand(Card.stringToCards("F5 F5 F15 F15 F40 D5 S10 H10 H10 B15 B15 E30"));
                game.getPlayerByID("P2")
                    .overwriteHand(Card.stringToCards("F5 F5 F15 F15 D5 S10 B15 H10 H10 B15 B15 L20"));
                game.getPlayerByID("P3")
                    .overwriteHand(Card.stringToCards("F5 F5 F5 F15 D5 S10 S10 S10 H10 H10 B15 L20"));
                game.getPlayerByID("P4")
                    .overwriteHand(Card.stringToCards("F5 F15 F15 F40 D5 D5 S10 H10 H10 B15 L20 E30"));

                /* Rig adventure deck */
                rigDeck = new ArrayList<>();
                // Quest 1
                rigDeck.addAll(Card.stringToCards("F30 Sword Battle-axe"));   // Stage 1
                rigDeck.addAll(Card.stringToCards("F10 Lance Lance"));        // Stage 2
                rigDeck.addAll(Card.stringToCards("Lance Battle-axe Sword")); // Stage 3
                rigDeck.addAll(Card.stringToCards("F5 F30 Lance"));           // Stage 4

                // 12 Sponsor reward cards
                rigDeck.addAll(Card.stringToCards("F5 F5 F5 F10 F15 F20 F40 F70 D5 D5 S10 H10"));

                // 8 Prosperity triggered cards
                rigDeck.addAll(Card.stringToCards("F70 Dagger"));           // P1
                rigDeck.addAll(Card.stringToCards("Lance Excalibur"));      // P2
                rigDeck.addAll(Card.stringToCards("Battle-axe Lance"));     // P3
                rigDeck.addAll(Card.stringToCards("Sword Horse"));          // P4

                // 2 Queen's Favor triggered cards for P4
                rigDeck.addAll(Card.stringToCards("Sword Lance"));

                // Quest 2
                rigDeck.addAll(Card.stringToCards("D5 D5 D5"));  // Stage 1
                rigDeck.addAll(Card.stringToCards("S10 H10"));   // Stage 2
                rigDeck.addAll(Card.stringToCards("B15 B15"));   // Stage 3
                // 7 Sponsor reward cards
                rigDeck.addAll(Card.stringToCards("F5 F10 F15 F20 D5 H10 L20"));

                game.getAdventureDeck().addToDrawPile(rigDeck.reversed());

                /* Rig event deck */
                rigDeck = new ArrayList<>(Card.stringToCards("Q4 Plague Prosperity"));
                rigDeck.add(new Card("Queen's Favor")); // Space in name necessitates separate addition
                rigDeck.add(new Card("Q3"));

                game.getEventDeck().addToDrawPile(rigDeck.reversed());
                break;
            case 4: // 0_winner_quest
                // Rig initial hands of each player. P1 will sponsor; other players all lose in the first round
                // Other players will start with empty hands and use the single cards they draw from deciding to
                // participate in the first round.
                game.getPlayerByID("P1").overwriteHand(Card.stringToCards("F15 Battle-axe F40 Sword Lance Horse"));
                game.getPlayerByID("P2").overwriteHand(Card.stringToCards(""));
                game.getPlayerByID("P3").overwriteHand(Card.stringToCards(""));
                game.getPlayerByID("P4").overwriteHand(Card.stringToCards(""));

                // Rig adventure deck; cards added first should be drawn last
                rigDeck = new ArrayList<>();
                rigDeck.addAll(Card.stringToCards("Sword Battle-axe Lance"));  // Stage 1, player participation
                rigDeck.addAll(Card.stringToCards("F5 F10 F15 F20 F40 F70 D5 D5"));  // Sponsor rewards
                game.getAdventureDeck().addToDrawPile(rigDeck.reversed());

                // Rig event deck with one Q2 on top
                game.getEventDeck().addToDrawPile(new Card("Q2"));
                break;
            default:
                throw new IllegalArgumentException("Invalid scenario number (" + scenario + ")!");
        }
    }

    @When("{player} draws a quest of {int} stages")
    public void player_draws_a_quest_of_n_stages(Player p, int stages) {
        game.setCurrentPlayer(p);
        game.setCurrentEvent(game.drawEventCard());
        assertEquals("Correct quest card drawn", new Card(Card.CardType.QUEST, "Quest", 'Q', stages),
                     game.getCurrentEventCard());
    }

    @When("{player} draws the event {eventCard}")
    public void player_draws_e_card(Player p, Card eventCard) {
        game.setCurrentPlayer(p);
        game.setCurrentEvent(game.drawEventCard());
        assertEquals("Correct event (E) card drawn", eventCard, game.getCurrentEventCard());
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
    public void player_builds_stage_with(Player p, int stage, List<Card> stageCards) {
        assertEquals("Player should be the sponsor to build stages", p, game.getSponsor());
        assertEquals("Stage number should be correct", game.viewQuestStages().size() + 1, stage);

        game.addInput(Game.buildDiscardString(game.viewEffectiveSponsorHand(), stageCards) + "quit\n");
        game.buildAndAddStage();
    }

    @Then("stage {int} of the quest begins")
    public void stage_n_of_the_quest_begins(int stage) {
        game.startNewStage();

        assertEquals("Correct stage number", game.getStageNum(), stage);
    }

    @Then("{player} withdraws from the stage")
    public void player_stage_withdraw(Player p) {
        game.addInput("y\n"); // Agree to withdraw
        game.promptWithdraw(p);
        assertFalse("Player should be removed from eligible list after withdrawing", game.viewEligible().contains(p));
    }

    @Then("{player} decides to participate in the stage, drawing {card}")
    public void player_stage_participate(Player p, Card drawn) {
        player_stage_participate_discard(p, drawn, null); // Discarding nothing because no trim needed
    }

    @Then("{player} decides to participate in the stage, drawing {card} and trimming {card}")
    public void player_stage_participate_discard(Player p, Card drawn, Card toDiscard) {
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

        // Verify cards used in attack are removed
        for (final Card c : attackCards) {
            initialHand.remove(c);
        }
        assertEquals("Cards discarded after attack", initialHand, p.getHand());
    }

    @Then("{player} wins the stage")
    public void player_stage_assert_won(Player p) {
        assertTrue("Player should still be eligible", game.viewEligible().contains(p));
    }

    @Then("{player} loses the stage")
    public void player_stage_assert_lost(Player p) {
        assertFalse("Player should not be eligible anymore", game.viewEligible().contains(p));
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
        // All players lost, or all stages were played
        assertTrue(!game.eligibleRemaining() || game.questLength() == game.getStageNum());
    }

    @Then("the sponsor updates their hand, drawing {cardList} and trimming {cardList}")
    public void quest_finished_sponsor_updates_hand_with_discard(List<Card> toDraw, List<Card> toTrim) {
        Player sponsor = game.getSponsor();

        // Sponsor will discard all cards used to build the quest and draw as many, and additionally draw
        // cards equal to the number of stages in the quest.
        assertEquals("Must draw correct amount of cards in step definition", game.cardsInQuest() + game.questLength(),
                     toDraw.size());
        assertEquals("Must trim correct amount of cards in step definition",
                     Math.max(0, (sponsor.getHandSize() + game.questLength()) - 12), toTrim.size());

        List<Card> expectedHand = new ArrayList<>(game.viewEffectiveSponsorHand());
        expectedHand.addAll(toDraw);
        Collections.sort(expectedHand);

        // Trim cards if sponsor rewards would make their hand exceed the max size.
        game.addInput(Game.buildDiscardString(expectedHand, toTrim) + "\n");
        game.updateSponsorCardsAfterQuest();
        game.endTurn();

        // Verify sponsor hand is correct
        for (final Card c : toTrim) {
            expectedHand.remove(c);
        }
        assertEquals("Sponsor hand is updated correctly after quest ends", expectedHand, sponsor.getHand());
    }

    @Then("{player} won the game")
    public void player_is_winner(Player p) {
        assertTrue(game.getWinners().contains(p));
    }

    @Then("{player} did not win the game")
    public void player_is_not_winner(Player p) {
        assertFalse(game.getWinners().contains(p));
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

    @Then("the event runs")
    public void the_event_runs() {
        int turnTransitions = 0; // For card draws, input needs to account for turn transitions

        switch (game.getCurrentEventCard().getName()) {
            case "Queen's Favor" -> turnTransitions++;
            case "Prosperity" -> turnTransitions += game.getPlayerCount();
        }

        game.addInput("\n".repeat(turnTransitions) + "\n");
        game.runEvent();
    }

    @Then("the event runs, causing some players to trim their hands:")
    public void the_event_runs_trimming(DataTable table) {
        List<Map<String, String>> rows = table.asMaps(String.class, String.class);

        // For players with cards to trim defined, build input strings for trimming
        Map<Player, String> playerTrimMap = new HashMap<>();
        for (Map<String, String> columns : rows) {
            Player p = game.getPlayerByID(columns.get("player"));
            List<Card> toTrim = Card.stringToCards(columns.get("trimming"));

            playerTrimMap.put(p, Game.buildDiscardString(p.viewHand(), toTrim));
        }

        StringBuilder inputs = new StringBuilder();
        for (final Player p : game.getPlayersStartingCurrent()) {
            String trimStr = playerTrimMap.get(p);
            if (trimStr != null) {
                inputs.append(trimStr); // Trim cards if relevant
            }
            inputs.append("\n"); // End turn
        }

        game.addInput(inputs + "\n"); // End card drawer's turn after every event resolved
        game.runEvent();
    }
}
