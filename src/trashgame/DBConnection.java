package trashgame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/trashgame_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";  // Mặc định XAMPP
    private static final String PASS = "";      // Mặc định XAMPP (không mật khẩu)

    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("✅ Kết nối database thành công!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("❌ JDBC Driver không tìm thấy: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("❌ Lỗi SQL kết nối: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Lỗi chung kết nối: " + e.getMessage());
        }
        return null;
    }


    public static int loginUser(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return -1;
        }

        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username.trim());
            stmt.setString(2, password.trim());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int userId = rs.getInt("id");
                System.out.println("✅ Login thành công cho " + username + ", userId: " + userId);
                return userId;
            } else {
                System.out.println("❌ Login thất bại cho " + username);
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi login: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean registerUser(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return false;
        }

        String checkSql = "SELECT id FROM users WHERE username = ?";
        String insertSql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            // Kiểm tra username tồn tại
            checkStmt.setString(1, username.trim());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("❌ Username '" + username + "' đã tồn tại");
                return false;
            }

            // Insert user mới
            insertStmt.setString(1, username.trim());
            insertStmt.setString(2, password.trim());
            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Register thành công cho " + username);
                return true;
            } else {
                System.out.println("❌ Register thất bại cho " + username);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi register: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    public static List<String[]> getLeaderboard() {
        List<String[]> data = new ArrayList<>();
        // Sử dụng JOIN để lấy max score từ scores
        String sql = "SELECT u.username, COALESCE(MAX(s.score), 0) AS max_score " +
                     "FROM users u LEFT JOIN scores s ON u.id = s.user_id " +
                     "GROUP BY u.id, u.username " +
                     "ORDER BY max_score DESC LIMIT 10";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String user = rs.getString("username");
                int score = rs.getInt("max_score");
                data.add(new String[]{user, String.valueOf(score)});
            }
            System.out.println("✅ Lấy leaderboard thành công: " + data.size() + " kết quả");
        } catch (SQLException e) {
            System.err.println("❌ Lỗi getLeaderboard: " + e.getMessage());
            e.printStackTrace();
        }
        return data;
    }

    public static int getRoomIdByCode(String roomCode) {
        String sql = "SELECT id FROM rooms WHERE room_code = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Có phòng "+roomCode);
                return rs.getInt("id");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

 
