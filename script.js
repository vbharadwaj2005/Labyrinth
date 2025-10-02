const d = document;
const w = window;

let gameMode, difficulty, gameInProgress = false, isPaused = false;
let gameTimer, currentPosition, totalGameTimeInSeconds, score = 0;

const body = d.body;
const topBar = d.getElementById("topBar");
const gameModeOverlay = d.getElementById("gameModeOverlay");
const difficultyOverlay = d.getElementById("difficultyOverlay");

const N = 1 << 0, S = 1 << 1, W = 1 << 2, E = 1 << 3;

const difficultySettings = {
    easy: { cellSize: 30, cellSpacing: 20, time: 5 },
    medium: { cellSize: 25, cellSpacing: 15, time: 10 },
    hard: { cellSize: 20, cellSpacing: 10, time: 15 }
};

let cellSize, cellSpacing, cellWidth, cellHeight;
let cells, frontier, mazeAreaWidth, mazeAreaHeight;
let maxX, maxY, startCellIndex;
let mazeContext, gameContext, darknessContext;

function selectGameMode(mode) {
    gameMode = mode;
    gameModeOverlay.classList.add("hidden");
    difficultyOverlay.classList.remove("hidden");
}

function initializeGame(diff) {
    difficulty = diff;
    const settings = difficultySettings[difficulty];
    cellSize = settings.cellSize;
    cellSpacing = settings.cellSpacing;
    totalGameTimeInSeconds = settings.time * 60;

    difficultyOverlay.classList.add("hidden");
    topBar.classList.remove("hidden");
    d.getElementById("timer").textContent = `${settings.time}:00`;
    d.getElementById("pauseButton").disabled = true;
    if (gameMode === 'endless') d.getElementById("scoreDisplay").classList.remove("hidden");

    const gameWidth = w.innerWidth;
    const gameHeight = w.innerHeight - topBar.offsetHeight;
    cellWidth = Math.floor((gameWidth - cellSpacing) / (cellSize + cellSpacing));
    cellHeight = Math.floor((gameHeight - cellSpacing) / (cellSize + cellSpacing));
    maxX = cellWidth - 1;
    maxY = cellHeight - 1;
    mazeAreaWidth = cellWidth * (cellSize + cellSpacing) + cellSpacing;
    mazeAreaHeight = cellHeight * (cellSize + cellSpacing) + cellSpacing;
    
    mazeContext = createCanvas("mazeCanvas", gameWidth, gameHeight);
    gameContext = createCanvas("gameCanvas", gameWidth, gameHeight);
    if (gameMode === 'darkness') darknessContext = createCanvas("darknessCanvas", gameWidth, gameHeight);
    
    resetForNextMaze();
}

function resetForNextMaze() {
    cells = new Array(cellWidth * cellHeight);
    frontier = [];
    startCellIndex = maxY * cellWidth;
    cells[startCellIndex] = 0;
    frontier.push({ index: startCellIndex, direction: N });
    frontier.push({ index: startCellIndex, direction: E });
    
    generateMaze();
    drawInitialMarkers();

    if(gameMode === 'darkness' && currentPosition != null) updateDarkness();
    if(gameInProgress) {
        currentPosition = startCellIndex;
        drawPlayer();
    }
}

function createCanvas(id, gameWidth, gameHeight) {
    const canvas = d.createElement("canvas");
    canvas.id = id;
    canvas.width = gameWidth;
    canvas.height = gameHeight;
    body.appendChild(canvas);
    const context = canvas.getContext("2d");
    const hTranslate = Math.round((gameWidth - mazeAreaWidth) / 2);
    const vTranslate = Math.round((gameHeight - mazeAreaHeight) / 2);
    context.translate(hTranslate, vTranslate);
    return context;
}

function generateMaze() {
    mazeContext.clearRect(-w.innerWidth, -w.innerHeight, w.innerWidth*2, w.innerHeight*2);
    mazeContext.fillStyle = "#00FFFF";
    mazeContext.fillRect(0, 0, mazeAreaWidth, mazeAreaHeight);
    carvePath(startCellIndex);
    while (!exploreFrontier()) {}
}

