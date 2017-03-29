package augmentation;

import android.app.Activity;
import android.content.Context;
import android.hardware.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import com.honours.genar.myapplication2.app.ARData;
import com.honours.genar.myapplication2.app.Matrix;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Activity which handles the sensor events.
 * Attains instances of all sensors.
 * Has methods to handle the various sensors.
 * No UI.
 * Provides rotation matrix used to convert lattitude and longitude of POI's to X and Y coordinates.
 * Augmented Activity extends this class.
 */
public class SensorsActivity extends Activity implements SensorEventListener, LocationListener {

    private static final String TAG = "SensorsActivity";

    // Used to check if a task is currently in progress
    private static final AtomicBoolean computing = new AtomicBoolean(false);

    // Minimum Time and Distance between location updates
    private static final int MIN_TIME = 30*1000;
    private static final int MIN_DISTANCE = 10;

    // Temporary array used while rotating
    private static final float temp[] = new float[9];
    // Final rotated matrix
    private static final float rotation[] = new float[9];
    // Gravity values
    private static final float grav[] = new float[3];
    // Magnetic field values
    private static final float mag[] = new float[3];

    // Stores location of device on the world
    private static final Matrix worldCoord = new Matrix();
    // Compensation between magnetic and true north
    private static final Matrix magneticCompensatedCoord = new Matrix();
    private static final Matrix magneticNorthCompensation = new Matrix();
    // Stores matrix after being rotated 90deg along the X-Axis
    private static final Matrix xAxisRotation = new Matrix();


    private static GeomagneticField gmf = null;
    private static float smooth[] = new float[3];
    private static SensorManager sensorMgr = null;
    private static List<Sensor> sensors = null;
    private static Sensor sensorGrav = null;
    private static Sensor sensorMag = null;
    private static LocationManager locationMgr = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

