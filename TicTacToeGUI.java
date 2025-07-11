import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.sound.sampled.*;

public class TicTacToeGUI extends JFrame implements ActionListener {
    JButton[][] buttons = new JButton[3][3];
    JButton resetButton = new JButton("Reset");
    JButton exitButton = new JButton("Exit");
    JButton modeButton = new JButton("Switch to PvP");
    
    char currentPlayer = 'X';
    String playerX = "Player X", playerO = "AI";
    boolean isPvP = false;
    
    int scoreX = 0, scoreO = 0;
    JLabel statusLabel = new JLabel("Current turn: Player X");
    JLabel scoreLabel = new JLabel("Score - X: 0 | O: 0");

    public TicTacToeGUI() {
        setTitle("Tic-Tac-Toe");
        setSize(420, 550);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        askPlayerName();

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(scoreLabel);
        topPanel.add(statusLabel);

        JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, 60);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new JButton("");
                buttons[i][j].setFont(font);
                buttons[i][j].setBackground(Color.WHITE);
                buttons[i][j].addActionListener(this);
                boardPanel.add(buttons[i][j]);
            }
        }

        JPanel controlPanel = new JPanel(new FlowLayout());
        resetButton.addActionListener(e -> resetBoard());
        exitButton.addActionListener(e -> System.exit(0));
        modeButton.addActionListener(e -> toggleGameMode());
        controlPanel.add(resetButton);
        controlPanel.add(modeButton);
        controlPanel.add(exitButton);

        add(topPanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    void askPlayerName() {
        playerX = JOptionPane.showInputDialog(this, "Enter your name:", "Player X");
        if (playerX == null || playerX.trim().isEmpty()) playerX = "Player X";
        playerO = "AI";
    }

    void toggleGameMode() {
        isPvP = !isPvP;
        playerO = isPvP ? "Player O" : "AI";
        modeButton.setText(isPvP ? "Switch to PvAI" : "Switch to PvP");
        resetBoard();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        if (!btn.getText().equals("") || (currentPlayer == 'O' && !isPvP)) return;

        playSound("click.wav");
        btn.setText(String.valueOf(currentPlayer));
        btn.setForeground(currentPlayer == 'X' ? Color.BLUE : Color.RED);

        if (checkWin(currentPlayer)) {
            if (currentPlayer == 'X') scoreX++;
            else scoreO++;
            playSound("win.wav");
            showResult((currentPlayer == 'X' ? playerX : playerO) + " wins!");
        } else if (isDraw()) {
            playSound("draw.wav");
            showResult("It's a draw!");
        } else {
            switchPlayer();
            statusLabel.setText("Current turn: " + (currentPlayer == 'X' ? playerX : playerO));

            if (currentPlayer == 'O' && !isPvP) {
                SwingUtilities.invokeLater(() -> {
                    makeAIMove();
                    if (checkWin('O')) {
                        scoreO++;
                        playSound("win.wav");
                        showResult(playerO + " wins!");
                    } else if (isDraw()) {
                        playSound("draw.wav");
                        showResult("It's a draw!");
                    } else {
                        currentPlayer = 'X';
                        statusLabel.setText("Current turn: " + playerX);
                    }
                });
            }
        }
    }

    void makeAIMove() {
        int[] move = findBestMove();
        buttons[move[0]][move[1]].setText("O");
        buttons[move[0]][move[1]].setForeground(Color.RED);
        playSound("click.wav");
    }

    int[] findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] move = {-1, -1};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().equals("")) {
                    buttons[i][j].setText("O");
                    int score = minimax(false);
                    buttons[i][j].setText("");
                    if (score > bestScore) {
                        bestScore = score;
                        move[0] = i;
                        move[1] = j;
                    }
                }
            }
        }
        return move;
    }

    int minimax(boolean isMaximizing) {
        if (checkWin('O')) return 1;
        if (checkWin('X')) return -1;
        if (isDraw()) return 0;

        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (buttons[i][j].getText().equals("")) {
                    buttons[i][j].setText(isMaximizing ? "O" : "X");
                    int score = minimax(!isMaximizing);
                    buttons[i][j].setText("");
                    if (isMaximizing) bestScore = Math.max(score, bestScore);
                    else bestScore = Math.min(score, bestScore);
                }
            }
        }
        return bestScore;
    }

    boolean checkWin(char player) {
        String p = String.valueOf(player);
        for (int i = 0; i < 3; i++) {
            if (buttons[i][0].getText().equals(p) &&
                buttons[i][1].getText().equals(p) &&
                buttons[i][2].getText().equals(p)) return true;
            if (buttons[0][i].getText().equals(p) &&
                buttons[1][i].getText().equals(p) &&
                buttons[2][i].getText().equals(p)) return true;
        }
        if (buttons[0][0].getText().equals(p) &&
            buttons[1][1].getText().equals(p) &&
            buttons[2][2].getText().equals(p)) return true;
        if (buttons[0][2].getText().equals(p) &&
            buttons[1][1].getText().equals(p) &&
            buttons[2][0].getText().equals(p)) return true;

        return false;
    }

    boolean isDraw() {
        for (JButton[] row : buttons)
            for (JButton b : row)
                if (b.getText().equals("")) return false;
        return true;
    }

    void showResult(String message) {
        updateScore();
        JOptionPane.showMessageDialog(this, message);
        resetBoard();
    }

    void updateScore() {
        scoreLabel.setText("Score - " + playerX + ": " + scoreX + " | " + playerO + ": " + scoreO);
    }

    void switchPlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    void resetBoard() {
        for (JButton[] row : buttons)
            for (JButton b : row)
                b.setText("");
        currentPlayer = 'X';
        statusLabel.setText("Current turn: " + playerX);
    }

    void playSound(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) return;
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Sound error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new TicTacToeGUI();
    }
}
