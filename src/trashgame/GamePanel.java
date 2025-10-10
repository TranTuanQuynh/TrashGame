
package trashgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.table.DefaultTableModel;
import trashgame.ScoreListener;

public class GamePanel extends JPanel implements ActionListener, KeyListener, ScoreListener {
    private Timer timer;
    private MainFrame parent;
    private ArrayList<TrashItem> items = new ArrayList<>();
    private Bin organicBin, inorganicBin, recyclableBin;
    private int score = 0;
    private int lives = 3;
    private Random rand = new Random();
    private int spawnCounter = 0;
    private int spawnInterval = 100;
    private boolean gameOver = false;
    private int panelWidth = 1000;
    private int panelHeight = 600;
    private int fallSpeedTimer = 0;
    private JTable scoreTable;
    private DefaultTableModel scoreModel;
    
    private String username;
    private int lastSentScore = 0;
    
    private int binSpacing = 10;

    public GamePanel(MainFrame frame) {
        this.parent = frame;
        this.username = parent.getCurrentUsername();
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setBackground(Color.WHITE);
        setFocusable(true);
        addKeyListener(new KeyHandler());

        int startX = 100;
        organicBin = new Bin(startX, panelHeight - 60, "organic");
        inorganicBin = new Bin(organicBin.getX() + organicBin.getWidth() + binSpacing, panelHeight - 60, "inorganic");
        recyclableBin = new Bin(inorganicBin.getX() + inorganicBin.getWidth() + binSpacing, panelHeight - 60, "recyclable");

        initScoreTable();
        if(parent.getClient()!= null){
            parent.getClient().addScoreListener(this);
        }
        
        timer = new Timer(20, this);
        timer.start();
    }
    
    private void initScoreTable() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        JLabel scoreLabel = new JLabel("Điểm người chơi khác:");
        scoreModel = new DefaultTableModel(new Object[]{"Người chơi", "Điểm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        scoreTable = new JTable(scoreModel);
        scoreTable.setPreferredScrollableViewportSize(new Dimension(150, 150));
        scoreTable.setFillsViewportHeight(true);
        rightPanel.add(scoreLabel, BorderLayout.NORTH);
        rightPanel.add(new JScrollPane(scoreTable), BorderLayout.CENTER);

        add(rightPanel, BorderLayout.EAST);
    }
    
    // FIXED: Không còn tham số
    private void sendScoreToServer() {
        if (parent.getClient() != null && score > lastSentScore) {
            parent.getClient().sendMessage("SCORE_REALTIME:" + score);
            lastSentScore = score;
            System.out.println("📤 Gửi điểm số thời gian thực: " + score);
        }
    }
    
    @Override
    public void onScoreUpdate(String otherUsername, int otherScore) {
        boolean found = false;
        for (int i = 0; i < scoreModel.getRowCount(); i++) {
            if (scoreModel.getValueAt(i, 0).equals(otherUsername) && !otherUsername.equals(username)) {
                scoreModel.setValueAt(otherScore, i, 1);
                found = true;
                break;
            }
        }
        if (!found && !otherUsername.equals(username)) {
            scoreModel.addRow(new Object[]{otherUsername, otherScore});
        }
        repaint();
    }
    
    public void onRoomPlayerList(List<String[]> players) {
        scoreModel.setRowCount(0);
        for (String[] p : players) {
            if (!p[0].equals(username)) {
                scoreModel.addRow(new Object[]{p[0], Integer.parseInt(p[1])});
            }
        }
    }
    
    public void onStartGame() {
        System.out.println("Game started for " + username);
    }
    
    @Override
    public void onUserJoined(String username) {
        if (!username.equals(this.username)) {
            scoreModel.addRow(new Object[]{username, 0});
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameOver) {
            g.setColor(Color.BLACK);
            g.drawString("Game Over! Score: " + score,
                    panelWidth / 2 - 50, panelHeight / 2);
            return;
        }

        organicBin.draw(g);
        inorganicBin.draw(g);
        recyclableBin.draw(g);

        for (TrashItem item : items) {
            item.draw(g);
        }

        g.setColor(Color.BLACK);
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Lives: " + lives, 10, 40);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        spawnCounter++;
        if (spawnCounter >= spawnInterval) {
            boolean angled = (score >= 0);
            int binStartX = organicBin.getX();
            int binEndX = recyclableBin.getX() + recyclableBin.getWidth();
            TrashItem newItem = new TrashItem(panelWidth, panelHeight, binStartX, binEndX, angled, score);
            items.add(newItem);
            spawnCounter = 0;
        }

        if (fallSpeedTimer > 0 && fallSpeedTimer % 1500 == 0 && spawnInterval > 30) {
            spawnInterval -= 10;
        }

        for (TrashItem item : items) item.fall();

        fallSpeedTimer++;
        if (fallSpeedTimer > 500) {
            for (TrashItem item : items) item.increaseSpeed();
            fallSpeedTimer = 0;
        }

        checkCollisions();

        items.removeIf(item -> {
            if (item.getY() > panelHeight) {
                if (!"bomb".equals(item.getType())) {
                    lives--;
                    if (lives <= 0) endGame();
                }
                return true;
            }
            return false;
        });

        repaint();
    }
    
    public void cleanup() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
            System.out.println("Dừng timer của GamePanel cũ");
        }

