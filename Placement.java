/**
 * Represents a single word placement attempt on the Scrabble board.
 *

 */
public final class Placement {

    /** The two legal placement directions on the Scrabble board. */
    public enum Direction {
        ACROSS,
        DOWN
    }

    private final int row;
    private final int col;
    private final Direction dir;
    private final String word;

    /**
     * Constructs a new immutable Placement.
     *
     * @param row  zero-based row index (0–14)
     * @param col  zero-based column index (0–14)
     * @param dir  placement direction (ACROSS or DOWN)
     * @param word uppercase word string consisting of letters A–Z
     * @throws IllegalArgumentException if {@code word} is null or empty
     */
    public Placement(int row, int col, Direction dir, String word) {
        if (word == null || word.isEmpty()) {
            throw new IllegalArgumentException("Word cannot be null or empty.");
        }
        this.row = row;
        this.col = col;
        this.dir = dir;
        this.word = word.toUpperCase();
    }

    /** @return zero-based row index where placement begins. */
    public int getRow() {
        return row;
    }

    /** @return zero-based column index where placement begins. */
    public int getCol() {
        return col;
    }

    /** @return placement direction (ACROSS or DOWN). */
    public Direction getDirection() {
        return dir;
    }

    /** @return uppercase word string to be placed. */
    public String getWord() {
        return word;
    }

    @Override
    public String toString() {
        return String.format("Placement[%s, row=%d, col=%d, word=%s]", dir, row, col, word);
    }
}
