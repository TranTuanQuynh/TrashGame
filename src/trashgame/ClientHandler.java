
package trashgame;

import java.io.*;
import java.net.*;
import java.util.List;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private int userId = -1;
    private String roomID;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    public String getUsername() {
        return username;
    }
    
    public int getUserId() {
        return userId;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("📨 Received from client: " + line);
                handleMessage(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(String msg) {
        String[] parts = msg.split(":");
        String command = parts[0];

        switch (command) {
            case "LOGIN":
                String loginUsername = parts[1];
                String password = parts[2];
                int loginUserId = DBConnection.loginUser(loginUsername, password);
                if (loginUserId != -1) {
                    this.userId = loginUserId;
                    this.username = loginUsername;
                    out.println("LOGIN_SUCCESS:" + loginUserId);
                    System.out.println("✅ Server authenticated " + loginUsername + " successfully (userId=" + loginUserId + ")");
                } else {
                    out.println("LOGIN_FAIL");
                    System.out.println("❌ Server rejected login for " + loginUsername);
                }
                break;
                
            case "REGISTER":
                String regUsername = parts[1];
                String regPassword = parts[2];
                if (DBConnection.registerUser(regUsername, regPassword)) {
                    out.println("REGISTER_SUCCESS:" + regUsername);
                } else {
                    out.println("REGISTER_FAIL:Username already exists or DB error");
                }
                break;
                
            case "LEADERBOARD":
                List<String[]> leaderboard = DBConnection.getLeaderboard();
                StringBuilder sb = new StringBuilder("LEADERBOARD:");
                for (String[] row : leaderboard) {
                    sb.append(row[0]).append(":").append(row[1]).append(";");
                }
                out.println(sb.toString());
                break;
                
            case "CREATE_ROOM":
                roomID = parts[1];
                if (this.userId == -1 && parts.length > 3) {
                    this.userId = Integer.parseInt(parts[2]);
                    this.username = parts[3];
                    System.out.println("⚠️ WARNING: userId not set from LOGIN, using from message: " + this.userId);
                }
                
                System.out.println("🏠 Creating room: " + roomID + " for userId=" + this.userId + ", username=" + this.username);
                DBConnection.createRoom(roomID, this.userId, this.username);
                Server.addToRoom(roomID, this);
                break;

            case "JOIN_ROOM":
                roomID = parts[1];
                if (this.userId == -1 && parts.length > 3) {
                    this.userId = Integer.parseInt(parts[2]);
                    this.username = parts[3];
                    System.out.println("⚠️ WARNING: userId not set from LOGIN, using from message: " + this.userId);
                }
                
                System.out.println("🚪 Joining room: " + roomID + " for userId=" + this.userId + ", username=" + this.username);
                DBConnection.addPlayerToRoom(roomID, this.userId, this.username);
                Server.addToRoom(roomID, this);
                break;
            
            case "READY":
                String readyUsername = parts[1];
                Server.updateReadyStatus(roomID, readyUsername, true);
                break;

            case "UNREADY":
                String unreadyUsername = parts[1];
                Server.updateReadyStatus(roomID, unreadyUsername, false);
                break;

            // FIXED: Xử lý điểm cuối game (lưu vào DB)
            case "SCORE_FINAL":
                if (parts.length < 3) {
                    System.err.println("❌ SCORE_FINAL format sai: " + msg);
                    break;
                }
                
                int finalScore = Integer.parseInt(parts[1]);
                int finalUserId = Integer.parseInt(parts[2]);
                
                System.out.println("=== SCORE_FINAL (LƯU VÀO DB) ===");
                System.out.println("  Username: " + this.username);
                System.out.println("  UserId: " + finalUserId);
                System.out.println("  Score: " + finalScore);
                System.out.println("  RoomID: " + (roomID == null ? "NULL (single player)" : roomID));
                
                // Kiểm tra userId hợp lệ
                if (finalUserId <= 0) {
                    System.err.println("❌ ERROR: Invalid userId=" + finalUserId);
                    break;
                }
                
                // LƯU VÀO DATABASE
                DBConnection.updatePlayerScore(roomID, finalUserId, finalScore);
                System.out.println("✅ Đã gọi updatePlayerScore để lưu vào DB");
                break;
                
            // FIXED: Xử lý điểm thời gian thực (chỉ broadcast, KHÔNG lưu DB)
            case "SCORE_REALTIME":
                if (parts.length < 2) {
                    System.err.println("❌ SCORE_REALTIME format sai: " + msg);
                    break;
                }
                
                int realtimeScore = Integer.parseInt(parts[1]);
                System.out.println("🏆 " + username + " cập nhật điểm thời gian thực: " + realtimeScore);
                
                // CHỈ BROADCAST cho người chơi khác, KHÔNG lưu DB
                if (roomID != null) {
                    Server.broadcastScoreUpdate(roomID, username, realtimeScore);
                }
                break;
                
            case "REFRESH_ROOM":
                String refreshRoomID = parts[1];
                System.out.println("🔄 Refreshing room " + refreshRoomID);
                Server.broadcastRoomPlayers(refreshRoomID);
                break;
                
            default:
                System.out.println("⚠️ Unsupported command: " + command);
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }
}