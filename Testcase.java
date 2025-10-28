/**
 * Lightweight sanity checks for core board functionality.
 * Run with `java Testcase` after compilation.
 */
public final class Testcase {
    private int passed = 0;
    private int total = 0;

    public static void main(String[] args) {
        Testcase tests = new Testcase();
        tests.run();
    }

    private void run() {
        testInBounds();
        testRender();
        testCanPlace();
        testPlace();

        System.out.printf("Passed %d/%d checks.%n", passed, total);
        if (passed != total) {
            System.exit(1);
        }
    }

    private void testInBounds() {
        total++;
        Board board = new Board();
        boolean ok = board.inBounds(0, 0) && board.inBounds(14, 14)
                && !board.inBounds(-1, 0) && !board.inBounds(15, 5);
        record(ok, "Board.inBounds edge cases");
    }

    private void testRender() {
        total++;
        Board board = new Board();
        String output = board.render();
        boolean ok = output.contains("A |")
                && output.contains("15")
                && output.contains(".")
                && output.split("\n").length == 16;
        record(ok, "Board.render initial state");
    }

    private void testCanPlace() {
        total++;
        Board board = new Board();
        Placement okWord = new Placement(0, 0, Placement.Direction.ACROSS, "WORD");
        StringBuilder reason = new StringBuilder();
        boolean ok = board.canPlace(okWord, reason) && reason.length() == 0;

        Placement outOfBounds = new Placement(14, 13, Placement.Direction.ACROSS, "LONG");
        StringBuilder reason2 = new StringBuilder();
        ok &= !board.canPlace(outOfBounds, reason2)
                && reason2.toString().contains("bounds");

        board.place(okWord);
        Placement conflict = new Placement(0, 0, Placement.Direction.ACROSS, "WARP");
        StringBuilder reason3 = new StringBuilder();
        ok &= !board.canPlace(conflict, reason3)
                && reason3.toString().contains("conflict");

        record(ok, "Board.canPlace validations");
    }

    private void testPlace() {
        total++;
        Board board = new Board();
        Placement across = new Placement(1, 1, Placement.Direction.ACROSS, "HELLO");
        board.place(across);
        String rendered = board.render();
        boolean ok = rendered.contains("H")
                && rendered.contains("E")
                && rendered.contains("L")
                && rendered.contains("O");
        record(ok, "Board.place writes letters");
    }

    private void record(boolean condition, String description) {
        if (condition) {
            passed++;
            System.out.println("[PASS] " + description);
        } else {
            System.out.println("[FAIL] " + description);
        }
    }
}
