import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * Coordinates the Scrabble gameplay loop: player management, drawing tiles,
 * validating moves, and computing scores on top of the existing Board/Tile model.
 */
public class Game {
    private static final int RACK_SIZE = 7;

    private final Board board;
    private final TileBag tileBag;
    private final Dictionary dictionary;
    private final List<Player> players;
    private final char[][] boardState;
    private final boolean[][] blankSquares;
    private final SquareBonus[][] bonuses;

    private int currentPlayerIndex = 0;
    private int consecutivePasses = 0;
    private boolean gameOver = false;

    /**
     * Constructs a game using the default word list in {@code words.txt}.
     */
    public Game(List<String> playerNames) {
        this(playerNames, "words.txt");
    }

    /**
     * Constructs a game with a specified dictionary file.
     */
    public Game(List<String> playerNames, String dictionaryPath) {
        Objects.requireNonNull(playerNames, "playerNames");
        if (playerNames.size() < 2 || playerNames.size() > 4) {
            throw new IllegalArgumentException("Scrabble requires between 2 and 4 players.");
        }
        this.board = new Board();
        this.tileBag = new TileBag();
        this.dictionary = new Dictionary(dictionaryPath);
        this.players = new ArrayList<>();
        for (String name : playerNames) {
            Player p = new Player(Objects.requireNonNull(name, "player name"));
            players.add(p);
        }
        this.boardState = new char[Board.SIZE][Board.SIZE];
        this.blankSquares = new boolean[Board.SIZE][Board.SIZE];
        this.bonuses = buildBonusGrid();
        dealInitialHands();
    }

    /** @return immutable view of the players in seating order. */
    public List<Player> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /** @return the Board instance used for play. */
    public Board getBoard() {
        return board;
    }

    /** @return the player whose turn it currently is. */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /** @return number of tiles left in the bag. */
    public int tilesRemaining() {
        return tileBag.size();
    }

    /** @return number of consecutive passes recorded. */
    public int getConsecutivePasses() {
        return consecutivePasses;
    }

    /**
     * Returns a read-only view of the current player's rack.
     * Other players' racks are hidden.
     */
    public List<Tile> viewCurrentRack() {
        return getCurrentPlayer().getHand();
    }

    /**
     * Attempts to play a turn using official notation, e.g. {@code "H8 HELLO A"}.
     *
     * @return detailed outcome of the move.
     */
    public MoveResult playTurn(String notation) {
        Placement play;
        try {
            play = parseNotation(notation);
        } catch (IllegalArgumentException ex) {
            return MoveResult.failure(ex.getMessage());
        }

        if (!dictionary.isValidWord(play.getWord())) {
            return MoveResult.failure("Word '" + play.getWord() + "' is not in the dictionary.");
        }

        Player current = getCurrentPlayer();
        StringBuilder reason = new StringBuilder();
        if (!board.canPlace(play, reason)) {
            String message = reason.length() > 0 ? reason.toString() : "Cannot place word on board.";
            return MoveResult.failure(message);
        }

        TileUsage usage = planTileUsage(current, play);
        if (usage == null) {
            return MoveResult.failure("Rack does not contain the needed letters.");
        }

        int scored = scorePlacement(play, usage);
        consumeTilesForPlacement(current, play, usage);
        board.place(play);
        applyToBoardState(play, usage);
        current.addPoints(scored);
        refillRack(current);

        consecutivePasses = 0;
        checkGameEndAfterPlay(current);
        if (!gameOver) {
            advanceTurn();
        }

        return MoveResult.success(scored, "Played " + play.getWord() + " for " + scored + " points.");
    }

    /** Active player passes without playing a word. */
    public void passTurn() {
        consecutivePasses++;
        if (consecutivePasses >= players.size() * 2) {
            gameOver = true;
        } else {
            advanceTurn();
        }
    }

    /** @return whether the game has reached an end condition. */
    public boolean isGameOver() {
        return gameOver;
    }

    /** @return leaderboard sorted by descending score. */
    public List<Player> getLeaderboard() {
        List<Player> ordered = new ArrayList<>(players);
        ordered.sort(Comparator.comparingInt(Player::getScore).reversed().thenComparing(Player::getName));
        return ordered;
    }

