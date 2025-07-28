# A Game of Quests

## How to Run

- The Chrome web browser and an IDE (IntelliJ, Visual Studio Code etc.) is necessary to run the game and its tests.
- Before running anything, make sure Maven dependencies have been reloaded.
- **Start Backend**: In an IDE, open [Application.java](src/main/java/com/a3/Application.java) and run
  `main()`.

### Playing the game manually
- Using a Chrome instance,
  open [index.html](frontend/index.html). It will load the frontend JavaScript automatically, and be ready to
  communicate with the Java backend. A random game or predefined
  scenarios can be started in the UI; the game is otherwise text-based, and input can be entered in the text area and
  sent by clicking the button or pressing Enter.

### Selenium Tests (IDE)
- The tests are defined in [SeleniumTest.java](src/test/java/com/a3/SeleniumTest.java). All can be run sequentially in an IDE by running the `SeleniumTest` class: a browser window will be opened automatically for testing, and close automatically after test completion.
