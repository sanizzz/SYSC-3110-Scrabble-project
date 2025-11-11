import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class Model_Test {

    private Scrabble_Model model;
    private Scrabble_Model.Board board;

    @Before
    public void setup() {
        model = new Scrabble_Model();
        board = model.board;
    }

    @Test
    public void testRender() {
        String output = board.render();
        assertEquals(true, output.length() > 0);
    }

    @Test
    public void testInBounds() {
        assertEquals(true, board.inBounds(5, 5));
        assertEquals(false, board.inBounds(-1, 0));
    }

    @Test
    public void testCanPlace() {
        Scrabble_Model.Placement p = new Scrabble_Model.Placement(0, 0, Scrabble_Model.Placement.Direction.ACROSS, "HI");
        StringBuilder reason = new StringBuilder();
        assertEquals(true, board.canPlace(p, reason));
    }

    @Test
    public void testGetCell() {
        Scrabble_Model.Placement p = new Scrabble_Model.Placement(0, 0, Scrabble_Model.Placement.Direction.ACROSS, "HELLO");
        board.place(p);
        assertEquals('H', board.getCell(0, 0));
        assertEquals('E', board.getCell(0, 1));
    }

    @Test
    public void testPlacementGetters() {
        Scrabble_Model.Placement p = new Scrabble_Model.Placement(1, 1, Scrabble_Model.Placement.Direction.DOWN, "CAT");
        assertEquals(1, p.getRow());
        assertEquals(1, p.getCol());
        assertEquals(Scrabble_Model.Placement.Direction.DOWN, p.getDirection());
        assertEquals("CAT", p.getWord());
    }

    @Test
    public void testTileBagDealTileAndIsEmpty() {
        Scrabble_Model.TileBag bag = new Scrabble_Model.TileBag();
        int count = 0;
        while(!bag.isEmpty()) {
            Scrabble_Model.Tile t = bag.dealTile();
            assertEquals(false, t == null);
            count++;
        }
        assertEquals(true, bag.isEmpty());
    }

    @Test
    public void testPlayer() {
        Scrabble_Model.Player player = new Scrabble_Model.Player("Player1");
        Scrabble_Model.Tile tile = new Scrabble_Model.Tile(Scrabble_Model.Letter.A);
        assertEquals(0, player.handSize());
        player.addTile(tile);
        assertEquals(1, player.handSize());

        player.removeTile(tile);
        assertEquals(0, player.handSize());

        player.addPoints(10);
        assertEquals(10, player.getScore());
    }

    @Test
    public void testTile() {
        Scrabble_Model.Tile tile = new Scrabble_Model.Tile(Scrabble_Model.Letter.G);
        assertEquals(Scrabble_Model.Letter.G, tile.getLetter());
        assertEquals(Scrabble_Model.Letter.G.getPoints(), tile.getPoints());
    }

    @Test
    public void testLetter() {
        assertEquals(3, Scrabble_Model.Letter.B.getPoints());
    }

    @Test
    public void testDictionary() {
        Scrabble_Model.Dictionary dict = new Scrabble_Model.Dictionary("words.txt");
        assertEquals(false, dict.isValidWord("IMAGINE"));
    }
}