//    public static void updatePlayerScore(String roomID, int userId, int score) {
//        String sql = "UPDATE room_players SET score = ? WHERE room_id = ? AND user_id = ?";
//        try (Connection conn = connect();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setInt(1, score);
//            stmt.setString(2, roomID);  // roomID là string
//            stmt.setInt(3, userId);
//            int rowsAffected = stmt.executeUpdate();
//            System.out.println("✅ Cập nhật score cho user " + userId + " trong phòng " + roomID + ": " + rowsAffected + " dòng");
//        } catch (SQLException e) {
//            System.err.println("❌ Lỗi updatePlayerScore: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
    public static void updatePlayerScore(String roomId, int userId, int score) {
        String sql = "UPDATE room_players SET score = ? WHERE room_id = ? AND user_id = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, score);
            stmt.setString(2, roomId);
            stmt.setInt(3, userId);
            int rowsAffected = stmt.executeUpdate();
            if(rowsAffected>0){
                System.out.println("✅ Cập nhật điểm thành công: " + rowsAffected + " dòng");
            } else {
                System.out.println("⚠️ Không có dòng nào được cập nhật");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void createRoom(String roomID, int userId, String username) {
//        String insertRoom = "INSERT IGNORE INTO rooms (room_id) VALUES (?)";
//        String insertPlayer = "INSERT INTO room_players (room_id, user_id, username, score) VALUES (?, ?, ?, 0)";
//        try (Connection conn = connect();
//             PreparedStatement stmt1 = conn.prepareStatement(insertRoom);
//             PreparedStatement stmt2 = conn.prepareStatement(insertPlayer)) {
//            stmt1.setString(1, roomID);
//            int rows1 = stmt1.executeUpdate();
//            System.out.println("Tạo phòng " + roomID + ": " + rows1 + " dòng");
//
//            stmt2.setString(1, roomID);
//            stmt2.setInt(2, userId);
//            stmt2.setString(3, username);
//            int rows2 = stmt2.executeUpdate();
//            System.out.println("Thêm người chơi vào phòng: " + rows2 + " dòng");
//            if (rows2 > 0) {
//                System.out.println("✅ Tạo phòng thành công!");
//            } else {
//                System.out.println("⚠ Tạo phòng không thành công");
//            }
//        } catch (SQLException e) {
//            System.err.println("❌ Lỗi createRoom: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
    public static void createRoom(String roomID, int userId, String username) {
        String insertRoom = "INSERT IGNORE INTO rooms (room_code,created_by) VALUES (?,?)";
        String insertPlayer = "INSERT INTO room_players (room_id, user_id, score) VALUES (?, ?, 0)";
        try (Connection conn = connect();
             PreparedStatement stmt1 = conn.prepareStatement(insertRoom);
             PreparedStatement stmt2 = conn.prepareStatement(insertPlayer)) {
            stmt1.setString(1, roomID);
            stmt1.setInt(2, userId);
            int rowsAffected_1 = stmt1.executeUpdate();
            if(rowsAffected_1>0){
                System.out.println("✅ Tạo phòng thành công ở hàm createRoom: " + rowsAffected_1 + " dòng");
            } else {
                System.out.println("⚠ Tạo phòng không thành công ở hàm create Room");
            } 
            stmt2.setString(1, roomID);
            stmt2.setInt(2, userId);
            int rowsAffected_2 = stmt2.executeUpdate();
            if(rowsAffected_2>0){
                System.out.println("✅ Tham gia phòng thành công ở hàm createRoom: " + rowsAffected_2 + " dòng");
            } else {
                System.out.println("⚠ Tham gia phòng không thành công ở hàm createRoom");
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void addPlayerToRoom(String roomID, int userId, String username) {
//        String insertPlayer = "INSERT INTO room_players (room_id, user_id, username, score) VALUES (?, ?, ?, 0)";
//        try (Connection conn = connect();
//             PreparedStatement stmt = conn.prepareStatement(insertPlayer)) {
//            stmt.setString(1, roomID);
//            stmt.setInt(2, userId);
//            stmt.setString(3, username);
//            int rowsAffected = stmt.executeUpdate();
//            System.out.println("Thêm người chơi " + username + " vào phòng " + roomID + ": " + rowsAffected + " dòng");
//        } catch (SQLException e) {
//            System.err.println("❌ Lỗi addPlayerToRoom: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
    public static void addPlayerToRoom(String roomID, int userId, String username) {
        String insertPlayer = "INSERT INTO room_players (room_id, user_id, score) VALUES (?, ?, 0)";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(insertPlayer)) {
            stmt.setString(1, roomID);
            stmt.setInt(2, userId);
            int rowsAffected = stmt.executeUpdate();
            if(rowsAffected>0){
                System.out.println("✅ Người chơi tham gia phòng thành công ở hàm addPlayerToRoom: " + rowsAffected + " dòng");
            } else {
                System.out.println("⚠ Người chơi tham gia phòng không thành công ở hàm addPlayerToRoom");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    public static List<String[]> getPlayersInRoom(String roomID) {
//        List<String[]> players = new ArrayList<>();
//        String sql = "SELECT u.username, rp.score " +
//                     "FROM users u INNER JOIN room_players rp ON u.id = rp.user_id " +
//                     "WHERE rp.room_id = ?";
//        try (Connection conn = connect();
//             PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, roomID);
//            ResultSet rs = stmt.executeQuery();
//            while (rs.next()) {
//                String username = rs.getString("username");
//                int score = rs.getInt("score");
//                players.add(new String[]{username, String.valueOf(score)});
//            }
//            System.out.println("✅ Lấy " + players.size() + " người chơi trong phòng " + roomID);
//        } catch (SQLException e) {
//            System.err.println("❌ Lỗi getPlayersInRoom: " + e.getMessage());
//            e.printStackTrace();
//        }
//        return players;
//    }
    public static List<String[]> getPlayersInRoom(String roomID) {
        List<String[]> players = new ArrayList<>();
        String sql = "SELECT u.username, rp.score FROM users u INNER JOIN room_players rp ON u.id = rp.user_id WHERE rp.room_id = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                players.add(new String[]{rs.getString("username"), String.valueOf(rs.getInt("score"))});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return players;
    }
}