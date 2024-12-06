# F24 COMP 4004 Assignment 3

- Student Name (Last, First): Lee, William
- Student Number: 101181435

## How to Run

- Before running anything, make sure Maven dependencies have been reloaded.
- **Start Backend**: In IntelliJ, open [Application.java](src/main/java/com/a3/Application.java) and run `main()` by
  clicking on the gutter icon.
- **Play the game manually**: (Backend must be running!) Using a Chrome instance,
  open [index.html](frontend/index.html). It will load the frontend JavaScript automatically, and be ready to
  communicate with the Java backend. It is not necessary to run a server for the frontend. A random game or predefined
  scenarios can be started in the UI; the game is otherwise text-based, and input can be entered in the text area and
  sent by clicking the button or pressing Enter.
- **Selenium tests**: (Backend must be running!) In IntelliJ,
  open [SeleniumTest.java](src/test/java/com/a3/SeleniumTest.java) and run all tests in
  one shot by clicking on the gutter icon next to the class. JUnit was used to run the scenarios sequentially. _The
  scenario definition code is in this file, near the top._ The test results will show in IntelliJ, and the browser
  window will close shortly after test completion.
