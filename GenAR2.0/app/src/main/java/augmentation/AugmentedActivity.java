package augmentation;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.honours.genar.myapplication2.app.ARData;
import com.honours.genar.myapplication2.app.POIMarker;
import com.honours.genar.myapplication2.app.VerticalSeekBar;

import java.text.DecimalFormat;

/**
 * Activity responsible for the Augmentation.
 * Creates the CameraSurface & other components displayed.
 *
 */
public class AugmentedActivity extends SensorsActivity implements OnTouchListener {

    // Tag Used for LogCat
    private static final String TAG = "AugmentedActivity";
    // Format used when displaying on radar
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");
    // Zoombar Formatting
    private static final int ZOOMBAR_BACKGROUND_COLOR = Color.argb(125, 55, 55, 55);
    private static String END_TEXT = FORMAT.format(AugmentedActivity.MAX_ZOOM)+" km";
    private static final int END_TEXT_COLOR = Color.WHITE;

    protected static WakeLock wakeLock = null;
    protected static CameraSurface camScreen = null;    
    protected static VerticalSeekBar myZoomBar = null;
    protected static TextView endLabel = null;
    protected static LinearLayout zoomLayout = null;
    protected static AugmentedView augmentedView = null;

    // Radius Limit in Km
    public static float MAX_ZOOM = 100;
    public static float ONE_PERCENT = MAX_ZOOM/100f;
    public static float TEN_PERCENT = 10f*ONE_PERCENT;
    public static float TWENTY_PERCENT = 2f*TEN_PERCENT;
    public static float EIGHTY_PERCENT = 4f*TWENTY_PERCENT;

    public static boolean useCollisionDetection = true;
    public static boolean showRadar = true;
    public static boolean showZoomBar = true;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize Camera Surface
        camScreen = new CameraSurface(this);
        setContentView(camScreen);

        augmentedView = new AugmentedView(this);
        augmentedView.setOnTouchListener(this);
        LayoutParams augLayout = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        addContentView(augmentedView,augLayout);

        // Set up Zoom bar
        zoomLayout = new LinearLayout(this);
        zoomLayout.setVisibility((showZoomBar)? LinearLayout.VISIBLE: LinearLayout.GONE);
        zoomLayout.setOrientation(LinearLayout.VERTICAL);
        zoomLayout.setPadding(5, 5, 5, 5);
        zoomLayout.setBackgroundColor(ZOOMBAR_BACKGROUND_COLOR);

        endLabel = new TextView(this);
        endLabel.setText(END_TEXT);
        endLabel.setTextColor(END_TEXT_COLOR);
        LinearLayout.LayoutParams zoomTextParams =  new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        zoomLayout.addView(endLabel, zoomTextParams);

        myZoomBar = new VerticalSeekBar(this);
        myZoomBar.setMax(100);
        myZoomBar.setProgress(50);
        myZoomBar.setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);
        LinearLayout.LayoutParams zoomBarParams =  new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        zoomBarParams.gravity = Gravity.CENTER_HORIZONTAL;
        zoomLayout.addView(myZoomBar, zoomBarParams);

        FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(  LayoutParams.WRAP_CONTENT,
                                                                                    LayoutParams.MATCH_PARENT,
                                                                                    Gravity.RIGHT);
        addContentView(zoomLayout,frameLayoutParams);
        
        updateDataOnZoom();

        // Dim screen when not in use
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "DimLock");

        Log.d(TAG,"AA onCreate()");
    }

	@Override
	public void onResume() {
		super.onResume();
        Log.d(TAG, "AA onResume()");
		wakeLock.acquire();
	}

	@Override
	public void onPause() {
		super.onPause();
        Log.d(TAG, "AA onPause()");
		wakeLock.release();
	}
	
	@Override
    public void onSensorChanged(SensorEvent evt) {
        super.onSensorChanged(evt);

        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER || evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
        {
            // Calls invalidate(), which calls onDraw()
            augmentedView.postInvalidate();
        }
    }

    /**
     * SeekBar Change Listener
     * Allows for changing of Zoom levels to update the AR Content
     */
    private OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateDataOnZoom();
            camScreen.invalidate();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            //Not used
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            updateDataOnZoom();
            camScreen.invalidate();
        }
    };

    /**
     * Calculates the radius of within which to display POI's
     * Note: the higher up on the bar, the less accurate the radius
     * @return radius of which POI's to display
     */
    private static float calcZoomLevel(){

        int myZoomLevel = myZoomBar.getProgress();
        float out;

        float percent;
        if (myZoomLevel <= 25) {
            percent = myZoomLevel/25f;
            out = ONE_PERCENT * percent;
        } else if (myZoomLevel > 25 && myZoomLevel <= 50) {
            percent = (myZoomLevel-25f)/25f;
            out = ONE_PERCENT + (TEN_PERCENT * percent);
        } else if (myZoomLevel > 50 && myZoomLevel <= 75) {
            percent = (myZoomLevel-50f)/25f;
            out = TEN_PERCENT + (TWENTY_PERCENT * percent);
        } else {
            percent = (myZoomLevel-75f)/25f;
            out = TWENTY_PERCENT + (EIGHTY_PERCENT * percent);
        }

        DecimalFormat decim = new DecimalFormat("0.0");
        endLabel.setText(decim.format(out) + " km");
        return out;
    }

    /**
     * Updates radius in ARData to allow for the calculation of which POI's are to be displayed.
     */
    protected void updateDataOnZoom() {
        float zoomLevel = calcZoomLevel();
        ARData.setRadius(zoomLevel);
        ARData.setZoomLevel(FORMAT.format(zoomLevel));
        ARData.setZoomProgress(myZoomBar.getProgress());
    }

    public static void setMaxZoom(float max) {
        MAX_ZOOM = max;
        ONE_PERCENT = MAX_ZOOM/100f;
        TEN_PERCENT = 10f*ONE_PERCENT;
        TWENTY_PERCENT = 2f*TEN_PERCENT;
        EIGHTY_PERCENT = 4f*TWENTY_PERCENT;
        END_TEXT = FORMAT.format(AugmentedActivity.MAX_ZOOM) + " km";
        float zoomLevel = calcZoomLevel();
        ARData.setRadius(zoomLevel);
        ARData.setZoomLevel(FORMAT.format(zoomLevel));
        ARData.setZoomProgress(myZoomBar.getProgress());

    }

    /**
     * Method to handle onTouch events for the markers
     * Will be used to start new Details activity
     * @param view POIMarker being selected
     * @param me Type of motion used
     * @return Boolean - if conditions were met
     */
	public boolean onTouch(View view, MotionEvent me) {
	    for (POIMarker marker : ARData.getMarkers()) {
	        if (marker.handleClick(me.getX(), me.getY())) {
	            if (me.getAction() == MotionEvent.ACTION_UP)
                    markerTouched(marker);
	            return true;
	        }
	    }
		return super.onTouchEvent(me);
	}

    /**
     * Default onTouch method for POI Markers.
     * Currently no default implementation.
     * Overridden in MainActivity.
     * @param marker POI POIMarker which was selected by user
     */
	protected void markerTouched(POIMarker marker) {
		Log.w(TAG, "markerTouched() not implemented.");
	}
}