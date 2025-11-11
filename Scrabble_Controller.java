import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Scrabble_Controller {

    private Scrabble_Model model;
    private Scrabble_View view;

    public Scrabble_Controller(Scrabble_Model model, Scrabble_View view) {
        this.model = model;
        this.view = view;

        updateDisplay();
        view.addPlaceWordListener(new PlaceWordListener());
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
        StringBuilder reason = new StringBuilder();

        if (!model.board.canPlace(placement, reason)) {
            view.showMessage("Cannot place word: " + reason);
            return;
        }

        if (!model.dictionary.isValidWord(word)) {
            view.showMessage("Invalid word: not in dictionary");
            return;
        }

        model.board.place(placement);
        int points = scorePlacement(placement);

        Scrabble_Model.Player player = model.getCurrentPlayer();
        player.addPoints(points);
        refillRack(player);

        model.advanceTurn();

        view.showMessage("Placed word: " + word + " for " + points + " points.");
        updateDisplay();
    }

    private void refillRack(Scrabble_Model.Player player) {
        while (player.handSize() < 7 && !model.tileBag.isEmpty()) {
            player.addTile(model.tileBag.dealTile());
        }
    }

    private int scorePlacement(Scrabble_Model.Placement placement) {
        return placement.getWord().length();  // Placeholder logic
    }

    private void updateDisplay() {
        view.displayBoard(model.board);
        Scrabble_Model.Player current = model.getCurrentPlayer();
        view.displayPlayerTiles(current.getHand());
        view.updateCurrentPlayer(model.currentPlayerIndex, current.getName());
    }
}
