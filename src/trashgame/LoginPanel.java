package trashgame;

import java.awt.FlowLayout;
import javax.swing.*;
import java.awt.event.*;

public class LoginPanel extends JPanel {
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JButton loginButton = new JButton("Login");
    private JButton registerButton = new JButton("Register");

    private MainFrame frame;
    private Client client;  // Nhận Client từ MainFrame để gửi lệnh qua server

    public LoginPanel(MainFrame frame, Client client) {
        this.frame = frame;
        this.client = client;

        setLayout(new FlowLayout());

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);
        add(registerButton);

        // Xử lý login - Gửi qua server
        loginButton.addActionListener(e -> {
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
        });

        // SỬA: Xử lý register - Gửi qua server, không gọi DB local
        registerButton.addActionListener(e -> {
            String user = usernameField.getText().trim();
            String pass = new String(passwordField.getPassword()).trim();

            if (user.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nhập đầy đủ thông tin!");
                return;
            }

            if (client != null) {
                // Gửi qua server
                client.setRegisterCallback(new Client.RegisterCallback() {
                    @Override
                    public void onRegisterSuccess(String username) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(LoginPanel.this, "✅ Đăng ký thành công! Hãy đăng nhập lại.");
                            usernameField.setText(user);
                            passwordField.setText(pass);
                            System.out.println("✅ Register thành công cho " + username);
                        });
                    }

                    @Override
                    public void onRegisterFail(String error) {
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(LoginPanel.this, "❌ Đăng ký thất bại: " + error);
                            System.out.println("❌ Register thất bại: " + error);
                        });
                    }
                });
                client.sendRegister(user, pass);  // Gửi đến server xử lý
            } else {
                // Fallback offline (nếu cần, nhưng không khuyến khích)
                JOptionPane.showMessageDialog(this, "Server offline, không thể register!");
            }
        });
    }

    // BỎ HOÀN TOÀN: Các hàm local checkLogin, getUserId, registerUser (không dùng nữa)
    // Không cần nữa vì server xử lý tất cả
}