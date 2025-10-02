
package trashgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    private Timer timer;
    private MainFrame parent;
    private ArrayList<TrashItem> items = new ArrayList<>();
    private Bin organicBin, inorganicBin, recyclableBin;
    private int score = 0;
    private int lives = 3;
    private Random rand = new Random();
    private int spawnCounter = 0;
    private int spawnInterval = 100; // tick giữa 2 spawn
    private boolean gameOver = false;
    private int panelWidth = 1000;
    private int panelHeight = 600;
    private int fallSpeedTimer = 0;

    private int binSpacing = 10; // khoảng cách cố định giữa các thùng

    public GamePanel(MainFrame frame) {
        this.parent = frame;
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setBackground(Color.WHITE); // màu nền game
        setFocusable(true);
        addKeyListener(new KeyHandler());

        // Initialize bins: đặt block ban đầu sao cho cả block nằm trong màn
        int startX = 100; // vị trí thùng rác ở vị trí 100px
        organicBin = new Bin(startX, panelHeight - 60, "organic"); //thùng cách đáy 60px
        inorganicBin = new Bin(organicBin.getX() + organicBin.getWidth() + binSpacing, panelHeight - 60, "inorganic");
        recyclableBin = new Bin(inorganicBin.getX() + inorganicBin.getWidth() + binSpacing, panelHeight - 60, "recyclable");

        timer = new Timer(20, this);//50FPS 
        timer.start();
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

        g.setColor(Color.BLACK);//màu chữ cho score và mạng
        g.drawString("Score: " + score, 10, 20);
        g.drawString("Lives: " + lives, 10, 40);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        // spawn
        spawnCounter++;//  đếm thời gian để tạo rác 
        if (spawnCounter >= spawnInterval) {//nếu thời gian lớn hơn điều kiện thì tạo
            boolean angled = (score >= 0); //nếu điểm lớn hơn ? thì thêm thay đổi góc độ rơi

            int binStartX = organicBin.getX();
            int binEndX = recyclableBin.getX() + recyclableBin.getWidth();

            TrashItem newItem = new TrashItem(panelWidth, panelHeight, binStartX, binEndX, angled, score);
            items.add(newItem);
            spawnCounter = 0;
        }

        // tăng độ khó an toàn
        if (fallSpeedTimer > 0 && fallSpeedTimer % 1500 == 0 && spawnInterval > 30) {
            spawnInterval -= 10;//giảm thời gian sinh giữa 2 lần tạo rác
        }

        // fall
        for (TrashItem item : items) item.fall();

        // tăng tốc
        fallSpeedTimer++;
        if (fallSpeedTimer > 500) {
            for (TrashItem item : items) item.increaseSpeed();
            fallSpeedTimer = 0;
        }

        // va chạm
        checkCollisions();

        // rác rơi ngoài: bom thì bỏ qua, khác thì trừ mạng 1 lần khi rơi ra ngoài
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

    private void checkCollisions() {
        java.util.List<TrashItem> toRemove = new java.util.ArrayList<>();

        for (TrashItem item : items) {
            // khi rác chạm "mức" thùng
            if (item.getY() + 20 >= organicBin.getY()) {
                // if bomb
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
                    score++; matched = true;
                } else if (checkBinCollision(item, inorganicBin) && "inorganic".equals(item.getType())) {
                    score++; matched = true;
                } else if (checkBinCollision(item, recyclableBin) && "recyclable".equals(item.getType())) {
                    score++; matched = true;
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
    
    private void updateScoreInDB(int user_id, int score) {
        String sql = "INSERT INTO scores (user_id,score,play_time) VALUES (?,?,CURRENT_TIMESTAMP)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, user_id);
            stmt.setInt(2, score);
            stmt.executeUpdate();
            System.out.println(stmt.toString());
            System.out.println("✅ Điểm số đã được lưu cho " + user_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void endGame() {
        gameOver = true;
        if (timer != null) timer.stop();
        // cập nhật điểm số vào DB
        updateScoreInDB(parent.getCurrentUser(), score);
        SwingUtilities.invokeLater(() -> parent.showControlPanel(score));
    }

    // move toàn block thùng sao cho giữ khoảng cách cố định và không ra ngoài màn
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
