package augmentation;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;
import com.honours.genar.myapplication2.app.ARData;
import com.honours.genar.myapplication2.app.POIMarker;
import com.honours.genar.myapplication2.app.Radar;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class which extends View
 * Provides a custom view which is responsible for:
 *      - Drawing zoom bar and radar,
 *      - Drawing the markers that show the data
 */
public class AugmentedView extends View {

    // Used to check process of drawing is currently happening
    private static final AtomicBoolean drawing = new AtomicBoolean(false);

    private static final Radar radar = new Radar();

    // Stores Location of Markers
    private static final float[] locationArray = new float[3];

    // Temp cache while drawing
    private static final List<POIMarker> cache = new ArrayList<POIMarker>();
    private static final TreeSet<POIMarker> updated = new TreeSet<POIMarker>();
    // Used to adjust markers so they don't overlap
    private static final int COLLISION_ADJUSTMENT = 100;

    public AugmentedView(Context context) {
        super(context);
    }

	@Override
    protected void onDraw(Canvas canvas) {

        if (canvas == null) return;

        if (drawing.compareAndSet(false, true)) {

	        List<POIMarker> collection = ARData.getMarkers();

            // Add all markers on Radar to cache
            cache.clear();
            for (POIMarker m : collection) {
                m.update(canvas, 0, 0);
                if (m.isOnRadar()) cache.add(m);
	        }
            collection = cache;

            // Adjustments made for collisions
	        if (AugmentedActivity.useCollisionDetection)
                adjustForCollisions(canvas,collection);

	        ListIterator<POIMarker> iter = collection.listIterator(collection.size());
	        while (iter.hasPrevious()) {
	            POIMarker marker = iter.previous();
	            marker.draw(canvas);
	        }

	        if (AugmentedActivity.showRadar)
                radar.draw(canvas);

	        drawing.set(false);
        }
    }

    /**
     * Adjusts placement of markers to avoid overlapping
     * Currently shifts up
     * @param canvas Canvas on which the markers are being drawn
     * @param collection List of Markers
     */
	private static void adjustForCollisions(Canvas canvas, List<POIMarker> collection) {
	    updated.clear();
        for (POIMarker marker1 : collection) {
            if (updated.contains(marker1) || !marker1.isInView()) continue;

            int collisions = 1;
            for (POIMarker marker2 : collection) {
                if ( marker1.equals(marker2) || updated.contains(marker2) || !marker2.isInView()) continue;

                if (marker1.isMarkerOnMarker(marker2)) {
                    marker2.getLocation().get(locationArray);
                    float x = locationArray[0];
                    float h = collisions*COLLISION_ADJUSTMENT;
                    locationArray[0] = x+h;
                    marker2.getLocation().set(locationArray);
                    marker2.update(canvas,0,0);
                    collisions++;
                    updated.add(marker2);
                }
            }
            updated.add(marker1);
        }
	}
}