//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import static org.junit.jupiter.api.Assertions.*;
//
////Test all methods in board class-> Methods in board class invoke all methods in all classes
//
//class BoardTest {
//    private Board board;
//    @BeforeEach
//    void setUp()
//    {
//        board = new Board();
//    }
//
//    @Test
//    void testInBounds()
//    {
//        assertTrue(board.inBounds(0, 0));
//        assertFalse(board.inBounds(20, 20));
//    }
//
//    @Test
//    void testRender()
//    {
//        String output = board.render();
//        assertTrue(output.contains("A |"));
//        assertTrue(output.contains("1"));
//        assertTrue(output.contains("."));
//        assertEquals(16, output.split("\n").length);
//    }
//
//    @Test
//    void testCanPlace()
//    {
//        Placement word = new Placement(0, 0, Placement.Direction.ACROSS, "WORD");
//        StringBuilder reason = new StringBuilder();
//        assertTrue(board.canPlace(word, reason));
//        assertEquals(0, reason.length());
//
//        Placement word1 = new Placement(14, 13, Placement.Direction.ACROSS, "LONG");
//        StringBuilder reason1 = new StringBuilder();
//        assertFalse(board.canPlace(word1, reason1));
//        assertTrue(reason1.toString().contains("Out of bounds"));
//
//        Placement word2 = new Placement(0, 0, Placement.Direction.ACROSS, "WORD");
//        board.place(word2);
//        Placement word3 = new Placement(0, 0, Placement.Direction.ACROSS, "WARP");
//        StringBuilder reason2 = new StringBuilder();
//        assertFalse(board.canPlace(word3, reason2));
//        assertTrue(reason2.toString().contains("Letter conflict"));
//    }
//
//    @Test
//    void testPlace()
//    {
//        Placement across = new Placement(1, 1, Placement.Direction.ACROSS, "HELLO");
//        board.place(across);
//        String rendered = board.render();
//        assertTrue(rendered.contains("H"));
//        assertTrue(rendered.contains("E"));
//        assertTrue(rendered.contains("L"));
//        assertTrue(rendered.contains("O"));
//    }}
