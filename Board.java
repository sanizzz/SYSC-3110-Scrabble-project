/**
 Purpose: Owns the 15×15 grid, validates placements (bounds/overwrite/reuse),
 and renders the board in text.
*/


public class Board {
    public static void main(String[] args)
    {
        Board b = new Board();
        System.out.println(b.inBounds(0, 0));    // true
        System.out.println(b.inBounds(14, 14));  // true
        System.out.println(b.inBounds(-1, 0));   // false
        System.out.println(b.inBounds(15, 5));   // false


    }



    public static final int SIZE = 15;// rows A-O and col 1-15
    private static final char EMPTY = '\0';// empty cell render as '.'


    private final char[][] grid = new char[SIZE][SIZE];

    public Board(){
        //Nothing needed — Java fills char arrays with '\0' automatically.
    }

    /**
     * function to create the grid layout using string builder
     * @return the grid layout of the board with labels for both rows and cols
     */
    public String render() {
        StringBuilder sb = new StringBuilder();

        // --- 1. Header row (column labels 1–15) ---
        sb.append("    "); // 4 spaces to align under row letters
        for (int col = 1; col <= 15; col++) {

            if (col < 10) {
                sb.append(" ").append(col).append(" ");
            } else {
                sb.append(col).append(" ");
            }
        }
        sb.append("\n");

        // --- 2. Each board row (A–O) ---
        for (int row = 0; row < 15; row++) {
            char rowLabel = (char) ('A' + row); // Convert 0→A, 1→B, etc.
            sb.append(rowLabel).append(" | ");   // Row label + divider

            for (int col = 0; col < 15; col++) {
                char value = grid[row][col];
                // Print '.' if empty; otherwise the letter on the board
                if (value == '\0') {
                    sb.append(".  "); // dot + two spaces for spacing
                } else {
                    sb.append(value).append("  ");
                }
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     *
     * @param row the row number
     * @param col the column number
     * @return true if word in bound and false if out of bounds
     */
    public boolean inBounds(int row, int col)
    {
        return row >= 0 && row < 15 && col >= 0 && col < 15;
    }

    /**
     *
     * @param p placement object which gives us word,row,col,direction
     * @param reason why the word cannot be placed
     * @return true if the word can be placed else false
     */

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
            if (existing != '\0' && existing != ch) {
                if (reason != null) reason.append("Letter conflict.");
                return false;
            }
            if (p.getDirection() == Placement.Direction.ACROSS) c++; else r++;
        }
        return true;
    }


    /**
     *
     * @param p tells us where and how to place the word
     */

    public void place(Placement p) {
        int r = p.getRow();
        int c = p.getCol();

        for (int i = 0; i < p.getWord().length(); i++) {
            char letter = p.getWord().charAt(i);

            // Only write into empty squares — never overwrite existing letters
            if (grid[r][c] == '\0') {
                grid[r][c] = letter;
            }

            // Move to the next cell depending on direction
            if (p.getDirection() == Placement.Direction.ACROSS) {
                c++;
            } else {
                r++;
            }
        }
    }






}
