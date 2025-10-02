//
//package trashgame;
//
//import java.awt.Color;
//import java.awt.Graphics;
//import java.util.Random;
//import java.util.Locale;
//
//public class TrashItem {
//    private double x;          // vị trí ngang có thể fractional
//    private int y;
//    private int speed;
//    private double dx;         // tốc độ ngang fractional
//    private String type;       // "organic","inorganic","recyclable","bomb"
//    private Random rand = new Random();
//    private Color color_trash;
//    private final int width = 20;
//    private final int height = 20;
//
//    /**
//     * @param panelWidth   chiều rộng panel (để spawn non-angled)
//     * @param panelHeight  chiều cao panel (để tính steps cho dx)
//     * @param binStartX    left của block thùng (organic.x)
//     * @param binEndX      right boundary của block (recyclable.x + width)
//     * @param angled       nếu true: spawn và kết thúc trong vùng block, có dx
//     * @param score        để quyết định thêm bom khi score >= 20
//     */
//    public TrashItem(int panelWidth, int panelHeight, int binStartX, int binEndX, boolean angled, int score) {
//        // đảm bảo vùng hợp lệ
//        int safeLeft = binStartX;
//        int safeRight = Math.max(binStartX + width, binEndX - width); // ensure > left
//
//        if (angled && safeRight > safeLeft) {
//            // spawn bắt đầu trong vùng block, và kết thúc trong vùng block
//            int range = safeRight - safeLeft + 1;
//            int xStart = safeLeft + rand.nextInt(range);
//            int xEnd = safeLeft + rand.nextInt(range);
//            this.x = xStart;
//            this.y = 0;
//            this.speed = 3;
//
//            // tính số bước (steps) rơi dự kiến: (distanceY / speed)
//            int steps = Math.max(1, (panelHeight - this.y) / Math.max(1, speed));
//            this.dx = (double)(xEnd - xStart) / (double)steps;
//        } else {
//            // non-angled: spawn toàn màn (nhưng tránh viền)
//            int maxX = Math.max(0, panelWidth - width);
//            this.x = rand.nextInt(maxX + 1);
//            this.y = 0;
//            this.speed = 3;
//            this.dx = 0;
//        }
//
//        // xác định loại (thêm bomb nếu score >= 20)
//        int randType = (score >= 0) ? rand.nextInt(4) : rand.nextInt(3);
//        switch (randType) {
//            case 0: type = "organic"; color_trash = Color.RED; break;
//            case 1: type = "inorganic"; color_trash = Color.BLUE; break;
//            case 2: type = "recyclable"; color_trash = Color.GREEN; break;
//            default: type = "bomb"; color_trash = Color.BLACK; break;
//        }
//    }
//
//    public void fall() {
//        y += speed;
//        x += dx;
//        // giữ within màn (an toàn)
//        if (x < 0) x = 0;
//        // cần panelWidth nếu muốn clamp phải truyền vào hoặc clamp ở GamePanel trước khi vẽ
//    }
//
//    public void increaseSpeed() {
//        speed += 1;
//    }
//
//    public void draw(Graphics g) {
//        g.setColor(color_trash);
//        g.fillRect((int)Math.round(x), y, width, height);
//        g.setColor(Color.BLACK);
//        g.drawString(type.substring(0, 1).toUpperCase(Locale.ENGLISH), (int)Math.round(x) + 5, y + 15);
//    }
//
//    public int getX() { return (int)Math.round(x); }
//    public int getY() { return y; }
//    public String getType() { return type; }
//}

package trashgame;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;
import java.util.Locale;

public class TrashItem {
    private double x;          // vị trí ngang (fractional)
    private int y;
    private int speed;
    private double dx;         // tốc độ ngang cơ bản
    private String type;       // "organic","inorganic","recyclable","bomb"
    private Random rand = new Random();
    private Color color_trash;
    private final int width = 20;
    private final int height = 20;

    // --- thêm cho lượn sóng ---
    private boolean wave = false;   // có phải rơi lượn sóng?
    private double waveAmplitude;   // biên độ sóng ngang
    private double waveFrequency;   // tần số sóng
    private int tick = 0;           // đếm bước để tính sin

    public TrashItem(int panelWidth, int panelHeight, int binStartX, int binEndX, boolean angled, int score) {
        int safeLeft = binStartX;
        int safeRight = Math.max(binStartX + width, binEndX - width);

        if (angled && safeRight > safeLeft) {
            int range = safeRight - safeLeft + 1;
            int xStart = safeLeft + rand.nextInt(range);
            int xEnd = safeLeft + rand.nextInt(range);
            this.x = xStart;
            this.y = 0;
            this.speed = 3;

            int steps = Math.max(1, (panelHeight - this.y) / Math.max(1, speed));
            this.dx = (double)(xEnd - xStart) / (double)steps;
        } else {
            int maxX = Math.max(0, panelWidth - width);
            this.x = rand.nextInt(maxX + 1);
            this.y = 0;
            this.speed = 3;
            this.dx = 0;
        }

        // --- chọn kiểu rơi ---
        // Nếu score >= 30 thì có xác suất spawn lượn sóng
        if (score >= 0 && rand.nextInt(100) < 30) { // 30% cơ hội
            wave = true;
            waveAmplitude = 40 + rand.nextInt(30);  // 40-70 px
            waveFrequency = 0.1 + rand.nextDouble() * 0.1; // dao động tần số
        }

        // xác định loại rác (bom nếu score >= 20)
        int randType = (score >= 0) ? rand.nextInt(4) : rand.nextInt(3);
        switch (randType) {
            case 0: type = "organic"; color_trash = Color.RED; break;
            case 1: type = "inorganic"; color_trash = Color.BLUE; break;
            case 2: type = "recyclable"; color_trash = Color.GREEN; break;
            default: type = "bomb"; color_trash = Color.BLACK; break;
        }
    }

    public void fall() {
        y += speed;

        if (wave) {
            // dao động theo sin
            x += Math.sin(tick * waveFrequency) * (waveAmplitude / 20.0);
            tick++;
        } else {
            x += dx;
        }

        if (x < 0) x = 0;
        // nếu muốn clamp bên phải cần truyền panelWidth để cắt
    }

    public void increaseSpeed() {
        speed += 1;
    }

    public void draw(Graphics g) {
        g.setColor(color_trash);
        g.fillRect((int)Math.round(x), y, width, height);
        g.setColor(Color.BLACK);
        g.drawString(type.substring(0, 1).toUpperCase(Locale.ENGLISH), (int)Math.round(x) + 5, y + 15);
    }

    public int getX() { return (int)Math.round(x); }
    public int getY() { return y; }
    public String getType() { return type; }
}
