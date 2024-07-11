package func;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

public class WhiteBoardShape implements Serializable {
    private static final long serialVersionUID = 1L;
    private Shape shape;
    private Color color;
    private String text;
    private Font font = new Font("Helvetica", Font.PLAIN, 30);;

    // 用于非文本形状的构造函数
    public WhiteBoardShape(Shape shape, Color color) {
        this(shape, color, null, null);
    }

    // 用于文本形状的构造函数
    public WhiteBoardShape(String text, int x, int y, Color color, Font font) {
        // 创建一个零大小的矩形以便存储文本的位置
        this(new Rectangle2D.Double(x, y, 0, 0), color, text, font);
    }


    public WhiteBoardShape(Shape shape, Color color, String text, Font font) {
        this.shape = shape;
        this.color = color;
        this.text = text;
        this.font = font;
    }

    public Shape getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }

    public String getText() {
        return text;
    }

    public Font getFont() {
        return font;
    }
    
    public boolean intersects(Rectangle2D rect) {
        if (text != null && font != null) {
            // 对于文本对象，检查文本的边界是否与橡皮擦的矩形相交
            Rectangle2D textBounds = new Rectangle2D.Double(
                shape.getBounds2D().getX(), shape.getBounds2D().getY() - font.getSize(),
                font.getSize() * text.length(), font.getSize()
            );
            return textBounds.intersects(rect);
        } else {
            return shape.intersects(rect);
        }
    }

    public void draw(Graphics2D graphics) {
    	graphics.setColor(color);
        if (text != null && font != null) {
        	graphics.setFont(font);
        	graphics.drawString(text, (float) shape.getBounds2D().getX(), (float) shape.getBounds2D().getY());
        } else {
        	graphics.draw(shape);
        }
    }

    // 新增的用于清除文本的方法
    public void clearText() {
        this.text = null;
    }
}

