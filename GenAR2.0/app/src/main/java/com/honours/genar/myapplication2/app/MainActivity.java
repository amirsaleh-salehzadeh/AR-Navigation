package com.honours.genar.myapplication2.app;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import augmentation.AugmentedActivity;
import datamanipulation.NetworkDataSource;
import datamanipulation.POIDAO;
import datamanipulation.ServerDataSource;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Main Activity of the Application
 * Extends Augmented Activity
 * Fetches POI Data to be shown to the user
 */
public class MainActivity extends AugmentedActivity {
    // LogCat Reference
    private static final String TAG = "MainActivity";
    private static final String locale = "en";
    // Used to simplify threading
    private static final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);
    // thread pool
    private static final ThreadPoolExecutor exeService = new ThreadPoolExecutor(1, 1, 20, TimeUnit.SECONDS, queue);
    // Store various sources (might not need as only have 1 source)
	private static final Map<String, NetworkDataSource> sources = new ConcurrentHashMap<String,NetworkDataSource>();

    public static ServerDataSource serverData;

    public static String fromTAG = null;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showRadar = !PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(SettingsActivity.PREF_KEY_HIDE_RADAR, true);
        showZoomBar = !PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(SettingsActivity.PREF_KEY_HIDE_ZOOM_BAR, true);
        AugmentedActivity.setMaxZoom(Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(SettingsActivity.PREF_KEY_MAXIMUM_RADIUS, "100000")));
        ARData.setDetailedRadius(Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(SettingsActivity.PREF_KEY_DETAILED_RADIUS, "10000")));
        ARData.setUseImageIcons(!PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(SettingsActivity.PREF_KEY_DISABLE_IMAGE_ICONS, true));



        // Connect to Server
        ServerDataSource.applicationConext = getApplicationContext();
        ServerDataSource.fetchRoles();

        Log.d(TAG,"onCreate() Hit");
    }

	@Override
    public void onStart() {
        super.onStart();
        // Obtain the last location data and update data
        Location last = ARData.getCurrentLocation();
        updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
        Log.d(TAG, "onStart() Hit");
    }

    @Override
    public void onResume(){
        super.onResume();

        if (fromTAG == null) return;

        if (fromTAG.equals(CollectionsActivity.TAG)){
            //ServerDataSource.fetchPOIMarkers();

        }
        else if (fromTAG.equals(RolesActivity.TAG)) {
            ServerDataSource.fetchPOICollections();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(TAG, "onOptionsItemSelected() item=" + item);
        switch (item.getItemId()) {
            case R.id.viewAllRoles:
                Intent rolesIntent = new Intent(this, RolesActivity.class);
                startActivity(rolesIntent);
                break;
            case R.id.viewAllCollections:
                Intent collectionsIntent = new Intent(this, CollectionsActivity.class);
                startActivity(collectionsIntent);
                break;
            case R.id.settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.exit:
                finish();
                break;
        }
        return true;
    }

	@Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        
        updateData(location.getLatitude(), location.getLongitude(), location.getAltitude());
    }

    /**
     * Overridden method handling what happens when a marker is selected
     * Launches Intent for Details activity
     * @param marker POI marker being selected
     */
	@Override
	protected void markerTouched(POIMarker marker) {

        Intent intent = new Intent(this,POIDetailsActivity.class);

        if (marker.getCollection() == null){
            intent.putExtra("Name", marker.getName());
            intent.putExtra("Desc", marker.getDescription());

            intent.putExtra("lat",marker.getLattitude());
            intent.putExtra("lon",marker.getLongitude());

            intent.putExtra("Image",marker.getImage());

            startActivity(intent);
            return;
        }

        intent.putExtra("Name", marker.getName());
        intent.putExtra("Desc", marker.getDescription());

        intent.putExtra("lat",marker.getLattitude());
        intent.putExtra("lon",marker.getLongitude());

        if (marker.getImage() != null){
            intent.putExtra("Image",marker.getImage());
        }

        if (marker.getAudio() != null) {
            intent.putExtra("Audio",marker.getAudio());
        }

        if (marker.getVideoLink() != null) {
            intent.putExtra("Video",marker.getVideoLink());
        }

        if (marker.getWebsiteLink() != null){
            intent.putExtra("Web",marker.getWebsiteLink());
        }

        startActivity(intent);

	}

    @Override
	protected void updateDataOnZoom() {
	    super.updateDataOnZoom();
        Location last = ARData.getCurrentLocation();
        updateData(last.getLatitude(), last.getLongitude(), last.getAltitude());
	}
    
    private void updateData(final double lat, final double lon, final double alt) {
        try {
            exeService.execute(
                new Runnable() {
                    public void run() {
                        for (NetworkDataSource source : sources.values())
                            download(source, lat, lon, alt);
                    }
                }
            );
        } catch (RejectedExecutionException rej) {
            Log.w(TAG, "Not running new download Runnable, queue is full.");
        } catch (Exception e) {
            Log.e(TAG, "Exception running download Runnable.", e);
        }
    }
    
    private static void download(NetworkDataSource source, double lat, double lon, double alt) {

        if (source == null) return;
		
		String url;
		try {
			url = source.createRequestURL(lat, lon, alt, ARData.getRadius(), locale);    	
		} catch (NullPointerException e) {
            return;
		}
    	
		List<POIMarker> markers;
		try {
			markers = source.parse(url);
		} catch (NullPointerException e) {
            return;
		}

    	ARData.addMarkers(markers);
    }


}