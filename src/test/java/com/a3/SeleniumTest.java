package com.a3;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOError;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SeleniumTest {
    /* TEST CONSTANTS */
    // General delay after each action
    private static final int COMMAND_DELAY_MS = 800;
    // How long to keep the test window open after all tests have ended. Negative value will close it immediately.
    private static final int PERSIST_WINDOW_MS = 10000;

    private WebDriver driver;
    private WebElement outputConsole;
    private WebElement textArea;
    private WebElement sendButton;

    @BeforeAll
    void setupWindow() {
        String frontendURI;
        try {
            frontendURI = Paths.get("frontend/index.html").toUri().toString();
        } catch (IOError e) {
            // FALLBACK IF RUNNING INDEX.HTML DOESN'T WORK:
            // Run the backend, then run server in the frontend directory with 'npx http-server' and paste the URL here
            frontendURI = "http://127.0.0.1:8081";
        }

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized"); // Make window fullscreen.

        driver = new ChromeDriver(options);
        driver.get(frontendURI);

        /* Find and save references to common elements */
        outputConsole = driver.findElement(By.id("outputConsole"));
        textArea      = driver.findElement(By.id("inputTextArea"));
        sendButton    = driver.findElement(By.id("inputSubmitButton"));

        assertEquals("A Game of Quests", driver.getTitle(), "Correct window is opened");
    }

    @AfterAll
    void teardownWindow() throws InterruptedException {
        if (PERSIST_WINDOW_MS >= 0) {
            sleep(PERSIST_WINDOW_MS);
        }
        driver.quit();
    }

    /* SCENARIO DEFINITIONS START */
    @Test
    @DisplayName("Scenario 1: 'A1_scenario'")
    @Order(1)
    void scenario_1_A1_scenario() throws InterruptedException {
        clickScenarioButton(1);

        // P1: decline to sponsor Q4
        sendNo();
        sendReturn();

        // P2: agree to sponsor Q4
        sponsorAndBuildQuest(List.of("F5 Horse", "F15 Sword", "F15 Dagger Axe", "F40 Battle-axe"));

        // Stage 1
        participateAndTrim("F5"); // P1 participates
        participateAndTrim("F5"); // P3 participates
        participateAndTrim("F5"); // P4 participates
        buildAttack("Dagger Sword"); // P1 attacks, wins
        buildAttack("Sword Dagger"); // P3 attacks, wins
        buildAttack("Dagger Horse"); // P4 attacks, wins

        // Stage 2
        participateAndTrim(); // P1 participates
        participateAndTrim(); // P3 participates
        participateAndTrim(); // P4 participates
        buildAttack("Horse Sword"); // P1 attacks, loses
        buildAttack("Battle-axe Sword"); // P3 attacks, wins
        buildAttack("Horse Battle-axe"); // P4 attacks, wins

        // Stage 3
        participateAndTrim(); // P3 participates
        participateAndTrim(); // P4 participates
        buildAttack("Lance Horse Sword"); // P3 attacks, wins
        buildAttack("Battle-axe Sword Lance"); // P4 attacks, wins

        // Stage 4
        participateAndTrim(); // P3 participates
        participateAndTrim(); // P4 participates
        buildAttack("Battle-axe Horse Lance"); // P3 attacks, loses
        buildAttack("Dagger Sword Lance Excalibur"); // P4 attacks, wins

        // Sponsor P2 trims
        pickCards("F5 F5 F10 F70");
        // (Do not send <return> here, it will move on to the next turn and potentially draw more cards)

        /* Asserts */
        assertAll("Shield count",
                  () -> assertEquals(0, readShieldCount(1), "P1"),
                  () -> assertEquals(0, readShieldCount(2), "P2"),
                  () -> assertEquals(0, readShieldCount(3), "P3"),
                  () -> assertEquals(4, readShieldCount(4), "P4"));

        assertAll("Hand size",
                  () -> assertEquals(9, readCardCount(1), "P1"),
                  () -> assertEquals(12, readCardCount(2), "P2"),
                  () -> assertEquals(5, readCardCount(3), "P3"),
                  () -> assertEquals(4, readCardCount(4), "P4"));

        // A1_scenario does not specify specific cards for P2; they draw 13 random cards and can trim whichever ones.
        assertAll("Cards in hand",
                  () -> assertEquals("F5 F10 F15 F15 F30 H10 B15 B15 L20", getHandAsString(1), "P1"),
                  () -> assertEquals("F5 F5 F15 F30 S10", getHandAsString(3), "P3"),
                  () -> assertEquals("F15 F15 F40 L20", getHandAsString(4), "P4"));

        // Partial game scenario; winner declaration is not made
    }

    @Test
    @DisplayName("Scenario 2: '2winner_game_2winner_quest'")
    @Order(2)
    void scenario_2_2winner_game_2winner_quest() throws InterruptedException {
        clickScenarioButton(2);

        // P1: agree to sponsor Q4
        sponsorAndBuildQuest(List.of("F5", "F5 Dagger", "F10 Horse", "F10 Axe"));

        // Stage 1
        participateAndTrim("F5"); // P2 participates
        participateAndTrim("F5"); // P3 participates
        participateAndTrim("F10"); // P4 participates
        buildAttack("Horse"); // P2 attacks, wins
        buildAttack(); // P3 attacks with nothing, loses
        buildAttack("Horse"); // P4 attacks, wins

        // Stage 2
        participateAndTrim(); // P2 participates
        participateAndTrim(); // P4 participates
        buildAttack("Sword"); // P2 attacks, wins
        buildAttack("Sword"); // P4 attacks, wins

        // Stage 3
        participateAndTrim(); // P2 participates
        participateAndTrim(); // P4 participates
        buildAttack("Horse Sword"); // P2 attacks, wins
        buildAttack("Horse Sword"); // P4 attacks, wins

        // Stage 4
        participateAndTrim(); // P2 participates
        participateAndTrim(); // P4 participates
        buildAttack("Sword Axe"); // P2 attacks, wins
        buildAttack("Sword Axe"); // P4 attacks, wins

        // Sponsor P1 trims
        pickCards("F5 F10 F15 F15");
        sendReturn();

        // P2: decline to sponsor Q3
        sendNo();
        sendReturn();

        // P3: agree to sponsor Q3
        sponsorAndBuildQuest(List.of("F5", "F5 Dagger", "F5 Horse"));

        // Stage 1
        sendYes(); // P1 withdraws
        participateAndTrim(); // P2 participates
        participateAndTrim(); // P4 participates
        buildAttack("Dagger"); // P2 attacks, wins
        buildAttack("Dagger"); // P4 attacks, wins

        // Stage 2
        participateAndTrim(); // P2 participates
        participateAndTrim(); // P4 participates
        buildAttack("Axe"); // P2 attacks, wins
        buildAttack("Axe"); // P4 attacks, wins

        // Stage 4
        participateAndTrim(); // P2 participates
        participateAndTrim(); // P4 participates
        buildAttack("Excalibur"); // P2 attacks, wins
        buildAttack("Excalibur"); // P4 attacks, wins

        // Sponsor P3 trims
        pickCards("F20 F25 F30");
        sendReturn();

        /* Asserts */
        assertAll("Shield count",
                  () -> assertEquals(0, readShieldCount(1), "P1"),
                  () -> assertEquals(7, readShieldCount(2), "P2"),
                  () -> assertEquals(0, readShieldCount(3), "P3"),
                  () -> assertEquals(7, readShieldCount(4), "P4"));

        assertAll("Hand size",
                  () -> assertEquals(12, readCardCount(1), "P1"),
                  () -> assertEquals(9, readCardCount(2), "P2"),
                  () -> assertEquals(12, readCardCount(3), "P3"),
                  () -> assertEquals(9, readCardCount(4), "P4"));

        assertAll("Cards in hand",
                  () -> assertEquals("F15 F15 F20 F20 F20 F20 F25 F25 F30 H10 B15 L20", getHandAsString(1), "P1"),
                  () -> assertEquals("F10 F15 F15 F25 F30 F40 F50 L20 L20", getHandAsString(2), "P2"),
                  () -> assertEquals("F20 F40 D5 D5 S10 H10 H10 H10 H10 B15 B15 L20", getHandAsString(3), "P3"),
                  () -> assertEquals("F15 F15 F20 F25 F30 F50 F70 L20 L20", getHandAsString(4), "P4"));

        String last5Lines = consoleLastNLines(5);
        assertAll("Correct winners are displayed",
                  () -> assertTrue(last5Lines.contains("The game has concluded!"), "Print that game has finished"),
                  () -> assertFalse(last5Lines.contains("P1"), "P1 has not won"),
                  () -> assertTrue(last5Lines.contains("P2"), "P2 has won"),
                  () -> assertFalse(last5Lines.contains("P3"), "P3 has not won"),
                  () -> assertTrue(last5Lines.contains("P4"), "P4 has won"));
    }

    @Test
    @DisplayName("Scenario 3: '1winner_game_with_events'")
    @Order(3)
    void scenario_3_1winner_game_with_events() throws InterruptedException {
        clickScenarioButton(3);

        // P1: agree to sponsor Q4
        sponsorAndBuildQuest(List.of("F5", "F10", "F15", "F20"));

        // Stage 1
        participateAndTrim("F5"); // P2 participates
        participateAndTrim("F10"); // P3 participates
        participateAndTrim("F20"); // P4 participates
        buildAttack("Sword"); // P2 attacks, wins
        buildAttack("Sword"); // P3 attacks, wins
        buildAttack("Sword"); // P4 attacks, wins

        // Stage 2
        participateAndTrim(); // P2 participates
        participateAndTrim(); // P3 participates
        participateAndTrim(); // P4 participates
        buildAttack("Horse"); // P2 attacks, wins
        buildAttack("Horse"); // P3 attacks, wins
        buildAttack("Horse"); // P4 attacks, wins

        // Stage 3
        participateAndTrim(); // P2 participates
        participateAndTrim(); // P3 participates
        participateAndTrim(); // P4 participates
        buildAttack("Axe"); // P2 attacks, wins
        buildAttack("Axe"); // P3 attacks, wins
        buildAttack("Axe"); // P4 attacks, wins

        // Stage 4
        participateAndTrim(); // P2 participates
        participateAndTrim(); // P3 participates
        participateAndTrim(); // P4 participates
        buildAttack("Lance"); // P2 attacks, wins
        buildAttack("Lance"); // P3 attacks, wins
        buildAttack("Lance"); // P4 attacks, wins

        // Sponsor P1 trims
        pickCards("F5 F5 F10 F10");
        sendReturn();

        // P2 draws Plague, loses 2 shields
        sendReturn();

        // P3 draws Prosperity, all 4 players receive 2 adventure cards (NOTE: starting from current player).
        pickCards("F5");     // P3
        sendReturn();
        pickCards("F20");    // P4
        sendReturn();
        pickCards("F5 F10"); // P1
        sendReturn();
        pickCards("F5");     // P2
        sendReturn();
        sendReturn();

        // P4 draws Queen's Favor, draws 2 cards
        pickCards("F25 F30"); // P4 trims
        sendReturn();

        // P1: agree to sponsor Q3
        sponsorAndBuildQuest(List.of("F15", "F15 Dagger", "F20 Dagger"));

        // Stage 1
        participateAndTrim("F5"); // P2 participates
        participateAndTrim("F10"); // P3 participates
        participateAndTrim("F20"); // P4 participates
        buildAttack("Axe"); // P2 attacks, wins
        buildAttack("Axe"); // P3 attacks, wins
        buildAttack("Horse"); // P4 attacks, loses

        // Stage 2
        participateAndTrim(); // P2 participates
        participateAndTrim(); // P3 participates
        buildAttack("Axe Horse"); // P2 attacks, wins
        buildAttack("Axe Sword"); // P3 attacks, wins

        // Stage 3
        participateAndTrim(); // P2 participates
        participateAndTrim(); // P3 participates
        buildAttack("Lance Sword"); // P2 attacks, wins
        buildAttack("Excalibur"); // P3 attacks, wins

        // Sponsor P1 trims
        pickCards("F15 F15 F15");
        sendReturn();

        /* Asserts */
        assertAll("Shield count",
                  () -> assertEquals(0, readShieldCount(1), "P1"),
                  () -> assertEquals(5, readShieldCount(2), "P2"),
                  () -> assertEquals(7, readShieldCount(3), "P3"),
                  () -> assertEquals(4, readShieldCount(4), "P4"));

        assertAll("Hand size",
                  () -> assertEquals(12, readCardCount(1), "P1"),
                  () -> assertEquals(9, readCardCount(2), "P2"),
                  () -> assertEquals(10, readCardCount(3), "P3"),
                  () -> assertEquals(11, readCardCount(4), "P4"));

        assertAll("Cards in hand",
                  () -> assertEquals("F25 F25 F35 D5 D5 S10 S10 S10 S10 H10 H10 H10", getHandAsString(1), "P1"),
                  () -> assertEquals("F15 F25 F30 F40 S10 S10 S10 H10 E30", getHandAsString(2), "P2"),
                  () -> assertEquals("F10 F25 F30 F40 F50 S10 S10 H10 H10 L20", getHandAsString(3), "P3"),
                  () -> assertEquals("F25 F25 F30 F50 F70 D5 D5 S10 S10 B15 L20", getHandAsString(4), "P4"));

        String last5Lines = consoleLastNLines(5);
        assertAll("Correct winners are displayed",
                  () -> assertTrue(last5Lines.contains("The game has concluded!"), "Print that game has finished"),
                  () -> assertFalse(last5Lines.contains("P1"), "P1 has not won"),
                  () -> assertFalse(last5Lines.contains("P2"), "P2 has not won"),
                  () -> assertTrue(last5Lines.contains("P3"), "P3 has won"),
                  () -> assertFalse(last5Lines.contains("P4"), "P4 has not won"));
    }

    @Test
    @DisplayName("Scenario 4: '0_winner_quest'")
    @Order(4)
    void scenario_4_0_winner_quest() throws InterruptedException {
        clickScenarioButton(4);

        // P1: agree to sponsor Q2
        sponsorAndBuildQuest(List.of("F50 Dagger Sword Horse Axe Lance", "F70 Dagger Sword Horse Axe Lance"));

        // Stage 1
        participateAndTrim("F5"); // P2 participates
        participateAndTrim("F15"); // P3 participates
        participateAndTrim("F10"); // P4 participates
        buildAttack("Excalibur");   // P2 attacks, loses
        buildAttack(); // P3 attacks with nothing, loses
        buildAttack(); // P4 attacks with nothing, loses

        // Sponsor P1 trims
        pickCards("F5 F10");
        // (Do not send <return> here, it will move on to the next turn and potentially draw more cards)

        /* Asserts */
        assertAll("Shield count",
                  () -> assertEquals(0, readShieldCount(1), "P1"),
                  () -> assertEquals(0, readShieldCount(2), "P2"),
                  () -> assertEquals(0, readShieldCount(3), "P3"),
                  () -> assertEquals(0, readShieldCount(4), "P4"));

        assertAll("Hand size",
                  () -> assertEquals(12, readCardCount(1), "P1"),
                  () -> assertEquals(11, readCardCount(2), "P2"),
                  () -> assertEquals(12, readCardCount(3), "P3"),
                  () -> assertEquals(12, readCardCount(4), "P4"));

        assertAll("Cards in hand",
                  () -> assertEquals("F15 D5 D5 D5 D5 S10 S10 S10 H10 H10 H10 H10", getHandAsString(1), "P1"),
                  () -> assertEquals("F5 F5 F10 F15 F15 F20 F20 F25 F30 F30 F40", getHandAsString(2), "P2"),
                  () -> assertEquals("F5 F5 F10 F15 F15 F20 F20 F25 F25 F30 F40 L20", getHandAsString(3), "P3"),
                  () -> assertEquals("F5 F5 F10 F15 F15 F20 F20 F25 F25 F30 F50 E30", getHandAsString(4), "P4"));

        // Partial game scenario; winner declaration is not made
    }
    /* SCENARIO DEFINITIONS END */

    private void sendReturn() throws InterruptedException {
        send("");
    }

    private void sendYes() throws InterruptedException {
        send("y");
    }

    private void sendNo() throws InterruptedException {
        send("n");
    }

    private void sendQuit() throws InterruptedException {
        send("quit");
    }

    private void participateAndTrim() throws InterruptedException {
        participateAndTrim("");
    }

    private void participateAndTrim(String cardIDs) throws InterruptedException {
        sendNo(); // To participate, have to say NO to withdrawing
        pickCards(cardIDs); // Pick cards to trim
        sendReturn();
    }

    private void buildAttack() throws InterruptedException {
        buildAttack("");
    }

    private void buildAttack(String cardIDs) throws InterruptedException {
        pickCards(cardIDs); // Pick cards
        sendQuit(); // Finalize attack
        sendReturn(); // End turn
    }

    private void sponsorAndBuildQuest(List<String> stageCardIDs) throws InterruptedException {
        sendYes();
        for (String cardIDs : stageCardIDs) {
            buildStage(cardIDs);
        }
        sendReturn();
    }

    private void buildStage(String cardIDs) throws InterruptedException {
        pickCards(cardIDs);
        sendQuit();
    }

    // Run pickCard() on a space-separated string of card IDs.
    private void pickCards(String cardIDs) throws InterruptedException, RuntimeException {
        if (cardIDs.isBlank()) {
            return;
        }

        List<String> cards = List.of(cardIDs.split(" "));

        for (String card : cards) {
            String cardName = card;
            switch (cardName.toLowerCase()) {
                case "dagger" -> cardName = "D5";
                case "sword" -> cardName = "S10";
                case "horse" -> cardName = "H10";
                case "battle-axe", "axe" -> cardName = "B15";
                case "lance" -> cardName = "L20";
                case "excalibur" -> cardName = "E30";
            }

            List<WebElement> cardMatches = driver.findElements(By.xpath(
                    "//table[@id='cardSelectionTable']//td[text() = '" + cardName + "']"));

            if (cardMatches.isEmpty()) {
                throw new RuntimeException("'" + cardName + "' not found in selection!");
            } else {
                String cardIndex = Objects.requireNonNull(cardMatches.getFirst().getAttribute("id")).split("_")[1];
                send(cardIndex);
            }
        }
    }

    private void send(String text) throws InterruptedException {
        textArea.sendKeys(text);
        sleep(COMMAND_DELAY_MS);
        sendButton.click();
        sleep(Math.max(300, COMMAND_DELAY_MS)); // At least 200ms needed for backend to update
    }

    private void clickScenarioButton(int scenarioNum) throws InterruptedException {
        driver.findElement(By.id("scenarioButton" + scenarioNum)).click();
        sleep(COMMAND_DELAY_MS);
    }

    private int readShieldCount(int playerNum) {
        return Integer.parseInt(driver.findElement(By.id("P" + playerNum + "_shields")).getText());
    }

    private int readCardCount(int playerNum) {
        return Integer.parseInt(driver.findElement(By.id("P" + playerNum + "_handSize")).getText());
    }

    // Return a space-separated string of the specified player's hand, ordered.
    private String getHandAsString(int playerNum) {
        List<WebElement> cardElements = driver.findElements(By.xpath(
                "//td[starts-with(@id, 'P" + playerNum + "_card_')]"));

        StringJoiner sj = new StringJoiner(" ");
        for (WebElement card : cardElements) {
            sj.add(card.getText());
        }

        return sj.toString();
    }

    // Return the last n lines of the page console ('\n'-separated) as a string.
    @SuppressWarnings("SameParameterValue")
    private String consoleLastNLines(int n) {
        final List<String> stringLines = Arrays.asList(outputConsole.getText().split("\n"));
        final int stringLineCount = stringLines.size();
        return String.join("\n", stringLines.subList(Math.max(0, stringLineCount - n), stringLineCount));
    }
}
