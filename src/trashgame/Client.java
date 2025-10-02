
package trashgame;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Client {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int userId;
    private String username;

    // Danh sách listener để UI (RoomOptionsPanel, GamePanel) đăng ký
    private final List<ScoreListener> listeners = new CopyOnWriteArrayList<>();

    public Client(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);

        // Thread nhận tin nhắn từ server
        new Thread(() -> {
            try {
                String msg;
                while ((msg = in.readLine()) != null) {
                    handleMessage(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Thêm/xóa listener
    public void addScoreListener(ScoreListener l) { listeners.add(l); }
    public void removeScoreListener(ScoreListener l) { listeners.remove(l); }

    // Gửi tin nhắn tới server
    public void sendMessage(String msg) {
        out.println(msg);
    }

    // Xử lý tin nhắn từ server
    private void handleMessage(String msg) {
        if (msg.startsWith("USER_JOINED")) {
            String user = msg.split(":")[1];
            SwingUtilities.invokeLater(() -> {
                for (ScoreListener l : listeners) {
                    l.onUserJoined(user);
                }
            });

        } else if (msg.startsWith("SCORE_UPDATE")) {
            String[] parts = msg.split(":");
            String user = parts[1];
            int score = Integer.parseInt(parts[2]);
            SwingUtilities.invokeLater(() -> {
                for (ScoreListener l : listeners) {
                    l.onScoreUpdate(user, score);
                }
            });

//        } else if (msg.startsWith("ROOM_PLAYERS")) {  // THÊM: Xử lý danh sách đầy đủ người chơi
//            // Parse: ROOM_PLAYERS:username1,score1;username2,score2;...
//            String playerData = msg.substring("ROOM_PLAYERS:".length());  // Cắt prefix
//            String[] pairs = playerData.split(";");
//            List<String[]> players = new ArrayList<>();
//            for (String pair : pairs) {
//                if (pair.trim().isEmpty()) continue;
//                String[] userScore = pair.split(",");
//                if (userScore.length == 2) {
//                    players.add(new String[]{userScore[0], userScore[1]});
//                }
//            }
//            SwingUtilities.invokeLater(() -> {
//                for (ScoreListener l : listeners) {
//                    // Gọi onRoomPlayerList nếu listener hỗ trợ (cast hoặc thêm vào interface)
//                    if (l instanceof RoomOptionsPanel) {  // Giả định RoomOptionsPanel implement
//                        ((RoomOptionsPanel) l).onRoomPlayerList(players);
//                    }
//                }
//            });
//        } else if (msg.startsWith("START_GAME")) {  // THÊM: Nhận lệnh bắt đầu game
//            SwingUtilities.invokeLater(() -> {
//                for (ScoreListener l : listeners) {
//                    if (l instanceof RoomOptionsPanel) {
//                        ((RoomOptionsPanel) l).onStartGame();  // Chuyển sang GamePanel
//                    }
//                }
//            });
            } else if (msg.startsWith("ROOM_PLAYERS")) {  // SỬA: Xử lý danh sách đầy đủ
            // Parse: ROOM_PLAYERS:username1,score1;username2,score2;...
            String playerData = msg.substring("ROOM_PLAYERS:".length());
            String[] pairs = playerData.split(";");
            List<String[]> players = new ArrayList<>();
            for (String pair : pairs) {
                if (pair.trim().isEmpty()) continue;
                String[] userScore = pair.split(",");
                if (userScore.length == 2) {
                    players.add(new String[]{userScore[0], userScore[1]});
                }
            }
            SwingUtilities.invokeLater(() -> {
                for (ScoreListener l : listeners) {
                    // SỬA: Sử dụng instanceof để tránh cast error
                    if (l instanceof RoomOptionsPanel) {
                        ((RoomOptionsPanel) l).onRoomPlayerList(players);
                    } else if (l instanceof GamePanel) {
                        ((GamePanel) l).onRoomPlayerList(players);
                    }
                }
            });

        } else if (msg.startsWith("START_GAME")) {  // SỬA: Nhận lệnh bắt đầu game
            SwingUtilities.invokeLater(() -> {
                for (ScoreListener l : listeners) {
                    // SỬA: Sử dụng instanceof để tránh cast error
                    if (l instanceof RoomOptionsPanel) {
                        ((RoomOptionsPanel) l).onStartGame();
                    } else if (l instanceof GamePanel) {
                        ((GamePanel) l).onStartGame();
                    }
                }
            });
        } else if (msg.startsWith("PLAYER")) {  // GIỮ NGUYÊN: Xử lý cũ nếu cần
            // PLAYER:username:score
            String[] parts = msg.split(":");
            String user = parts[1];
            int score = Integer.parseInt(parts[2]);

            SwingUtilities.invokeLater(() -> {
                for (ScoreListener l : listeners) {
                    // thêm user mới (nếu chưa có)
                    l.onUserJoined(user);
                    // cập nhật điểm hiện tại
                    l.onScoreUpdate(user, score);
                }
            });
        }
    }
}