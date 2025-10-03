
package trashgame;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBConnection {
    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String url = "jdbc:mysql://localhost:3306/trashgame_db?useSSL=false&serverTimezone=UTC";
            String user = "root";   // mặc định của XAMPP
            String pass = "";       // nếu chưa đặt mật khẩu

            Connection conn = DriverManager.getConnection(url, user, pass);
            System.out.println("✅ Kết nối database thành công!");
            return conn;
        } catch (Exception e) {
            System.out.println("❌ Không thể kết nối đến database");
            e.printStackTrace();
            return null;
        }
    }
    
    public static List<String[]> getLeaderboard() {
        List<String[]> data = new ArrayList<>();
        String sql = "SELECT username, score FROM users  JOIN scores  ON users.id = scores.user_id ORDER BY score DESC LIMIT 10";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String user = rs.getString("username");
                int score = rs.getInt("score");
                data.add(new String[]{user, String.valueOf(score)});
            }
        } catch (Exception e) {
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



