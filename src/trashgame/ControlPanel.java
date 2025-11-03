
package trashgame;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class ControlPanel extends JPanel {
    private MainFrame parent;
    private JLabel scoreLabel;
    private JLabel titleLabel;
    
    public ControlPanel(MainFrame frame) {
        this.parent = frame;
        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(30, 30, 30));
        setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));
        
        // Panel trên: Title & Score
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);
        
        // Panel giữa: Buttons
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.CENTER);
        
        // Panel dưới: Footer (optional)
        JPanel footerPanel = createFooterPanel();
        add(footerPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        // Title
        titleLabel = new JLabel("🎮 End Game", JLabel.CENTER);
        titleLabel.setForeground(new Color(0, 255, 153));
        titleLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 32));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Space
        panel.add(Box.createVerticalStrut(20));
        
        // Score panel với background đẹp
        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(new BorderLayout());
        scorePanel.setBackground(new Color(50, 50, 50));
        scorePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0, 255, 153), 3),
            BorderFactory.createEmptyBorder(20, 40, 20, 40)
        ));
        scorePanel.setMaximumSize(new Dimension(400, 100));
        
        scoreLabel = new JLabel("Your Score: 0", JLabel.CENTER);
        scoreLabel.setForeground(new Color(255, 215, 0)); // Gold
        scoreLabel.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        
        scorePanel.add(scoreLabel, BorderLayout.CENTER);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(30));
        panel.add(scorePanel);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE;
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.anchor = GridBagConstraints.CENTER;
        
        // Nút Chơi lại
        JButton playAgainButton = createStyledButton("🔄 Play Again", new Color(0, 200, 100));
        playAgainButton.addActionListener(e -> parent.showModeSelection());
        panel.add(playAgainButton, gbc);
        
        // Nút Quay lại phòng
        JButton roomButton = createStyledButton("🏠 Back To Room", new Color(60, 130, 200));
        roomButton.addActionListener(e -> parent.showRoomOptions());
        panel.add(roomButton, gbc);

        JButton exitButton = createStyledButton("🚪 Exit Game", new Color(200, 50, 50));
        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc muốn thoát không?",
                "Xác nhận thoát",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        panel.add(exitButton, gbc);
        
        return panel;
    }
    
    private JPanel createFooterPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        
        JLabel tipLabel = new JLabel("💡 Tip: Thu thập rác đúng loại để được điểm cao!", JLabel.CENTER);
        tipLabel.setForeground(new Color(150, 150, 150));
        tipLabel.setFont(new Font("Segoe UI Emoji", Font.ITALIC, 12));
        
        panel.add(tipLabel);
        
        return panel;
    }

    private JButton createStyledButton(String text, Color accentColor) {
        JButton button = new JButton(text);
        
        // Style cơ bản
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(60, 60, 60));
        button.setFont(new Font("Segoe UI Emoji", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(250, 50));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Border với màu accent
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(accentColor, 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        // Hiệu ứng hover
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(accentColor);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentColor.brighter(), 3),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(60, 60, 60));
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentColor, 2),
                    BorderFactory.createEmptyBorder(10, 20, 10, 20)
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

    public void setScore(int score) {
        scoreLabel.setText("Your Score: " + score);
        
        // Thêm hiệu ứng animation cho score
        animateScore(score);
    }
    
    private void animateScore(int finalScore) {
        Timer timer = new Timer(30, null);
        final int[] currentScore = {0};
        final int increment = Math.max(1, finalScore / 50); // Tăng dần
        
        timer.addActionListener(e -> {
            currentScore[0] += increment;
            
            if (currentScore[0] >= finalScore) {
                currentScore[0] = finalScore;
                timer.stop();
                
                // Flash effect khi đạt điểm cuối
                flashScoreLabel();
            }
            
            scoreLabel.setText("Your Score: " + currentScore[0]);
        });
        
        timer.start();
    }
  
    private void flashScoreLabel() {
        Timer flashTimer = new Timer(200, null);
        final int[] flashCount = {0};
        final Color originalColor = scoreLabel.getForeground();
        
        flashTimer.addActionListener(e -> {
            if (flashCount[0] % 2 == 0) {
                scoreLabel.setForeground(Color.WHITE);
            } else {
                scoreLabel.setForeground(originalColor);
            }
            
            flashCount[0]++;
            
            if (flashCount[0] > 6) {
                flashTimer.stop();
                scoreLabel.setForeground(originalColor);
            }
        });
        
        flashTimer.start();
    }
}