
package trashgame;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.Locale;

public class Bin {
    private int x, y;
    private String type;
    private int width = 75;
    private int height = 90;
    private BufferedImage binImage;
    private boolean isFlashing = false;
    private long flashStartTime = 0;
    private static final int FLASH_DURATION = 300; // ms

    public Bin(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type.toLowerCase();
        loadImage();
    }

    private void loadImage() {
        String imagePath = "";
        try {
            switch (type) {
                case "organic":
                    imagePath = "/resources/organic_bin-removebg-preview_1.png";
                    break;
                case "inorganic":
                    imagePath = "/resources/inorganic_bin-removebg-preview_1.png";
                    break;
                case "recyclable":
                    imagePath = "/resources/recyclable_bin-removebg-preview_1.png";
                    break;
                default:
                    System.out.println("⚠️ Unknown bin type: " + type);
                    return;
            }

            // Load ảnh gốc
            BufferedImage originalImage = ImageIO.read(getClass().getResource(imagePath));
            
            if (originalImage != null) {
                // FIXED: Chuyển đổi sang BufferedImage với alpha channel
                binImage = convertToARGB(originalImage);
                System.out.println("✅ Load ảnh thành công: " + imagePath + 
                    " (Type: " + getImageTypeString(binImage.getType()) + ")");
            } else {
                System.err.println("❌ Không load được ảnh: " + imagePath);
            }
            
        } catch (IOException e) {
            System.err.println("❌ Lỗi đọc file: " + imagePath);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Không tìm thấy file: " + imagePath);
            e.printStackTrace();
        }
    }

    private BufferedImage convertToARGB(BufferedImage src) {
        // Tạo BufferedImage mới với TYPE_INT_ARGB (hỗ trợ trong suốt)
        BufferedImage newImage = new BufferedImage(
            src.getWidth(), 
            src.getHeight(), 
            BufferedImage.TYPE_INT_ARGB
        );
        
        // Copy ảnh gốc sang ảnh mới với alpha channel
        Graphics2D g = newImage.createGraphics();
        
        // BẬT khử răng cưa và rendering chất lượng cao
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        
        // Vẽ ảnh gốc lên ảnh mới
        g.drawImage(src, 0, 0, null);
        g.dispose();
        
        return newImage;
    }

    private String getImageTypeString(int type) {
        switch (type) {
            case BufferedImage.TYPE_INT_RGB: return "TYPE_INT_RGB (No Alpha)";
            case BufferedImage.TYPE_INT_ARGB: return "TYPE_INT_ARGB (With Alpha)";
            case BufferedImage.TYPE_INT_ARGB_PRE: return "TYPE_INT_ARGB_PRE";
            case BufferedImage.TYPE_4BYTE_ABGR: return "TYPE_4BYTE_ABGR";
            case BufferedImage.TYPE_3BYTE_BGR: return "TYPE_3BYTE_BGR (No Alpha)";
            default: return "CUSTOM (" + type + ")";
        }
    }

    public void moveLeft(int minX) {
        x = Math.max(minX, x - 10);
    }

    public void moveRight(int maxX) {
        x = Math.min(maxX - width, x + 10);
    }

    public void setX(int newX) {
        this.x = newX;
    }

    public void startFlash() {
        isFlashing = true;
        flashStartTime = System.currentTimeMillis();
    }

    public void update() {
        if (isFlashing && System.currentTimeMillis() - flashStartTime > FLASH_DURATION) {
            isFlashing = false;
        }
    }

    public void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        
        // CRITICAL: Bật các rendering hints cho vẽ ảnh trong suốt
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);

        if (binImage != null) {
            // Lưu composite gốc
            Composite originalComposite = g2.getComposite();
            
            if (isFlashing) {
                // Hiệu ứng nhấp nháy
                float alpha = (float) Math.abs(Math.sin((System.currentTimeMillis() - flashStartTime) / 50.0));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f + 0.5f * alpha));
            }
            
            // VẼ ảnh (với alpha channel được giữ nguyên)
            g2.drawImage(binImage, x, y, width, height, null);
            
            // Khôi phục composite
            g2.setComposite(originalComposite);

            // Vẽ chữ loại thùng
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            
            // Vẽ chữ có viền đen (để dễ đọc)
            String label = type.substring(0, 1).toUpperCase(Locale.ENGLISH);
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelX = x + (width - labelWidth) / 2;
            int labelY = y + height - 8;
            
            // Viền đen
            g2.setColor(Color.BLACK);
            g2.drawString(label, labelX - 1, labelY - 1);
            g2.drawString(label, labelX + 1, labelY - 1);
            g2.drawString(label, labelX - 1, labelY + 1);
            g2.drawString(label, labelX + 1, labelY + 1);
            
            // Chữ trắng
            g2.setColor(Color.WHITE);
            g2.drawString(label, labelX, labelY);
            
        } else {
            // Fallback: Vẽ khối màu nếu không có ảnh
            g2.setColor(getColorByType());
            g2.fillRoundRect(x, y, width, height, 15, 15);
            
            // Viền
            g2.setColor(Color.BLACK);
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x, y, width, height, 15, 15);
            
            // Chữ
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
            String label = type.substring(0, 1).toUpperCase(Locale.ENGLISH);
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            int labelHeight = fm.getAscent();
            g2.drawString(label, 
                x + (width - labelWidth) / 2, 
                y + (height + labelHeight) / 2 - 2);
        }
    }
    
    /**
     * Lấy màu theo loại thùng (dùng cho fallback)
     */
    private Color getColorByType() {
        switch (type) {
            case "organic":
                return new Color(76, 175, 80);   // Xanh lá
            case "inorganic":
                return new Color(158, 158, 158);  // Xám
            case "recyclable":
                return new Color(33, 150, 243);   // Xanh dương
            default:
                return Color.GRAY;
        }
    }

    public int getX() { return x; }
    public int getWidth() { return width; }
    public int getY() { return y; }
    public String getType() { return type; }
}