package com.a3;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Scanner;

@RestController
@CrossOrigin(origins = "*")
public class Controller {
    private Game game;
    private StringWriter output = new StringWriter();

    public Controller() {
        newGame();
    }

    @GetMapping("/start")
    public String startGame() {
        String inputStr = "n\n\ny\n1\n7\nquit\n2\n5\nquit\n2\n3\n4\nquit\n2\n3\nquit\n\nn\n1\n\nn\n1\n\nn\n1\n\n5\n5"
                          + "\nquit\n\n5\n4\nquit\n\n4\n6\nquit\n\nn\n\nn\n\nn\n\n7\n6\nquit\n\n9\n4\nquit\n\n6\n6"
                          + "\nquit\n\nn\n\nn\n\n9\n6\n5\nquit\n\n7\n5\n6\nquit\n\nn\n\nn\n\n7\n6\n6\nquit\n\n4\n4\n4"
                          + "\n5\nquit\n\n1\n1\n1\n1\n\n";
        StringWriter output = new StringWriter();
        game = new Game(new Scanner(inputStr), new PrintWriter(output));
        game.initGame(); // Initialises decks and players, sets up player hands
        game.enableInputEcho();

        Player p1 = game.getPlayerByID("P1"), p2 = game.getPlayerByID("P2"), p3 = game.getPlayerByID("P3"), p4 =
                game.getPlayerByID("P4");

        // Rig initial hands of each player
        p1.overwriteHand(Card.stringToCards("F5 F5 F15 F15 D5 S10 S10 H10 H10 B15 B15 L20"));
        p2.overwriteHand(Card.stringToCards("F5 F5 F15 F15 F40 D5 S10 H10 H10 B15 B15 E30"));
        p3.overwriteHand(Card.stringToCards("F5 F5 F5 F15 D5 S10 S10 S10 H10 H10 B15 L20"));
        p4.overwriteHand(Card.stringToCards("F5 F15 F15 F40 D5 D5 S10 H10 H10 B15 L20 E30"));

        // Rig adventure deck; cards added first should be drawn last
        ArrayList<Card> rigAdvDeck = new ArrayList<>();
        rigAdvDeck.addAll(Card.stringToCards("F30 Sword Battle-axe"));  // Stage 1
        rigAdvDeck.addAll(Card.stringToCards("F10 Lance Lance"));       // Stage 2
        rigAdvDeck.addAll(Card.stringToCards("Battle-axe Sword"));      // Stage 3
        rigAdvDeck.addAll(Card.stringToCards("F30 Lance"));             // Stage 4
        game.getAdventureDeck().addToDrawPile(rigAdvDeck.reversed());

        game.getEventDeck().addToDrawPile(new Card("Q4")); // Rig event deck with one Q4 on top

        game.runTurn(); // Do one turn

        return output.toString();
    }

    private void newGame() {
        game = new Game(new PrintWriter(output));
        game.initGame(); // Set up a new game with a standard deck and random hands per player.
        game.enableInputEcho();
    }
}
