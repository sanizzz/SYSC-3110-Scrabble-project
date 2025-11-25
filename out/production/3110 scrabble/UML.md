# UML and Sequence Notes

## Class Relationships
- **Scrabble_Model** – Central facade that owns `Board`, `TileBag`, `Dictionary`, players, undo/redo stacks, and persistence helpers.
  - Aggregates `Board` with a 15x15 grid plus blank tracking arrays.
  - Composes `TileBag`, `Player`, `Tile`, `Letter`, and `Dictionary` to represent the full game state.
  - Depends on `PremiumSquare` matrices supplied by `BoardConfigLoader.BoardLayout`.
- **BoardConfigLoader** – Parses XML files into `BoardLayout` objects used to seed the model. `BoardLibrary` exposes lookup/list operations for the GUI prompt.
- **Scrabble_View** – Swing `JFrame` with panels for the board grid, racks, and control buttons (Place/Undo/Redo/Save/Load). Raises events handled by the controller.
- **Scrabble_Controller** – Mediator translating UI events to `Scrabble_Model` commands. Manages file choosers for persistence and keeps the view in sync with model state.
- **PremiumSquare** – Enum encapsulating letter and word multipliers shared by the model, loader, and scoring logic.

## Sequence: Undoing a Move
1. **User clicks Undo** ➜ `Scrabble_View` fires the undo listener registered by `Scrabble_Controller`.
2. **Controller** calls `model.undo()`.
3. **Scrabble_Model.undo** pushes the current `GameState` to the redo stack, pops the previous snapshot from the undo stack, restores board tiles, blank flags, player racks/scores, bag contents, and turn index.
4. **Controller** refreshes the view (`displayBoard`, `displayPlayerTiles`, `updateCurrentPlayer`) and updates button states (`setUndoEnabled`, `setRedoEnabled`).

## Sequence: Loading a Saved Game
1. **User clicks Load** ➜ `Scrabble_Controller` opens a `JFileChooser` and, upon confirmation, calls `model.loadGame(file)`.
2. **Scrabble_Model.loadGame** deserializes a `GameState`, restores board letters, blank flags, players, tile bag, premium layout, and board name, then clears undo/redo stacks.
3. **Controller** invokes `updateDisplay()` and `updateHistoryButtons()` to show the restored state and disables history buttons because the stacks are empty after loading.
