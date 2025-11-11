import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class Scrabble_View extends JFrame {
    private JPanel boardPanel;
    private JPanel playerTilesPanel;
    private JPanel controlPanel;
    private JButton placeWordButton;
    private JLabel currentPlayerLabel;
    private JTextField wordField, rowField, colField, directionField;

    private int numPlayers = 2;

    public Scrabble_View() {
        setTitle("Scrabble Game");
        setSize(1000, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        promptPlayerCount();
        setupUI();
        setVisible(true);
    }

    private void promptPlayerCount() {
        while (true) {
            String input = JOptionPane.showInputDialog(this, "Enter number of players (1-4):", "Players", JOptionPane.QUESTION_MESSAGE);
            if (input == null) System.exit(0);
            try {
                int val = Integer.parseInt(input.trim());
                if (val >= 1 && val <= 4) {
                    numPlayers = val;
                    break;
                }
            } catch (Exception ignored) {}
            JOptionPane.showMessageDialog(this, "Please enter a number from 1 to 4.");
        }
    }
    public int getNumPlayers() { return numPlayers; }

    private void setupUI() {
        boardPanel = new JPanel(new GridLayout(15,15));
        boardPanel.setBorder(BorderFactory.createTitledBorder("Scrabble Board"));
        add(boardPanel, BorderLayout.CENTER);

        playerTilesPanel = new JPanel(new FlowLayout());
        playerTilesPanel.setBorder(BorderFactory.createTitledBorder("Your Tiles"));
        add(playerTilesPanel, BorderLayout.SOUTH);

        controlPanel = new JPanel(new GridLayout(0,1));
        placeWordButton = new JButton("Place Word");
        currentPlayerLabel = new JLabel("Current Player: 1");

        wordField = new JTextField(10);
        rowField = new JTextField(2);
        colField = new JTextField(2);
        directionField = new JTextField(7);

        controlPanel.add(new JLabel("Word:"));
        controlPanel.add(wordField);
        controlPanel.add(new JLabel("Row"));
        controlPanel.add(rowField);
        controlPanel.add(new JLabel("Col"));
        controlPanel.add(colField);
        controlPanel.add(new JLabel("Direction (A/D):"));
        controlPanel.add(directionField);

        controlPanel.add(placeWordButton);
        controlPanel.add(currentPlayerLabel);
        add(controlPanel, BorderLayout.EAST);
    }

    public void displayBoard(Scrabble_Model.Board board) {
        boardPanel.removeAll();
        for (int row = 0; row < Scrabble_Model.Board.SIZE; row++)
            for (int col = 0; col < Scrabble_Model.Board.SIZE; col++) {
                char value = board.getCell(row, col);
                JButton cell = new JButton(value == '\0' ? "." : "" + value);
                cell.setEnabled(false);
                boardPanel.add(cell);
            }
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    public void displayPlayerTiles(List<Scrabble_Model.Tile> tiles) {
        playerTilesPanel.removeAll();
        for (Scrabble_Model.Tile tile : tiles) {
            JButton tileBtn = new JButton(tile.getLetter().toString());
            tileBtn.setEnabled(false);
            playerTilesPanel.add(tileBtn);
        }
        playerTilesPanel.revalidate();
        playerTilesPanel.repaint();
    }

    public void updateCurrentPlayer(int playerIndex, String name) {
        currentPlayerLabel.setText("Current Player: " + (playerIndex + 1) + " (" + name + ")");
    }

    public void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg);
    }

    public void addPlaceWordListener(ActionListener listener) {
        placeWordButton.addActionListener(listener);
    }
    public String getWordInput() { return wordField.getText().trim(); }
    public int getRowInput() { try { return Integer.parseInt(rowField.getText()); } catch(Exception ex) { return 0; } }
    public int getColInput() { try { return Integer.parseInt(colField.getText()); } catch(Exception ex) { return 0; } }
    public Scrabble_Model.Placement.Direction getDirectionInput() {
        String dir = directionField.getText().trim().toUpperCase();
        if ("ACROSS".equals(dir)) return Scrabble_Model.Placement.Direction.ACROSS;
        else return Scrabble_Model.Placement.Direction.DOWN;
    }
}
