
package trashgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
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
    private int panelHeight = 650;
    private int fallSpeedTimer = 0;
    private JTable scoreTable;
    private DefaultTableModel scoreModel;
    
    private String username;
    private int lastSentScore = 0;
    
    private int binSpacing = 10;
    
    // Background image
    private BufferedImage backgroundImage;
    
    // THÊM: Kiểm tra có phải chơi trong phòng không
    private boolean isMultiplayerMode = false;
    
    // THÊM: Panel chứa bảng điểm
    private JPanel scorePanel;
    
    private static final String[] BACKGROUND_PATHS = {
        "/resources/background_gamepanel_1.png",
        "/resources/background_gamepanel_2.png",
        "/resources/background_gamepanel_3.png",
        "/resources/background_gamepanel_4.png",
        "/resources/background_gamepanel_5.png"
    };

    public GamePanel(MainFrame frame) {
        this.parent = frame;
        this.username = parent.getCurrentUsername();
        
        // THÊM: Kiểm tra có Client không (multiplayer mode)
        this.isMultiplayerMode = (parent.getClient() != null);
        
        setPreferredSize(new Dimension(panelWidth, panelHeight));
        setLayout(null);  // THAY ĐỔI: Dùng absolute positioning
        
        loadRandomBackground();
        
        setFocusable(true);
        addKeyListener(new KeyHandler());

        int startX = 100;
        organicBin = new Bin(startX, panelHeight - 90, "organic");
        inorganicBin = new Bin(organicBin.getX() + organicBin.getWidth() + binSpacing, panelHeight - 90, "inorganic");
        recyclableBin = new Bin(inorganicBin.getX() + inorganicBin.getWidth() + binSpacing, panelHeight - 90, "recyclable");

        // CHỈ khởi tạo bảng điểm nếu đang chơi multiplayer
        if (isMultiplayerMode) {
            initScoreTable();
            parent.getClient().addScoreListener(this);
        }
        
        timer = new Timer(20, this);
        timer.start();
    }
    
    private void loadRandomBackground() {
        int randomIndex = rand.nextInt(BACKGROUND_PATHS.length);
        String selectedPath = BACKGROUND_PATHS[randomIndex];
        System.out.println("🎲 Random background: #" + (randomIndex + 1) + " - " + selectedPath);
        loadBackgroundImage(selectedPath);
    }
    
    private void loadBackgroundImage(String imagePath) {
        try {
            System.out.println("🖼️ Đang load background: " + imagePath);
            
            java.net.URL imageURL = getClass().getResource(imagePath);
            if (imageURL == null) {
                System.err.println("❌ Không tìm thấy file background: " + imagePath);
                tryLoadFallbackBackground(imagePath);
                return;
            }
            
            BufferedImage originalImage = ImageIO.read(imageURL);
            
            if (originalImage != null) {
                backgroundImage = new BufferedImage(
                    originalImage.getWidth(),
                    originalImage.getHeight(),
                    BufferedImage.TYPE_INT_RGB
                );
                
                Graphics2D g = backgroundImage.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                   RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.drawImage(originalImage, 0, 0, null);
                g.dispose();
                
                System.out.println("✅ Load background thành công!");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi load background: " + e.getMessage());
            tryLoadFallbackBackground(imagePath);
        }
    }
    
    private void tryLoadFallbackBackground(String failedPath) {
        System.out.println("🔄 Thử load background dự phòng...");
        
        for (String path : BACKGROUND_PATHS) {
            if (!path.equals(failedPath)) {
                try {
                    java.net.URL imageURL = getClass().getResource(path);
                    if (imageURL != null) {
                        BufferedImage originalImage = ImageIO.read(imageURL);
                        if (originalImage != null) {
                            backgroundImage = new BufferedImage(
                                originalImage.getWidth(),
                                originalImage.getHeight(),
                                BufferedImage.TYPE_INT_RGB
                            );
                            
                            Graphics2D g = backgroundImage.createGraphics();
                            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                               RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                            g.drawImage(originalImage, 0, 0, null);
                            g.dispose();
                            
                            System.out.println("✅ Load background dự phòng thành công: " + path);
                            return;
                        }
                    }
                } catch (Exception e) {
                    // Tiếp tục thử
                }
            }
        }
        
        createDefaultBackground();
    }
    
    private void createDefaultBackground() {
        backgroundImage = new BufferedImage(panelWidth, panelHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = backgroundImage.createGraphics();
        
        GradientPaint gradient = new GradientPaint(
            0, 0, new Color(135, 206, 235),
            0, panelHeight, new Color(34, 139, 34)
        );
        g.setPaint(gradient);
        g.fillRect(0, 0, panelWidth, panelHeight);
        
        g.dispose();
        System.out.println("✅ Tạo background gradient mặc định thành công");
    }
    
    /**
     * NÂNG CẤP: Khởi tạo bảng điểm đẹp, trong suốt, góc phải
     */
    private void initScoreTable() {
        // Tạo panel chứa
        scorePanel = new JPanel();
        scorePanel.setLayout(new BorderLayout(5, 5));
        scorePanel.setOpaque(false);  // Trong suốt
        
        // Vị trí: góc phải, cách lề 10px
        int scorePanelWidth = 200;
        int scorePanelHeight = 250;
        int scorePanelX = panelWidth - scorePanelWidth - 20;
        int scorePanelY = 10;
        scorePanel.setBounds(scorePanelX, scorePanelY, scorePanelWidth, scorePanelHeight);
        
        // Tiêu đề
        JLabel scoreLabel = new JLabel("🏆 Score Board", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        scoreLabel.setForeground(Color.WHITE);
        scoreLabel.setOpaque(true);
        scoreLabel.setBackground(new Color(0, 0, 0, 150));  // Đen trong suốt
        scoreLabel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        
        // Model cho bảng
        scoreModel = new DefaultTableModel(new Object[]{"Tên", "Điểm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Tạo table
        scoreTable = new JTable(scoreModel);
        
        // STYLE TABLE: Trong suốt, màu đẹp
        styleScoreTable();
        
        // ScrollPane trong suốt
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        // Thêm vào panel
        scorePanel.add(scoreLabel, BorderLayout.NORTH);
        scorePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Thêm vào GamePanel
        add(scorePanel);
        
        System.out.println("✅ Khởi tạo bảng điểm multiplayer");
    }
    
    /**
     * Style bảng điểm đẹp
     */
    private void styleScoreTable() {
        // Font
        scoreTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        scoreTable.setRowHeight(30);
        
        // Màu nền trong suốt
        scoreTable.setOpaque(false);
        scoreTable.setBackground(new Color(0, 0, 0, 120));  // Đen trong suốt
        scoreTable.setForeground(Color.WHITE);
        
        // Header
        scoreTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        scoreTable.getTableHeader().setBackground(new Color(0, 0, 0, 180));
        scoreTable.getTableHeader().setForeground(Color.YELLOW);
        scoreTable.getTableHeader().setOpaque(false);
        
        // Grid
        scoreTable.setShowGrid(true);
        scoreTable.setGridColor(new Color(255, 255, 255, 50));  // Grid nhạt
        
        // Renderer cho cells
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Background trong suốt
                c.setBackground(new Color(0, 0, 0, 100));
                c.setForeground(Color.WHITE);
                
                // Highlight row được chọn
                if (isSelected) {
                    c.setBackground(new Color(255, 255, 0, 100));  // Vàng trong suốt
                    c.setForeground(Color.BLACK);
                }
                
                setHorizontalAlignment(SwingConstants.CENTER);
                ((JLabel) c).setOpaque(true);
                
                return c;
            }
        };
        
        // Áp dụng renderer cho cả 2 cột
        scoreTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        scoreTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        
        // Điều chỉnh độ rộng cột
        scoreTable.getColumnModel().getColumn(0).setPreferredWidth(120);  // Cột Tên
        scoreTable.getColumnModel().getColumn(1).setPreferredWidth(80);   // Cột Điểm
    }
    
    private void sendScoreToServer() {
        if (parent.getClient() != null && score > lastSentScore) {
            parent.getClient().sendMessage("SCORE_REALTIME:" + score);
            lastSentScore = score;
            System.out.println("📤 Gửi điểm số thời gian thực: " + score);
        }
    }
    
    @Override
    public void onScoreUpdate(String otherUsername, int otherScore) {
        if (!isMultiplayerMode) return;  // CHỈ xử lý nếu multiplayer
        
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
        if (!isMultiplayerMode) return;
        
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
        if (!isMultiplayerMode) return;
        
        if (!username.equals(this.username)) {
            scoreModel.addRow(new Object[]{username, 0});
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // BƯỚC 1: Vẽ background
        if (backgroundImage != null) {
            g2.drawImage(backgroundImage, 0, 0, panelWidth, panelHeight, null);
        } else {
            g2.setColor(new Color(135, 206, 235));
            g2.fillRect(0, 0, panelWidth, panelHeight);
        }
        
        // BƯỚC 2: Vẽ game over (nếu có)
        if (gameOver) {
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, panelWidth, panelHeight);
            
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 48));
            String gameOverText = "Game Over!";
            FontMetrics fm = g2.getFontMetrics();
            int textWidth = fm.stringWidth(gameOverText);
            g2.drawString(gameOverText, (panelWidth - textWidth) / 2, panelHeight / 2 - 30);
            
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 32));
            String scoreText = "Score: " + score;
            textWidth = fm.stringWidth(scoreText);
            g2.drawString(scoreText, (panelWidth - textWidth) / 2, panelHeight / 2 + 20);
            
            return;
        }

        // BƯỚC 3: Vẽ các thùng rác
        organicBin.draw(g);
        inorganicBin.draw(g);
        recyclableBin.draw(g);

        // BƯỚC 4: Vẽ các rác đang rơi
        for (TrashItem item : items) {
            item.draw(g);
        }

        // BƯỚC 5: Vẽ UI (score, lives)
        drawUI(g2);
    }
    
    private void drawUI(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                           RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        int uiPadding = 10;
        int uiX = uiPadding;
        int uiY = uiPadding;
        int uiWidth = 180;
        int uiHeight = 70;
        
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRoundRect(uiX, uiY, uiWidth, uiHeight, 15, 15);
        
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(uiX, uiY, uiWidth, uiHeight, 15, 15);
        
        g2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        String scoreText = "Score: " + score;
        drawTextWithShadow(g2, scoreText, uiX + 15, uiY + 30);
        
        String livesText = "Lives: " + lives;
        drawTextWithShadow(g2, livesText, uiX + 15, uiY + 55);
    }
    
    private void drawTextWithShadow(Graphics2D g2, String text, int x, int y) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.drawString(text, x + 2, y + 2);
        
        g2.setColor(Color.WHITE);
        g2.drawString(text, x, y);
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
                    sendScoreToServer();
                } else if (checkBinCollision(item, inorganicBin) && "inorganic".equals(item.getType())) {
                    score++; 
                    matched = true;
                    sendScoreToServer();
                } else if (checkBinCollision(item, recyclableBin) && "recyclable".equals(item.getType())) {
                    score++; 
                    matched = true;
                    sendScoreToServer();
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
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}

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