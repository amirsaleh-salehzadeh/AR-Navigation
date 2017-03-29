package com.honours.genar.myapplication2.app;

import android.location.Location;
import android.util.Log;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Global control and storage class (simpler to store all data in one place)
 */
public abstract class ARData {

    private static final String TAG = "ARData";

    // Lists of POI's, Collections and Roles
	private static final Map<String,POIMarker> markerList = new ConcurrentHashMap<String,POIMarker>();
    private static final Map<String, POICollection> collectionList = new ConcurrentHashMap<String,POICollection>();
    private static final Map<String,Role> roleList = new ConcurrentHashMap<String,Role>();
    private static final ArrayList<String> accessedRoleList = new ArrayList<String>();
    private static final List<POIMarker> marker_cache = new CopyOnWriteArrayList<POIMarker>();
    private static final List<POICollection> collection_cache = new CopyOnWriteArrayList<POICollection>();
    private static final List<Role> role_cache = new CopyOnWriteArrayList<Role>();

    private static Role currentRole =  null;

    private static Account currentAccount = null;

    // Atomic boolean indicates state.
    // Atomic boolean used as you can compare and set.
    private static final AtomicBoolean unsafe = new AtomicBoolean(false);
    // Array of Location Data
    private static final float[] locationArray = new float[3];
    //Default location
    public static final Location hardFix = new Location("Default");
    static {
        hardFix.setLatitude(0);
        hardFix.setLongitude(0);
        hardFix.setAltitude(1);
    }

    private static final Object radiusLock = new Object();
    private static float radius = new Float(20);
    private static String zoomLevel = "";
    private static final Object zoomProgressLock = new Object();
    private static int zoomProgress = 0;
    private static Location currentLocation = hardFix;
    private static Matrix rotationMatrix = new Matrix();
    private static final Object azimuthLock = new Object();
    private static float azimuth = 0;
    private static final Object pitchLock = new Object();
    private static float pitch = 0;
    private static final Object rollLock = new Object();
    private static float roll = 0;

    private static float detailedRadius = 1000;
    private static Boolean useImageIcons = false;

    public static void setDetailedRadius(float detailedRadius) {
        ARData.detailedRadius = detailedRadius;
    }

    public static void setZoomLevel(String zoomLevel) {
    	if (zoomLevel==null) throw new NullPointerException();

    	synchronized (ARData.zoomLevel) {
    	    ARData.zoomLevel = zoomLevel;
    	}
    }

    public static void setZoomProgress(int zoomProgress) {
        synchronized (ARData.zoomProgressLock) {
            if (ARData.zoomProgress != zoomProgress) {
                ARData.zoomProgress = zoomProgress;
                if (unsafe.compareAndSet(false, true)) {
                    Log.v(TAG, "Setting unsafe flag!");
                    marker_cache.clear();
                    collection_cache.clear();
                }
            }
        }
    }

    public static void setRadius(float radius) {
        synchronized (ARData.radiusLock) {
            ARData.radius = radius;
        }
    }

    public static float getRadius() {
        synchronized (ARData.radiusLock) {
            return ARData.radius;
        }
    }

    public static void setCurrentLocation(Location currentLocation) {
    	if (currentLocation==null) throw new NullPointerException();

    	Log.d(TAG, "current location. location=" + currentLocation.toString());
    	synchronized (currentLocation) {
    	    ARData.currentLocation = currentLocation;
    	}
        onLocationChanged(currentLocation);
    }

    public static Location getCurrentLocation() {
        synchronized (ARData.currentLocation) {
            return ARData.currentLocation;
        }
    }

    public static void setRotationMatrix(Matrix rotationMatrix) {
        synchronized (ARData.rotationMatrix) {
            ARData.rotationMatrix = rotationMatrix;
        }
    }

    public static Matrix getRotationMatrix() {
        synchronized (ARData.rotationMatrix) {
            return rotationMatrix;
        }
    }

    public static List<POIMarker> getMarkers() {
        if (unsafe.compareAndSet(true, false)) {
            Log.v(TAG, "unsafe flag found, resetting all marker heights to zero.");
            for(POIMarker ma : markerList.values()) {
                ma.getLocation().get(locationArray);
                locationArray[1] = ma.getInitialY();
                ma.getLocation().set(locationArray);
            }

            Log.v(TAG, "Populating the marker_cache.");
            List<POIMarker> copy = new ArrayList<POIMarker>();
            copy.addAll(markerList.values());
            Collections.sort(copy, marker_comparator);
            marker_cache.clear();
            marker_cache.addAll(copy);
        }
        return Collections.unmodifiableList(marker_cache);
    }

    public static List<POICollection> getPOICollections() {

        Log.v(TAG, "Populating the collection_cache.");
        List<POICollection> copy = new ArrayList<POICollection>();
        copy.addAll(collectionList.values());
        Collections.sort(copy, collection_comparator);
        collection_cache.clear();
        collection_cache.addAll(copy);

        for(POICollection c : collection_cache){
            if (c.getDistance() > 100000){
                collection_cache.remove(c);
            }
        }

        return Collections.unmodifiableList(collection_cache);
    }

    public static List<Role> getRoles() {

        Log.v(TAG, "Populating the role_cache.");
        List<Role> copy = new ArrayList<Role>();
        copy.addAll(roleList.values());
        Collections.sort(copy, role_comparator);
        role_cache.clear();
        role_cache.addAll(copy);

        return Collections.unmodifiableList(role_cache);
    }

    public static List<String> getAccessedRoles() {

        return Collections.unmodifiableList(accessedRoleList);
    }

