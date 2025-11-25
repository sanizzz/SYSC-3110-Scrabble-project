# Scrabble User Manual

## Launching the Game
1. Ensure `words.txt`, the `boards/` directory, and all `.java` sources are in the same folder.
2. Compile the project:
   ```
   javac *.java
   ```
3. Start the GUI:
   ```
   java Scrabble_Frame
   ```
4. When prompted, enter the number of players (1-4) and select a board layout from the list.

## Playing a Turn
- The board grid shows placed tiles; empty cells display `.`.
- Your rack appears at the bottom. Use the controls on the right to enter:
  - **Word:** Letter sequence to place.
  - **Row / Col:** Zero-based coordinates for the starting square.
  - **Direction:** `ACROSS` or `DOWN`.
- Press **Place Word**. The model validates bounds, rack contents, and dictionary membership, then applies premium multipliers from the chosen layout.
- Points earned display in a dialog, and your rack automatically refills from the shared tile bag.

## Undo and Redo
- **Undo** reverts the most recent valid move, restoring board letters, both players' racks, scores, and the tile bag.
- **Redo** reapplies moves undone in sequence.
- Buttons enable only when an action is available; any new move clears the redo stack to maintain a linear history.

## Saving and Loading Games
1. Click **Save** to open a file chooser, choose a destination, and confirm. The save file stores:
   - Board layout name and tile placement
   - Player scores and racks
   - Tile bag contents
   - Current player turn and undo/redo stacks
2. Click **Load** to pick an existing save. The game resumes at the exact state captured in that file.
3. Save files are Java serialization streams; keep them alongside the project for compatibility.

## Custom Boards
- XML files inside `boards/` describe premium squares using `<premium type="..." row="..." col="..."/>` entries.
- Add new layouts by dropping extra XML files into the folder. They appear automatically in the startup board selector.

## Troubleshooting
- **Dictionary Errors:** Ensure `words.txt` is present; invalid words show an error dialog without consuming tiles.
- **IO Failures:** Saving/loading outside writable directories will raise errors; choose a path inside your user folder.
- **Missing Boards:** If `boards/` is deleted or empty, the launcher will warn and exit.
