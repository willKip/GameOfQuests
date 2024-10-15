import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        PrintWriter output = new PrintWriter(System.out);

        Game game = new Game(input, output);
        game.initPlayers(); // Initialises decks and players, sets up player hands

        List<Player> winners = new ArrayList<>();

        /* Take more turns as long as there are no winners */
        while (winners.isEmpty()) {
            /* Indicate start of player turn */
            game.printPlayerTurnStart();

            /* Current player draws from the Event deck. */
            Card eventCard = game.drawEventCard();

            Card.CardType eventCardType = eventCard.getCardType();

            // Print event card effects
            game.printEventCard(eventCard);

            // Carry out event card
            if (eventCardType == Card.CardType.EVENT) {
                // Resolve E card
                game.doEvent(eventCard);
            } else if (eventCardType == Card.CardType.QUEST) {
                /* Q card drawn: Start a Quest. */
                int stageCount = eventCard.getValue();

                Player sponsor = game.findSponsor(stageCount);

                if (sponsor == null) {
                    // No sponsor found, no quest
                    output.println("No sponsor was found.");
                    output.flush();
                    continue;
                }

                List<List<Card>> questStages = game.buildQuest(sponsor, stageCount);
                // Sponsor's turn ends: other players should not see built stages
                game.printTurnEndOf(sponsor);

                // Add all but sponsor to eligible list
                List<Player> eligible = game.getPlayersStartingCurrent();
                eligible.remove(sponsor);

                // For each stage of the quest:
                for (int stageNum = 1; stageNum <= stageCount; stageNum++) {
                    // Print stage number
                    output.println("[Stage " + stageNum + "]");

                    // Print eligible players
                    StringJoiner sj = new StringJoiner(", ");
                    output.print("Eligible players: ");
                    for (final Player p : eligible) {
                        sj.add(p.getID());
                    }
                    output.println(sj);

                    // Prompt for participation, remove from eligible list if withdrawing
                    game.promptWithdraw(eligible);

                    if (eligible.isEmpty()) {
                        // No players left, end quest here
                        break;
                    } else {
                        // Run the stage for participating players
                        int stageValue = Game.cardSum(questStages.get(stageNum - 1));
                        game.runStage(eligible, stageValue, stageNum, stageCount);
                    }
                }

                // All cards used by sponsor to build quest are discarded;
                // they draw the same number of cards + the number of stages.
                int sponsorReward = stageCount;
                for (final List<Card> stage : questStages) {
                    for (final Card c : stage) {
                        game.discard(c);
                        sponsorReward++;
                    }
                }

                output.println("The quest is over. The sponsor will draw " + sponsorReward + " cards.");
                output.flush();
                sponsor.addToHand(game.drawAdventureCards(sponsorReward));
            }

            // Discard event card afterwards
            game.discard(eventCard);

            /* Indicate end of current player's turn */
            game.printTurnEndOf(game.getCurrentPlayer());

            /* Check for winners, add to list if there are any */
            winners.addAll(game.getWinners());

            /* Switch turn to next player in turn order */
            game.setCurrentPlayer(game.getNextPlayer(game.getCurrentPlayer()));
        }

        /* Declare game end, list winners */
        game.printGameEnd(winners);
    }
}
