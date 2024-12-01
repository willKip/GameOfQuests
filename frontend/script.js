const apiBaseUrl = "http://localhost:8080";

// Return the first line of a multiline string.
function getFirstLine(input) {
    return input.slice(0, input.indexOf("\n"));
}

function updateConsole(text) {
    const console = document.getElementById("outputText");
    console.innerText = text;
    console.scrollTop = console.scrollHeight;
}

async function startGame() {
    try {
        const response = await fetch(`${apiBaseUrl}/start`);
        const result = await response.text();

        console.log(`startGame() received a response: '${getFirstLine(result)}...'`);
        updateConsole(result);
    } catch (error) {
        console.error("Error in startGame():", error);
    }
}
