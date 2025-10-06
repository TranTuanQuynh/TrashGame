
package trashgame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoomOptionsPanel extends JPanel implements ScoreListener {
    private JButton createButton = new JButton("Tạo phòng");
    private JButton joinButton = new JButton("Tham gia phòng");
    private JButton readyButton = new JButton("Ready");
    private JTable playerTable;
    private DefaultTableModel tableModel;

    private Client client;
    private int userId;
    private String username;
    private MainFrame parent;
    private boolean isReady = false;

    // ----- Constructor nâng cao (có client) -----
    public RoomOptionsPanel(MainFrame frame, Client client, int userId, String username) {
        this.parent = frame;
        this.client = client;
        this.userId = userId;
        this.username = username;
        initUI();

        // đăng ký listener để nhận cập nhật realtime
        this.client.addScoreListener(this);

        attachNetworkedActions();
        
        loadInitialRoom();
    }

    // --- khởi tạo UI ---
    private void initUI() {
        setLayout(new BorderLayout());

        // nút
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createButton);
        buttonPanel.add(joinButton);
        buttonPanel.add(readyButton);
        add(buttonPanel, BorderLayout.NORTH);

        // bảng người chơi trong phòng
        tableModel = new DefaultTableModel(new Object[]{"Người chơi", "Điểm"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        playerTable = new JTable(tableModel);
        add(new JScrollPane(playerTable), BorderLayout.CENTER);
    }

    // --- hành vi khi có client (kết nối server thật) ---
    private void attachNetworkedActions() {
        createButton.addActionListener(e -> {
            String roomID = JOptionPane.showInputDialog(this, "Nhập mã phòng muốn tạo (ví dụ 12345):");
            if (roomID != null && !roomID.trim().isEmpty()) {
                // gửi yêu cầu tạo phòng
                client.sendMessage("CREATE_ROOM:" + roomID + ":" + userId + ":" + username);
                System.out.println ("Người chơi "+ username+ "tạo phòng "+ roomID);  // SỬA: In roomID thay vì userId
                parent.setCurrentRoomID(roomID);
            }
        });

        joinButton.addActionListener(e -> {
            String roomID = JOptionPane.showInputDialog(this, "Nhập RoomID cần tham gia:");
            if (roomID != null && !roomID.trim().isEmpty()) {
                client.sendMessage("JOIN_ROOM:" + roomID + ":" + userId + ":" + username);
                System.out.println ("Người chơi "+ username+ "muốn tham gia phòng "+ roomID);  // SỬA: In roomID thay vì userId
                parent.setCurrentRoomID(roomID);
            }
        });
        
        // THÊM: Xử lý nút Ready
        readyButton.addActionListener(e -> {
            if (!isReady) {
                client.sendMessage("READY:" + username);  // Gửi READY:username
                readyButton.setText("Ready!");  // Thay đổi text
                isReady = true;
            } else {
                // Có thể thêm unready nếu cần
                client.sendMessage("UNREADY:" + username);
                readyButton.setText("Ready");
                isReady = false;
            }
        });
    }

    private void loadInitialRoom() {
        tableModel.setRowCount(0);  // Clear bảng
        tableModel.addRow(new Object[]{username, 0});  // Thêm chính mình
        System.out.println("📊 Load phòng ban đầu: Chỉ có " + username);  // Log debug
    }

    // SỬA: Load phòng cũ - Gửi REFRESH_ROOM thay vì JOIN_ROOM (không INSERT duplicate)
    public void loadPreviousRoom(String roomID) {
        if (client != null) {
            client.sendMessage("REFRESH_ROOM:" + roomID);  // SỬA: Gửi REFRESH_ROOM (không gửi userId/username để tránh insert)
            parent.setCurrentRoomID(roomID);
            System.out.println("🔄 Refresh phòng cũ: " + roomID + " (không insert duplicate)");
        }
    }

    // THÊM: Load phòng rỗng ban đầu
    private void loadEmptyRoom() {
        tableModel.setRowCount(0);  // Clear bảng
        tableModel.addRow(new Object[]{username, 0});  // Thêm chính mình
    }

    // SỬA: updatePlayerTable - Check duplicate trước khi add
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

    // ----- ScoreListener callbacks -----
    @Override
    public void onScoreUpdate(String username, int score) {
        updatePlayerTable(username, score);
    }

    @Override
    public void onUserJoined(String username) {
        updatePlayerTable(username, 0);
    }

    // SỬA: onRoomPlayerList - Clear bảng trước khi load (tránh duplicate)
    public void onRoomPlayerList(List<String[]> players) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("🧹 Clear bảng cũ: " + tableModel.getRowCount() + " rows");  // Log debug
            tableModel.setRowCount(0);  // SỬA: Clear trước khi load để tránh lặp
            for (String[] p : players) {
                updatePlayerTable(p[0], Integer.parseInt(p[1]));  // Sử dụng updatePlayerTable để check duplicate
            }
            System.out.println("📊 Load mới: " + players.size() + " người");  // Log debug
        });
    }
    
     //THÊM: Nhận lệnh bắt đầu game
    public void onStartGame() {
        SwingUtilities.invokeLater(() -> {
            parent.startMultiplayerGame();  // Giả định MainFrame có phương thức này để chuyển sang GamePanel
        });
    }
}