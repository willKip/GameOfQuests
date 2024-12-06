package com.a3;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@CrossOrigin(origins = "*")
public class Controller {
    private Boolean gameStarted;
    private Game game;

    /* Asynchronous game I/O members */
    @SuppressWarnings("FieldCanBeLocal")
    private ExecutorService executor;
    private PipedOutputStream writeToStream;
    private PipedInputStream readFromStream;
    private StringWriter outputBuffer;

    public Controller() throws IOException {
        gameStarted = false;
        initIO();
    }

    @PostMapping("/start")
    public Map<String, Object> start(@RequestParam(value = "scenario", defaultValue = "0") String scenarioId) throws
                                                                                                              InterruptedException {
        executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try {
                newGame(Integer.parseInt(scenarioId));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            game.startGameLoop();
        });
        return getGameState();
    }

    @PostMapping("/submit")
    public Map<String, Object> submit(
            @RequestParam(value = "submittedText", defaultValue = "") String submittedText) throws IOException,
                                                                                                   InterruptedException {
        writeToStream.write((submittedText + "\n").getBytes());
        writeToStream.flush();
        return getGameState();
    }

    private Map<String, Object> getGameState() throws InterruptedException {
        Thread.sleep(100); // Give short delay before retrieving game state to allow it to update

        Map<String, Object> gameStateMap = new HashMap<>();

        // Return the output buffer's content, then clear it
        gameStateMap.put("gameText", outputBuffer.toString());
        outputBuffer.getBuffer().setLength(0);

        gameStateMap.put("gameStarted", gameStarted);
        gameStateMap.put("currSelectionMenu", game.getCurrentCardsInSelectMenu());

        Map<String, Map<String, String>> playersMap = new HashMap<>();
        for (Player p : game.getPlayersStartingCurrent()) {
            Map<String, String> playerInfoMap = new HashMap<>();
            playerInfoMap.put("shields", String.valueOf(p.getShields()));
            playerInfoMap.put("hand", p.getHandString());
            playerInfoMap.put("handSize", String.valueOf(p.getHandSize()));
            playersMap.put(p.getID(), playerInfoMap);
        }
        gameStateMap.put("players", playersMap);

        return gameStateMap;
    }

    private void initIO() throws IOException {
        readFromStream = new PipedInputStream();
        writeToStream  = new PipedOutputStream(readFromStream);
        outputBuffer   = new StringWriter();
    }

    private void newGame(int scenarioId) throws IOException {
        gameStarted = true;
        initIO();

        game = new Game(new Scanner(readFromStream), new PrintWriter(outputBuffer));
        game.initGame(); // Set up a new game with a standard deck and random hands per player.
        game.enableInputEcho(); // Input to the game will be displayed in the output
        game.setSelectionMenuRedirected(); // Hides card selection menu in the text console since webpage UI shows it

        ArrayList<Card> rigDeck;

        switch (scenarioId) {
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
                game.getPlayerByID("P1").overwriteHand(Card.stringToCards(
                        "F5 F5 F10 F10 F15 F15 Dagger Horse Horse Battle-axe Battle-axe Lance"));
                game.getPlayerByID("P2").overwriteHand(Card.stringToCards(
                        "F40 F50 Horse Horse Sword Sword Sword Battle-axe Battle-axe Lance Lance Excalibur"));
                game.getPlayerByID("P3").overwriteHand(Card.stringToCards(
                        "F5 F5 F5 F5 Dagger Dagger Dagger Horse Horse Horse Horse Horse"));
                game.getPlayerByID("P4").overwriteHand(Card.stringToCards(
                        "F50 F70 Horse Horse Sword Sword Sword Battle-axe Battle-axe Lance Lance Excalibur"));

                /* Rig adventure deck */
                rigDeck = new ArrayList<>();
                // Quest 1
                rigDeck.addAll(Card.stringToCards("F5 F40 F10"));   // Stage 1
                rigDeck.addAll(Card.stringToCards("F10 F30"));      // Stage 2
                rigDeck.addAll(Card.stringToCards("F30 F15"));      // Stage 3
                rigDeck.addAll(Card.stringToCards("F15 F20"));      // Stage 4
                // 11 Sponsor reward cards for P1
                rigDeck.addAll(Card.stringToCards("F5 F10 F15 F15 F20 F20 F20 F20 F25 F25 F30"));

                // Quest 2
                rigDeck.addAll(Card.stringToCards("Dagger Dagger")); // Stage 1
                rigDeck.addAll(Card.stringToCards("F15 F15"));       // Stage 2
                rigDeck.addAll(Card.stringToCards("F25 F25"));       // Stage 3
                // 8 Sponsor reward cards for P3
                rigDeck.addAll(Card.stringToCards("F20 F20 F25 F30 Sword Battle-axe Battle-axe Lance"));

                game.getAdventureDeck().addToDrawPile(rigDeck.reversed());

                /* Rig event deck */
                rigDeck = new ArrayList<>(Card.stringToCards("Q4 Q3"));
                game.getEventDeck().addToDrawPile(rigDeck.reversed());
                break;
            case 3: // 1winner_game_with_events
                // Rig initial hands of each player
                game.getPlayerByID("P1").overwriteHand(Card.stringToCards(
                        "F5 F5 F10 F10 F15 F15 F20 F20 Dagger Dagger Dagger Dagger"));
                game.getPlayerByID("P2").overwriteHand(Card.stringToCards(
                        "F25 F30 Horse Horse Sword Sword Sword Battle-axe Battle-axe Lance Lance Excalibur"));
                game.getPlayerByID("P3").overwriteHand(Card.stringToCards(
                        "F25 F30 Horse Horse Sword Sword Sword Battle-axe Battle-axe Lance Lance Excalibur"));
                game.getPlayerByID("P4").overwriteHand(Card.stringToCards(
                        "F25 F30 F70 Horse Horse Sword Sword Sword Battle-axe Battle-axe Lance Lance"));

                /* Rig adventure deck */
                rigDeck = new ArrayList<>();
                // Quest 1
                rigDeck.addAll(Card.stringToCards("F5 F10 F20"));   // Stage 1
                rigDeck.addAll(Card.stringToCards("F15 F5 F25"));   // Stage 2
                rigDeck.addAll(Card.stringToCards("F5 F10 F20"));   // Stage 3
                rigDeck.addAll(Card.stringToCards("F5 F10 F20"));   // Stage 4

                // 8 Sponsor reward cards for P1
                rigDeck.addAll(Card.stringToCards("F5 F5 F10 F10 F15 F15 F15 F15"));

                // 8 Prosperity triggered cards; order starts with current player P3!
                rigDeck.addAll(Card.stringToCards("Battle-axe F40"));   // P3
                rigDeck.addAll(Card.stringToCards("Dagger Dagger"));    // P4
                rigDeck.addAll(Card.stringToCards("F25 F25"));          // P1
                rigDeck.addAll(Card.stringToCards("Horse Sword"));      // P2

                // 2 Queen's Favor triggered cards for P4
                rigDeck.addAll(Card.stringToCards("F30 F25"));

                // Quest 2
                rigDeck.addAll(Card.stringToCards("Battle-axe Horse F50")); // Stage 1
                rigDeck.addAll(Card.stringToCards("Sword Sword"));          // Stage 2
                rigDeck.addAll(Card.stringToCards("F40 F50"));              // Stage 3
                // 8 Sponsor reward cards for P1
                rigDeck.addAll(Card.stringToCards("Horse Horse Horse Sword Sword Sword Sword F35"));

                game.getAdventureDeck().addToDrawPile(rigDeck.reversed());

                /* Rig event deck */
                rigDeck = new ArrayList<>(Card.stringToCards("Q4 Plague Prosperity"));
                rigDeck.add(new Card("Queen's Favor")); // Space in name necessitates separate addition
                rigDeck.add(new Card("Q3"));

                game.getEventDeck().addToDrawPile(rigDeck.reversed());
                break;
            case 4: // 0_winner_quest
                // Rig initial hands of each player
                game.getPlayerByID("P1").overwriteHand(Card.stringToCards(
                        "F50 F70 Dagger Dagger Horse Horse Sword Sword Battle-axe Battle-axe Lance Lance"));
                game.getPlayerByID("P2").overwriteHand(Card.stringToCards(
                        "F5 F5 F10 F15 F15 F20 F20 F25 F30 F30 F40 Excalibur"));
                game.getPlayerByID("P3").overwriteHand(Card.stringToCards(
                        "F5 F5 F10 F15 F15 F20 F20 F25 F25 F30 F40 Lance"));
                game.getPlayerByID("P4").overwriteHand(Card.stringToCards(
                        "F5 F5 F10 F15 F15 F20 F20 F25 F25 F30 F50 Excalibur"));

                // Rig adventure deck; cards added first should be drawn last
                rigDeck = new ArrayList<>();
                // Stage 1, player participation
                rigDeck.addAll(Card.stringToCards("F5 F15 F10"));
                // 14 Sponsor rewards for P1
                rigDeck.addAll(Card.stringToCards(
                        "F5 F10 F15 Dagger Dagger Dagger Dagger Horse Horse Horse Horse Sword Sword Sword"));
                game.getAdventureDeck().addToDrawPile(rigDeck.reversed());

                // Rig event deck with one Q2 on top
                game.getEventDeck().addToDrawPile(new Card("Q2"));
                break;
        }
    }
}
