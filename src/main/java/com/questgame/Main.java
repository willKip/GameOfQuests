package com.questgame;

import java.io.PrintWriter;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Game game = new Game(new Scanner(System.in), new PrintWriter(System.out));
        game.initGame(); // Initialises decks and players, sets up player hands
        game.startGameLoop(); // Do game loop until winners found
    }
}
