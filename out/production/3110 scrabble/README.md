# Scrabble Milestone 4

## Overview
Milestone 4 delivers a full-featured Swing Scrabble client with selectable board layouts, multi-level undo/redo, and persistent save/load support. Players can choose among the classic layout plus two custom variants backed by XML definitions, and every move is scored with the appropriate premium-square multipliers.

### Feature Highlights
- **Custom boards:** XML files in `boards/` describe premium squares. The GUI prompts players to choose a layout before the match starts, and the model loads the matching multipliers for scoring.
- **Undo / Redo:** Every completed move snapshots board tiles, racks, scores, bag contents, and the current turn. Users can step backward or forward multiple turns via dedicated buttons.
- **Serialization:** Save and Load buttons serialize the full model to disk using Java serialization so sessions can be paused and resumed exactly where they left off.
- **MVC GUI:** `Scrabble_View` renders the 15x15 board, player racks, and control panel. `Scrabble_Controller` wires actions to the enriched `Scrabble_Model`, which owns validation, scoring, history, and persistence logic.

## Running the Game
```
javac *.java
java Scrabble_Frame
```

Workflow:
1. On startup, choose the number of players (1-4) and select a board layout from the list populated from `boards/*.xml`.
2. Each turn, enter the word, row (0-based), column (0-based), and direction (`ACROSS` or `DOWN`) then press **Place Word**.
3. Use **Undo** / **Redo** to traverse move history. Buttons enable/disable automatically.
4. Click **Save** or **Load** to persist a session. Saves include board layout, bag contents, racks, scores, and the current player.

## Repository Layout
- `boards/` – XML definitions for *Classic*, *Diamond*, and *Rings* premium-square layouts.
- `BoardConfigLoader.java` & `PremiumSquare.java` – XML parser and premium metadata shared across the model.
- `Scrabble_Model.java` – Core model with scoring, undo/redo stacks, serialization, and supporting classes (Board, TileBag, Player, etc.).
- `Scrabble_View.java` – Swing UI with board grid, rack display, controls, and board selection prompts.
- `Scrabble_Controller.java` – Bridges user actions to the model and manages file choosers for Save/Load.
- `Scrabble_Frame.java` – Application entry point that loads board definitions and boots the MVC stack.
- `GameFeaturesTest.java`, `Model_Test.java` – JUnit suites covering board utilities plus undo/redo and persistence flows.
- `USER_MANUAL.md` – Step-by-step instructions for end users.
- `UML.md` – Textual description of the Milestone 4 class and sequence diagrams.

## Contributions by Milestone
- **Pranav Gupta:** Model refactors, custom board loader, serialization layer (M4); earlier CLI enhancements (M1-M2).
- **Sanidhya Khanna:** Swing view redesign, controller wiring, user manual authoring (M4); initial GUI shell (M2).
- **Morgan Huang:** Undo/redo design, premium-square scoring, UML documentation (M4); data-structure cleanup (M3).
- **Nitish Grover:** Test suite expansion, README/known-issues updates (M4); tile bag/dictionary utilities (M1-M2).

## Known Issues / Limitations
- Word validation still relies on a static `words.txt` dictionary and does not check cross-word validity or adjacency beyond overlap conflicts.
- Premium layouts assume 15x15 boards; additional sizes require further model changes.
- Save files are not versioned; changing class structure may invalidate older saves.

## Contributors
- Pranav Gupta
- Sanidhya Khanna
- Morgan Huang
- Nitish Grover
