import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the bag that players draw tiles from.
 */
public class TileBag {
    private List<Tile> tiles;

    /**
     * Constructs a new TileBag object.
     */
    public TileBag() {
        this.tiles = new ArrayList<>();
        initializeTileBag();
    }

    /**
     * Adds all the necessary tiles for a game of Scrabble and randomizing the
     * draw order.
     */
    public void initializeTileBag() {
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

        for (int i = 0; i < 2; i++) {
            tiles.add(new Tile(Letter.O));
        }

        for (int i = 0; i < 9; i++) {
            tiles.add(new Tile(Letter.A));
            tiles.add(new Tile(Letter.I));
        }

        for (int i = 0; i < 12; i++) {
            tiles.add(new Tile(Letter.E));
        }

        shakeTileBag();
    }

    /**
     * Randomizes the order tiles are drawn from the bag.
     */
    public void shakeTileBag() {
        Collections.shuffle(tiles);
    }

    /**
     * Removes and returns a tile from the bag.
     *
     * @return A tile object that has been removed from the bag.
     */
    public Tile dealTile() {
        return tiles.removeFirst();
    }

    /**
     * Checks whether the bag is empty or not.
     *
     * @return True if the bag is empty. False if otherwise.
     */
    public boolean isEmpty() {
        return tiles.isEmpty();
    }

    /**
     * Prepares the bag for a new game of scrabble.
     */
    public void resetTileBag() {
        tiles.clear();
        initializeTileBag();
    }
}
