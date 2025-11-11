import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class Scrabble_Model {

    public Board board;
    public TileBag tileBag;
    public Dictionary dictionary;
    public ArrayList<Player> players;
    public int currentPlayerIndex;

    public Scrabble_Model() {
        board = new Board();
        tileBag = new TileBag();
        dictionary = new Dictionary("words.txt");
        players = new ArrayList<>();
        currentPlayerIndex = 0;
        for (Player p : players) {
            while (p.handSize() < 7 && !tileBag.isEmpty())
                p.addTile(tileBag.dealTile());
        }
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    public void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    // --- MODEL CLASSES BELOW ---

    public static class Board {
        public static final int SIZE = 15;
        private static final char EMPTY = '\0';
        private final char[][] grid = new char[SIZE][SIZE];

        public Board() {}

        public String render() {
            StringBuilder sb = new StringBuilder();
            sb.append("    ");
            for (int col = 1; col <= SIZE; col++) {
                sb.append(col < 10 ? " " + col + " " : col + " ");
            }
            sb.append("\n");
            for (int row = 0; row < SIZE; row++) {
                char rowLabel = (char) ('A' + row);
                sb.append(rowLabel).append(" | ");
                for (int col = 0; col < SIZE; col++) {
                    sb.append(grid[row][col] == EMPTY ? ". " : grid[row][col] + " ");
                }
                sb.append("\n");
            }
            return sb.toString();
        }

        public boolean inBounds(int row, int col) {
            return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
        }

        public boolean canPlace(Placement p, StringBuilder reason) {
            int r = p.getRow(), c = p.getCol();
            for (int i = 0; i < p.getWord().length(); i++) {
                if (!inBounds(r, c)) {
                    if (reason != null) reason.append("Out of bounds.");
                    return false;
                }
                char existing = grid[r][c];
                char ch = p.getWord().charAt(i);
                if (existing != EMPTY && existing != ch) {
                    if (reason != null) reason.append("Letter conflict.");
                    return false;
                }
                if (p.getDirection() == Placement.Direction.ACROSS) c++; else r++;
            }
            return true;
        }

        public void place(Placement p) {
            int r = p.getRow(), c = p.getCol();
            for (int i = 0; i < p.getWord().length(); i++) {
                if (grid[r][c] == EMPTY) {
                    grid[r][c] = p.getWord().charAt(i);
                }
                if (p.getDirection() == Placement.Direction.ACROSS) c++; else r++;
            }
        }

        public char getCell(int row, int col) {
            return inBounds(row, col) ? grid[row][col] : EMPTY;
        }
    }

    public static class Placement {
        public enum Direction {ACROSS, DOWN}
        private final int row, col;
        private final Direction dir;
        private final String word;

        public Placement(int row, int col, Direction dir, String word) {
            if (word == null || word.isEmpty())
                throw new IllegalArgumentException("Word cannot be null or empty.");
            this.row = row; this.col = col; this.dir = dir; this.word = word.toUpperCase();
        }
        public int getRow() { return row; }
        public int getCol() { return col; }
        public Direction getDirection() { return dir; }
        public String getWord() { return word; }
    }

    public static class TileBag {
        private final List<Tile> tiles;
        public TileBag() {
            tiles = new ArrayList<>();
            initializeTileBag();
        }
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
            for (int i = 0; i < 3; i++) tiles.add(new Tile(Letter.G));
            Collections.shuffle(tiles);
        }
        public Tile dealTile() {
            if (tiles.isEmpty()) throw new IllegalStateException("Tile bag empty");
            return tiles.remove(tiles.size() - 1);
        }
        public boolean isEmpty() { return tiles.isEmpty(); }
    }

    public static class Player {
        private String name;
        private int score;
        private List<Tile> hand;
        public Player(String name) { this.name = name; score = 0; hand = new ArrayList<>(); }
        public String getName() { return name; }
        public int getScore() { return score; }
        public List<Tile> getHand() { return Collections.unmodifiableList(hand); }
        public void addTile(Tile tile) { hand.add(tile); }
        public void removeTile(Tile tile) { hand.remove(tile); }
        public int handSize() { return hand.size(); }
        public void addPoints(int pts) { score += pts; }
    }

    public static class Tile {
        private final Letter letter;
        public Tile(Letter letter) { this.letter = letter; }
        public Letter getLetter() { return letter; }
        public int getPoints() { return letter.getPoints(); }
        @Override public String toString() { return letter.toString() + ": " + getPoints(); }
    }

    public enum Letter {
        A(1), B(3), C(3), D(2), E(1), F(4), G(2), H(4),
        I(1), J(8), K(5), L(1), M(3), N(1), O(1), P(3),
        Q(10), R(1), S(1), T(1), U(1), V(4), W(4), X(8),
        Y(4), Z(10), BLANK(0);
        private final int points;
        Letter(int points) { this.points = points; }
        public int getPoints() { return points; }
    }

    public static class Dictionary {
        private final Set<String> words = new HashSet<>();
        public Dictionary(String path) { loadDictionary(path); }
        private void loadDictionary(String path) {
            try (BufferedReader br = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
                String line;
                while ((line = br.readLine()) != null) {
                    String sanitized = line.replaceAll("[^A-Za-z]", " ");
                    for (String token : sanitized.split("\\s+"))
                        if (!token.isEmpty()) words.add(token.toUpperCase(Locale.ROOT));
                }
            } catch (IOException e) { throw new IllegalStateException("Failed to load dictionary", e);}
        }
        public boolean isValidWord(String word) {
            if (word == null) return false;
            return words.contains(word.toUpperCase(Locale.ROOT));
        }
    }
}
