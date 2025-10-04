//
//package trashgame;
//
//import java.awt.*;
//import javax.swing.*;
//
//public class MainFrame extends JFrame {
//    private CardLayout cardLayout;
//    private JPanel cards;
//
//    private LoginPanel loginPanel;
//    private ModeSelectionPanel modePanel;
//    private RoomOptionsPanel roomPanel;
//    private GamePanel gamePanel;
//    private ControlPanel controlPanel;
//
//    public static final String CARD_LOGIN = "Login";
//    public static final String CARD_MODE = "Mode";
//    public static final String CARD_ROOM = "Room";
//    public static final String CARD_GAME = "Game";
//    public static final String CARD_CONTROL = "Control";
//
//    private int currentUserId;           // user id
//    private String currentUsername;    // username
//    private Client client;             // client socket (nullable)
//
//    public MainFrame() {
//        setTitle("Game Phân Loại Rác");
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        setSize(1000, 700);
//        setLocationRelativeTo(null);
//
//        cardLayout = new CardLayout();
//        cards = new JPanel(cardLayout);
//
//        // Khởi tạo các panel cơ bản (không tạo gamePanel / roomPanel ở đây)
////        loginPanel = new LoginPanel(this);
//        //loginPanel = new LoginPanel(this, getClient());
//        modePanel = new ModeSelectionPanel(this);
//        controlPanel = new ControlPanel(this);
//
//        // Add vào CardLayout
//        cards.add(loginPanel, CARD_LOGIN);
//        cards.add(modePanel, CARD_MODE);
//        // roomPanel và gamePanel sẽ được tạo khi cần (tránh chạy ngầm)
//        cards.add(controlPanel, CARD_CONTROL);
//
//        add(cards);
//        setVisible(true);
//
//        // mặc định hiển thị login
//        cardLayout.show(cards, CARD_LOGIN);
//        Client client = null;
//        try {
//            client = new Client("localhost", 12345);  // Hoặc IP server
//        } catch (Exception e) {
//            System.err.println("❌ Không kết nối server, dùng offline mode");
//        }
//        setClient(client);
//
//        loginPanel = new LoginPanel(this, client);  // SỬA: Pass Client
//    }
//    // Trong MainFrame.java, thêm phương thức
//    public void startMultiplayerGame() {
//        if (gamePanel != null) {
//            cards.remove(gamePanel);
//            gamePanel = null;
//        }
//        gamePanel = new GamePanel(this);  // GamePanel sẽ đăng ký listener tự động
//        cards.add(gamePanel, CARD_GAME);
//        cardLayout.show(cards, CARD_GAME);
//        gamePanel.requestFocusInWindow();
//    }
//    // --- setters/getters cho user và client (gọi từ LoginPanel khi login thành công) ---
//    public void setCurrentUser(int user_id) {
//        this.currentUserId = user_id;
//    }
//
//    public int getCurrentUser() {
//        return currentUserId;
//    }
//
//    public void setCurrentUsername(String username) {
//        this.currentUsername = username;
//    }
//
//    public String getCurrentUsername() {
//        return currentUsername;
//    }
//
//    public void setClient(Client client) {
//        this.client = client;
//    }
//
//    public Client getClient() {
//        return client;
//    }
//
//    // Hiển thị ModeSelection
//    public void showModeSelection() {
//        cardLayout.show(cards, CARD_MODE);
//    }
//
//    // Hiển thị RoomOptions: tạo roomPanel khi cần (nếu có client & username dùng constructor nâng cao)
////    public void showRoomOptions() {
////        // remove cũ nếu có
////        if (roomPanel != null) {
////            cards.remove(roomPanel);
////            roomPanel = null;
////        }
////
////        if (client != null && currentUserId != 0 && currentUsername != null) {
////            // tạo RoomOptionsPanel với client (nên dùng khi đã login và client kết nối)
////            roomPanel = new RoomOptionsPanel(this, client, currentUserId, currentUsername);
////        } else {
////            // fallback: tạo RoomOptionsPanel "không cần client"
////            roomPanel = new RoomOptionsPanel(this);
////        }
////
////        cards.add(roomPanel, CARD_ROOM);
////        cardLayout.show(cards, CARD_ROOM);
////        roomPanel.requestFocusInWindow();
////    }
//// Thay thế toàn bộ method showRoomOptions() hiện tại bằng đoạn này
//public void showRoomOptions() {
//    // remove cũ nếu có
//    if (roomPanel != null) {
//        cards.remove(roomPanel);
//        roomPanel = null;
//    }
//
//    // Kiểm tra đã có client và user chưa
//    if (client != null && currentUserId != 0 && currentUsername != null && !currentUsername.isEmpty()) {
//        // tạo RoomOptionsPanel với client (phải có client và user đã login)
//        roomPanel = new RoomOptionsPanel(this, client, currentUserId, currentUsername);
//        cards.add(roomPanel, CARD_ROOM);
//        cardLayout.show(cards, CARD_ROOM);
//        roomPanel.requestFocusInWindow();
//    } else {
//        // Nếu chưa login / chưa kết nối client thì thông báo hoặc chuyển về login
//        JOptionPane.showMessageDialog(this,
//                "Bạn phải đăng nhập và kết nối tới server trước khi vào phần tạo/tham gia phòng.",
//                "Chưa có kết nối",
//                JOptionPane.WARNING_MESSAGE);
//        // Optionally: show login card
//        cardLayout.show(cards, CARD_LOGIN);
//    }
//}
//
//    public void startSinglePlayer() {
//        // Xoá GamePanel cũ nếu có (tránh add trùng)
//        if (gamePanel != null) {
//            cards.remove(gamePanel);
//            gamePanel = null;
//        }
//
//        // Tạo GamePanel mới
//        gamePanel = new GamePanel(this);
//        cards.add(gamePanel, CARD_GAME);
//
//        // Hiển thị
//        cardLayout.show(cards, CARD_GAME);
//        gamePanel.requestFocusInWindow();
//    }
//
//    public void showControlPanel(int finalScore) {
//        System.out.println("Switching to ControlPanel with score " + finalScore);
//        controlPanel.setScore(finalScore); // cập nhật điểm
//        cardLayout.show(cards, CARD_CONTROL);
//        cards.revalidate();
//        cards.repaint();
//    }
//
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(MainFrame::new);
//    }
//}
package trashgame;

