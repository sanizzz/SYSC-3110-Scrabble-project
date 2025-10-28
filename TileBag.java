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
     * Adds all the necessary tiles for a game of Scrabble and randomizes the
     * draw order.
     */
    public void initializeTileBag() {
        addTile(new Tile(Letter.J));
        addTile(new Tile(Letter.K));
        addTile(new Tile(Letter.Q));
        addTile(new Tile(Letter.X));
        addTile(new Tile(Letter.Z));

        for (int i = 0; i < 2; i++) {
            addTile(new Tile(Letter.B));
            addTile(new Tile(Letter.C));
            addTile(new Tile(Letter.F));
            addTile(new Tile(Letter.H));
            addTile(new Tile(Letter.M));
            addTile(new Tile(Letter.P));
            addTile(new Tile(Letter.V));
            addTile(new Tile(Letter.W));
            addTile(new Tile(Letter.Y));
        }

        for (int i = 0; i < 3; i++) {
            addTile(new Tile(Letter.G));
        }

        for (int i = 0; i < 4; i++) {
            addTile(new Tile(Letter.D));
            addTile(new Tile(Letter.L));
            addTile(new Tile(Letter.S));
            addTile(new Tile(Letter.U));
        }

        for (int i = 0; i < 6; i++) {
            addTile(new Tile(Letter.N));
            addTile(new Tile(Letter.R));
            addTile(new Tile(Letter.T));
        }

        for (int i = 0; i < 2; i++) {
            addTile(new Tile(Letter.O));
        }

        for (int i = 0; i < 9; i++) {
            addTile(new Tile(Letter.A));
            addTile(new Tile(Letter.I));
        }

        for (int i = 0; i < 12; i++) {
            addTile(new Tile(Letter.E));
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
     * Adds a tile to the tile bag.
     *
     * @param tile The tile to be added.
     */
    public void addTile(Tile tile) {
        tiles.add(tile);
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
