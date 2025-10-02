package trashgame;

import javax.swing.*;

public class ControlPanel extends JPanel {
    private JButton modeButton = new JButton("Mode Selection");
    private JButton roomButton = new JButton("Room Options");
    private JLabel scoreLabel = new JLabel("Score: 0");

    public ControlPanel(MainFrame frame) {
        add(scoreLabel);
        add(modeButton);
        add(roomButton);

        modeButton.addActionListener(e -> frame.showModeSelection());
        roomButton.addActionListener(e -> frame.showRoomOptions());
    }

    public void setScore(int score) {
        scoreLabel.setText("Score: " + score);
    }
}
