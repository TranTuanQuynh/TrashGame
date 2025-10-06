
package trashgame;

import java.awt.FlowLayout;
import javax.swing.*;
import java.awt.event.*;

public class ControlPanel extends JPanel {
    private MainFrame parent;
    private JLabel scoreLabel;

    public ControlPanel(MainFrame frame) {
        this.parent = frame;
        setLayout(new FlowLayout());

        scoreLabel = new JLabel("Điểm của bạn: 0", JLabel.CENTER);
        add(scoreLabel);

        // Nút quay lại ModeSelection
        JButton modeButton = new JButton("Chọn chế độ");
        modeButton.addActionListener(e -> parent.showModeSelection());
        add(modeButton);

        // Nút quay lại RoomOption (hiện phòng cũ nếu có)
        JButton roomButton = new JButton("Quay lại phòng");
        roomButton.addActionListener(e -> parent.showRoomOptions());  // Gọi showRoomOptions, nó sẽ load phòng cũ nếu có currentRoomID
        add(roomButton);
    }

    public void setScore(int score) {
        scoreLabel.setText("Điểm của bạn: " + score);
    }
}