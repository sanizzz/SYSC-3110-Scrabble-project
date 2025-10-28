import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an individual Scrabble player.
 */
public class Player {
    private String name;
    private int score;
    private List<Tile> hand;

    /**
     * Constructs a new Player object.
     *
     * @param name The name of the player.
     */
    public Player(String name) {
        this.name = name;
        this.score = 0;
        this.hand = new ArrayList<>();
    }

    /** @return The name of the player. */
    public String getName() {
        return name;
    }

    /** @return The total score of the player. */
    public int getScore() {
        return score;
    }

<<<<<<< HEAD
    /** @return The tiles in the player's hand as an unmodifiable view. */
    public List<Tile> getHand() {
        return Collections.unmodifiableList(hand);
    }

=======
>>>>>>> 47a75b0d58002970b515e4647f771d4fcf9d7cca
    /**
     * Adds a tile to the player's hand.
     *
     * @param tile The tile to be added.
     */
    public void addTile(Tile tile) {
        hand.add(tile);
    }

    /**
     * Removes a tile from the player's hand as indicated by its index.
     * This occurs when the player plays or swaps a tile.
     *
     * @param index The index of the tile to be removed.
     * @return The tile that has been removed.
     */
<<<<<<< HEAD
    public void removeTile(Tile tile) {
        hand.remove(tile);
    }

    /** @return current number of tiles held. */
    public int handSize() {
        return hand.size();
    }

    /**
     * Checks if the player's hand contains a specific letter.
     *
     * @param letter The letter to check for.
     * @return True if the player's hand contains the letter. False otherwise.
     */
    public boolean hasTile(Letter letter) {
        if (letter == null) {
            return false;
        }
        for (Tile t : hand) {
            if (t.getLetter() == letter) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convenience overload that accepts a string token and uppercases it.
     *
     * @param letter token such as "A" or "BLANK".
     * @return true when the player holds the tile.
     */
    public boolean hasTile(String letter) {
        if (letter == null) {
            return false;
        }
        return hasTile(Letter.valueOf(letter.toUpperCase()));
    }

    /**
     * Removes and returns the first tile that matches the requested letter.
     *
     * @param letter Letter being consumed from the rack.
     * @return the removed tile, or {@code null} when not present.
     */
    public Tile takeTile(Letter letter) {
        if (letter == null) {
            return null;
        }
        for (int i = 0; i < hand.size(); i++) {
            Tile tile = hand.get(i);
            if (tile.getLetter() == letter) {
                hand.remove(i);
                return tile;
            }
        }
        return null;
    }
=======
    public Tile removeTile(int index) {
        return hand.remove(index);
    }
>>>>>>> 47a75b0d58002970b515e4647f771d4fcf9d7cca

    /**
     * Adds points to the player's total score.
     *
     * @param points The points gained from playing a word.
     */
    public void addPoints(int points) {
        score += points;
    }

    /**
     * Checks whether the player's hand is full or not.
     *
     * @param handSize The maximum size of a player's hand.
     * @return True if the player's hand is full. False if otherwise.
     */
    public boolean isHandFull(int handSize) {
        return (hand.size() == handSize);
    }

    /**
     * Checks whether the player has played out (emptied) their hand.
     *
     * @return True if the player has no more tiles. False if otherwise.
     */
    public boolean hasPlayedOut() {
        return hand.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("-----").append(name).append("'s Hand").append("-----\n");

        int i = 0;
        for (Tile t : hand) {
            sb.append(i).append(". ").append(t.toString()).append("\n");
            i++;
        }

        return sb.toString();
    }
}



