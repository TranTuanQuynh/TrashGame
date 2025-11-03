
package trashgame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class RoomOptionsPanel extends JPanel implements ScoreListener {
    private JButton createButton;
    private JButton joinButton;
    private JButton readyButton;
    private JButton backButton;  // THÊM: Nút Back
    private JTable playerTable;
    private DefaultTableModel tableModel;

    private Client client;
    private int userId;
    private String username;
    private MainFrame parent;
    private boolean isReady = false;

    public RoomOptionsPanel(MainFrame frame, Client client, int userId, String username) {
        this.parent = frame;
        this.client = client;
        this.userId = userId;
        this.username = username;
        
        initUI();
        
        if (this.client != null) {
            this.client.addScoreListener(this);
        }
        
        attachNetworkedActions();
        loadInitialRoom();
    }

    private void initUI() {
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Panel trên: Title + Back button
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Panel giữa: Bảng người chơi
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);
        
        // Panel dưới: Action buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        
        // Nút Back ở góc trái
        backButton = createIconButton("← Back", new Color(100, 100, 100));
        backButton.setPreferredSize(new Dimension(100, 40));
        
        // Title ở giữa
        JLabel titleLabel = new JLabel("🏠 Waiting Room", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 255, 153));
        
        // Info label
        JLabel infoLabel = new JLabel("👤 " + username, SwingConstants.RIGHT);
        infoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        infoLabel.setForeground(new Color(200, 200, 200));
        
        panel.add(backButton, BorderLayout.WEST);
        panel.add(titleLabel, BorderLayout.CENTER);
        panel.add(infoLabel, BorderLayout.EAST);
        
        return panel;
    }
    
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        
        // Label cho bảng
        JLabel tableLabel = new JLabel("📋 Player List", SwingConstants.LEFT);
        tableLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        tableLabel.setForeground(Color.WHITE);
        tableLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        
        // Table model
        tableModel = new DefaultTableModel(new Object[]{"👤 Player", "⭐ Score"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Table với style
        playerTable = new JTable(tableModel);
        stylePlayerTable();
        
        // Scroll pane trong suốt
        JScrollPane scrollPane = new JScrollPane(playerTable);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 153), 2));
        scrollPane.setBackground(new Color(40, 40, 40));
        
        panel.add(tableLabel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private void stylePlayerTable() {
        // Font và kích thước
        playerTable.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        playerTable.setRowHeight(40);
        
        // Màu sắc
        playerTable.setBackground(new Color(50, 50, 50));
        playerTable.setForeground(Color.WHITE);
        playerTable.setSelectionBackground(new Color(0, 200, 100));
        playerTable.setSelectionForeground(Color.BLACK);
        
        // Header
        playerTable.getTableHeader().setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        playerTable.getTableHeader().setBackground(new Color(40, 40, 40));
        playerTable.getTableHeader().setForeground(new Color(0, 255, 153));
        playerTable.getTableHeader().setPreferredSize(new Dimension(0, 45));
        
        // Grid
        playerTable.setShowGrid(true);
        playerTable.setGridColor(new Color(80, 80, 80));
        
        // Cell renderer với icon và màu
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                setHorizontalAlignment(SwingConstants.CENTER);
                
                // Background xen kẽ
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(50, 50, 50));
                    } else {
                        c.setBackground(new Color(60, 60, 60));
                    }
                }
                
                if (column == 0 && value.equals(username)) {
                    setFont(getFont().deriveFont(Font.BOLD));
                    if (!isSelected) {
                        c.setBackground(new Color(70, 70, 100));
                    }
                }
                
                return c;
            }
        };
        
        playerTable.setDefaultRenderer(Object.class, renderer);
        
        playerTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        playerTable.getColumnModel().getColumn(1).setPreferredWidth(100);
    }
    
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        JPanel row1 = new JPanel(new GridLayout(1, 2, 10, 0));
        row1.setOpaque(false);
        
        createButton = createStyledButton("🏗️ Create Room", new Color(0, 150, 255));
        joinButton = createStyledButton("🚪 Join Room", new Color(0, 200, 100));
        
        row1.add(createButton);
        row1.add(joinButton);
        
        panel.add(row1, gbc);
        
        gbc.gridy = 1;
        readyButton = createStyledButton("✓ Ready", new Color(255, 165, 0));
        readyButton.setPreferredSize(new Dimension(0, 50));
        panel.add(readyButton, gbc);
        
        return panel;
    }
    
    private JButton createStyledButton(String text, Color accentColor) {
        JButton button = new JButton(text);
        
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(60, 60, 60));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 45));
        
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
       
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(accentColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentColor.brighter(), 3),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(60, 60, 60));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentColor, 2),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
                ));
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(accentColor.darker());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(accentColor);
            }
        });
        
        return button;
    }
    
    private JButton createIconButton(String text, Color color) {
        JButton button = new JButton(text);
        
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hiệu ứng hover đơn giản
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }

    private void attachNetworkedActions() {
        
        backButton.addActionListener(e -> {
            // Quay lại ModeSelectionPanel
            parent.showModeSelection();
            System.out.println("🔙 Back ModeSelection");
        });
        
        createButton.addActionListener(e -> {
            String roomID = JOptionPane.showInputDialog(
                this, 
                "Nhập mã phòng muốn tạo (ví dụ: 12345):",
                "Tạo phòng",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (roomID != null && !roomID.trim().isEmpty()) {
                client.sendMessage("CREATE_ROOM:" + roomID + ":" + userId + ":" + username);
                System.out.println("✅ Người chơi " + username + " tạo phòng " + roomID);
                parent.setCurrentRoomID(roomID);
                
                showNotification("Đã tạo phòng: " + roomID, new Color(0, 200, 100));
            }
        });

        joinButton.addActionListener(e -> {
            String roomID = JOptionPane.showInputDialog(
                this, 
                "Nhập mã phòng cần tham gia:",
                "Tham gia phòng",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (roomID != null && !roomID.trim().isEmpty()) {
                client.sendMessage("JOIN_ROOM:" + roomID + ":" + userId + ":" + username);
                System.out.println("✅ Người chơi " + username + " tham gia phòng " + roomID);
                parent.setCurrentRoomID(roomID);
                
                showNotification("Đang tham gia phòng: " + roomID, new Color(0, 150, 255));
            }
        });
        
        readyButton.addActionListener(e -> {
            if (!isReady) {
                client.sendMessage("READY:" + username);
                readyButton.setText("✓ Ready!");
                readyButton.setBackground(new Color(0, 200, 100));
                isReady = true;
                
                showNotification("You are ready!", new Color(0, 200, 100));
            } else {
                client.sendMessage("UNREADY:" + username);
                readyButton.setText("✓ Ready");
                readyButton.setBackground(new Color(60, 60, 60));
                isReady = false;
                
                showNotification("You are waiting", new Color(255, 165, 0));
            }
        });
    }
    
    private void showNotification(String message, Color color) {
        JLabel notif = new JLabel(message, SwingConstants.CENTER);
        notif.setFont(new Font("Segoe UI Emoji", Font.BOLD, 14));
        notif.setForeground(Color.WHITE);
        notif.setOpaque(true);
        notif.setBackground(color);
        notif.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        final JWindow popup = new JWindow();
        popup.add(notif);
        popup.pack();
        
        Point parentLocation = getLocationOnScreen();
        int x = parentLocation.x + getWidth() - popup.getWidth() - 20;
        int y = parentLocation.y + 20;
        popup.setLocation(x, y);
        
        popup.setVisible(true);
        
        Timer timer = new Timer(2000, ev -> popup.dispose());
        timer.setRepeats(false);
        timer.start();
    }

    private void loadInitialRoom() {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{username, 0});
        System.out.println("📊 Load phòng ban đầu: Chỉ có " + username);
    }

    public void loadPreviousRoom(String roomID) {
        if (client != null) {
            client.sendMessage("REFRESH_ROOM:" + roomID);
            parent.setCurrentRoomID(roomID);
            System.out.println("🔄 Refresh phòng cũ: " + roomID);
        }
    }

    public void updatePlayerTable(String user, int score) {
        boolean found = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(user)) {
                tableModel.setValueAt(score, i, 1);
                found = true;
                System.out.println("🔄 Update điểm cho " + user + ": " + score);
                break;
            }
        }
        if (!found) {
            tableModel.addRow(new Object[]{user, score});
            System.out.println("➕ Thêm người chơi mới: " + user + " (" + score + ")");
        }
    }

    @Override
    public void onScoreUpdate(String username, int score) {
        updatePlayerTable(username, score);
    }

    @Override
    public void onUserJoined(String username) {
        updatePlayerTable(username, 0);
        showNotification(username + " đã tham gia!", new Color(0, 150, 255));
    }

    public void onRoomPlayerList(List<String[]> players) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("🧹 Clear bảng cũ: " + tableModel.getRowCount() + " rows");
            tableModel.setRowCount(0);
            for (String[] p : players) {
                updatePlayerTable(p[0], Integer.parseInt(p[1]));
            }
            System.out.println("📊 Load mới: " + players.size() + " người");
        });
    }
    
    public void onStartGame() {
        SwingUtilities.invokeLater(() -> {
            parent.startMultiplayerGame();
        });
    }
}