    /**
     * Console entry point for the Scrabble game. Players are prompted for their
     * names, then take turns entering moves in official notation or passing.
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to Console Scrabble!");
        int playerCount = promptPlayerCount(scanner);
        List<String> names = new ArrayList<>();
        for (int i = 1; i <= playerCount; i++) {
            System.out.print("Enter name for Player " + i + ": ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) {
                name = "Player" + i;
            }
            names.add(name);
        }

        Game game = new Game(names);
        boolean quitEarly = false;

        while (!game.isGameOver()) {
            Player current = game.getCurrentPlayer();
            System.out.println("\nCurrent board:");
            System.out.println(game.getBoard().render());
            printScores(game);
            System.out.println("Tiles remaining in bag: " + game.tilesRemaining());
            System.out.println(current.getName() + "'s rack: " + formatRack(game.viewCurrentRack()));

            System.out.print("Enter move (e.g. H8 WORD A), PASS, or QUIT: ");
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }
            if (input.equalsIgnoreCase("QUIT")) {
                quitEarly = true;
                break;
            }
            if (input.equalsIgnoreCase("PASS")) {
                game.passTurn();
                if (game.isGameOver()) {
                    System.out.println("Passing threshold reached. Ending game.");
                } else {
                    System.out.println(current.getName() + " passes.");
                }
                continue;
            }

            MoveResult result = game.playTurn(input);
            if (result.success()) {
                System.out.println(result.message());
            } else {
                System.out.println("Invalid move: " + result.message());
            }
        }

        System.out.println("\nGame over" + (quitEarly ? " (early quit)." : "."));
        System.out.println("Final board:");
        System.out.println(game.getBoard().render());
        printFinalScores(game);
        scanner.close();
    }

    private static int promptPlayerCount(Scanner scanner) {
        while (true) {
            System.out.print("Enter number of players (2-4): ");
            String line = scanner.nextLine().trim();
            try {
                int count = Integer.parseInt(line);
                if (count >= 2 && count <= 4) {
                    return count;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Please enter a numeric value between 2 and 4.");
        }
    }

    private static void printScores(Game game) {
        System.out.println("Scores:");
        for (Player player : game.getPlayers()) {
            System.out.println(" - " + player.getName() + ": " + player.getScore());
        }
    }

    private static void printFinalScores(Game game) {
        System.out.println("Final scores:");
        for (Player player : game.getLeaderboard()) {
            System.out.println(" - " + player.getName() + ": " + player.getScore());
        }
    }

    private static String formatRack(List<Tile> rack) {
        if (rack.isEmpty()) {
            return "(empty)";
        }
        StringBuilder sb = new StringBuilder();
        for (Tile tile : rack) {
            sb.append(tile.getLetter().name());

            sb.append("(").append(tile.getPoints()).append(") ");
        }
        return sb.toString().trim();
    }

    private void dealInitialHands() {
        for (Player player : players) {
            refillRack(player);
        }
    }

    private void refillRack(Player player) {
        while (player.handSize() < RACK_SIZE && !tileBag.isEmpty()) {
            player.addTile(tileBag.dealTile());
        }
    }

    private void advanceTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }

    private void applyToBoardState(Placement play, TileUsage usage) {
        int r = play.getRow();
        int c = play.getCol();
        for (int i = 0; i < play.getWord().length(); i++) {
            if (boardState[r][c] == '\0') {
                boardState[r][c] = play.getWord().charAt(i);
                blankSquares[r][c] = usage.isBlankAt(i);
            }
            if (play.getDirection() == Placement.Direction.ACROSS) {
                c++;
            } else {
                r++;
            }
        }
    }

    private void checkGameEndAfterPlay(Player current) {
        if (current.handSize() == 0 && tileBag.isEmpty()) {
            gameOver = true;
        }
    }

    private int scorePlacement(Placement play, TileUsage usage) {
        int wordMultiplier = 1;
        int total = 0;

        int r = play.getRow();
        int c = play.getCol();
        for (int i = 0; i < play.getWord().length(); i++) {
            boolean newTile = boardState[r][c] == '\0';
            int letterScore;
            if (newTile) {
                letterScore = usage.isBlankAt(i) ? 0 : letterFor(play.getWord().charAt(i)).getPoints();
            } else {
                letterScore = blankSquares[r][c] ? 0 : letterFor(boardState[r][c]).getPoints();
            }

            SquareBonus bonus = bonuses[r][c];
            if (newTile) {
                letterScore *= bonus.letterMultiplier;
                wordMultiplier *= bonus.wordMultiplier;
            }
            total += letterScore;

            if (play.getDirection() == Placement.Direction.ACROSS) {
                c++;
            } else {
                r++;
            }
        }

        if (usage.newTileCount == RACK_SIZE) {
            total += 50; // bingo bonus
        }
        return total * wordMultiplier;
    }

    private TileUsage planTileUsage(Player player, Placement play) {
        Map<Letter, Integer> rackCounts = new EnumMap<>(Letter.class);
        for (Tile tile : player.getHand()) {
            rackCounts.merge(tile.getLetter(), 1, Integer::sum);
        }

        int r = play.getRow();
        int c = play.getCol();
        List<Boolean> blanks = new ArrayList<>(play.getWord().length());
        int newTiles = 0;

        for (int i = 0; i < play.getWord().length(); i++) {
            if (boardState[r][c] == '\0') {
                newTiles++;
                Letter needed = letterFor(play.getWord().charAt(i));
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

            if (play.getDirection() == Placement.Direction.ACROSS) {
                c++;
            } else {
                r++;
            }
        }

        return new TileUsage(blanks, newTiles);
    }

    private void consumeTilesForPlacement(Player player, Placement play, TileUsage usage) {
        int r = play.getRow();
        int c = play.getCol();
        for (int i = 0; i < play.getWord().length(); i++) {
            if (boardState[r][c] == '\0') {
                if (usage.isBlankAt(i)) {
                    if (player.takeTile(Letter.BLANK) == null) {
                        throw new IllegalStateException("Expected blank tile during placement.");
                    }
                } else {
                    Letter needed = letterFor(play.getWord().charAt(i));
                    if (player.takeTile(needed) == null) {
                        throw new IllegalStateException("Expected tile " + needed + " during placement.");
                    }
                }
            }

            if (play.getDirection() == Placement.Direction.ACROSS) {
                c++;
            } else {
                r++;
            }
        }
    }

    private static Placement parseNotation(String notation) {
        if (notation == null) {
            throw new IllegalArgumentException("Notation required.");
        }
        String[] tokens = notation.trim().split("\\s+");
        if (tokens.length < 2 || tokens.length > 3) {
            throw new IllegalArgumentException("Notation must be '<coord> <word> [A|D]'.");
        }
        String coord = tokens[0].toUpperCase(Locale.ROOT);
        if (coord.length() < 2) {
            throw new IllegalArgumentException("Coordinate must resemble 'H8'.");
        }

        char rowChar = coord.charAt(0);
        if (rowChar < 'A' || rowChar > 'O') {
            throw new IllegalArgumentException("Row must fall between A and O.");
        }
        int row = rowChar - 'A';
        int col;
        try {
            col = Integer.parseInt(coord.substring(1)) - 1;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Column must be numeric.", ex);
        }
        if (col < 0 || col >= Board.SIZE) {
            throw new IllegalArgumentException("Column must fall between 1 and 15.");
        }

        String word = tokens[1].toUpperCase(Locale.ROOT);
        if (word.isEmpty()) {
            throw new IllegalArgumentException("Word required.");
        }

        Placement.Direction direction = Placement.Direction.ACROSS;
        if (tokens.length == 3) {
            String dirToken = tokens[2].toUpperCase(Locale.ROOT);
            if (dirToken.startsWith("D")) {
                direction = Placement.Direction.DOWN;
            } else if (!dirToken.startsWith("A")) {
                throw new IllegalArgumentException("Direction must be A or D.");
            }
        }

        return new Placement(row, col, direction, word);
    }

    private static SquareBonus[][] buildBonusGrid() {
        SquareBonus[][] layout = new SquareBonus[Board.SIZE][Board.SIZE];
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                layout[r][c] = SquareBonus.NORMAL;
            }
        }

        set(layout, SquareBonus.TRIPLE_WORD, new int[][]{
                {0, 0}, {0, 7}, {0, 14},
                {7, 0}, {7, 14},
                {14, 0}, {14, 7}, {14, 14}
        });

        set(layout, SquareBonus.DOUBLE_WORD, new int[][]{
                {1, 1}, {2, 2}, {3, 3}, {4, 4},
                {1, 13}, {2, 12}, {3, 11}, {4, 10},
                {10, 4}, {11, 3}, {12, 2}, {13, 1},
                {10, 10}, {11, 11}, {12, 12}, {13, 13},
                {7, 7}
        });

        set(layout, SquareBonus.TRIPLE_LETTER, new int[][]{
                {1, 5}, {1, 9},
                {5, 1}, {5, 5}, {5, 9}, {5, 13},
                {9, 1}, {9, 5}, {9, 9}, {9, 13},
                {13, 5}, {13, 9}
        });

        set(layout, SquareBonus.DOUBLE_LETTER, new int[][]{
                {0, 3}, {0, 11},
                {2, 6}, {2, 8},
                {3, 0}, {3, 7}, {3, 14},
                {6, 2}, {6, 6}, {6, 8}, {6, 12},
                {7, 3}, {7, 11},
                {8, 2}, {8, 6}, {8, 8}, {8, 12},
                {11, 0}, {11, 7}, {11, 14},
                {12, 6}, {12, 8},
                {14, 3}, {14, 11}
        });
        return layout;
    }

    private static void set(SquareBonus[][] layout, SquareBonus bonus, int[][] coords) {
        for (int[] coord : coords) {
            layout[coord[0]][coord[1]] = bonus;
        }
    }

    private Letter letterFor(char ch) {
        return Letter.valueOf(String.valueOf(Character.toUpperCase(ch)));
    }

    private enum SquareBonus {
        NORMAL(1, 1),
        DOUBLE_LETTER(2, 1),
        TRIPLE_LETTER(3, 1),
        DOUBLE_WORD(1, 2),
        TRIPLE_WORD(1, 3);

        final int letterMultiplier;
        final int wordMultiplier;

        SquareBonus(int letterMultiplier, int wordMultiplier) {
            this.letterMultiplier = letterMultiplier;
            this.wordMultiplier = wordMultiplier;
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
            Boolean flag = blanks.get(index);
            return flag != null && flag;
        }
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

        public static MoveResult success(int points, String message) {
            return new MoveResult(true, message, points);
        }

        public static MoveResult failure(String message) {
            return new MoveResult(false, message, 0);
        }

        public boolean success() {
            return success;
        }

        public String message() {
            return message;
        }

        public int points() {
            return points;
        }
    }
}