        if (parent.getClient() != null) {
            parent.getClient().removeScoreListener(this);
            System.out.println("Xóa ScoreListener của GamePanel cũ");
        }

        gameOver = true;
    }

    private void checkCollisions() {
        java.util.List<TrashItem> toRemove = new java.util.ArrayList<>();

        for (TrashItem item : items) {
            if (item.getY() + 20 >= organicBin.getY()) {
                if ("bomb".equals(item.getType())) {
                    if (checkBinCollision(item, organicBin) ||
                        checkBinCollision(item, inorganicBin) ||
                        checkBinCollision(item, recyclableBin)) {
                        score = Math.max(0, score - 1);
                        lives--;
                        if (lives <= 0) endGame();
                    }
                    toRemove.add(item);
                    continue;
                }

                boolean matched = false;
                if (checkBinCollision(item, organicBin) && "organic".equals(item.getType())) {
                    score++; 
                    matched = true;
                    sendScoreToServer();  // FIXED: Không có tham số
                } else if (checkBinCollision(item, inorganicBin) && "inorganic".equals(item.getType())) {
                    score++; 
                    matched = true;
                    sendScoreToServer();  // FIXED: Không có tham số
                } else if (checkBinCollision(item, recyclableBin) && "recyclable".equals(item.getType())) {
                    score++; 
                    matched = true;
                    sendScoreToServer();  // FIXED: Không có tham số
                }

                if (!matched) {
                    lives--;
                    if (lives <= 0) endGame();
                }
                toRemove.add(item);
            }
        }
        items.removeAll(toRemove);
    }

    private boolean checkBinCollision(TrashItem item, Bin bin) {
        return item.getX() + 20 > bin.getX()
                && item.getX() < bin.getX() + bin.getWidth();
    }

    private void endGame() {
        if(gameOver) return;
        gameOver = true;
        if (timer != null) timer.stop();
        
        // FIXED: Gửi điểm cuối cùng để lưu vào DB
        if (parent.getClient() != null) {
            int user_id = parent.getCurrentUser();
            parent.getClient().sendMessage("SCORE_FINAL:" + score + ":" + user_id);
            System.out.println("📤 Gửi score cuối để lưu DB: " + score + " (userId=" + user_id + ")");
        }
        
        SwingUtilities.invokeLater(() -> parent.showControlPanel(score));
    }

    private void moveBlockBy(int deltaX) {
        int binWidth = organicBin.getWidth();
        int blockWidth = 3 * binWidth + 2 * binSpacing;
        int newOrganicX = organicBin.getX() + deltaX;

        if (newOrganicX < 0) newOrganicX = 0;
        if (newOrganicX + blockWidth > panelWidth) newOrganicX = panelWidth - blockWidth;

        organicBin.setX(newOrganicX);
        inorganicBin.setX(newOrganicX + binWidth + binSpacing);
        recyclableBin.setX(newOrganicX + 2 * (binWidth + binSpacing));
    }

    @Override
    public void keyTyped(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void keyPressed(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void keyReleased(KeyEvent e) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class KeyHandler implements KeyListener {
        @Override
        public void keyPressed(KeyEvent e) {
            if (gameOver) return;
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT) {
                moveBlockBy(-10);
            } else if (key == KeyEvent.VK_RIGHT) {
                moveBlockBy(+10);
            } else if (key == KeyEvent.VK_DOWN) {
                for (TrashItem item : items) item.increaseSpeed();
            }
        }
        
        @Override public void keyTyped(KeyEvent e) {}
        @Override public void keyReleased(KeyEvent e) {}
    }
}