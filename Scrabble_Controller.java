import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class Scrabble_Controller {

    private final Scrabble_Model model;
    private final Scrabble_View view;

    public Scrabble_Controller(Scrabble_Model model, Scrabble_View view) {
        this.model = model;
        this.view = view;

        updateDisplay();
        view.addPlaceWordListener(new PlaceWordListener());
        view.addUndoListener(e -> handleUndo());
        view.addRedoListener(e -> handleRedo());
        view.addSaveListener(e -> handleSave());
        view.addLoadListener(e -> handleLoad());
        updateHistoryButtons();
        view.updateBoardName(model.getBoardName());
    }

    class PlaceWordListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String word = view.getWordInput();
            int row = view.getRowInput();
            int col = view.getColInput();
            Scrabble_Model.Placement.Direction dir = view.getDirectionInput();
            placeWord(row, col, dir, word);
        }
    }

    private void placeWord(int row, int col,
                           Scrabble_Model.Placement.Direction direction, String word) {

        Scrabble_Model.Placement placement = new Scrabble_Model.Placement(row, col, direction, word);
        Scrabble_Model.MoveResult result = model.playWord(placement);
        view.showMessage(result.getMessage());
        if (result.isSuccess()) {
            updateDisplay();
        }
        updateHistoryButtons();
    }

    private void updateDisplay() {
        view.displayBoard(model.board);
        Scrabble_Model.Player current = model.getCurrentPlayer();
        view.displayPlayerTiles(current.getHand());
        view.updateCurrentPlayer(model.currentPlayerIndex, current.getName());
        view.updateBoardName(model.getBoardName());
    }

    private void handleUndo() {
        if (model.undo()) {
            updateDisplay();
        }
        updateHistoryButtons();
    }

    private void handleRedo() {
        if (model.redo()) {
            updateDisplay();
        }
        updateHistoryButtons();
    }

    private void handleSave() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                model.saveGame(file);
                view.showMessage("Game saved to " + file.getAbsolutePath());
            } catch (Exception ex) {
                view.showMessage("Failed to save game: " + ex.getMessage());
            }
        }
    }

    private void handleLoad() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                model.loadGame(file);
                updateDisplay();
                updateHistoryButtons();
                view.showMessage("Game loaded from " + file.getAbsolutePath());
            } catch (Exception ex) {
                view.showMessage("Failed to load game: " + ex.getMessage());
            }
        }
    }

    private void updateHistoryButtons() {
        view.setUndoEnabled(model.canUndo());
        view.setRedoEnabled(model.canRedo());
    }
}
