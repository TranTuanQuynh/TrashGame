package trashgame;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ModeSelectionPanel extends JPanel {
    private MainFrame parent;
    private JLabel leaderboardLabel;  // Label hiển thị leaderboard
    private Client client;  // Lấy Client từ parent

    public ModeSelectionPanel(MainFrame frame) {
        this.parent = frame;
        this.client = parent.getClient();  // Lấy Client từ MainFrame

        setLayout(new BorderLayout());

        // SỬA: Tạo label cho leaderboard
        leaderboardLabel = new JLabel("Đang load bảng xếp hạng...", JLabel.CENTER);
        add(leaderboardLabel, BorderLayout.NORTH);

        // Nút chơi
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton singlePlayer = new JButton("Chơi đơn");
        singlePlayer.addActionListener(e -> parent.startSinglePlayer());
        buttonPanel.add(singlePlayer);

        JButton multiplayer = new JButton("Chơi multiplayer");
        multiplayer.addActionListener(e -> parent.showRoomOptions());
        buttonPanel.add(multiplayer);

        add(buttonPanel, BorderLayout.SOUTH);

        // SỬA: Load leaderboard qua server (không gọi DBConnection)
        loadLeaderboard();
    }

    // THÊM: Load leaderboard qua server
    private void loadLeaderboard() {
        if (client != null) {
            // Set callback để nhận phản hồi từ server
            client.setLeaderboardCallback(new Client.LeaderboardCallback() {
                @Override
                public void onLeaderboardReceived(List<String[]> leaderboard) {
                    SwingUtilities.invokeLater(() -> {
                        if (leaderboard.isEmpty()) {
                            leaderboardLabel.setText("Không có dữ liệu bảng xếp hạng");
                        } else {
                            StringBuilder sb = new StringBuilder("<html><body>");
                            sb.append("<h3>Bảng xếp hạng:</h3>");
                            for (int i = 0; i < leaderboard.size(); i++) {
                                String[] row = leaderboard.get(i);
                                sb.append((i + 1)).append(". ").append(row[0]).append(": ").append(row[1]).append("<br>");
                            }
                            sb.append("</body></html>");
                            leaderboardLabel.setText(sb.toString());
                        }
                    });
                }

                @Override
                public void onLeaderboardFail(String error) {
                    SwingUtilities.invokeLater(() -> {
                        leaderboardLabel.setText("Lỗi load bảng xếp hạng: " + error);
                    });
                }
            });
            client.sendLeaderboardRequest();  // Gửi "LEADERBOARD" đến server
        } else {
            // Fallback nếu không có client (offline)
            leaderboardLabel.setText("Không kết nối server, không load được bảng xếp hạng");
        }
    }
}