const API_BASE_URL = "http://localhost:8080";

const outputConsole = document.getElementById("outputConsole");
const playerInfoTable = document.getElementById("playerInfoTable");
const cardSelectionTable = document.getElementById("cardSelectionTable");
const sendButton = document.getElementById("inputSubmitButton");
const inputTextArea = document.getElementById("inputTextArea");

// Enable pressing <Enter> to send text from the text area.
inputTextArea.addEventListener("keyup", (event) => {
    if (event.key === "Enter") sendButton.click();
});

let consoleText = "";

async function startGame(scenario = 0) {
    clearConsole();

    if (scenario === 0) {
        appendToConsole("Starting a new random game...\n");
    } else {
        appendToConsole(`Starting a game with cards rigged for scenario ${scenario}...\n`);
    }

    try {
        const response = await fetch(`${API_BASE_URL}/start?scenario=${scenario}`, { method: "POST" });
        const gameState = await response.json();
        updatePage(gameState);
    } catch (error) {
        const errorText = `startGame() failed: ${error}`;
        console.error(errorText);
        appendToConsole(errorText + "\n");
    }
}

async function sendText() {
    const textToSend = inputTextArea.value; // Save text in text area
    inputTextArea.value = ""; // Clear text area

    try {
        console.log(`Sending '${textToSend}'...`);
        const response = await fetch(`${API_BASE_URL}/submit?submittedText=${textToSend}`, { method: "POST" });
        const gameState = await response.json();
        updatePage(gameState);
    } catch (error) {
        const sentMessage = textToSend === "" ? "(blank message)" : textToSend.substring(0, 10);
        const errorText = `sendText() failed to send message '${sentMessage}': ${error}`;
        console.error(errorText);
        appendToConsole(errorText + "\n");
    }
}

function initPlayerInfoTable() {
    playerInfoTable.innerHTML = ""; // Wipe table contents

    const tHead = playerInfoTable.createTHead();

    const playerCell = document.createElement("th");
    playerCell.textContent = "Player";
    tHead.appendChild(playerCell);

    const shieldsCell = document.createElement("th");
    shieldsCell.textContent = "Shields";
    tHead.appendChild(shieldsCell);

    const handSizeCell = document.createElement("th");
    handSizeCell.textContent = "Card#";
    tHead.appendChild(handSizeCell);

    for (let i = 0; i < 12; i++) {
        const cardCell = document.createElement("th");
        cardCell.textContent = `${i + 1}`;
        tHead.appendChild(cardCell);
    }
}

function updatePage(gameState) {
    if (gameState == null) {
        console.error("Invalid gameState object!");
        return;
    }

    console.log(`Received response object: '${getFirstLine(JSON.stringify(gameState)).substring(0, 100)}(...)'`);

    if (gameState["gameStarted"]) {
        // Enable input elements of page
        sendButton.disabled = false;
        sendButton.textContent = "Send";
        inputTextArea.disabled = false;

        // Add received game text to the console
        appendToConsole(gameState["gameText"]);

        // Update card selection table
        cardSelectionTable.innerHTML = ""; // Wipe table contents
        if (gameState["currSelectionMenu"].length !== 0) {
            const tHead = cardSelectionTable.createTHead();
            const row = cardSelectionTable.insertRow(0);
            row.id = "selectionRow";

            gameState["currSelectionMenu"].forEach((cardID, index) => {
                const cardNumber = index + 1;

                const cardNumberHeader = document.createElement("th");
                cardNumberHeader.textContent = `${cardNumber}`;
                tHead.appendChild(cardNumberHeader);

                const cardCell = row.insertCell(index);
                cardCell.textContent = cardID;
                cardCell.id = `selection_${cardNumber}`;
            });
        }

        // Clear player info table and repopulate with player info
        initPlayerInfoTable();
        const sortedPIDs = Object.keys(gameState["players"]);
        sortedPIDs.sort(); // Sort by player IDs, ascending
        sortedPIDs.forEach((playerID, index) => {
            const { shields, handSize, hand } = gameState["players"][playerID];
            const cardList = hand.split(" ");

            const row = playerInfoTable.insertRow(index);

            const playerCell = row.insertCell(0);
            playerCell.textContent = playerID;

            const shieldsCell = row.insertCell(1);
            shieldsCell.textContent = shields;
            shieldsCell.id = `${playerID}_shields`;

            const handSizeCell = row.insertCell(2);
            handSizeCell.textContent = handSize;
            handSizeCell.id = `${playerID}_handSize`;

            for (const [i, card] of cardList.entries()) {
                const cardNum = i + 1;

                const cardCell = row.insertCell(i + 3);
                // noinspection JSValidateTypes
                cardCell.textContent = card;
                cardCell.id = `${playerID}_card_${cardNum}`;
                if (cardNum > 12) {
                    const cardCell = document.createElement("th");
                    cardCell.textContent = `${cardNum}`;
                    playerInfoTable.tHead.appendChild(cardCell);
                }
            }
        });
    } else {
        appendToConsole("\nThere is no active game.");
    }
}

// Return the first line of a multiline string.
function getFirstLine(input) {
    return input.slice(0, input.indexOf("\n"));
}

function clearConsole() {
    consoleText = "";
}

function appendToConsole(text) {
    consoleText += text;
    outputConsole.innerText = consoleText;
    outputConsole.scrollTop = outputConsole.scrollHeight; // Scroll console to bottom
}
