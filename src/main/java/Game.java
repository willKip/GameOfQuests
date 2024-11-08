import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public final class Game {
    private static PrintWriter output = null;
    private static Scanner input = null;

    private final Deck adventureDeck;
    private final Deck eventDeck;
    private final List<Player> playerList; // Ordered list of players, representing turn order as well
    private int currPlayerIndex;     // Index of player list denoting whose turn it is in the game.

    // Turn-specific variables
    private Card currentEvent;
    private Player sponsor;
    private List<List<Card>> questStages;
    private int stageNum;
    private List<Player> eligible;

    public Game() {
        // If no output is needed, initialise a PrintWriter that discards all bytes.
        this(new PrintWriter(OutputStream.nullOutputStream()));
    }

    public Game(PrintWriter output) {
        // If no input is needed, 'input' an empty string (do no input).
        this(new Scanner(""), output);
    }

    public Game(Scanner input, PrintWriter output) {
        Game.input = input;
        Game.output = output;

        this.adventureDeck = new Deck();
        this.eventDeck = new Deck();
        this.playerList = new ArrayList<>();
        this.currPlayerIndex = 0; // Game starts with the first player in the list

        this.currentEvent = null;
        this.sponsor = null;
        this.questStages = new ArrayList<>();
        this.eligible = new ArrayList<>();
        this.stageNum = 0;
    }

    // Return the string input sequence required to pick and remove cards from a list, in order.
    static String buildDiscardInput(final List<Card> toDiscard, final List<Card> cardSource) throws RuntimeException {
        if (toDiscard.isEmpty() || cardSource.isEmpty()) {
            return "";
        }

        List<String> indices = new ArrayList<>();

        List<Card> toDiscardCopy = new ArrayList<>(toDiscard);
        List<Card> cardSourceCopy = new ArrayList<>(cardSource);

        while (!toDiscardCopy.isEmpty()) {
            Card target = toDiscardCopy.removeFirst();
            boolean found = false;

            ListIterator<Card> iter = cardSourceCopy.listIterator();
            while (iter.hasNext()) {
                int i = iter.nextIndex();
                Card c = iter.next();

                if (c.equals(target)) {
                    iter.remove();
                    indices.add(String.valueOf(i + 1)); // Convert to human-readable index
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new RuntimeException(
                        "Card '" + target + "' could not be found in hand '" + Card.cardsToString(cardSourceCopy, " ")
                        + "'!");
            }
        }

        return String.join("\n", indices) + "\nquit\n";
    }

    // Displays a prompt to select cards from a 1-indexed list; returns the user input.
    // Note: Must -1 from user index selection since the displayed is 1-index, not the card list's true 0-index
    static String cardSelection(final String prompt, final List<Card> cards) {
        output.println(prompt);

        if (cards.isEmpty()) {
            output.println("[-] (no cards)");
        } else {
            int i = 0;
            for (final Card c : cards) {
                output.println("[" + (i + 1) + "] " + c.getCardID());
                i++;
            }
        }
        output.print("> ");
        output.flush();
        return input.nextLine();
    }

    static int cardSum(final List<Card> cards) {
        int total = 0;

        for (final Card c : cards) {
            total += c.getValue();
        }

        return total;
    }

    private void initTurnVars() {
        currentEvent = null;
        sponsor = null;
        questStages = new ArrayList<>();
        eligible = new ArrayList<>(playerList);
        stageNum = 0;
    }

    // Returns an unmodifiable view of the current eligible player list.
    public List<Player> viewEligible() {
        return List.copyOf(eligible);
    }

    public void setEligible(final List<Player> playerList) {
        this.eligible = new ArrayList<>(playerList);
    }

    public boolean eligibleRemaining() {
        return !eligible.isEmpty();
    }

    public void printEligible() {
        StringJoiner sj = new StringJoiner(", ");
        output.print("Eligible players: ");
        for (final Player p : eligible) {
            sj.add(p.getID());
        }
        output.println(sj);
        output.flush();
    }

    // Returns an unmodifiable view of the current list of quest stages.
    public List<List<Card>> viewQuestStages() {
        return List.copyOf(questStages);
    }

    // Returns an unmodifiable list of all the cards used to build the stages of the quest so far, in order.
    public List<Card> viewCardsOfQuest() {
        List<Card> usedCards = new ArrayList<>();
        questStages.forEach(usedCards::addAll);
        return usedCards;
    }

    public int getStageNum() {
        return stageNum;
    }

    public void setStageNum(final int stageNum) {
        this.stageNum = stageNum;
    }

    public void setQuestStages(final List<List<Card>> questStages) {
        this.questStages = new ArrayList<>(questStages);
    }

    public Player getSponsor() {
        return sponsor;
    }

    public void setSponsor(final Player p) {
        this.sponsor = p;
    }

    public Card getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(final Card c) {
        this.currentEvent = c;
    }

    // Overwrites existing input with the given string.
    public void overrideInput(final String string) {
        input = new Scanner(string);
    }

    // Return the corresponding player from the index, wrapping around if
    // an index larger than the player list size is given.
    private Player getPlayerFromIndex(final int i) {
        return playerList.get(i % playerList.size());
    }

    // Set up the decks of a standard game, clearing the current decks (acting as a reset).
    public void initDecks() {
        adventureDeck.clearDeck();
        eventDeck.clearDeck();

        // Foe
        adventureDeck.addToDrawPile(Card.newCard("F5"), 8);
        adventureDeck.addToDrawPile(Card.newCard("F10"), 7);
        adventureDeck.addToDrawPile(Card.newCard("F15"), 8);
        adventureDeck.addToDrawPile(Card.newCard("F20"), 7);
        adventureDeck.addToDrawPile(Card.newCard("F25"), 7);
        adventureDeck.addToDrawPile(Card.newCard("F30"), 4);
        adventureDeck.addToDrawPile(Card.newCard("F35"), 4);
        adventureDeck.addToDrawPile(Card.newCard("F40"), 2);
        adventureDeck.addToDrawPile(Card.newCard("F50"), 2);
        adventureDeck.addToDrawPile(Card.newCard("F70"), 1);

        // Weapon
        adventureDeck.addToDrawPile(Card.newCard("D5"), 6);
        adventureDeck.addToDrawPile(Card.newCard("S10"), 16);
        adventureDeck.addToDrawPile(Card.newCard("H10"), 12);
        adventureDeck.addToDrawPile(Card.newCard("B15"), 8);
        adventureDeck.addToDrawPile(Card.newCard("L20"), 6);
        adventureDeck.addToDrawPile(Card.newCard("E30"), 2);

        // Quest
        eventDeck.addToDrawPile(Card.newCard("Q2"), 3);
        eventDeck.addToDrawPile(Card.newCard("Q3"), 4);
        eventDeck.addToDrawPile(Card.newCard("Q4"), 3);
        eventDeck.addToDrawPile(Card.newCard("Q5"), 2);

        // Event
        eventDeck.addToDrawPile(Card.newCard("Plague"), 1);
        eventDeck.addToDrawPile(Card.newCard("Queen's Favor"), 2);
        eventDeck.addToDrawPile(Card.newCard("Prosperity"), 2);

        adventureDeck.shuffleDeck();
        eventDeck.shuffleDeck();
    }

    // Set up and add 4 players to the game, in the order P1 > P2 > P3 > P4 > P1 > ...
    public void initGame() {
        playerList.clear();

        initDecks(); // Decks must be initialised for players to be able to draw cards

        currPlayerIndex = 0;

        final int NUM_PLAYERS = 4;
        final int DRAW_COUNT = 12;

        for (int i = 0; i < NUM_PLAYERS; i++) {
            int playerNumber = i + 1;
            Player newPlayer = new Player(playerNumber);
            newPlayer.addToHand(drawAdventureCards(DRAW_COUNT));
            playerList.add(newPlayer);
        }

        initTurnVars();
    }

    public int getPlayerCount() {
        return playerList.size();
    }

    public Deck getEventDeck() {
        return eventDeck;
    }

    public Deck getAdventureDeck() {
        return adventureDeck;
    }

    public Card drawAdventureCard() {
        return adventureDeck.draw();
    }

    // Draw n cards from the Adventure deck and return them in a list, maintaining draw order.
    public List<Card> drawAdventureCards(final int n) {
        ArrayList<Card> drawnCards = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            drawnCards.add(drawAdventureCard());
        }

        return drawnCards;
    }

    public Card drawEventCard() {
        return eventDeck.draw();
    }

    public void discard(final Card c) {
        switch (c.getCardType()) {
            case Card.CardType.FOE:
            case Card.CardType.WEAPON:
                adventureDeck.addToDiscardPile(c);
                break;
            case Card.CardType.QUEST:
            case Card.CardType.EVENT:
                eventDeck.addToDiscardPile(c);
                break;
        }
    }

    // Return the player matching the given number (e.g. 1 for ID "P1")
    public Player getPlayerByNumber(final int n) {
        return getPlayerByID("P" + n);
    }

    // Return the player matching the given ID string in the format e.g. "P1", "P2"...
    public Player getPlayerByID(final String id) {
        for (final Player p : playerList) {
            if (Objects.equals(p.getID(), id)) {
                return p;
            }
        }

        throw new IllegalArgumentException("Invalid player ID '" + id + "'!"); // Player not found
    }

    // Return the current Player in the game's turn order.
    public Player getCurrentPlayer() {
        return playerList.get(currPlayerIndex);
    }

    // Set the current player to the supplied player.
    public void setCurrentPlayer(final Player p) {
        currPlayerIndex = playerList.indexOf(p);
    }

    // Set the current player to the supplied player based on ID.
    public void setCurrentPlayer(final String id) {
        setCurrentPlayer(getPlayerByID(id));
    }

    // Return the player who will play a turn after the current player
    public Player getNextPlayer(final Player p) {
        return getPlayerFromIndex(playerList.indexOf(p) + 1);
    }

    // Return a list of Players in the game, ordered in turn order and starting with the current player.
    // Use for iterating through the Players in order just once.
    public List<Player> getPlayersStartingCurrent() {
        ArrayList<Player> orderedPlayers = new ArrayList<>();

        for (int i = 0; i < getPlayerCount(); i++) {
            orderedPlayers.add(getPlayerFromIndex(currPlayerIndex + i));
        }
        return orderedPlayers;
    }

    // Print whose turn it is, and display that player's hand.
    public void printCurrentPlayerTurnStart() {
        Player currPlayer = getCurrentPlayer();

        output.println("[" + currPlayer.getID() + "]'s Turn:");
        output.println(currPlayer.getHandString());

        output.flush();
    }

    // Return a list of players who have met the victory condition (7 or more shields).
    // The returned list is empty if no players are eligible.
    public List<Player> getWinners() {
        ArrayList<Player> winners = new ArrayList<>();

        for (final Player p : playerList) {
            if (p.getShields() >= 7) {
                winners.add(p);
            }
        }

        return winners;
    }

    // Print that the game has ended, and list the players given as the winners.
    public void printGameEnd(final List<Player> players) {
        StringJoiner sj = new StringJoiner(", ");

        for (final Player p : players) {
            sj.add(p.getID());
        }

        // Comma-separated list of winning players from the supplied list.
        String winnersString = sj.toString();

        output.println();
        output.println("The game has concluded!");
        output.println("Winner(s): " + winnersString);

        output.flush();
    }

    public void printTurnEndOf(final Player player) {
        output.println("The turn of " + player.getID() + " has ended!");
        output.print("Press <return> to continue... > ");
        output.flush();

        input.nextLine();

        // Flush display with several newlines
        output.print("\n".repeat(50));
        output.flush();
    }

    public void printEventCard(final Card ec) {
        if (ec.getCardType() == Card.CardType.QUEST) {
            output.println("Drawing an Event card...");
            output.println("A Quest of " + ec.getValue() + " stages!");
        } else if (ec.getCardType() == Card.CardType.EVENT) {
            output.println("Drawing an Event card...");

            String eventDesc = "";

            switch (ec.getName()) {
                case "Plague" -> eventDesc = "Current player loses 2 Shields";
                case "Queen's Favor" -> eventDesc = "Current player draws 2 Adventure cards";
                case "Prosperity" -> eventDesc = "All players draw 2 Adventure cards";
            }

            output.println("Event: " + ec.getName() + " - " + eventDesc);
        }

        output.flush();
    }

    // Applies the given E card event's effects to the appropriate targets.
    public void runEvent() {
        Player currPlayer = getCurrentPlayer();
        final int eventValue = currentEvent.getValue();

        switch (currentEvent.getName()) {
            case "Plague":
                // Remove current player's shields
                currPlayer.removeShields(eventValue);
                output.println("Your shield count is now " + currPlayer.getShields() + ".");
                break;
            case "Queen's Favor":
            case "Prosperity":
                for (final Player p : getPlayersStartingCurrent()) {
                    List<Card> cards = drawAdventureCards(eventValue);

                    output.println(p.getID() + ": you drew " + Card.cardsToString(cards, ", ") + ".");
                    p.addToHand(cards);
                    output.println("Hand: " + p.getHandString());
                    output.flush();

                    if (Objects.equals(currentEvent.getName(), "Queen's Favor")) {
                        // Queen's Favor only affects current player (card drawer);
                        // otherwise the effect is the same as Prosperity.
                        break;
                    }

                    printTurnEndOf(p);
                }
                break;
        }

        output.flush();
    }

    // Prompt a player to sponsor a quest of given length.
    // If they agree, make them the sponsor and return true;
    // otherwise, return false.
    public boolean promptToSponsor(final Player player, final int questLength) {
        output.print(player.getID() + ": Would you like to sponsor this Quest of " + questLength + " stages? (y/n) > ");
        output.flush();

        boolean answer = input.nextLine().equalsIgnoreCase("y");

        if (answer) {
            sponsor = player;
            eligible.remove(player);
            return true;
        } else {
            printTurnEndOf(player);
            return false;
        }
    }

    // Helper method to prompt each player in turn to sponsor a quest of given length.
    // Return true if a sponsor was found; false if no one agreed to sponsor.
    public boolean promptPlayersToSponsor(final int questLength) {
        for (final Player p : getPlayersStartingCurrent()) {
            if (promptToSponsor(p, questLength)) {
                return true;
            }
        }
        return false;
    }

    // Make the current sponsor build a quest with the cards in their hand.
    // NOTE: The cards are not really removed from their hand until the quest ends.
    public void buildAndAddStage() {
        if (sponsor == null) {
            throw new RuntimeException("No sponsor exists to build a quest!");
        }

        final int stageNum = questStages.size() + 1;
        final int prevStageValue = questStages.isEmpty() ? 0 : cardSum(questStages.getLast());

        output.println("\n[Stage " + stageNum + "]");

        List<Card> stageCards = new ArrayList<>();

        // Remove cards that were picked for previous stages (but not truly removed from the sponsor hand until the
        // quest ends)
        List<Card> sponsorHandCopy = new ArrayList<>(sponsor.viewHand());
        for (final Card used : viewCardsOfQuest()) {
            if (!sponsorHandCopy.remove(used)) {
                throw new RuntimeException(
                        "A card used in a previous stage was not found anymore in the sponsor hand!");
            }
        }

        boolean foeAdded = false;

        while (true) {
            output.flush();

            output.print("Stage Cards: ");
            if (stageCards.isEmpty()) {
                output.println("(empty)");
            } else {
                output.println(Card.cardsToString(stageCards, " "));
            }

            output.println("Stage Value: " + cardSum(stageCards));

            String userInput =
                    cardSelection("Enter a card position to add it to the stage, or type 'quit':", sponsorHandCopy);
            boolean userQuits = userInput.equalsIgnoreCase("quit");

            if (!userQuits) {
                /* Card index entered: attempt to add it to stage. Max 1 Foe, no repeat Weapons. */

                int selectedIndex = Integer.parseInt(userInput) - 1;
                Card selectedCard = sponsorHandCopy.get(selectedIndex);
                Card.CardType selectedType = selectedCard.getCardType();

                output.println();

                if (selectedType == Card.CardType.FOE) {
                    // If adding a Foe, save that a Foe was added; if there is already a Foe in the stage, indicate so
                    if (foeAdded) {
                        output.println("Invalid: Cannot add more than one Foe card to a stage.");
                        output.println();
                        continue;
                    } else {
                        foeAdded = true;
                    }
                } else if (selectedType == Card.CardType.WEAPON && stageCards.contains(selectedCard)) {
                    // If adding a weapon, indicate repeats
                    output.println("Invalid: Cannot add a repeat Weapon card to a stage.");
                    output.println();
                    continue;
                }

                // No problems with card, add to stage
                stageCards.add(sponsorHandCopy.remove(selectedIndex));
                Collections.sort(stageCards);
            } else {
                /* 'quit' entered: attempt to finalise stage. Exactly 1 Foe, 0 or more non-repeat weapons. */

                if (stageCards.isEmpty()) {
                    output.println("A stage cannot be empty\n");
                } else if (!foeAdded) {
                    output.println("A stage must have a Foe card\n");
                } else if (cardSum(stageCards) <= prevStageValue) {
                    output.println(
                            "Insufficient value for this stage, need strictly greater than " + prevStageValue + "\n");
                } else {
                    // Stage is valid
                    output.println("Stage Completed: " + Card.cardsToString(stageCards, " "));
                    output.flush();
                    questStages.addLast(stageCards);
                    break;
                }
            }
        }
    }

    public List<Card> buildAttack(final Player player) {
        List<Card> attackCards = new ArrayList<>();

        while (true) {
            output.flush();

            output.print("Attack Cards: ");
            if (attackCards.isEmpty()) {
                output.println("(empty)");
            } else {
                output.println(Card.cardsToString(attackCards, " "));
            }

            output.println("Attack Value: " + cardSum(attackCards));

            String userInput =
                    cardSelection("Enter a card position to add it to the attack, or type 'quit':", player.getHand());
            boolean userQuits = userInput.equalsIgnoreCase("quit");

            if (!userQuits) {
                /* Card index entered: attempt to add it to attack. Only non-repeat Weapons allowed. */
                int selectedIndex = Integer.parseInt(userInput) - 1;

                Card selectedCard = player.getHand().get(selectedIndex);
                Card.CardType selectedType = selectedCard.getCardType();

                output.println();

                if (selectedType != Card.CardType.WEAPON) {
                    output.println("Invalid: Attacks can only use Weapons.\n");
                } else if (attackCards.contains(selectedCard)) {
                    output.println("Invalid: Cannot add a repeat Weapon card to an attack.\n");
                } else {
                    // No problems with card, remove from player hand and add to attack
                    attackCards.add(player.getHand().remove(selectedIndex));
                    Collections.sort(attackCards);
                }
            } else {
                /* 'quit' entered: finalise attack. */
                output.print("Attack Built (Value " + cardSum(attackCards) + "): ");
                output.println(Card.cardsToString(attackCards, " "));
                output.flush();
                return attackCards;
            }
        }
    }

    public void doAttack(final Player p) {
        final int stageValue = Game.cardSum(questStages.get(stageNum - 1));
        final int stageCount = questStages.size();

        output.println(p.getID() + ": Build an attack for stage " + stageNum);
        output.flush();

        List<Card> attackCards = buildAttack(p);

        boolean wonRound = Game.cardSum(attackCards) >= stageValue;

        if (wonRound) {
            // Player wins, remain eligible.
            output.println(p.getID() + ": You have won the stage.");
            if (stageNum == stageCount) {
                // If last stage, get shield rewards as well
                p.addShields(stageCount);
                output.println("You have won the quest! You also get " + stageCount + " shields. You now have "
                               + p.getShields() + " shields.");
            }
        } else {
            // Player loses, cannot play anymore
            eligible.remove(p);
            output.println(p.getID() + ": You have lost the stage.");
        }

        output.flush();

        printTurnEndOf(p);

        // Cards used for the attack of the current stage are discarded by the game.
        for (final Card c : attackCards) {
            discard(c);
        }
    }

    public void runStage() {
        stageNum++; // Increment stage number at start of stage; initialised to 0

        // Each player sets up a valid attack
        for (final Player p : viewEligible()) {
            doAttack(p);
        }
    }

    // Prompt player to join quest. Removes from eligible player list if refused, draws card (and ends their turn to
    // flush screen) if they agree
    public void promptWithdraw(final Player p) {
        output.print(p.getID() + ": Would you like to withdraw from this quest? (y/n) > ");
        output.flush();

        if (input.nextLine().equalsIgnoreCase("y")) {
            // Player withdrawing
            eligible.remove(p);
        } else {
            // Player participating, draw 1 adventure card
            Card drawn = drawAdventureCard();

            output.println("Drew 1 card: " + drawn.getCardID());
            output.flush();

            p.addToHand(drawn);
            printTurnEndOf(p);
        }
    }

    public void updateSponsorCardsAfterQuest() {
        // All cards used by sponsor to build quest are discarded;
        // they draw the same number of cards + the number of stages.
        int sponsorReward = questStages.size();
        for (final List<Card> stage : viewQuestStages()) {
            for (final Card c : stage) {
                sponsor.getHand().remove(c);
                discard(c);
                sponsorReward++;
            }
        }

        output.println("The quest is over. The sponsor will draw " + sponsorReward + " cards.");
        output.flush();
        sponsor.addToHand(drawAdventureCards(sponsorReward));
    }

    public void runQuest() {
        int stageCount = currentEvent.getValue();

        // Prompt each player, starting from current, to sponsor.
        if (!promptPlayersToSponsor(stageCount)) {
            // No sponsor found, no quest
            output.println("No sponsor was found.");
            output.flush();
            return;
        }

        // Build a quest by prompting player
        for (int i = 0; i < stageCount; i++) {
            buildAndAddStage();
        }
        printTurnEndOf(sponsor); // Sponsor's turn ends: other players should not see built stages

        // For each stage of the quest:
        for (int i = 0; i < stageCount; i++) {
            // Print stage number
            output.println("[Stage " + stageNum + "]");
            printEligible(); // Print eligible players

            // Prompt each player for participation
            for (final Player p : viewEligible()) {
                promptWithdraw(p);
            }

            if (eligibleRemaining()) {
                runStage(); // Run the stage for participating players
            } else {
                break; // No players left, end quest here
            }
        }

        updateSponsorCardsAfterQuest();
    }

    // Run a turn for the current player, and hand off the turn to the next player.
    public void runTurn() {
        printCurrentPlayerTurnStart();     // Indicate start of player turn
        setCurrentEvent(drawEventCard());  // Current player draws from the Event deck
        printEventCard(currentEvent); // Print event card effects

        /* Resolve event card */
        switch (currentEvent.getCardType()) {
            case EVENT -> runEvent();
            case QUEST -> runQuest();
            default -> throw new RuntimeException("'" + currentEvent + "' is not Event or Quest type!");
        }
        discard(currentEvent); // Discard event card afterwards

        initTurnVars(); // Clear variables for a new turn
        printTurnEndOf(getCurrentPlayer()); // Indicate end of current player (card drawer)'s turn
    }

    // Given an initialised game, run turns until winners are found.
    public void turnLoop() {
        while (true) {
            runTurn();
            List<Player> winners = getWinners(); // Check if winners have resulted
            if (winners.isEmpty()) {
                // No winners; Switch turn to next player in turn order
                setCurrentPlayer(getNextPlayer(getCurrentPlayer()));
            } else {
                // Declare game end, list winners, end game
                printGameEnd(winners);
                System.exit(0);
            }
        }
    }
}
