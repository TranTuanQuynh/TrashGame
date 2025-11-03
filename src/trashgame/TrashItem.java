
package trashgame;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.Random;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class TrashItem {
    private double x;
    private int y;
    private int speed;
    private double dx;
    private String type;
    private Random rand = new Random();
    private Color color_trash;
    private final int width = 40;
    private final int height = 40;
    
    // --- Wave motion ---
    private boolean wave = false;
    private double waveAmplitude;
    private double waveFrequency;
    private int tick = 0;
    
    private static Map<String, List<BufferedImage>> trashImagesMap = new HashMap<String, List<BufferedImage>>();
    private static boolean imagesLoaded = false;
    private BufferedImage currentImage;

    private static final Map<String, String[]> IMAGE_PATHS = new HashMap<String, String[]>() {{
        put("organic", new String[] {
            "/resources/trash_apple-removebg-preview.png",
            "/resources/trash_banana-removebg-preview.png",
            "/resources/trash_chicken-removebg-preview.png"
            // Thêm nhiều ảnh hơn nếu có
        });
        
        put("inorganic", new String[] {
            "/resources/trash_broken_ceramics-removebg-preview.png",
            "/resources/trash_diapers-removebg-preview.png",
            "/resources/trash_plastic_bag-removebg-preview.png"
        });
        
        put("recyclable", new String[] {
            "/resources/trash_can-removebg-preview.png",
            "/resources/trash_cardboard-removebg-preview.png",
            "/resources/trash_plastic_bottles-removebg-preview.png"
        });
        
        put("bomb", new String[] {
            "/resources/trash_bomb.png"
        });
    }};

    static {
        loadAllImages();
    }
 
    private static void loadAllImages() {
        if (imagesLoaded) return;
        
        System.out.println("🗑️ Đang load ảnh rác với nhiều variants...");
        int totalLoaded = 0;
        
        for (Map.Entry<String, String[]> entry : IMAGE_PATHS.entrySet()) {
            String type = entry.getKey();
            String[] paths = entry.getValue();
            
            List<BufferedImage> imageList = new ArrayList<BufferedImage>();
            
            // Load từng ảnh trong mảng
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i];
                
                try {
                    java.net.URL imageURL = TrashItem.class.getResource(path);
                    
                    if (imageURL == null) {
                        System.err.println("⚠️ Không tìm thấy: " + path);
                        continue;
                    }
                    
                    BufferedImage originalImage = ImageIO.read(imageURL);
                    
                    if (originalImage != null) {
                        BufferedImage argbImage = convertToARGB(originalImage);
                        imageList.add(argbImage);
                        totalLoaded++;
                        System.out.println("✅ Load " + type + " variant #" + (i+1) + " thành công");
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Lỗi load " + path + ": " + e.getMessage());
                }
            }
            
            // Lưu danh sách ảnh của loại này
            if (!imageList.isEmpty()) {
                trashImagesMap.put(type, imageList);
                System.out.println("📦 Loại " + type + " có " + imageList.size() + " ảnh");
            } else {
                System.err.println("⚠️ Không load được ảnh nào cho loại " + type);
            }
        }
        
        imagesLoaded = true;
        System.out.println("✅ Load xong tổng cộng " + totalLoaded + " ảnh rác!");
    }
   
    private static BufferedImage convertToARGB(BufferedImage src) {
        BufferedImage newImage = new BufferedImage(
            src.getWidth(), 
            src.getHeight(), 
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g = newImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(src, 0, 0, null);
        g.dispose();
        
        return newImage;
    }
    
    private BufferedImage getRandomImageForType(String type) {
        List<BufferedImage> imageList = trashImagesMap.get(type);
        
        if (imageList == null || imageList.isEmpty()) {
            return null;
        }
     
        int randomIndex = rand.nextInt(imageList.size());
        return imageList.get(randomIndex);
    }

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
        
        // Wave motion
        if (score >= 0 && rand.nextInt(100) < 30) {
            wave = true;
            waveAmplitude = 40 + rand.nextInt(30);
            waveFrequency = 0.1 + rand.nextDouble() * 0.1;
        }
        
        // Xác định loại rác
        int randType = (score >= 0) ? rand.nextInt(4) : rand.nextInt(3);
        switch (randType) {
            case 0: 
                type = "organic"; 
                color_trash = Color.RED;
                break;
            case 1: 
                type = "inorganic"; 
                color_trash = Color.BLUE;
                break;
            case 2: 
                type = "recyclable"; 
                color_trash = Color.GREEN;
                break;
            default: 
                type = "bomb"; 
                color_trash = Color.BLACK;
                break;
        }
 
        currentImage = getRandomImageForType(type);
        
        if (currentImage == null) {
            System.out.println("⚠️ Không có ảnh cho " + type + ", dùng hình vẽ mặc định");
        }
    }

    public void fall() {
        y += speed;
        if (wave) {
            x += Math.sin(tick * waveFrequency) * (waveAmplitude / 20.0);
            tick++;
        } else {
            x += dx;
        }
        if (x < 0) x = 0;
    }

    public void increaseSpeed() {
        speed += 1;
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        int drawX = (int) Math.round(x);
        int drawY = y;
        
        if (currentImage != null) {
            // VẼ ẢNH
            g2.drawImage(currentImage, drawX, drawY, width, height, null);
         
        } else {
            // FALLBACK: Vẽ hình vuông màu
            g2.setColor(color_trash);
            g2.fillRect(drawX, drawY, width, height);
            
            g2.setColor(Color.BLACK);
            g2.drawRect(drawX, drawY, width, height);
            
            g2.drawString(type.substring(0, 1).toUpperCase(Locale.ENGLISH), 
                         drawX + 5, drawY + 15);
        }
    }

    public int getX() { 
        return (int) Math.round(x); 
    }
    
    public int getY() { 
        return y; 
    }
    
    public String getType() { 
        return type; 
    }
}