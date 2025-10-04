
package trashgame;

import java.awt.FlowLayout;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginPanel extends JPanel {
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JButton loginButton = new JButton("Login");
    private JButton registerButton = new JButton("Register");

    private MainFrame frame;
    private Client client;  // Nhận Client từ parent để gửi login qua server

    public LoginPanel(MainFrame frame, Client client) {
        this.frame = frame;
        this.client = client;  // Lưu Client

        setLayout(new FlowLayout());  // Layout đơn giản

        add(new JLabel("Username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(loginButton);
        add(registerButton);

        // Xử lý login
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = usernameField.getText().trim();
                String pass = new String(passwordField.getPassword()).trim();

                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "Nhập đầy đủ thông tin!");
                    return;
                }

                if (client != null) {
                    // SỬA: Gửi login qua Client (server xử lý)
                    client.setLoginCallback(new Client.LoginCallback() {
                        @Override  // SỬA: @Override đúng cho interface method
                        public void onLoginSuccess(int userId, String username) {
                            SwingUtilities.invokeLater(() -> {  
                                JOptionPane.showMessageDialog(LoginPanel.this, "✅ Login thành công!");
                                System.out.println("✅ Login thành công, userId=" + userId);

                                // Lưu thông tin user vào MainFrame
                                frame.setCurrentUser(userId);
                                frame.setCurrentUsername(username);

                                // Chuyển sang ModeSelection
                                frame.showModeSelection();
                            });
                        }

                        @Override  // SỬA: @Override đúng cho interface method
                        public void onLoginFail() {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(LoginPanel.this, "❌ Sai username hoặc password!");
                                System.out.println("❌ Login không thành công");
                                usernameField.setText("");  // Xóa để thử lại
                                passwordField.setText("");
                            });
                        }
                    });

                    client.sendLogin(user, pass);  // Gửi đến server
                } else {
                    // Fallback nếu không có client (offline mode)
                    JOptionPane.showMessageDialog(LoginPanel.this, "Không có kết nối server! Sử dụng chế độ offline.");
                    performOfflineLogin(user, pass);  // Gọi hàm local nếu cần
                }
            }
        });

        // Xử lý register (giữ local, hoặc sửa tương tự login nếu muốn server xử lý)
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = usernameField.getText().trim();
                String pass = new String(passwordField.getPassword()).trim();

                if (user.isEmpty() || pass.isEmpty()) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "Nhập đầy đủ thông tin!");
                    return;
                }

                // SỬA: Nếu muốn server xử lý, thêm client.sendRegister(user, pass); tương tự login
                // Hiện giữ local fallback
                if (registerUser(user, pass)) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "✅ Đăng ký thành công! Hãy đăng nhập lại.");
                    usernameField.setText(user);
                    passwordField.setText(pass);
                } else {
                    JOptionPane.showMessageDialog(LoginPanel.this, "❌ Đăng ký thất bại! Username đã tồn tại.");
                }
            }
        });
    }

    // THÊM: Fallback offline login (nếu không có client)
    private void performOfflineLogin(String username, String password) {
        int userId = getUserId(username, password);  // Gọi hàm local
        if (userId > 0) {
            JOptionPane.showMessageDialog(this, "✅ Offline login thành công!");
            frame.setCurrentUser(userId);
            frame.setCurrentUsername(username);
            frame.showModeSelection();
        } else {
            JOptionPane.showMessageDialog(this, "❌ Offline login thất bại!");
        }
    }

    // Giữ nguyên các hàm local (checkLogin, getUserId, registerUser) cho fallback offline
    private boolean checkLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private int getUserId(String username, String password) {
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id"); 
                } else {
                    return -1; 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return -1; 
        }
    }

    private boolean registerUser(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }

        String checkSql = "SELECT * FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = DBConnection.connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                return false; 
            }

            insertStmt.setString(1, username);
            insertStmt.setString(2, password); 
            insertStmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}