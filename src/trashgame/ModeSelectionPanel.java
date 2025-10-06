//package trashgame;
//
//import javax.swing.*;
//import java.awt.*;
//import java.util.List;
//
//public class ModeSelectionPanel extends JPanel {
//    private MainFrame parent;
//    private JLabel leaderboardLabel;  // Label hiển thị leaderboard
//    private Client client;  // Lấy Client từ parent
//
//    public ModeSelectionPanel(MainFrame frame) {
//        this.parent = frame;
//        this.client = parent.getClient();  // Lấy Client từ MainFrame
//
//        setLayout(new BorderLayout());
//
//        // SỬA: Tạo label cho leaderboard
//        leaderboardLabel = new JLabel("Đang load bảng xếp hạng...", JLabel.CENTER);
//        add(leaderboardLabel, BorderLayout.NORTH);
//
//        // Nút chơi
//        JPanel buttonPanel = new JPanel(new FlowLayout());
//        JButton singlePlayer = new JButton("Chơi đơn");
//        singlePlayer.addActionListener(e -> parent.startSinglePlayer());
//        buttonPanel.add(singlePlayer);
//
//        JButton multiplayer = new JButton("Chơi multiplayer");
//        multiplayer.addActionListener(e -> parent.showRoomOptions());
//        buttonPanel.add(multiplayer);
//
//        add(buttonPanel, BorderLayout.SOUTH);
//
//        // SỬA: Load leaderboard qua server (không gọi DBConnection)
//        loadLeaderboard();
//    }
//
//    // THÊM: Load leaderboard qua server
//    private void loadLeaderboard() {
//        if (client != null) {
//            // Set callback để nhận phản hồi từ server
//            client.setLeaderboardCallback(new Client.LeaderboardCallback() {
//                @Override
//                public void onLeaderboardReceived(List<String[]> leaderboard) {
//                    SwingUtilities.invokeLater(() -> {
//                        if (leaderboard.isEmpty()) {
//                            leaderboardLabel.setText("Không có dữ liệu bảng xếp hạng");
//                        } else {
//                            StringBuilder sb = new StringBuilder("<html><body>");
//                            sb.append("<h3>Bảng xếp hạng:</h3>");
//                            for (int i = 0; i < leaderboard.size(); i++) {
//                                String[] row = leaderboard.get(i);
//                                sb.append((i + 1)).append(". ").append(row[0]).append(": ").append(row[1]).append("<br>");
//                            }
//                            sb.append("</body></html>");
//                            leaderboardLabel.setText(sb.toString());
//                        }
//                    });
//                }
//
//                @Override
//                public void onLeaderboardFail(String error) {
//                    SwingUtilities.invokeLater(() -> {
//                        leaderboardLabel.setText("Lỗi load bảng xếp hạng: " + error);
//                    });
//                }
//            });
//            client.sendLeaderboardRequest();  // Gửi "LEADERBOARD" đến server
//        } else {
//            // Fallback nếu không có client (offline)
//            leaderboardLabel.setText("Không kết nối server, không load được bảng xếp hạng");
//        }
//    }
//}
package trashgame;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ModeSelectionPanel extends JPanel {
    private MainFrame parent;
    private JTable leaderboardTable;
    private DefaultTableModel tableModel;
    private Client client;
    
    // Animation và effects
    private Timer animationTimer;
    private float titleGlow = 0;
    private boolean titleGlowIncreasing = true;
    private List<Particle> particles = new ArrayList<Particle>();
    private Random random = new Random();

    public ModeSelectionPanel(MainFrame frame) {
        this.parent = frame;
        this.client = parent.getClient();

        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // ================== TIÊU ĐỀ VỚI HIỆU ỨNG ==================
        JPanel titlePanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Vẽ glow effect cho title
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                
                RadialGradientPaint gradient = new RadialGradientPaint(
                    centerX, centerY, 150,
                    new float[]{0f, 1f},
                    new Color[]{
                        new Color(255, 215, 0, (int)(50 + titleGlow * 30)),
                        new Color(255, 215, 0, 0)
                    }
                );
                g2d.setPaint(gradient);
                g2d.fillOval(centerX - 150, centerY - 50, 300, 100);
            }
        };
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BorderLayout());
        
        JLabel title = new JLabel("*** LEADERBOARD ***", JLabel.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(new Color(255, 215, 0));
        titlePanel.add(title);
        
        add(titlePanel, BorderLayout.NORTH);

        // ================== PANEL GIỮA: BẢNG + DECORATIONS ==================
        JPanel centerPanel = new JPanel(new BorderLayout(15, 15));
        centerPanel.setOpaque(false);
        
        // Container với glassmorphism
        JPanel glassContainer = new JPanel(new BorderLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Glassmorphism background
                g2d.setColor(new Color(255, 255, 255, 15));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                
                // Border gradient
                GradientPaint border = new GradientPaint(
                    0, 0, new Color(255, 215, 0, 80),
                    getWidth(), getHeight(), new Color(138, 43, 226, 80)
                );
                g2d.setPaint(border);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 25, 25);
            }
        };
        glassContainer.setOpaque(false);
        glassContainer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // ================== BẢNG LEADERBOARD ==================
        String[] columnNames = {"HANG", "NGUOI CHOI", "DIEM"};
        tableModel = new DefaultTableModel(columnNames, 0);
        leaderboardTable = new JTable(tableModel);
        leaderboardTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        leaderboardTable.setRowHeight(40);
        leaderboardTable.setEnabled(false);
        leaderboardTable.setOpaque(false);
        leaderboardTable.setShowGrid(false);
        leaderboardTable.setIntercellSpacing(new Dimension(0, 5));

        // Custom header
        JTableHeader header = leaderboardTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 17));
        header.setBackground(new Color(30, 30, 50, 200));
        header.setForeground(new Color(255, 215, 0));
        header.setPreferredSize(new Dimension(header.getWidth(), 45));
        
        // Custom renderer với hiệu ứng đẹp
        leaderboardTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
                
                label.setOpaque(true);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                
                // Màu nền gradient cho từng hàng
                if (row == 0) {
                    label.setBackground(new Color(255, 215, 0, 50)); // Vàng
                    label.setForeground(new Color(255, 223, 0));
                    label.setFont(new Font("Segoe UI", Font.BOLD, 18));
                } else if (row == 1) {
                    label.setBackground(new Color(192, 192, 192, 40)); // Bạc
                    label.setForeground(new Color(220, 220, 220));
                    label.setFont(new Font("Segoe UI", Font.BOLD, 17));
                } else if (row == 2) {
                    label.setBackground(new Color(205, 127, 50, 40)); // Đồng
                    label.setForeground(new Color(255, 160, 80));
                    label.setFont(new Font("Segoe UI", Font.BOLD, 16));
                } else {
                    if (row % 2 == 0) {
                        label.setBackground(new Color(40, 40, 70, 60));
                    } else {
                        label.setBackground(new Color(50, 50, 90, 40));
                    }
                    label.setForeground(new Color(200, 200, 220));
                    label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                }
                
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(leaderboardTable);
        scrollPane.setPreferredSize(new Dimension(550, 320));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        glassContainer.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(glassContainer, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // ================== PANEL NÚT PHÍA DƯỚI ==================
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        JButton singlePlayer = createModernButton("Single-Player", 
            new Color(50, 205, 50), new Color(34, 139, 34));
        singlePlayer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.startSinglePlayer();
            }
        });
        buttonPanel.add(singlePlayer);

        JButton multiplayer = createModernButton("Multiplayer", 
            new Color(30, 144, 255), new Color(0, 100, 200));
        multiplayer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                parent.showRoomOptions();
            }
        });
        buttonPanel.add(multiplayer);

        add(buttonPanel, BorderLayout.SOUTH);

        // ================== ANIMATION TIMER ==================
        initParticles();
        animationTimer = new Timer(50, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Title glow animation
                if (titleGlowIncreasing) {
                    titleGlow += 0.05f;
                    if (titleGlow >= 1) titleGlowIncreasing = false;
                } else {
                    titleGlow -= 0.05f;
                    if (titleGlow <= 0) titleGlowIncreasing = true;
                }
                
                // Update particles
                updateParticles();
                
                repaint();
            }
        });
        animationTimer.start();

        // ================== LOAD LEADERBOARD ==================
        loadLeaderboard();
    }

    // ====== TẠO NÚT HIỆN ĐẠI VỚI HIỆU ỨNG ======
    private JButton createModernButton(String text, final Color baseColor, final Color hoverColor) {
        JButton btn = new JButton(text) {
            private float hoverAmount = 0f;
            private Timer hoverTimer;
            
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        if (hoverTimer != null) hoverTimer.stop();
                        hoverTimer = new Timer(20, new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                hoverAmount = Math.min(1f, hoverAmount + 0.1f);
                                repaint();
                            }
                        });
                        hoverTimer.start();
                    }
                    
                    public void mouseExited(MouseEvent e) {
                        if (hoverTimer != null) hoverTimer.stop();
                        hoverTimer = new Timer(20, new ActionListener() {
                            public void actionPerformed(ActionEvent evt) {
                                hoverAmount = Math.max(0f, hoverAmount - 0.1f);
                                repaint();
                                if (hoverAmount <= 0) ((Timer)evt.getSource()).stop();
                            }
                        });
                        hoverTimer.start();
                    }
                });
            }
            
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Shadow
                g2.setColor(new Color(0, 0, 0, 100));
                g2.fillRoundRect(3, 5, getWidth() - 3, getHeight() - 3, 20, 20);
                
                // Button background với gradient
                Color currentColor = interpolateColor(baseColor, hoverColor, hoverAmount);
                GradientPaint gp = new GradientPaint(
                    0, 0, currentColor,
                    0, getHeight(), currentColor.darker()
                );
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 3, getHeight() - 5, 20, 20);
                
                // Glossy highlight
                g2.setColor(new Color(255, 255, 255, 60));
                g2.fillRoundRect(5, 5, getWidth() - 13, getHeight() / 2 - 5, 15, 15);
                
                // Border
                g2.setColor(new Color(255, 255, 255, 100));
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(1, 1, getWidth() - 5, getHeight() - 7, 20, 20);
                
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(220, 55));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return btn;
    }
    
    private Color interpolateColor(Color c1, Color c2, float ratio) {
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * ratio);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * ratio);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * ratio);
        return new Color(r, g, b);
    }

    // ====== PARTICLE SYSTEM ======
    private void initParticles() {
        for (int i = 0; i < 30; i++) {
            particles.add(new Particle());
        }
    }
    
    private void updateParticles() {
        for (int i = 0; i < particles.size(); i++) {
            Particle p = particles.get(i);
            p.update();
            if (p.y > getHeight()) {
                p.reset();
            }
        }
    }
    
    private class Particle {
        float x, y, speed, size;
        Color color;
        
        Particle() {
            reset();
        }
        
        void reset() {
            x = random.nextInt(getWidth() > 0 ? getWidth() : 800);
            y = -random.nextInt(200);
            speed = 0.5f + random.nextFloat() * 1.5f;
            size = 2 + random.nextFloat() * 3;
            
            int colorChoice = random.nextInt(3);
            if (colorChoice == 0) color = new Color(255, 215, 0, 100);
            else if (colorChoice == 1) color = new Color(138, 43, 226, 100);
            else color = new Color(255, 255, 255, 80);
        }
        
        void update() {
            y += speed;
        }
        
        void draw(Graphics2D g2d) {
            g2d.setColor(color);
            g2d.fill(new Ellipse2D.Float(x, y, size, size));
        }
    }

    // ====== LOAD LEADERBOARD ======
    private void loadLeaderboard() {
        if (client != null) {
            client.setLeaderboardCallback(new Client.LeaderboardCallback() {
                public void onLeaderboardReceived(List<String[]> leaderboard) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            tableModel.setRowCount(0);
                            if (leaderboard.isEmpty()) {
                                tableModel.addRow(new Object[]{"-", "Chua co du lieu", "-"});
                            } else {
                                for (int i = 0; i < leaderboard.size(); i++) {
                                    String[] row = leaderboard.get(i);
                                    String rank;
                                    if (i == 0) rank = "#1";
                                    else if (i == 1) rank = "#2";
                                    else if (i == 2) rank = "#3";
                                    else rank = "#" + (i + 1);
                                    tableModel.addRow(new Object[]{rank, row[0], row[1]});
                                }
                            }
                        }
                    });
                }

                public void onLeaderboardFail(String error) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            tableModel.setRowCount(0);
                            tableModel.addRow(new Object[]{"X", "Loi: " + error, "-"});
                        }
                    });
                }
            });
            client.sendLeaderboardRequest();
        } else {
            tableModel.addRow(new Object[]{"!", "Khong ket noi server", "-"});
        }
    }

    // ====== NỀN ANIMATED GRADIENT ======
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Multi-color gradient background
        GradientPaint gp = new GradientPaint(
            0, 0, new Color(20, 0, 40),
            getWidth(), getHeight(), new Color(60, 0, 100)
        );
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        
        // Draw particles
        for (int i = 0; i < particles.size(); i++) {
            particles.get(i).draw(g2d);
        }
    }
    
    // Cleanup timer khi panel bị remove
    public void removeNotify() {
        super.removeNotify();
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}