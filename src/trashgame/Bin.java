//package trashgame;
//
//import java.awt.Color;
//import java.awt.Graphics;
//import java.util.Locale;
//
//public class Bin {
//    private int x, y;
//    private String type; // "organic", "inorganic", "recyclable"
//    private int width = 50;
//    private int height = 50;
//    private Color color_bin;
//
//    public Bin(int x, int y, String type) {
//        this.x = x;
//        this.y = y;
//        this.type = type;
//        switch (type) {
//            case "organic": this.color_bin = Color.RED; break;
//            case "inorganic": this.color_bin = Color.BLUE; break;
//            case "recyclable": this.color_bin = Color.GREEN; break;
//        }
//    }
//
//    public void moveLeft(int minX) {
//        int newX = x - 10;
//        if (newX >= minX) {
//            x = newX;
//        } else {
//            x = minX;
//        }
//    }
//
//    public void moveRight(int maxX) {
//        int newX = x + 10;
//        if (newX + width <= maxX) {
//            x = newX;
//        } else {
//            x = maxX - width;
//        }
//    }
//
//    public void draw(Graphics g) {
//        g.setColor(color_bin);
//        g.fillRect(x, y, width, height);
//        g.setColor(Color.BLACK);
//        g.drawString(type.substring(0, 1).toUpperCase(Locale.ENGLISH), x + 20, y + 30);
//    }
//    public void setX(int x){
//        this.x=x;
//    }
//    public int getX() { return x; }
//    public int getWidth() { return width; }
//    public int getY() { return y; }
//    public String getType() { return type; }
//}

package trashgame;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Locale;

public class Bin {
    private int x, y;
    private String type; // "organic", "inorganic", "recyclable"
    private int width = 50;
    private int height = 50;
    private Color color_bin;

    public Bin(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
        switch (type) {
            case "organic": this.color_bin = Color.RED; break;
            case "inorganic": this.color_bin = Color.BLUE; break;
            case "recyclable": this.color_bin = Color.GREEN; break;
        }
    }

    public void moveLeft(int minX) {
        int newX = x - 10;
        if (newX >= minX) {
            x = newX;
        } else {
            x = minX;
        }
    }

    public void moveRight(int maxX) {
        int newX = x + 10;
        if (newX + width <= maxX) {
            x = newX;
        } else {
            x = maxX - width;
        }
    }

    // mới: cho phép set trực tiếp
    public void setX(int newX) {
        this.x = newX;
    }

    public void draw(Graphics g) {
        g.setColor(color_bin);
        g.fillRect(x, y, width, height);
        g.setColor(Color.BLACK);
        g.drawString(type.substring(0, 1).toUpperCase(Locale.ENGLISH), x + 20, y + 30);
    }

    public int getX() { return x; }
    public int getWidth() { return width; }
    public int getY() { return y; }
    public String getType() { return type; }
}