function exploreFrontier() {
    if (frontier.length === 0) return true;
    const edge = popRandom(frontier);
    if (!edge) return true;
    const i0 = edge.index;
    const d0 = edge.direction;
    const i1 = i0 + (d0 === N ? -cellWidth : d0 === S ? cellWidth : d0 === W ? -1 : 1);
    if (i1 < 0 || i1 >= cells.length || cells[i1] != null) return false;
    const d1 = d0 === N ? S : d0 === S ? N : d0 === W ? E : W;
    cells[i0] |= d0;
    cells[i1] = d1;
    carvePath(i1);
    if (d0 === N) carveSouth(i1); else if (d0 === S) carveSouth(i0);
    else if (d0 === W) carveEast(i1); else if (d0 === E) carveEast(i0);
    const x1 = i1 % cellWidth, y1 = Math.floor(i1 / cellWidth);
    if (y1 > 0 && cells[i1 - cellWidth] == null) frontier.push({ index: i1, direction: N });
    if (y1 < maxY && cells[i1 + cellWidth] == null) frontier.push({ index: i1, direction: S });
    if (x1 > 0 && cells[i1 - 1] == null) frontier.push({ index: i1, direction: W });
    if (x1 < maxX && cells[i1 + 1] == null) frontier.push({ index: i1, direction: E });
    return false;
}

function getCellCoordinates(cellIndex) {
    const i = cellIndex % cellWidth;
    const j = Math.floor(cellIndex / cellWidth);
    const x = i * (cellSize + cellSpacing) + cellSpacing;
    const y = j * (cellSize + cellSpacing) + cellSpacing;
    return { x, y };
}

function carvePath(index) {
    mazeContext.fillStyle = "#000000";
    const { x, y } = getCellCoordinates(index);
    mazeContext.fillRect(x, y, cellSize, cellSize);
}

function carveEast(index) {
    const { x, y } = getCellCoordinates(index);
    mazeContext.fillRect(x + cellSize, y, cellSpacing, cellSize);
}

function carveSouth(index) {
    const { x, y } = getCellCoordinates(index);
    mazeContext.fillRect(x, y + cellSize, cellSize, cellSpacing);
}

function popRandom(array) {
    if (!array.length) return null;
    const i = Math.floor(Math.random() * array.length);
    const element = array[i];
    array[i] = array[array.length - 1];
    array.pop();
    return element;
}

function drawInitialMarkers() {
    const { x, y } = getCellCoordinates(maxX);
    mazeContext.fillStyle = "red";
    mazeContext.fillRect(x, y, cellSize, cellSize);
}

function drawPlayer() {
    gameContext.clearRect(-w.innerWidth, -w.innerHeight, w.innerWidth * 2, w.innerHeight * 2);
    const { x, y } = getCellCoordinates(currentPosition);
    gameContext.beginPath();
    gameContext.arc(x + cellSize / 2, y + cellSize / 2, cellSize / 2, 0, 2 * Math.PI);
    gameContext.fillStyle = "red";
    gameContext.fill();
    if(gameMode === 'darkness') updateDarkness();
}

function updateDarkness() {
    const { x, y } = getCellCoordinates(currentPosition);
    const centerX = x + cellSize / 2;
    const centerY = y + cellSize / 2;
    const radius = cellSize * 2.5;

    darknessContext.clearRect(-w.innerWidth, -w.innerHeight, w.innerWidth*2, w.innerHeight*2);
    darknessContext.fillStyle = "black";
    darknessContext.fillRect(-w.innerWidth, -w.innerHeight, w.innerWidth*2, w.innerHeight*2);
    darknessContext.globalCompositeOperation = 'destination-out';
    darknessContext.beginPath();
    darknessContext.arc(centerX, centerY, radius, 0, 2 * Math.PI);
    darknessContext.fill();
    darknessContext.globalCompositeOperation = 'source-over';
}

function handleInput(direction) {
    if (!gameInProgress) return;
    if (direction === 'up') move(N, -cellWidth);
    if (direction === 'down') move(S, cellWidth);
    if (direction === 'left') move(W, -1);
    if (direction === 'right') move(E, 1);
}

function move(direction, indexChange) {
    if (cells[currentPosition] & direction) {
        currentPosition += indexChange;
        drawPlayer();
        checkForWin();
    }
}

function checkForWin() {
    if (currentPosition === maxX) {
        if (gameMode === 'endless') {
            score++;
            d.getElementById('scoreDisplay').textContent = `Score: ${score}`;
            resetForNextMaze();
        } else {
            gameComplete(true);
        }
    }
}

