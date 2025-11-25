import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;
import java.nio.file.Paths;
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
            BoardConfigLoader.BoardLibrary library;
            try {
                library = BoardConfigLoader.loadLibrary(Paths.get("boards"));
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Failed to load board layouts: " + ex.getMessage());
                return;
            }

            Scrabble_View view = new Scrabble_View(library.getBoardNames());
            Scrabble_Model model = new Scrabble_Model(library.require(view.getSelectedBoardName()));
            model.setupPlayers(view.getNumPlayers());
            new Scrabble_Controller(model, view);
        });
    }
}
