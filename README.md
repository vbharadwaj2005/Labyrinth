# Labyrinth

Navigate through procedurally generated mazes, reaching the exit before time runs out.

## How to Play

- Use the on-screen D-pad, swipe gestures, or keyboard (arrow keys / WASD) to move.
- Your goal is to reach the green cell at the top-right corner.
- Choose from three game modes and three difficulty levels.

## Game Modes

| Mode | Description |
|---|---|
| **Classic** | Single maze, timed. Reach the exit before the clock hits zero. |
| **Endless** | Consecutive mazes with a score counter. Each completed maze resets the board and adds to your score. Game ends when time runs out. |
| **Darkness** | Same as Classic but with limited visibility — only a small radius around the player is lit. |

## Difficulty Levels

| Difficulty | Cell Size | Spacing | Time |
|---|---|---|---|
| Easy | 30px | 20px | 5 min |
| Medium | 25px | 15px | 10 min |
| Hard | 20px | 10px | 15 min |

## Features

- **Maze generation** via randomized Prim's algorithm
- Three game modes with distinct mechanics
- Fog-of-war effect in Darkness mode
- Swipe gesture and keyboard input support
- Persistent best scores per difficulty (Endless mode)
- Procedurally generated sound effects (no audio assets)
- Dark theme with animated menu transitions

## Tech Stack

| Component | Detail |
|---|---|
| Language | Kotlin 1.9.22 |
| Framework | libGDX 1.12.1 (LWJGL3 backend) |
| Build | Gradle 8.5 (Kotlin DSL) |
| Font | Inter-Regular (FreeType) |
| Platform | Desktop JVM |

## Getting Started

### Prerequisites

- JDK 8 or newer
- Gradle 8.5 (wrapper included)

### Run

```bash
./gradlew :core:run
```

### Build JAR

```bash
./gradlew :core:build
java -jar core/build/libs/core-1.0.0.jar
```

## Controls

| Input | Action |
|---|---|
| D-pad buttons | Move up / down / left / right |
| Swipe | Move in swipe direction (min 50px threshold) |
| Arrow keys / WASD | Move |
| Back button | Return to menu |
| Pause button | Pause / resume the game |
