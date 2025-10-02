
package trashgame;

import java.util.List;

public interface ScoreListener {
    void onScoreUpdate(String username, int score);
    void onUserJoined(String username);

    // THÊM: Nhận toàn bộ danh sách người chơi để load UI
    default void onRoomPlayerList(List<String[]> players) {
        // Default implementation: Có thể override trong RoomOptionsPanel
        System.out.println("Nhận danh sách người chơi: " + players.size() + " người");
    }
    
    default void onStartGame() {
        // Default: Override trong RoomOptionsPanel để chuyển panel
    }
}