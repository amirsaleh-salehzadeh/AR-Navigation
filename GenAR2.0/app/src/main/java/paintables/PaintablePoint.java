package paintables;

import android.graphics.Canvas;

/**
 * Draws a single point of set width and height on a canvas
 */
public class PaintablePoint extends PaintableObject {
    private static int width=5;
    private static int height=5;
    private int color = 0;
    private boolean fill = false;
    
    public PaintablePoint(int color, boolean fill) {
    	set(color, fill);
    }

    public void set(int color, boolean fill) {
        this.color = color;
        this.fill = fill;
    }

	@Override
    public void paint(Canvas canvas) {
    	if (canvas==null) throw new NullPointerException();
    	
        setFill(fill);
        setColor(color);
        paintRect(canvas, -1, -1, width, height);
    }

	@Override
    public float getWidth() {
        return width;
    }

	@Override
    public float getHeight() {
        return height;
    }
}