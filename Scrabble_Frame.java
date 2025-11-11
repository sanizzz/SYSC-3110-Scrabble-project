import javax.swing.SwingUtilities;
/**
 * The Scrabble_Frame class contains the entry point for the Scrabble game application.
 * It is responsible for initializing the Model-View-Controller (MVC) components
 * and launching the graphical user interface in a safe manner.
 * 
 * 
 * @author Nitish, Pranav, Sanidhya, Morgan
 * @version 1.0
 */

public class Scrabble_Frame {
    /**
     * The main method and entry point of the application.
     * 
     * This method performs the following steps:
     * 
     *   Creates the GUI Scrabble_View and the game Scrabble_Model}.
     *   Clears any default players in the model.
     *   Prompts the user (via the view) for the number of players.
     *   Creates and adds { Scrabble_Model.Player} objects to the model.
     *   Deals tiles from the { Scrabble_Model.TileBag} to each player's hand.
     *   Initializes the { Scrabble_Controller} to handle user interactions.
     * 
     *
     * @param args command-line arguments
     */
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