import java.awt.*;
import javax.swing.*;

public class MainFrame extends JFrame {
    private CardLayout cardLayout;
    private JPanel cards;

    private LoginPanel loginPanel;
    private ModeSelectionPanel modePanel;
    private RoomOptionsPanel roomPanel;
    private GamePanel gamePanel;
    private ControlPanel controlPanel;

    public static final String CARD_LOGIN = "Login";
    public static final String CARD_MODE = "Mode";
    public static final String CARD_ROOM = "Room";
    public static final String CARD_GAME = "Game";
    public static final String CARD_CONTROL = "Control";

    private int currentUserId;           // user id
    private String currentUsername;    // username
    private Client client;             // client socket (nullable)

    public MainFrame() {
        setTitle("Game Phân Loại Rác");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // THÊM: Tạo Client trước (kết nối server, nếu lỗi dùng offline mode)
        client = null;
        try {
            client = new Client("localhost", 12345);  // Hoặc IP server nếu remote
            System.out.println("✅ Kết nối client thành công");
        } catch (Exception e) {
            System.err.println("❌ Không kết nối được server, dùng offline mode");
            e.printStackTrace();
        }
        setClient(client);

        // SỬA: Tạo loginPanel trước khi add vào cards
        loginPanel = new LoginPanel(this, client);  // Pass Client cho server mode

        // Khởi tạo các panel cơ bản khác
        modePanel = new ModeSelectionPanel(this);
        controlPanel = new ControlPanel(this);

        // Add vào CardLayout (bây giờ loginPanel đã tồn tại)
        cards.add(loginPanel, CARD_LOGIN);
        cards.add(modePanel, CARD_MODE);
        // roomPanel và gamePanel sẽ được tạo khi cần (tránh chạy ngầm)
        cards.add(controlPanel, CARD_CONTROL);

        add(cards);
        setVisible(true);

        // mặc định hiển thị login
        cardLayout.show(cards, CARD_LOGIN);
    }

    // --- setters/getters cho user và client (gọi từ LoginPanel khi login thành công) ---
    public void setCurrentUser(int user_id) {
        this.currentUserId = user_id;
    }

    public int getCurrentUser() {
        return currentUserId;
    }

    public void setCurrentUsername(String username) {
        this.currentUsername = username;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Client getClient() {
        return client;
    }

    // Hiển thị ModeSelection
    public void showModeSelection() {
        cardLayout.show(cards, CARD_MODE);
    }

    // Hiển thị RoomOptions: tạo roomPanel khi cần (nếu có client & username dùng constructor nâng cao)
    public void showRoomOptions() {
        // remove cũ nếu có
        if (roomPanel != null) {
            cards.remove(roomPanel);
            roomPanel = null;
        }

        // Kiểm tra đã có client và user chưa
        if (client != null && currentUserId != 0 && currentUsername != null && !currentUsername.isEmpty()) {
            // tạo RoomOptionsPanel với client (phải có client và user đã login)
            roomPanel = new RoomOptionsPanel(this, client, currentUserId, currentUsername);
            cards.add(roomPanel, CARD_ROOM);
            cardLayout.show(cards, CARD_ROOM);
            roomPanel.requestFocusInWindow();
        } else {
            // Nếu chưa login / chưa kết nối client thì thông báo hoặc chuyển về login
            JOptionPane.showMessageDialog(this,
                    "Bạn phải đăng nhập và kết nối tới server trước khi vào phần tạo/tham gia phòng.",
                    "Chưa có kết nối",
                    JOptionPane.WARNING_MESSAGE);
            // Optionally: show login card
            cardLayout.show(cards, CARD_LOGIN);
        }
    }

    public void startSinglePlayer() {
        // Xoá GamePanel cũ nếu có (tránh add trùng)
        if (gamePanel != null) {
            cards.remove(gamePanel);
            gamePanel = null;
        }

        // Tạo GamePanel mới
        gamePanel = new GamePanel(this);
        cards.add(gamePanel, CARD_GAME);

        // Hiển thị
        cardLayout.show(cards, CARD_GAME);
        gamePanel.requestFocusInWindow();
    }

    // THÊM: Phương thức startMultiplayerGame (từ mã trước)
    public void startMultiplayerGame() {
        if (gamePanel != null) {
            cards.remove(gamePanel);
            gamePanel = null;
        }
        gamePanel = new GamePanel(this);  // GamePanel sẽ đăng ký listener tự động
        cards.add(gamePanel, CARD_GAME);
        cardLayout.show(cards, CARD_GAME);
        gamePanel.requestFocusInWindow();
    }

    public void showControlPanel(int finalScore) {
        System.out.println("Switching to ControlPanel with score " + finalScore);
        controlPanel.setScore(finalScore); // cập nhật điểm
        cardLayout.show(cards, CARD_CONTROL);
        cards.revalidate();
        cards.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}