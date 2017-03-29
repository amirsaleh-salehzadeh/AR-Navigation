package paintables;

import android.graphics.Canvas;
import android.graphics.Color;
import com.honours.genar.myapplication2.app.ARData;
import com.honours.genar.myapplication2.app.POIMarker;
import com.honours.genar.myapplication2.app.Radar;

/**
 * Used to draw the POI Markers' relative positions on the Radar
 * Contains a paintablePoint object
 */
public class PaintableRadarPoints extends PaintableObject {
    private final float[] locationArray = new float[3];
	private PaintablePoint paintablePoint = null;
	private PaintablePosition pointContainer = null;

	@Override
    public void paint(Canvas canvas) {
		if (canvas==null) throw new NullPointerException();

		float range = ARData.getRadius() * 1000;
		float scale = range / Radar.RADIUS;
		for (POIMarker pm : ARData.getMarkers()) {
		    pm.getLocation().get(locationArray);
		    float x = locationArray[0] / scale;
		    float y = locationArray[2] / scale;
		    if ((x*x+y*y)<(Radar.RADIUS*Radar.RADIUS)) {
		        if (paintablePoint==null) paintablePoint = new PaintablePoint(Color.WHITE,true);
		        else paintablePoint.set(Color.WHITE,true);

		        if (pointContainer==null) pointContainer = new PaintablePosition( 	paintablePoint, 
		                (x+Radar.RADIUS-1), 
		                (y+Radar.RADIUS-1), 
		                0, 
		                1);
		        else pointContainer.set(paintablePoint, 
		                (x+Radar.RADIUS-1), 
		                (y+Radar.RADIUS-1), 
		                0, 
		                1);

		        pointContainer.paint(canvas);
		    }
		}
    }

	@Override
    public float getWidth() {
        return Radar.RADIUS * 2;
    }

	@Override
    public float getHeight() {
        return Radar.RADIUS * 2;
    }
}