//package trashgame;
//
//import java.awt.FlowLayout;
//import javax.swing.*;
//import java.awt.event.*;
//
//public class LoginPanel extends JPanel {
//    private JTextField usernameField = new JTextField(20);
//    private JPasswordField passwordField = new JPasswordField(20);
//    private JButton loginButton = new JButton("Login");
//    private JButton registerButton = new JButton("Register");
//
//    private MainFrame frame;
//    private Client client;  // Nhận Client từ MainFrame để gửi lệnh qua server
//
//    public LoginPanel(MainFrame frame, Client client) {
//        this.frame = frame;
//        this.client = client;
//
//        setLayout(new FlowLayout());
//
//        add(new JLabel("Username:"));
//        add(usernameField);
//        add(new JLabel("Password:"));
//        add(passwordField);
//        add(loginButton);
//        add(registerButton);
//
//        // Xử lý login - Gửi qua server
//        loginButton.addActionListener(e -> {
//            String user = usernameField.getText().trim();
//            String pass = new String(passwordField.getPassword()).trim();
//
//            if (user.isEmpty() || pass.isEmpty()) {
//                JOptionPane.showMessageDialog(this, "Nhập đầy đủ thông tin!");
//                return;
//            }
//
//            if (client != null) {
//                client.setLoginCallback(new Client.LoginCallback() {
//                    @Override
//                    public void onLoginSuccess(int userId, String username) {
//                        JOptionPane.showMessageDialog(LoginPanel.this, "✅ Login thành công!");
//                        frame.setCurrentUser(userId);
//                        frame.setCurrentUsername(username);
//                        frame.showModeSelection();
//                    }
//
//                    @Override
//                    public void onLoginFail() {
//                        JOptionPane.showMessageDialog(LoginPanel.this, "❌ Sai username hoặc password!");
//                        usernameField.setText("");
//                        passwordField.setText("");
//                    }
//                });
//                client.sendLogin(user, pass);
//            } else {
//                JOptionPane.showMessageDialog(this, "Server offline!");
//            }
//        });
//
//        // SỬA: Xử lý register - Gửi qua server, không gọi DB local
//        registerButton.addActionListener(e -> {
//            String user = usernameField.getText().trim();
//            String pass = new String(passwordField.getPassword()).trim();
//
//            if (user.isEmpty() || pass.isEmpty()) {
//                JOptionPane.showMessageDialog(this, "Nhập đầy đủ thông tin!");
//                return;
//            }
//
//            if (client != null) {
//                // Gửi qua server
//                client.setRegisterCallback(new Client.RegisterCallback() {
//                    @Override
//                    public void onRegisterSuccess(String username) {
//                        SwingUtilities.invokeLater(() -> {
//                            JOptionPane.showMessageDialog(LoginPanel.this, "✅ Đăng ký thành công! Hãy đăng nhập lại.");
//                            usernameField.setText(user);
//                            passwordField.setText(pass);
//                            System.out.println("✅ Register thành công cho " + username);
//                        });
//                    }
//
//                    @Override
//                    public void onRegisterFail(String error) {
//                        SwingUtilities.invokeLater(() -> {
//                            JOptionPane.showMessageDialog(LoginPanel.this, "❌ Đăng ký thất bại: " + error);
//                            System.out.println("❌ Register thất bại: " + error);
//                        });
//                    }
//                });
//                client.sendRegister(user, pass);  // Gửi đến server xử lý
//            } else {
//                // Fallback offline (nếu cần, nhưng không khuyến khích)
//                JOptionPane.showMessageDialog(this, "Server offline, không thể register!");
//            }
//        });
//    }
//
//    // BỎ HOÀN TOÀN: Các hàm local checkLogin, getUserId, registerUser (không dùng nữa)
//    // Không cần nữa vì server xử lý tất cả
//}

package trashgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class LoginPanel extends JPanel {
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JButton loginButton = new JButton("Login");
    private JButton registerButton = new JButton("Register");

    private MainFrame frame;
    private Client client;

    // Ảnh nền + logo
    private Image backgroundImage;
    private Image logoImage;

    public LoginPanel(MainFrame frame, Client client) {
        this.frame = frame;
        this.client = client;

        // Load ảnh (đặt trong resources hoặc cùng thư mục src)
        try {
            backgroundImage = new ImageIcon("src/resources/background.png").getImage();
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh!");
            backgroundImage = null;
        }
        try {
            logoImage = new ImageIcon("src/resources/logo.png").getImage();
        } catch (Exception e) {
            System.err.println("Không tìm thấy ảnh!");
            backgroundImage = null;
        }

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo
        JLabel logoLabel = new JLabel(new ImageIcon(logoImage.getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(logoLabel, gbc);

        // Tiêu đề
        JLabel title = new JLabel("🎮 GAME LOGIN");
        title.setFont(new Font("Segoe UI Emoji", Font.BOLD, 28));
        title.setForeground(Color.CYAN);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 1;
        add(title, gbc);

        gbc.gridwidth = 1;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 2;
        add(userLabel, gbc);

        styleField(usernameField);
        gbc.gridx = 1; gbc.gridy = 2;
        add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 3;
        add(passLabel, gbc);

        styleField(passwordField);
        gbc.gridx = 1; gbc.gridy = 3;
        add(passwordField, gbc);

        // Button
        styleButton(loginButton, new Color(0, 180, 0), new Color(0, 220, 0));
        styleButton(registerButton, new Color(0, 120, 200), new Color(30, 160, 255));

        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(loginButton);
        btnPanel.add(registerButton);

        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        add(btnPanel, gbc);

        // Event
        loginButton.addActionListener(e -> doLogin());
        registerButton.addActionListener(e -> doRegister());
    }

    // Vẽ nền
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(new Color(40, 40, 40, 180)); // mờ nền
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.CYAN);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.CYAN, 2, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleButton(JButton btn, Color normal, Color hover) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setForeground(Color.WHITE);
        btn.setBackground(normal);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) { btn.setBackground(hover); }
            @Override
            public void mouseExited(MouseEvent e) { btn.setBackground(normal); }
        });
    }

    // === Logic login & register giữ nguyên ===
    private void doLogin() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập đầy đủ thông tin!");
            return;
        }
        if (client != null) {
            client.setLoginCallback(new Client.LoginCallback() {
                @Override
                public void onLoginSuccess(int userId, String username) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "✅ Login thành công!");
                    frame.setCurrentUser(userId);
                    frame.setCurrentUsername(username);
                    frame.showModeSelection();
                }
                @Override
                public void onLoginFail() {
                    JOptionPane.showMessageDialog(LoginPanel.this, "❌ Sai username hoặc password!");
                    usernameField.setText("");
                    passwordField.setText("");
                }
            });
            client.sendLogin(user, pass);
        } else {
            JOptionPane.showMessageDialog(this, "Server offline!");
        }
    }

    private void doRegister() {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword()).trim();
        if (user.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nhập đầy đủ thông tin!");
            return;
        }
        if (client != null) {
            client.setRegisterCallback(new Client.RegisterCallback() {
                @Override
                public void onRegisterSuccess(String username) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LoginPanel.this, "✅ Đăng ký thành công! Hãy đăng nhập lại.");
                        usernameField.setText(user);
                        passwordField.setText(pass);
                    });
                }
                @Override
                public void onRegisterFail(String error) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(LoginPanel.this, "❌ Đăng ký thất bại: " + error);
                    });
                }
            });
            client.sendRegister(user, pass);
        } else {
            JOptionPane.showMessageDialog(this, "Server offline, không thể register!");
        }
    }
}
