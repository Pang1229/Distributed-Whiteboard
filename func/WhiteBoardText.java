package func;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public class WhiteBoardText extends Rectangle2D.Double {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String text;
    private Color color;
    private Font font;

    public WhiteBoardText(String text, int x, int y, Color color, Font font) {
        super(x, y, 0, 0);
        this.text = text;
        this.color = color;
        this.font = font;       
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }

    public Font getFont() {
        return font;
    }
    
    public void setFont(Font font) {
        this.font = font;
    }

    public void draw(Graphics2D graphics) {
    	graphics.drawString(text, (float) x, (float) y);
    	graphics.setColor(color);
    	graphics.setFont(font);
    }
}
