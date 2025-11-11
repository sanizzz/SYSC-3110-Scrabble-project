import javax.swing.SwingUtilities;

public class Scrabble_Frame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Scrabble_View view = new Scrabble_View();
            Scrabble_Model model = new Scrabble_Model();

            // Set up correct number of players based on user input before starting controller.
            model.players.clear();
            int numPlayers = view.getNumPlayers();
            for (int i = 1; i <= numPlayers; ++i) {
                model.players.add(new Scrabble_Model.Player("Player" + i));
            }
            for (Scrabble_Model.Player p : model.players) {
                while (p.handSize() < 7 && !model.tileBag.isEmpty())
                    p.addTile(model.tileBag.dealTile());
            }

            new Scrabble_Controller(model, view);
        });
    }
}
