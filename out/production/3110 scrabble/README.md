# Scrabble Milestone 1

## Overview
Interactive console Scrabble prototype. Supports 2-4 players placing words on a 15x15 board, drawing from a shared tile bag, and scoring with standard Scrabble bonuses.

## Components
- `Board.java` - Manages the board grid, validates placements, and renders ASCII output.
- `Game.java` - Console controller for turn order, input parsing, scoring, and game flow.
- `Player.java` - Tracks player identity, rack contents, and cumulative score.
- `TileBag.java`, `Tile.java`, `Letter.java` - Define tiles and provide shuffled draw mechanics.
- `Placement.java` - Immutable value object describing a word placement.
- `Dictionary.java` - Loads legal words from `words.txt` for move validation.
- `words.txt` - Plain word list consumed by the dictionary loader.

## Running the Game
```
javac *.java
java Game
```

When prompted, enter 2-4 player names (blank names default to `PlayerN`). Each turn, the current player sees the board, scores, rack, and remaining tiles, then enters:
- `<Row><Column> <WORD> <Direction>` to place a word (e.g. `H8 HELLO A` or `H8 HELLO D`).
- `PASS` to skip a turn.
- `QUIT` to end the session early.

The game ends automatically when the bag is exhausted and a player empties their rack, or when everyone passes twice in a row.

## Contributors
- Pranav Gupta
- Sanidhya Khanna
- Morgan Huang
- Nitish Grover