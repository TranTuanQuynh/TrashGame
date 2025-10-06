
package trashgame;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {
    private static final int PORT = 12345;
    private static Map<String, List<ClientHandler>> rooms = new ConcurrentHashMap<>();
    // THÊM: Theo dõi trạng thái ready: roomID -> Map<username, Boolean>
    private static Map<String, Map<String, Boolean>> readyStatus = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("✅ Server đang chạy trên cổng " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("🔗 Client kết nối: " + socket);
                ClientHandler handler = new ClientHandler(socket);
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // thêm client vào phòng
    public static void addToRoom(String roomID, ClientHandler client) {
        rooms.putIfAbsent(roomID, new CopyOnWriteArrayList<>());
        rooms.get(roomID).add(client);
        
        // Khởi tạo ready status cho phòng mới
        readyStatus.putIfAbsent(roomID, new ConcurrentHashMap<>());
        readyStatus.get(roomID).put(client.getUsername(), false);  // Ban đầu chưa ready
        // THÊM: Broadcast danh sách người chơi sau khi thêm (để đồng bộ UI cho tất cả client)
        broadcastRoomPlayers(roomID);
    }

    // gửi danh sách người chơi trong phòng cho tất cả client trong phòng
    public static void broadcastRoomPlayers(String roomID) {
        List<ClientHandler> clients = rooms.get(roomID);
        if (clients == null) return;

        List<String[]> players = DBConnection.getPlayersInRoom(roomID);  // Lấy từ DB

        // SỬA: Tạo chuỗi đúng định dạng: ROOM_PLAYERS:username1,score1;username2,score2;...
        StringBuilder sb = new StringBuilder("ROOM_PLAYERS:");
        for (int i = 0; i < players.size(); i++) {
            sb.append(players.get(i)[0])  // username
              .append(",")
              .append(players.get(i)[1]); // score
            if (i < players.size() - 1) {
                sb.append(";");
            }
        }
        String message = sb.toString();
        System.out.println("📊 Broadcast danh sách cho phòng " + roomID + ": " + message);

        // Gửi đến tất cả client trong phòng
        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }
    
    // Trong Server.java, thêm phương thức mới
    public static void broadcastScoreUpdate(String roomID, String username, int score) {
        List<ClientHandler> clients = rooms.get(roomID);
        if (clients == null) return;

        String message = "SCORE_UPDATE:" + username + ":" + score;
        System.out.println("📊 Broadcast score update cho phòng " + roomID + ": " + message);

        for (ClientHandler c : clients) {
            c.sendMessage(message);
        }
    }
    
//    public static void updateReadyStatus(String roomID, String username, boolean ready) {
//        Map<String, Boolean> status = readyStatus.get(roomID);
//        if (status != null) {
//            status.put(username, ready);
//
//            // Kiểm tra tất cả ready
//            boolean allReady = status.values().stream().allMatch(b -> b);
//            List<ClientHandler> clients = rooms.get(roomID);
//            int totalPlayers = clients != null ? clients.size() : 0;
//
//            if (allReady && totalPlayers > 0) {
//                System.out.println("🚀 Tất cả người chơi trong phòng " + roomID + " đã ready! Bắt đầu game.");
//                // Broadcast START_GAME
//                for (ClientHandler c : clients) {
//                    c.sendMessage("START_GAME");
//                }
//                // Reset ready status cho ván mới nếu cần
//                status.clear();
//            } else {
//                System.out.println("⏳ Phòng " + roomID + ": " + countReady(status) + "/" + totalPlayers + " ready");
//            }
//        }
//    }
    public static void updateReadyStatus(String roomID, String username, boolean ready) {
        Map<String, Boolean> status = readyStatus.get(roomID);
        List<ClientHandler> clients = rooms.get(roomID);

        if (status != null && clients != null) {
            status.put(username, ready);

            // Đếm số người ready
            int readyCount = (int) status.values().stream().filter(b -> b).count();
            int totalPlayers = clients.size();

            System.out.println("Phòng " + roomID + ": " + readyCount + "/" + totalPlayers + " ready");

            // THÊM: Kiểm tra số lượng khớp
            if (readyCount == totalPlayers && totalPlayers > 0 && status.size() == totalPlayers) {
                System.out.println("Tất cả người chơi ready! Bắt đầu game.");

                for (ClientHandler c : clients) {
                    c.sendMessage("START_GAME");
                }

                // Reset ready status cho ván mới
                status.clear();
            }
        }
    }
    // THÊM: Helper đếm số ready
    private static int countReady(Map<String, Boolean> status) {
        return (int) status.values().stream().filter(b -> b).count();
    }
}