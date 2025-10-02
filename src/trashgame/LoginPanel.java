//package trashgame;
//
//import javax.swing.*;
//import java.awt.event.*;
//import java.sql.*;
//
//public class LoginPanel extends JPanel {
//    private JTextField usernameField = new JTextField(20);
//    private JPasswordField passwordField = new JPasswordField(20);
//    private JButton loginButton = new JButton("Login");
//    private JButton registerButton = new JButton("Register");
//
//    public LoginPanel(MainFrame frame) {
//        add(new JLabel("Username:"));
//        add(usernameField);
//        add(new JLabel("Password:"));
//        add(passwordField);
//        add(loginButton);
//        add(registerButton);
//
//        // Xử lý login
//        loginButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String user = usernameField.getText();
//                String pass = new String(passwordField.getPassword());
//                int user_id = getUserId(user, pass);
//
//                if (checkLogin(user, pass)) {
//                    JOptionPane.showMessageDialog(LoginPanel.this, "✅ Login thành công!");
//                    System.out.print("✅Login thành công");
//                    frame.setCurrentUser(user_id);
//                    frame.showModeSelection();
//
//                } else {
//                    JOptionPane.showMessageDialog(LoginPanel.this, "❌ Sai username hoặc password!");
//                    System.out.print("❌Login không thành công");
//                }
//            }
//        });
//
//        // Xử lý register
//        registerButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String user = usernameField.getText();
//                String pass = new String(passwordField.getPassword());
//
//                if (registerUser(user, pass)) {
//                    JOptionPane.showMessageDialog(LoginPanel.this, "✅ Đăng ký thành công! Hãy đăng nhập lại.");
//                } else {
//                    JOptionPane.showMessageDialog(LoginPanel.this, "❌ Đăng ký thất bại! Username đã tồn tại hoặc lỗi DB.");
//                }
//            }
//        });
//    }
//
//    private boolean checkLogin(String username, String password) {
//        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
//
//        try (Connection conn = DBConnection.connect();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setString(1, username);
//            stmt.setString(2, password);
//
//            ResultSet rs = stmt.executeQuery();
//            return rs.next();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//    
//    private int getUserId(String username, String password) {
//        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
//        try (Connection conn = DBConnection.connect();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            stmt.setString(1, username);
//            stmt.setString(2, password);
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    return rs.getInt("id"); // Lấy giá trị id từ ResultSet
//                } else {
//                    return -1; // Trả về -1 nếu không tìm thấy user
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return -1; // Trả về -1 nếu có lỗi
//        }
//    }
//
//    private boolean registerUser(String username, String password) {
//        // kiểm tra username trống
//        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
//            return false;
//        }
//
//        String checkSql = "SELECT * FROM users WHERE username = ?";
//        String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";
//
//        try (Connection conn = DBConnection.connect();
//             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
//             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
//
//            // kiểm tra tồn tại
//            checkStmt.setString(1, username);
//            ResultSet rs = checkStmt.executeQuery();
//            if (rs.next()) {
//                return false; // username đã có
//            }
//
//            // thêm mới
//            insertStmt.setString(1, username);
//            insertStmt.setString(2, password); // ⚠ demo: plain text, thực tế nên hash
//            insertStmt.executeUpdate();
//            return true;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//}
//        
package trashgame;

import javax.swing.*;
import java.awt.event.*;
import java.sql.*;

public class LoginPanel extends JPanel {
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JButton loginButton = new JButton("Login");
    private JButton registerButton = new JButton("Register");

    private MainFrame frame;

    public LoginPanel(MainFrame frame) {
        this.frame = frame;

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

                int user_id = getUserId(user, pass);

                if (user_id > 0 && checkLogin(user, pass)) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "✅ Login thành công!");
                    System.out.println("✅ Login thành công, userId=" + user_id);

                    // Lưu thông tin user vào MainFrame
                    frame.setCurrentUser(user_id);
                    frame.setCurrentUsername(user);

                    // Khởi tạo client (socket kết nối tới server)
                    try {
                        Client client = new Client("localhost", 12345); 
                        frame.setClient(client);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(LoginPanel.this, "❌ Không kết nối được server!");
                        ex.printStackTrace();
                        return;
                    }

                    // Chuyển sang ModeSelection
                    frame.showModeSelection();

                } else {
                    JOptionPane.showMessageDialog(LoginPanel.this, "❌ Sai username hoặc password!");
                    System.out.println("❌ Login không thành công");
                }
            }
        });

        // Xử lý register
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String user = usernameField.getText().trim();
                String pass = new String(passwordField.getPassword()).trim();

                if (registerUser(user, pass)) {
                    JOptionPane.showMessageDialog(LoginPanel.this, "✅ Đăng ký thành công! Hãy đăng nhập lại.");
                } else {
                    JOptionPane.showMessageDialog(LoginPanel.this, "❌ Đăng ký thất bại! Username đã tồn tại hoặc lỗi DB.");
                }
            }
        });
    }

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
