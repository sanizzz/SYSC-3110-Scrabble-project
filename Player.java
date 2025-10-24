import java.util.ArrayList;
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

    /** @return The tiles in the player's hand. */
    public List<Tile> getHand() {
        return hand;
    }

    /**
     * Adds a tile to the player's hand.
     *
     * @param tile The tile to be added.
     */
    public void addTile(Tile tile) {
        hand.add(tile);
    }

    /**
     * Removes a tile from the player's hand.
     *
     * @param tile The tile to be removed.
     */
    public void removeTile(Tile tile) {
        hand.remove(tile);
    }

    /**
     * Checks if the player's hand contains a specific letter.
     *
     * @param letter The letter to check for as a String.
     * @return True if the player's hand contains the letter. False if otherwise.
     */
    public boolean hasTile(String letter) {
        for (Tile t : hand) {
            if (Letter.valueOf(letter) == t.getLetter()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds points to the player's total score.
     *
     * @param points The points gained from playing a word.
     */
    public void addPoints(int points) {
        score += points;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("-----").append(name).append("-----\n");
        sb.append("Hand: ");

        for (Tile t : hand) {
            sb.append(t.toString()).append(" ");
        }

        return sb.toString();
    }
}
