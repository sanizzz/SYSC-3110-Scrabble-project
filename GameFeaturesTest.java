import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameFeaturesTest {

    private Scrabble_Model buildModel() throws Exception {
        BoardConfigLoader.BoardLibrary library = BoardConfigLoader.loadLibrary(Paths.get("boards"));
        BoardConfigLoader.BoardLayout layout = library.require("Classic");
        Scrabble_Model.Dictionary dictionary = new Scrabble_Model.Dictionary(Arrays.asList("HELLO", "WORLD"));
        Scrabble_Model model = new Scrabble_Model(layout, dictionary);
        model.setupPlayers(2);
        return model;
    }

    @Test
    public void testUndoRedoRestoresMove() throws Exception {
        Scrabble_Model model = buildModel();
        Scrabble_Model.Player player = model.getCurrentPlayer();
        player.setRack(Arrays.asList(
                Scrabble_Model.Letter.H,
                Scrabble_Model.Letter.E,
                Scrabble_Model.Letter.L,
                Scrabble_Model.Letter.L,
                Scrabble_Model.Letter.O,
                Scrabble_Model.Letter.A,
                Scrabble_Model.Letter.B));

        Scrabble_Model.Placement placement = new Scrabble_Model.Placement(0, 0, Scrabble_Model.Placement.Direction.ACROSS, "HELLO");
        Scrabble_Model.MoveResult result = model.playWord(placement);
        assertTrue(result.isSuccess());
        assertEquals('H', model.board.getCell(0, 0));
        assertTrue(model.canUndo());

        assertTrue(model.undo());
        assertEquals(Scrabble_Model.Board.EMPTY, model.board.getCell(0, 0));
        assertEquals(0, model.getCurrentPlayer().getScore());
        assertTrue(model.canRedo());

        assertTrue(model.redo());
        assertEquals('H', model.board.getCell(0, 0));
    }

    @Test
    public void testSaveAndLoadRestoresState() throws Exception {
        Scrabble_Model model = buildModel();
        Scrabble_Model.Player player = model.getCurrentPlayer();
        player.setRack(Arrays.asList(
                Scrabble_Model.Letter.H,
                Scrabble_Model.Letter.E,
                Scrabble_Model.Letter.L,
                Scrabble_Model.Letter.L,
                Scrabble_Model.Letter.O,
                Scrabble_Model.Letter.A,
                Scrabble_Model.Letter.B));
        Scrabble_Model.Placement placement = new Scrabble_Model.Placement(1, 1, Scrabble_Model.Placement.Direction.DOWN, "HELLO");
        Scrabble_Model.MoveResult result = model.playWord(placement);
        assertTrue(result.isSuccess());

        File saveFile = File.createTempFile("scrabble", ".sav");
        model.saveGame(saveFile);

        model.board.reset();
        player.setScore(0);
        player.clearHand();

        model.loadGame(saveFile);
        assertEquals('H', model.board.getCell(1, 1));
        assertTrue(model.getCurrentPlayer().getScore() > 0);
        saveFile.delete();
    }
}