function gameComplete(isWin) {
    if (!gameInProgress && !isPaused) return;
    gameInProgress = false;
    isPaused = false;
    clearInterval(gameTimer);
    
    const winMessageEl = d.getElementById("winMessage");
    const gameMessageEl = d.getElementById("gameMessage");

    if (isWin) {
        const timerElement = d.getElementById("timer");
        const timeArray = timerElement.textContent.split(/[:]+/);
        const remainingSeconds = parseInt(timeArray[0], 10) * 60 + parseInt(timeArray[1], 10);
        const timeTaken = totalGameTimeInSeconds - remainingSeconds;
        const minutes = Math.floor(timeTaken / 60);
        const seconds = timeTaken % 60;
        const formattedTime = `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
        winMessageEl.textContent = `Won in ${formattedTime}`;
    } else {
        gameMessageEl.textContent = (gameMode === 'endless') ? `Game Over!`: "Out of time!";
    }
    
    d.getElementById("startButton").disabled = false;
    d.getElementById("pauseButton").textContent = "Pause";
    d.getElementById("pauseButton").disabled = true;
}

function startTimer() {
    const timerElement = d.getElementById("timer");
    let timeArray = timerElement.textContent.split(/[:]+/);
    let totalSeconds = parseInt(timeArray[0], 10) * 60 + parseInt(timeArray[1], 10);
    gameTimer = setInterval(() => {
        if (totalSeconds <= 0) {
            gameComplete(false);
            return;
        }
        totalSeconds--;
        const minutes = Math.floor(totalSeconds / 60);
        let seconds = totalSeconds % 60;
        timerElement.textContent = `${minutes}:${String(seconds).padStart(2, '0')}`;
    }, 1000);
}

function startGame() {
    if (gameInProgress) return;
    gameInProgress = true;
    isPaused = false;
    currentPosition = startCellIndex;
    drawPlayer();
    const minutes = Math.floor(totalGameTimeInSeconds / 60);
    d.getElementById("gameMessage").textContent = "";
    d.getElementById("winMessage").textContent = "";
    d.getElementById("timer").textContent = `${minutes}:00`;
    startTimer();
    d.getElementById("startButton").disabled = true;
    d.getElementById("pauseButton").disabled = false;
}

function togglePause() {
    if (!d.getElementById("startButton").disabled) return;
    const pauseButton = d.getElementById("pauseButton");
    isPaused = !isPaused;
    if (isPaused) {
        gameInProgress = false;
        clearInterval(gameTimer);
        pauseButton.textContent = "Resume";
        d.getElementById("gameMessage").textContent = "Paused";
    } else {
        gameInProgress = true;
        startTimer();
        pauseButton.textContent = "Pause";
        d.getElementById("gameMessage").textContent = "";
    }
}

w.addEventListener("resize", () => location.reload(), true);
d.getElementById("backButton").addEventListener("click", () => location.reload());

d.getElementById("classicModeBtn").addEventListener("click", () => selectGameMode("classic"));
d.getElementById("endlessModeBtn").addEventListener("click", () => selectGameMode("endless"));
d.getElementById("darknessModeBtn").addEventListener("click", () => selectGameMode("darkness"));

d.getElementById("easyBtn").addEventListener("click", () => initializeGame("easy"));
d.getElementById("mediumBtn").addEventListener("click", () => initializeGame("medium"));
d.getElementById("hardBtn").addEventListener("click", () => initializeGame("hard"));

d.getElementById("startButton").addEventListener("click", startGame);
d.getElementById("pauseButton").addEventListener("click", togglePause);

w.addEventListener("keydown", e => {
    if (!gameInProgress) return;
    const key = e.which;
    if (key >= 37 && key <= 40) e.preventDefault();
    if (key === 37) handleInput('left');
    if (key === 38) handleInput('up');
    if (key === 39) handleInput('right');
    if (key === 40) handleInput('down');
});

d.getElementById("touchUp").addEventListener("click", () => handleInput('up'));
d.getElementById("touchDown").addEventListener("click", () => handleInput('down'));
d.getElementById("touchLeft").addEventListener("click", () => handleInput('left'));
d.getElementById("touchRight").addEventListener("click", () => handleInput('right'));

let touchStartX = 0, touchStartY = 0;
w.addEventListener('touchstart', e => {
    touchStartX = e.changedTouches[0].screenX;
    touchStartY = e.changedTouches[0].screenY;
}, { passive: true });

w.addEventListener('touchend', e => {
    const touchEndX = e.changedTouches[0].screenX;
    const touchEndY = e.changedTouches[0].screenY;
    const deltaX = touchEndX - touchStartX;
    const deltaY = touchEndY - touchStartY;
    const absDeltaX = Math.abs(deltaX);
    const absDeltaY = Math.abs(deltaY);

    if (Math.max(absDeltaX, absDeltaY) < 50) return;

    if (absDeltaX > absDeltaY) {
        handleInput(deltaX > 0 ? 'right' : 'left');
    } else {
        handleInput(deltaY > 0 ? 'down' : 'up');
    }
}, { passive: true });