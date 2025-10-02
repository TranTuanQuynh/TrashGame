
package trashgame;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ModeSelectionPanel extends JPanel {
    private JButton singleButton = new JButton("Chơi cá nhân");
    private JButton roomButton = new JButton("Chơi theo phòng");

    public ModeSelectionPanel(MainFrame frame) {
        setLayout(new BorderLayout());

        // ====== Bảng leaderboard ======
        String[] columnNames = {"Username", "Score"};
        List<String[]> leaderboard = DBConnection.getLeaderboard(); // lấy top từ DB
        String[][] data = leaderboard.toArray(new String[0][]);

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);

        JLabel title = new JLabel("🏆 Bảng xếp hạng", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel leaderboardPanel = new JPanel(new BorderLayout());
        leaderboardPanel.add(title, BorderLayout.NORTH);
        leaderboardPanel.add(scrollPane, BorderLayout.CENTER);

        // ====== Panel chứa nút ======
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(singleButton);
        buttonPanel.add(roomButton);

        // ====== Add vào ModeSelectionPanel ======
        add(leaderboardPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // ====== Gắn sự kiện nút ======
        singleButton.addActionListener(e -> frame.startSinglePlayer());
        roomButton.addActionListener(e -> frame.showRoomOptions());
    }
}

