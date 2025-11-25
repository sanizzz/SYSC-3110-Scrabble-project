import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Scrabble_Model {

    public Board board;
    public TileBag tileBag;
    public Dictionary dictionary;
    public ArrayList<Player> players;
    public int currentPlayerIndex;

    private PremiumSquare[][] premiumSquares;
    private boolean[][] blankSquares;
    private final Deque<GameState> undoStack = new ArrayDeque<>();
    private final Deque<GameState> redoStack = new ArrayDeque<>();
    private String boardName;

    public Scrabble_Model() {
        this(new BoardConfigLoader.BoardLayout("Default", createAllNormalLayout()));
    }

    public Scrabble_Model(BoardConfigLoader.BoardLayout layout) {
        this(layout, new Dictionary("words.txt"));
    }

    public Scrabble_Model(BoardConfigLoader.BoardLayout layout, Dictionary dictionary) {
        this.board = new Board();
        this.tileBag = new TileBag();
        this.dictionary = Objects.requireNonNull(dictionary, "dictionary");
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        applyLayout(layout.getName(), layout.copySquares());
    }

    private static PremiumSquare[][] createAllNormalLayout() {
        PremiumSquare[][] layout = new PremiumSquare[Board.SIZE][Board.SIZE];
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                layout[r][c] = PremiumSquare.NORMAL;
            }
        }
        return layout;
    }

    private void applyLayout(String name, PremiumSquare[][] layout) {
        this.boardName = name;
        this.premiumSquares = layout;
        this.blankSquares = new boolean[Board.SIZE][Board.SIZE];
        this.board.reset();
        this.undoStack.clear();
        this.redoStack.clear();
    }

    public void setupPlayers(int playerCount) {
        if (playerCount < 1 || playerCount > 4) {
            throw new IllegalArgumentException("Player count must be between 1 and 4");
        }
        players.clear();
        for (int i = 1; i <= playerCount; i++) {
            Player p = new Player("Player" + i);
            refillRack(p);
            players.add(p);
        }
        currentPlayerIndex = 0;
        undoStack.clear();
        redoStack.clear();
    }

    public String getBoardName() {
        return boardName;
    }

    public MoveResult playWord(Placement placement) {
        if (players.isEmpty()) {
            return MoveResult.failure("No players have joined the game.");
        }
        StringBuilder reason = new StringBuilder();
        if (!board.canPlace(placement, reason)) {
            String message = reason.length() > 0 ? reason.toString() : "Cannot place word.";
            return MoveResult.failure(message);
        }
        if (!dictionary.isValidWord(placement.getWord())) {
            return MoveResult.failure("Word not found in dictionary.");
        }
        Player current = getCurrentPlayer();
        TileUsage usage = planTileUsage(current, placement);
        if (usage == null) {
            return MoveResult.failure("Rack does not contain the needed letters.");
        }

        pushUndoState();
        redoStack.clear();

        int scored = scorePlacement(placement, usage);
        consumeTilesForPlacement(current, placement, usage);
        applyPlacement(placement, usage);
        current.addPoints(scored);
        refillRack(current);
        advanceTurn();
        return MoveResult.success(scored, placement.getWord());
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public boolean undo() {
        if (!canUndo()) {
            return false;
        }
        redoStack.push(new GameState(this));
        GameState previous = undoStack.pop();
        previous.restore(this);
        return true;
    }

    public boolean redo() {
        if (!canRedo()) {
            return false;
        }
        undoStack.push(new GameState(this));
        GameState restored = redoStack.pop();
        restored.restore(this);
        return true;
    }

    public void saveGame(File file) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file))) {
            out.writeObject(new GameState(this));
        }
    }

    public void loadGame(File file) throws IOException, ClassNotFoundException {
        GameState state;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(file))) {
            state = (GameState) in.readObject();
        }
        state.restore(this);
        undoStack.clear();
        redoStack.clear();
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void advanceTurn() {
        if (players.isEmpty()) {
            currentPlayerIndex = 0;
        } else {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
    }

    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    private void pushUndoState() {
        undoStack.push(new GameState(this));
    }

    private void refillRack(Player player) {
        while (player.handSize() < 7 && !tileBag.isEmpty()) {
            player.addTile(tileBag.dealTile());
        }
    }

    private TileUsage planTileUsage(Player player, Placement placement) {
        Map<Letter, Integer> rackCounts = new EnumMap<>(Letter.class);
        for (Tile tile : player.getHand()) {
            rackCounts.merge(tile.getLetter(), 1, Integer::sum);
        }
        int r = placement.getRow();
        int c = placement.getCol();
        List<Boolean> blanks = new ArrayList<>(placement.getWord().length());
        int newTiles = 0;
        for (int i = 0; i < placement.getWord().length(); i++) {
            if (board.getCell(r, c) == Board.EMPTY) {
                newTiles++;
                Letter needed = letterFor(placement.getWord().charAt(i));
                int available = rackCounts.getOrDefault(needed, 0);
                if (available > 0) {
                    rackCounts.put(needed, available - 1);
                    blanks.add(Boolean.FALSE);
                } else {
                    int blankCount = rackCounts.getOrDefault(Letter.BLANK, 0);
                    if (blankCount == 0) {
                        return null;
                    }
                    rackCounts.put(Letter.BLANK, blankCount - 1);
                    blanks.add(Boolean.TRUE);
                }
            } else {
                blanks.add(null);
            }
            if (placement.getDirection() == Placement.Direction.ACROSS) {
                c++;
            } else {
                r++;
            }
        }
        return new TileUsage(blanks, newTiles);
    }

    private void consumeTilesForPlacement(Player player, Placement placement, TileUsage usage) {
        int r = placement.getRow();
        int c = placement.getCol();
        for (int i = 0; i < placement.getWord().length(); i++) {
            if (board.getCell(r, c) == Board.EMPTY) {
                if (usage.isBlankAt(i)) {
                    if (player.takeTile(Letter.BLANK) == null) {
                        throw new IllegalStateException("Expected blank tile not found.");
                    }
                } else {
                    Letter needed = letterFor(placement.getWord().charAt(i));
                    if (player.takeTile(needed) == null) {
                        throw new IllegalStateException("Tile missing during placement: " + needed);
                    }
                }
            }
            if (placement.getDirection() == Placement.Direction.ACROSS) {
                c++;
            } else {
                r++;
            }
        }
    }

    private void applyPlacement(Placement placement, TileUsage usage) {
        int r = placement.getRow();
        int c = placement.getCol();
        for (int i = 0; i < placement.getWord().length(); i++) {
            boolean squareEmpty = board.getCell(r, c) == Board.EMPTY;
            if (squareEmpty) {
                blankSquares[r][c] = usage.isBlankAt(i);
            }
            if (placement.getDirection() == Placement.Direction.ACROSS) {
                c++;
            } else {
                r++;
            }
        }
        board.place(placement);
    }

    private int scorePlacement(Placement placement, TileUsage usage) {
        int r = placement.getRow();
        int c = placement.getCol();
        int total = 0;
        int wordMultiplier = 1;
        for (int i = 0; i < placement.getWord().length(); i++) {
            boolean squareEmpty = board.getCell(r, c) == Board.EMPTY;
            int letterScore;
            if (squareEmpty) {
                letterScore = usage.isBlankAt(i) ? 0 : letterFor(placement.getWord().charAt(i)).getPoints();
            } else {
                letterScore = blankSquares[r][c] ? 0 : letterFor(board.getCell(r, c)).getPoints();
            }
            PremiumSquare premium = premiumSquares[r][c];
            if (squareEmpty) {
                letterScore *= premium.letterMultiplier();
                wordMultiplier *= premium.wordMultiplier();
            }
            total += letterScore;
            if (placement.getDirection() == Placement.Direction.ACROSS) {
                c++;
            } else {
                r++;
            }
        }
        if (usage.getNewTileCount() == 7) {
            total += 50;
        }
        return total * wordMultiplier;
    }

    private Letter letterFor(char ch) {
        return Letter.valueOf(String.valueOf(Character.toUpperCase(ch)));
    }

    private PremiumSquare[][] copyPremiums() {
        PremiumSquare[][] copy = new PremiumSquare[premiumSquares.length][premiumSquares[0].length];
        for (int r = 0; r < premiumSquares.length; r++) {
            System.arraycopy(premiumSquares[r], 0, copy[r], 0, premiumSquares[r].length);
        }
        return copy;
    }

    private boolean[][] copyBlanks() {
        boolean[][] copy = new boolean[blankSquares.length][blankSquares[0].length];
        for (int r = 0; r < blankSquares.length; r++) {
            System.arraycopy(blankSquares[r], 0, copy[r], 0, blankSquares[r].length);
        }
        return copy;
    }

    public static final class MoveResult {
        private final boolean success;
        private final String message;
        private final int points;

        private MoveResult(boolean success, String message, int points) {
            this.success = success;
            this.message = message;
            this.points = points;
        }

        public static MoveResult success(int points, String word) {
            return new MoveResult(true, "Placed " + word + " for " + points + " points.", points);
        }

        public static MoveResult failure(String reason) {
            return new MoveResult(false, reason, 0);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public int getPoints() {
            return points;
        }
    }

    private static final class TileUsage {
        private final List<Boolean> blanks;
        private final int newTileCount;

        TileUsage(List<Boolean> blanks, int newTileCount) {
            this.blanks = blanks;
            this.newTileCount = newTileCount;
        }

        boolean isBlankAt(int index) {
            return Boolean.TRUE.equals(blanks.get(index));
        }

        int getNewTileCount() {
            return newTileCount;
        }
    }

    private static final class GameState implements Serializable {
        private static final long serialVersionUID = 1L;
        private final char[][] grid;
        private final boolean[][] blanks;
        private final List<PlayerState> players;
        private final List<Letter> bagLetters;
        private final int currentPlayerIndex;
        private final PremiumSquare[][] premiums;
        private final String boardName;

        GameState(Scrabble_Model model) {
            this.grid = model.board.snapshot();
            this.blanks = model.copyBlanks();
            this.players = new ArrayList<>();
            for (Player player : model.players) {
                players.add(new PlayerState(player));
            }
            this.bagLetters = model.tileBag.snapshotLetters();
            this.currentPlayerIndex = model.currentPlayerIndex;
            this.premiums = model.copyPremiums();
            this.boardName = model.boardName;
        }

        void restore(Scrabble_Model model) {
            model.board.restore(grid);
            model.blankSquares = model.copyBlanksTemplate(blanks);
            model.players.clear();
            for (PlayerState state : players) {
                Player player = new Player(state.name);
                player.setScore(state.score);
                player.setRack(state.letters);
                model.players.add(player);
            }
            model.tileBag.restoreFromLetters(bagLetters);
            model.currentPlayerIndex = Math.min(currentPlayerIndex, Math.max(0, model.players.size() - 1));
            model.premiumSquares = model.copyPremiumsTemplate(premiums);
            model.boardName = boardName;
        }
    }

    private boolean[][] copyBlanksTemplate(boolean[][] source) {
        boolean[][] copy = new boolean[source.length][source[0].length];
        for (int r = 0; r < source.length; r++) {
            System.arraycopy(source[r], 0, copy[r], 0, source[r].length);
        }
        return copy;
    }

    private PremiumSquare[][] copyPremiumsTemplate(PremiumSquare[][] source) {
        PremiumSquare[][] copy = new PremiumSquare[source.length][source[0].length];
        for (int r = 0; r < source.length; r++) {
            System.arraycopy(source[r], 0, copy[r], 0, source[r].length);
        }
        return copy;
    }

    private static final class PlayerState implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final int score;
        private final List<Letter> letters;

        PlayerState(Player player) {
            this.name = player.getName();
            this.score = player.getScore();
            this.letters = player.snapshotRack();
        }
    }

    public static class Board {
        public static final int SIZE = 15;
        public static final char EMPTY = '\0';
        private final char[][] grid = new char[SIZE][SIZE];

        public Board() {}

        public void reset() {
            for (int r = 0; r < SIZE; r++) {
                for (int c = 0; c < SIZE; c++) {
                    grid[r][c] = EMPTY;
                }
            }
        }

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
            int r = p.getRow();
            int c = p.getCol();
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
            int r = p.getRow();
            int c = p.getCol();
            for (int i = 0; i < p.getWord().length(); i++) {
                if (grid[r][c] == EMPTY) {
                    grid[r][c] = p.getWord().charAt(i);
                }
                if (p.getDirection() == Placement.Direction.ACROSS) c++; else r++;
            }
        }

        public char getCell(int row, int col) {
            if (!inBounds(row, col)) {
                return EMPTY;
            }
            return grid[row][col];
        }

        public char[][] snapshot() {
            char[][] copy = new char[SIZE][SIZE];
            for (int r = 0; r < SIZE; r++) {
                System.arraycopy(grid[r], 0, copy[r], 0, SIZE);
            }
            return copy;
        }

        public void restore(char[][] state) {
            for (int r = 0; r < SIZE; r++) {
                System.arraycopy(state[r], 0, grid[r], 0, SIZE);
            }
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
            for (int i = 0; i < 12; i++) tiles.add(new Tile(Letter.E));
            Collections.shuffle(tiles);
        }
        public Tile dealTile() {
            if (tiles.isEmpty()) throw new IllegalStateException("Tile bag empty");
            return tiles.remove(tiles.size() - 1);
        }
        public boolean isEmpty() { return tiles.isEmpty(); }
        public int size() { return tiles.size(); }
        public List<Letter> snapshotLetters() {
            List<Letter> letters = new ArrayList<>();
            for (Tile tile : tiles) {
                letters.add(tile.getLetter());
            }
            return letters;
        }
        public void restoreFromLetters(List<Letter> letters) {
            tiles.clear();
            for (Letter letter : letters) {
                tiles.add(new Tile(letter));
            }
        }
    }

    public static class Player {
        private String name;
        private int score;
        private final List<Tile> hand;
        public Player(String name) { this.name = name; score = 0; hand = new ArrayList<>(); }
        public String getName() { return name; }
        public int getScore() { return score; }
        public List<Tile> getHand() { return Collections.unmodifiableList(hand); }
        public List<Letter> snapshotRack() {
            List<Letter> letters = new ArrayList<>();
            for (Tile tile : hand) {
                letters.add(tile.getLetter());
            }
            return letters;
        }
        public void setRack(List<Letter> letters) {
            hand.clear();
            for (Letter letter : letters) {
                hand.add(new Tile(letter));
            }
        }
        public void addTile(Tile tile) { hand.add(tile); }
        public void removeTile(Tile tile) { hand.remove(tile); }
        public void clearHand() { hand.clear(); }
        public int handSize() { return hand.size(); }
        public void addPoints(int pts) { score += pts; }
        public void setScore(int value) { score = value; }
        public Tile takeTile(Letter letter) {
            for (int i = 0; i < hand.size(); i++) {
                Tile tile = hand.get(i);
                if (tile.getLetter() == letter) {
                    hand.remove(i);
                    return tile;
                }
            }
            return null;
        }
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
        public Dictionary(List<String> entries) {
            for (String entry : entries) {
                if (entry != null && !entry.isEmpty()) {
                    words.add(entry.toUpperCase(Locale.ROOT));
                }
            }
        }
        private void loadDictionary(String path) {
            try (java.io.BufferedReader br = Files.newBufferedReader(Paths.get(path), StandardCharsets.UTF_8)) {
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