    public static void clearMarkers(){
        markerList.clear();
        marker_cache.clear();
    }

    public static void clearPOIMarkers(){

        for (POIMarker m : markerList.values()){
            if (m.getCollection() != null){
                markerList.remove(m);
            }
        }

        for (POIMarker m : marker_cache){
            if (m.getCollection() != null){
                marker_cache.remove(m);
            }
        }

    }

    public static void clearPOICollections(){
        collectionList.clear();
        collection_cache.clear();
    }

    public static void clearRoles(){
        roleList.clear();
        role_cache.clear();
    }

    public static void clearAccessedRoles(){
        accessedRoleList.clear();
    }

    public static Role getCurrentRole() {
        return currentRole;
    }

    public static void setCurrentRole(Role currentRole) {
        ARData.currentRole = currentRole;
    }

    public static Account getCurrentAccount() {
        return currentAccount;
    }

    public static void setCurrentAccount(Account currentAccount) {
        ARData.currentAccount = currentAccount;
    }

    public static Boolean getUseImageIcons() {
        return useImageIcons;
    }

    public static void setUseImageIcons(Boolean useImageIcons) {
        ARData.useImageIcons = useImageIcons;
    }

    public static void setAzimuth(float azimuth) {
        synchronized (azimuthLock) {
            ARData.azimuth = azimuth;
        }
    }

    public static float getAzimuth() {
        synchronized (azimuthLock) {
            return ARData.azimuth;
        }
    }

    public static void setPitch(float pitch) {
        synchronized (pitchLock) {
            ARData.pitch = pitch;
        }
    }

    public static float getPitch() {
        synchronized (pitchLock) {
            return ARData.pitch;
        }
    }

    public static void setRoll(float roll) {
        synchronized (rollLock) {
            ARData.roll = roll;
        }
    }

    public static float getRoll() {
        synchronized (rollLock) {
            return ARData.roll;
        }
    }

    private static final Comparator<POIMarker> marker_comparator = new Comparator<POIMarker>() {
        public int compare(POIMarker arg0, POIMarker arg1) {
            return Double.compare(arg0.getDistance(), arg1.getDistance());
        }
    };

    private static final Comparator<POICollection> collection_comparator = new Comparator<POICollection>() {
        public int compare(POICollection arg0, POICollection arg1) {
            return arg0.compareTo(arg1);
        }
    };

    private static final Comparator<Role> role_comparator = new Comparator<Role>() {
        public int compare(Role arg0, Role arg1) {
            return arg0.compareTo(arg1);
        }
    };

    public static void addMarkers(Collection<POIMarker> markers) {
    	if (markers==null) throw new NullPointerException();

    	if (markers.size()<=0) return;

    	Log.d(TAG, "New markers, updating markers. new markers=" + markers.toString());
    	for(POIMarker marker : markers) {
    	    if (!markerList.containsKey(marker.getName())) {
    	        marker.calcRelativePosition(ARData.getCurrentLocation());
    	        markerList.put(marker.getName(),marker);
    	    }
    	}

    	if (unsafe.compareAndSet(false, true)) {
    	    Log.v(TAG, "Setting unsafe flag!");
    	    marker_cache.clear();
    	}
    }

    public static void addPOICollections(Collection<POICollection> poiCollections) {
        if (poiCollections==null) throw new NullPointerException();

        if (poiCollections.size()<=0) return;

        Log.d(TAG, "New POICollections, updating POICollections. new POICOllections=" + poiCollections.toString());
        for(POICollection coll : poiCollections) {
            if (!collectionList.containsKey(coll.getName())) {
                collectionList.put(coll.getName(),coll);
            }
        }

        /*if (unsafe.compareAndSet(false, true)) {
            Log.v(TAG, "Setting unsafe flag!");
            collection_cache.clear();
        }*/
    }

    public static void addRoles(Collection<Role> roles) {
        if (roles == null) throw new NullPointerException();

        if (roles.size()<=0) return;

        Log.d(TAG, "New roles, updating Roles. new roles=" + roles.toString());
        for(Role r : roles) {
            if (!roleList.containsKey(r.getName())) {
                roleList.put(r.getName(),r);
            }
        }
        /*if (unsafe.compareAndSet(false, true)) {
            Log.v(TAG, "Setting unsafe flag!");
            collection_cache.clear();
        }*/
    }

    public static void addAccessedRoles(Collection<String> accessedRoles) {
        if (accessedRoles == null) throw new NullPointerException();

        if (accessedRoles.size()<=0) return;

        Log.d(TAG, "New Accessed Roles, updating Roles. new roles=" + accessedRoles.toString());
        for(String r : accessedRoles) {
            if (!accessedRoleList.contains(r)) {
                accessedRoleList.add(r);
            }
        }
    }

    public static boolean CollectionInRadius(String collectionName){

        if (!collectionList.containsKey(collectionName)) return false;

        POICollection collection = collectionList.get(collectionName);

        float[] distanceArray = new float[1];

        Location.distanceBetween(getCurrentLocation().getLatitude(),getCurrentLocation().getLongitude(),collection.getLattitude(), collection.getLongitude(),distanceArray);

        return distanceArray[0] < detailedRadius;
    }

    private static void onLocationChanged(Location location) {
        Log.d(TAG, "New location, updating markers. location=" + location.toString());
        for(POIMarker ma: markerList.values()) {
            ma.calcRelativePosition(location);
        }

        if (unsafe.compareAndSet(false, true)) {
            Log.v(TAG, "Setting unsafe flag!");
            marker_cache.clear();
            collection_cache.clear();
        }
    }


}