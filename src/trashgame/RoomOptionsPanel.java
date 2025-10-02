
package trashgame;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

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
            }
        });

        joinButton.addActionListener(e -> {
            String roomID = JOptionPane.showInputDialog(this, "Nhập RoomID cần tham gia:");
            if (roomID != null && !roomID.trim().isEmpty()) {
                client.sendMessage("JOIN_ROOM:" + roomID + ":" + userId + ":" + username);
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

    // cập nhật hoặc thêm dòng cho user
    public void updatePlayerTable(String user, int score) {
        boolean found = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(user)) {
                tableModel.setValueAt(score, i, 1);
                found = true;
                break;
            }
        }
        if (!found) {
            tableModel.addRow(new Object[]{user, score});
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

    public void onRoomPlayerList(java.util.List<String[]> players) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setRowCount(0); // clear
            for (String[] p : players) {
                tableModel.addRow(new Object[]{p[0], p[1]});
            }
        });
    }
    
     //THÊM: Nhận lệnh bắt đầu game
    public void onStartGame() {
        SwingUtilities.invokeLater(() -> {
            parent.startMultiplayerGame();  // Giả định MainFrame có phương thức này để chuyển sang GamePanel
        });
    }
}
