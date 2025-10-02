
package trashgame;

import java.io.*;
import java.net.*;

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

            // THÊM: Xử lý SCORE (nếu chưa có)
            case "SCORE":
                int score = Integer.parseInt(parts[1]);  // SCORE:score
                // Cập nhật DB
                int roomNumericId = DBConnection.getRoomIdByCode(roomID);  // Giả định bạn có hàm này
                if (roomNumericId != -1) {
                    DBConnection.updatePlayerScore(roomNumericId, userId, score);
                }
                // Broadcast update
                Server.broadcastRoomPlayers(roomID);  // Cập nhật danh sách score
                break;
            default:
                System.out.println("⚠️ Lệnh chưa hỗ trợ: " + command);
        }
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }
}   