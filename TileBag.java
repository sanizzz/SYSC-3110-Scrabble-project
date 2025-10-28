import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the bag that players draw tiles from.
 */
public class TileBag {
    private final List<Tile> tiles;

    public TileBag() {
        this.tiles = new ArrayList<>();
        initializeTileBag();
    }

    /**
     * Adds the full Scrabble tile set and shuffles the draw order.
     */
    private void initializeTileBag() {
        tiles.clear();

        tiles.add(new Tile(Letter.J));
        tiles.add(new Tile(Letter.K));
        tiles.add(new Tile(Letter.Q));
        tiles.add(new Tile(Letter.X));
        tiles.add(new Tile(Letter.Z));

        for (int i = 0; i < 2; i++) {
            tiles.add(new Tile(Letter.B));
            tiles.add(new Tile(Letter.C));
            tiles.add(new Tile(Letter.F));
            tiles.add(new Tile(Letter.H));
            tiles.add(new Tile(Letter.M));
            tiles.add(new Tile(Letter.P));
            tiles.add(new Tile(Letter.V));
            tiles.add(new Tile(Letter.W));
            tiles.add(new Tile(Letter.Y));
            tiles.add(new Tile(Letter.BLANK));
        }

        for (int i = 0; i < 3; i++) {
            tiles.add(new Tile(Letter.G));
        }

        for (int i = 0; i < 4; i++) {
            tiles.add(new Tile(Letter.D));
            tiles.add(new Tile(Letter.L));
            tiles.add(new Tile(Letter.S));
            tiles.add(new Tile(Letter.U));
        }

        for (int i = 0; i < 6; i++) {
            tiles.add(new Tile(Letter.N));
            tiles.add(new Tile(Letter.R));
            tiles.add(new Tile(Letter.T));
        }

        for (int i = 0; i < 8; i++) {
            tiles.add(new Tile(Letter.O));
        }

        for (int i = 0; i < 9; i++) {
            tiles.add(new Tile(Letter.A));
            tiles.add(new Tile(Letter.I));
        }

        for (int i = 0; i < 12; i++) {
            tiles.add(new Tile(Letter.E));
        }

        shuffle();
    }

    /** Randomizes the tile order. */
    public void shuffle() {
        Collections.shuffle(tiles);
    }

    /**
     * Removes and returns one tile from the bag.
     *
     * @return The drawn tile.
     */
    public Tile dealTile() {
        if (tiles.isEmpty()) {
            throw new IllegalStateException("Cannot deal from an empty tile bag.");
        }
        return tiles.remove(tiles.size() - 1);
    }

    /** @return true when no tiles remain. */
    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    /** @return number of tiles left. */
    public int size() {
        return tiles.size();
    }
}