	@Override
    public void onStart() {
        super.onStart();

        // Set values of X-Axis Rotation Matrix

        double angleX = Math.toRadians(-90);
        double angleY = Math.toRadians(-90);

        xAxisRotation.set( 1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math.sin(angleX), 0f,
                (float) Math.sin(angleX),(float) Math.cos(angleX));

        // Obtain Sensors

        try {
            sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

            sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
            
            if (sensors.size() > 0) 
            {
            	sensorGrav = sensors.get(0);
            }
            
            sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

            if (sensors.size() > 0) 
            {
            	sensorMag = sensors.get(0);
            }
            sensorMgr.registerListener(this, sensorGrav, SensorManager.SENSOR_DELAY_NORMAL);
            sensorMgr.registerListener(this, sensorMag, SensorManager.SENSOR_DELAY_NORMAL);

            locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DISTANCE, this);

            try {

                try {
                    Location gps = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location network = locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(gps != null)
                    {
                        onLocationChanged(gps);
                    }
                    else if (network != null)
                    {
                        onLocationChanged(network);
                    }
                    else
                    {
                        onLocationChanged(ARData.hardFix);
                    }
                } catch (Exception ex2) {
                    onLocationChanged(ARData.hardFix);
                }

                gmf = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(),
                                           (float) ARData.getCurrentLocation().getLongitude(),
                                           (float) ARData.getCurrentLocation().getAltitude(), 
                                           System.currentTimeMillis());

                // Re-assign value of AngleY to negative declination
                angleY = Math.toRadians(-gmf.getDeclination());

                synchronized (magneticNorthCompensation) {

                    magneticNorthCompensation.toIdentity();

                    magneticNorthCompensation.set((float) Math.cos(angleY), 0f, (float) Math.sin(angleY), 0f, 1f, 0f,
                                                  (float) -Math.sin(angleY), 0f, (float) Math.cos(angleY));
    
                    magneticNorthCompensation.prod(xAxisRotation);
                }
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
        } catch (Exception ex1) {
            // Unregister sensors
            try {
                if (sensorMgr != null) {
                    sensorMgr.unregisterListener(this, sensorGrav);
                    sensorMgr.unregisterListener(this, sensorMag);
                    sensorMgr = null;
                }
                if (locationMgr != null) {
                    locationMgr.removeUpdates(this);
                    locationMgr = null;
                }
            } catch (Exception ex2) {
            	ex2.printStackTrace();
            }
        }
    }

	@Override
    protected void onStop() {
        super.onStop();

        // Release sensors

        try {
            try {
                sensorMgr.unregisterListener(this, sensorGrav);
                sensorMgr.unregisterListener(this, sensorMag);
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
            sensorMgr = null;

            try {
                locationMgr.removeUpdates(this);
            } catch (Exception ex) {
            	ex.printStackTrace();
            }
            locationMgr = null;
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }

    /**
     * Handles the changing of sensors by re-assigning the Gravity and Magnetic Field values.
     * Values are passed through a LowPassFilter before being assigned.
     * @param evt Type of sensor event that triggered the method (Accelerometer or Magnetic Field)
     */
    public void onSensorChanged(SensorEvent evt) {

        // If computing variable is true, return, otherwise set to true.
    	if (!computing.compareAndSet(false, true)) return;

        if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            smooth = LowPassFilter.filter(0.5f, 1.0f, evt.values, grav);
            grav[0] = smooth[0];
            grav[1] = smooth[1];
            grav[2] = smooth[2];
        } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smooth = LowPassFilter.filter(2.0f, 4.0f, evt.values, mag);
            mag[0] = smooth[0];
            mag[1] = smooth[1];
            mag[2] = smooth[2];
        }

        // Get real-world coordinates and store in temp
        SensorManager.getRotationMatrix(temp, null, grav, mag);

        // Remap to work with Landscape device
        SensorManager.remapCoordinateSystem(temp, SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X, rotation);

        // Convert rotation array to a matrix
        worldCoord.set(rotation[0], rotation[1], rotation[2], rotation[3], rotation[4], rotation[5], rotation[6], rotation[7], rotation[8]);

        magneticCompensatedCoord.toIdentity();

        synchronized (magneticNorthCompensation) {
            magneticCompensatedCoord.prod(magneticNorthCompensation);
        }

        magneticCompensatedCoord.prod(worldCoord);

        magneticCompensatedCoord.invert(); 

        //Roatation matrix used to convert Lat and Long of POI's to X and Y coordinates for display.
        ARData.setRotationMatrix(magneticCompensatedCoord);

        computing.set(false);
    }

    public void onProviderDisabled(String provider) {
        //Not Used
    }
	
    public void onProviderEnabled(String provider) {
        //Not Used
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Not Used
    }

    /**
     * Handles the change in GPS coordinates of the device.
     * Updates currentLocation in ARData.
     * Compensates for Magnetic Declination
     * @param location
     */
    public void onLocationChanged(Location location) {

        // Set new Location
        ARData.setCurrentLocation(location);
        gmf = new GeomagneticField((float) ARData.getCurrentLocation().getLatitude(),
                (float) ARData.getCurrentLocation().getLongitude(),
                (float) ARData.getCurrentLocation().getAltitude(), 
                System.currentTimeMillis());

        // Same Calculations as onStart()
        double angleY = Math.toRadians(-gmf.getDeclination());

        synchronized (magneticNorthCompensation) {
            magneticNorthCompensation.toIdentity();

            magneticNorthCompensation.set((float) Math.cos(angleY), 0f, (float) Math.sin(angleY), 0f, 1f, 0f,
                    (float) -Math.sin(angleY), 0f, (float) Math.cos(angleY));
    
            magneticNorthCompensation.prod(xAxisRotation);
        }
    }

    /**
     * Notifies of any unreliable Magnetic Field values
     * @param sensor Type of sensor
     * @param accuracy Accuracy value of the sensor
     */
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

        if (sensor == null) throw new NullPointerException();
		
        if(sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD && accuracy== SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.e(TAG, "Compass data unreliable");
        }
    }
}