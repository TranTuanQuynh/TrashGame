
package trashgame;

import java.io.*;
import java.net.*;
import java.util.List;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private int userId;
    private String roomID;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }
    
    public String getUsername() {
        return username;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String line;
            while ((line = in.readLine()) != null) {
                System.out.println("📩 Nhận từ client: " + line);
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
                int loginUserId = DBConnection.loginUser(loginUsername, password);  // Kiểm tra DB trên server
                if (loginUserId != -1) {
                    out.println("LOGIN_SUCCESS:" + loginUserId);  // Trả thành công
                    System.out.println("✅ Server xác thực " + loginUsername + " thành công");
                } else {
                    out.println("LOGIN_FAIL");  // Trả thất bại
                    System.out.println("❌ Server từ chối login cho " + loginUsername);
                }
                break;
            case "REGISTER":
                String regUsername = parts[1];
                String regPassword = parts[2];
                if (DBConnection.registerUser(regUsername, regPassword)) {  // Gọi hàm register trên server
                    out.println("REGISTER_SUCCESS:" + regUsername);
                } else {
                    out.println("REGISTER_FAIL:Username đã tồn tại hoặc lỗi DB");
                }
                break;   
            case "LEADERBOARD":
                List<String[]> leaderboard = DBConnection.getLeaderboard();  // Server gọi DB
                StringBuilder sb = new StringBuilder("LEADERBOARD:");
                for (String[] row : leaderboard) {
                    sb.append(row[0]).append(":").append(row[1]).append(";");
                }
                out.println(sb.toString());
                break;
            case "CREATE_ROOM":
                roomID = parts[1];
                userId = Integer.parseInt(parts[2]);
                username = parts[3];
                DBConnection.createRoom(roomID, userId, username);
                Server.addToRoom(roomID, this);  // SỬA: Gọi addToRoom (đã có broadcast bên trong)
                break;

            case "JOIN_ROOM":
                roomID = parts[1];
                userId = Integer.parseInt(parts[2]);
                username = parts[3];
                DBConnection.addPlayerToRoom(roomID, userId, username);
                Server.addToRoom(roomID, this);  // SỬA: Gọi addToRoom (đã có broadcast bên trong)
                break;
            
            case "READY":
                username = parts[1];  // READY:username
                Server.updateReadyStatus(roomID, username, true);
                break;

            case "UNREADY":
                username = parts[1];  // UNREADY:username
                Server.updateReadyStatus(roomID, username, false);
                break;

            case "SCORE":
                int score = Integer.parseInt(parts[1]);  // SCORE:score
                System.out.println("🏆 " + username + " cập nhật điểm: " + score);  

                DBConnection.updatePlayerScore(roomID, userId, score);  
                Server.broadcastScoreUpdate(roomID, username, score);
                break;
            case "REFRESH_ROOM":
                String roomID = parts[1];  // REFRESH_ROOM:roomID
                System.out.println("🔄 Refresh phòng " + roomID + " (không insert)");
                Server.broadcastRoomPlayers(roomID);  // Chỉ broadcast danh sách hiện tại từ DB
                break;
            default:
                System.out.println("⚠️ Lệnh chưa hỗ trợ: " + command);
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }
}   