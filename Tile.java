/**
 * Represents an individual letter tile.
 */
public class Tile {
    private Letter letter;

    /**
     * Constructs a new Tile object
     *
     * @param letter The Letter enum constant that the tile will have.
     */
    public Tile(Letter letter) {
        this.letter = letter;
    }

    /** @return The letter on the tile. */
    public String getLetter() {
        return this.letter.toString();
    }

    /** @return The point value of the tile. */
    public int getPoints() {
        return this.letter.getPoints();
    }

    @Override
    public String toString() {
        return letter.toString() + ": " + getPoints();
    }
}
