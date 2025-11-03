package trashgame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/trashgame_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";  
    private static final String PASS = "";      

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
//    public static void updatePlayerScore(String roomId, int userId, int score) {
//        String sql = "UPDATE room_players SET score = ? WHERE room_id = ? AND user_id = ?";
//        String insertscore = "INSERT INTO scores (user_id, score) VALUES (?,?)";
//        try (Connection conn = connect();
//             PreparedStatement stmt = conn.prepareStatement(sql);
//                PreparedStatement stmt2 = conn.prepareStatement(insertscore)) {
//            stmt.setInt(1, score);
//            stmt.setString(2, roomId);
//            stmt.setInt(3, userId);
//            stmt2.setInt(1,userId);
//            stmt2.setInt(2, score);
//            int rowsAffected = 0;
//            if(!roomId.isEmpty()) {
//                rowsAffected = stmt.executeUpdate();
//            }
//            int rowsAffected2 = stmt2.executeUpdate();
//            if(rowsAffected>0){
//                System.out.println("✅ Cập nhật điểm thành công: " + rowsAffected + " dòng");
//            } else {
//                System.out.println("⚠️ Không có dòng nào được cập nhật");
//            }
//            if(rowsAffected2>0){
//                System.out.println("✅ Cập nhật điểm ở bảng scores thành công: " + rowsAffected + " dòng");
//            } else {
//                System.out.println("⚠️ Không có dòng nào ở bảng scores được cập nhật");
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
     // FIXED: Ham cap nhat diem
    public static void updatePlayerScore(String roomId, int userId, int score) {
        Connection conn = null;
        try {
            conn = connect();
            if (conn == null) {
                System.err.println("Cannot connect to database!");
                return;
            }
            
            // BUOC 0: Kiem tra user co ton tai khong
            String checkUserSql = "SELECT id FROM users WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
                checkStmt.setInt(1, userId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    System.err.println("ERROR: userId=" + userId + " does NOT exist in users table!");
                    System.err.println("Cannot save score for non-existent user. Please check your userId.");
                    return;
                }
                System.out.println("User validation OK: userId=" + userId + " exists in database");
            }

            conn.setAutoCommit(false);
            
            if (roomId != null && !roomId.trim().isEmpty()) {
                String updateRoomSql = "UPDATE room_players SET score = ? WHERE room_id = ? AND user_id = ?";
                try (PreparedStatement stmt = conn.prepareStatement(updateRoomSql)) {
                    stmt.setInt(1, score);
                    stmt.setString(2, roomId);
                    stmt.setInt(3, userId);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        System.out.println("Room score updated successfully: " + rowsAffected + " row(s)");
                    } else {
                        System.out.println("Warning: No rows updated in room_players (user may not be in room)");
                    }
                }
            }
            
            // 2. Luu diem vao bang scores 
            String insertScoreSql = "INSERT INTO scores (user_id, score, play_time) VALUES (?, ?, NOW())";
            try (PreparedStatement stmt = conn.prepareStatement(insertScoreSql)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, score);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Score saved to scores table successfully: " + rowsAffected + " row(s)");
                } else {
                    System.out.println("Warning: No rows inserted in scores table");
                }
            }
            
            conn.commit();
            System.out.println("=== TRANSACTION COMMITTED === Score update completed for userId=" + userId + ", score=" + score);
            
        } catch (SQLException e) {
            System.err.println("=== SQL Error in updatePlayerScore ===");
            System.err.println("Error message: " + e.getMessage());
            System.err.println("userId attempted: " + userId);
            System.err.println("score attempted: " + score);
            System.err.println("roomId attempted: " + (roomId == null ? "NULL" : roomId));
            e.printStackTrace();

            if (conn != null) {
                try {
                    conn.rollback();
                    System.err.println("Transaction rolled back due to error");
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
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

    
    public static void addPlayerToRoom(String roomID, int userId, String username) {
        // THÊM: Check tồn tại trước INSERT
        String checkSql = "SELECT id FROM room_players WHERE room_id = ? AND user_id = ?";
        String insertSql = "INSERT IGNORE INTO room_players (room_id, user_id, score) VALUES (?, ?, 0)";  // SỬA: INSERT IGNORE để bỏ qua nếu tồn tại

        try (Connection conn = connect();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql);
             PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

            checkStmt.setString(1, roomID);
            checkStmt.setInt(2, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                System.out.println("👤 Người chơi " + username + " đã tồn tại trong phòng " + roomID + " (không insert)");
                return;  // Không insert nếu đã có
            }

            insertStmt.setString(1, roomID);
            insertStmt.setInt(2, userId);
            int rowsAffected = insertStmt.executeUpdate();
            System.out.println("Thêm người chơi " + username + " vào phòng " + roomID + ": " + rowsAffected + " dòng");
        } catch (SQLException e) {
            System.err.println("❌ Lỗi addPlayerToRoom: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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