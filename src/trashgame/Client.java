
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

    // THÊM: Callback cho login (để LoginPanel nhận phản hồi từ server)
    private LoginCallback loginCallback;

    // THÊM: Callback cho register (nếu cần)
    private RegisterCallback registerCallback;

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

    // THÊM: Set callback cho login
    public void setLoginCallback(LoginCallback callback) {
        this.loginCallback = callback;
    }

    // THÊM: Set callback cho register
    public void setRegisterCallback(RegisterCallback callback) {
        this.registerCallback = callback;
    }

    // Thêm/xóa listener
    public void addScoreListener(ScoreListener l) { listeners.add(l); }
    public void removeScoreListener(ScoreListener l) { listeners.remove(l); }

    // Gửi tin nhắn tới server
    public void sendMessage(String msg) {
        out.println(msg);
    }
    
    public void sendLogin(String username, String password) {
        this.username = username;  // SỬA: Lưu username tạm thời để dùng sau
        sendMessage("LOGIN:" + username + ":" + password);
    }
    
    public void sendRegister(String username, String password) {
        sendMessage("REGISTER:" + username + ":" + password);
    }

    // THÊM: Interface callback cho login
    public interface LoginCallback {
        void onLoginSuccess(int userId, String username);
        void onLoginFail();
    }

    // THÊM: Interface callback cho register
    public interface RegisterCallback {
        void onRegisterSuccess(String username);
        void onRegisterFail(String error);
    }

    // Xử lý tin nhắn từ server
    private void handleMessage(String msg) {
        if (msg.startsWith("LOGIN_SUCCESS")) {
            String[] parts = msg.split(":");
            userId = Integer.parseInt(parts[1]);
            SwingUtilities.invokeLater(() -> {
                // THÊM: Gọi callback login success
                if (loginCallback != null) {
                    loginCallback.onLoginSuccess(userId, username);  // Sử dụng username đã lưu
                }
                System.out.println("✅ Login thành công, userId: " + userId);
            });

        } else if (msg.startsWith("LOGIN_FAIL")) {
            SwingUtilities.invokeLater(() -> {
                // THÊM: Gọi callback login fail
                if (loginCallback != null) {
                    loginCallback.onLoginFail();
                }
                System.out.println("❌ Login thất bại");
            });

        } else if (msg.startsWith("REGISTER_SUCCESS")) {
            String regUsername = msg.split(":")[1];  // REGISTER_SUCCESS:username
            SwingUtilities.invokeLater(() -> {
                if (registerCallback != null) {
                    registerCallback.onRegisterSuccess(regUsername);
                }
                System.out.println("✅ Register thành công cho " + regUsername);
            });

        } else if (msg.startsWith("REGISTER_FAIL")) {
            String error = msg.substring("REGISTER_FAIL:".length());  // REGISTER_FAIL:error message
            SwingUtilities.invokeLater(() -> {
                if (registerCallback != null) {
                    registerCallback.onRegisterFail(error);
                }
                System.out.println("❌ Register thất bại: " + error);
            });

        } else if (msg.startsWith("USER_JOINED")) {
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