
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
    private Client client;   
    private String currentRoomID = "";// client socket (nullable)

    public MainFrame() {
        setTitle("Game Phân Loại Rác");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        client = null;
        try {
            client = new Client("localhost", 12345);  
            System.out.println("✅ Kết nối client thành công");
        } catch (Exception e) {
            System.err.println("❌ Không kết nối được server, dùng offline mode");
            e.printStackTrace();
        }
        setClient(client);

        loginPanel = new LoginPanel(this, client); 

        modePanel = new ModeSelectionPanel(this);
        controlPanel = new ControlPanel(this);

        cards.add(loginPanel, CARD_LOGIN);
        cards.add(modePanel, CARD_MODE);
        cards.add(controlPanel, CARD_CONTROL);

        add(cards);
        setVisible(true);

        cardLayout.show(cards, CARD_LOGIN);
    }

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

    public void showRoomOptions() {
        // remove cũ nếu có
        if (roomPanel != null) {
            cards.remove(roomPanel);
            roomPanel = null;
        }


        if (client != null && currentUserId != 0 && currentUsername != null && !currentUsername.isEmpty()) {
   
            roomPanel = new RoomOptionsPanel(this, client, currentUserId, currentUsername);
            cards.add(roomPanel, CARD_ROOM);
            cardLayout.show(cards, CARD_ROOM);
            roomPanel.requestFocusInWindow();
            if(!currentRoomID.isEmpty()){
                roomPanel.loadPreviousRoom(currentRoomID);
            }
        } else {
           
            JOptionPane.showMessageDialog(this,
                    "Bạn phải đăng nhập và kết nối tới server trước khi vào phần tạo/tham gia phòng.",
                    "Chưa có kết nối",
                    JOptionPane.WARNING_MESSAGE);

            cardLayout.show(cards, CARD_LOGIN);
        }
    }
    public void setCurrentRoomID(String roomID) {
        this.currentRoomID = roomID;
    }

    public String getCurrentRoomID() {
        return currentRoomID;
    }

    public void startSinglePlayer() {

        if (gamePanel != null) {
            gamePanel.cleanup();
            cards.remove(gamePanel);
            gamePanel = null;
        }

        gamePanel = new GamePanel(this);
        cards.add(gamePanel, CARD_GAME);

        cardLayout.show(cards, CARD_GAME);
        gamePanel.requestFocusInWindow();
    }

    public void startMultiplayerGame() {
        if (gamePanel != null) {
            gamePanel.cleanup();
            cards.remove(gamePanel);
            gamePanel = null;
        }
        gamePanel = new GamePanel(this);  
        cards.add(gamePanel, CARD_GAME);
        cardLayout.show(cards, CARD_GAME);
        gamePanel.requestFocusInWindow();
    }

    public void showControlPanel(int finalScore) {
        System.out.println("Switching to ControlPanel with score " + finalScore);
        if (client != null && currentRoomID != null && !currentRoomID.isEmpty()) {
            client.sendMessage("SCORE:" + finalScore);
            System.out.println("Gửi điểm " + finalScore + " lên server cho phòng " + currentRoomID);
        }

        controlPanel.setScore(finalScore); 
        cardLayout.show(cards, CARD_CONTROL);
        cards.revalidate();
        cards.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainFrame::new);
    }
}