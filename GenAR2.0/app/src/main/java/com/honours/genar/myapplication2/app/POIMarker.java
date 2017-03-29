package com.honours.genar.myapplication2.app;

import android.graphics.*;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import augmentation.CameraModel;
import augmentation.PhysicalLocationUtility;
import augmentation.Utilities;
import paintables.*;

import java.io.InputStream;
import java.text.DecimalFormat;

/**
 * POI POIMarker
 * Handles majority of the coding related to the markers.
 * Calculates if the marker should be displayed on the screen or not
 * Draws image and text of the marker
 */
public class POIMarker implements Comparable<POIMarker> {

    // Format of the distance shown on the Radar
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("@#");

    // Vectors used to find the location of the text and symbol using the rotaion matrix
    private static final Vector symbolVector = new Vector(0, 0, 0);
    private static final Vector textVector = new Vector(0, 1, 0);

    // Used in positioning and drwing the marker symbol and text
    private final Vector screenPositionVector = new Vector();
    private final Vector tmpSymbolVector = new Vector();
    private final Vector tmpVector = new Vector();
    private final Vector tmpTextVector = new Vector();
    private final float[] distanceArray = new float[1];
    private final float[] locationArray = new float[3];
    private final float[] screenPositionArray = new float[3];

    // Initial Y-Axis position for each marker
    private float initialY = 0.0f;

    private volatile static CameraModel cam = null;

    private volatile PaintableBoxedText textBox = null;
    private volatile PaintablePosition textContainer = null;

    protected final float[] symbolArray = new float[3];
    protected final float[] textArray = new float[3];

    protected volatile PaintableObject gpsSymbol = null;
    protected volatile PaintablePosition symbolContainer = null;

    private int ID;
    // Name of POI
    private String name = null;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    // Location of POI
    private volatile PhysicalLocationUtility physicalLocation = new PhysicalLocationUtility();
    // Description
    private volatile String description = null;
    // Image of the POI TODO: Download and store image
    private volatile String image = null;
    // ic_audio file associated to POI
    private volatile String audio = null;
    // Link to video of POI
    private volatile String videoLink = null;
    // Link to website of POI
    private volatile String websiteLink = null;
    // Collection of POIMarker
    private volatile String collection;

    private volatile Bitmap bitmap = null;

    // Distance from user to POI
    private volatile double distance = 0.0;

    private volatile boolean isOnRadar = false;
    private volatile boolean isInView = false;
    protected final Vector symbolXyzRelativeToCameraView = new Vector();
    protected final Vector textXyzRelativeToCameraView = new Vector();
    private final Vector locationXyzRelativeToPhysicalLocation = new Vector();
    private int color = Color.WHITE;

    private static boolean debugTouchZone = false;
    private static PaintableBox touchBox = null;
    private static PaintablePosition touchPosition = null;

    private static boolean debugCollisionZone = false;
    private static PaintableBox collisionBox = null;
    private static PaintablePosition collisionPosition = null;

    public POIMarker(String name, double latitude, double longitude, double altitude, String description, String image, String audio, String videoLink, String websiteLink, String collection, int color) {
        set(name, latitude, longitude, altitude, description, image, audio, videoLink, websiteLink, collection, color);

    }

    public POIMarker(){}

    private synchronized void set(String name, double latitude, double longitude, double altitude, String description, String image, String audio, String videoLink, String websiteLink, String collection, int color) {
        if (name == null) throw new NullPointerException();

        this.name = name;
        this.physicalLocation.set(latitude, longitude, altitude);
        this.description = description;
        this.image = image;
        this.audio = audio;
        this.videoLink = videoLink;
        this.websiteLink = websiteLink;
        this.collection = collection;
        this.color = color;
        this.isOnRadar = false;
        this.isInView = false;
        this.symbolXyzRelativeToCameraView.set(0, 0, 0);
        this.textXyzRelativeToCameraView.set(0, 0, 0);
        this.locationXyzRelativeToPhysicalLocation.set(0, 0, 0);
        this.initialY = 0.0f;

        Location.distanceBetween(physicalLocation.getLatitude(), physicalLocation.getLongitude(), latitude, longitude, distanceArray);
        distance = distanceArray[0];

        new DownloadImage().execute(image);

    }

    public synchronized String getName() {
        return this.name;
    }

    public synchronized String getDescription() {
        return description;
    }

    public synchronized String getImage() {
        return image;
    }

    public synchronized String getAudio() {
        return audio;
    }

    public synchronized String getVideoLink() {
        return videoLink;
    }

    public synchronized String getWebsiteLink() {
        return websiteLink;
    }

    public synchronized int getColor() {
        return this.color;
    }

    public void setColor(int c){color = c;}

    public synchronized double getDistance() {
        return this.distance;
    }

    public synchronized float getInitialY() {
        return this.initialY;
    }

    public synchronized boolean isOnRadar() {
        return this.isOnRadar;
    }

    public synchronized boolean isInView() {
        return this.isInView;
    }

    private synchronized Vector getScreenPosition() {
        symbolXyzRelativeToCameraView.get(symbolArray);
        textXyzRelativeToCameraView.get(textArray);
        float x = (symbolArray[0] + textArray[0]) / 2;
        float y = (symbolArray[1] + textArray[1]) / 2;
        float z = (symbolArray[2] + textArray[2]) / 2;

        if (textBox != null) y += (textBox.getHeight() / 2);

        screenPositionVector.set(x, y, z);
        return screenPositionVector;
    }

    public synchronized Vector getLocation() {
        return this.locationXyzRelativeToPhysicalLocation;
    }

    public synchronized double getLattitude(){
        return physicalLocation.getLatitude();
    }

    public synchronized double getLongitude(){
        return physicalLocation.getLongitude();
    }

    public synchronized String getCollection(){
        return collection;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
        new DownloadImage().execute(image);
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public void setVideoLink(String videoLink) {
        this.videoLink = videoLink;
    }

    public void setWebsiteLink(String websiteLink) {
        this.websiteLink = websiteLink;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public void setLatLong(double latitude, double longitude){
        Location.distanceBetween(physicalLocation.getLatitude(), physicalLocation.getLongitude(), latitude, longitude, distanceArray);
        distance = distanceArray[0];
    }

    private synchronized float getHeight() {
        if (symbolContainer == null || textContainer == null) return 0f;
        return symbolContainer.getHeight() + textContainer.getHeight();
    }

    private synchronized float getWidth() {
        if (symbolContainer == null || textContainer == null) return 0f;
        float w1 = textContainer.getWidth();
        float w2 = symbolContainer.getWidth();
        return (w1 > w2) ? w1 : w2;
    }

    public synchronized void update(Canvas canvas, float addX, float addY) {
        if (canvas == null) throw new NullPointerException();

        if (cam == null) cam = new CameraModel(canvas.getWidth(), canvas.getHeight(), true);
        cam.set(canvas.getWidth(), canvas.getHeight(), false);
        cam.setViewAngle(CameraModel.DEFAULT_VIEW_ANGLE);
        populateMatrices(cam, addX, addY);
        updateRadar();
        updateView();
    }

    private synchronized void populateMatrices(CameraModel cam, float addX, float addY) {
        if (cam == null) throw new NullPointerException();


        tmpSymbolVector.set(symbolVector);
        tmpSymbolVector.add(locationXyzRelativeToPhysicalLocation);
        tmpSymbolVector.prod(ARData.getRotationMatrix());

        tmpTextVector.set(textVector);
        tmpTextVector.add(locationXyzRelativeToPhysicalLocation);
        tmpTextVector.prod(ARData.getRotationMatrix());

        cam.projectPoint(tmpSymbolVector, tmpVector, addX, addY);
        symbolXyzRelativeToCameraView.set(tmpVector);
        symbolXyzRelativeToCameraView.setY(500);
        cam.projectPoint(tmpTextVector, tmpVector, addX, addY);
        textXyzRelativeToCameraView.set(tmpVector);
        textXyzRelativeToCameraView.setY(500);

    }

    private synchronized void updateRadar() {
        isOnRadar = false;


            float range = ARData.getRadius() * 1000;
            float scale = range / Radar.RADIUS;
            locationXyzRelativeToPhysicalLocation.get(locationArray);
            float x = locationArray[0] / scale;
            float y = locationArray[2] / scale; // z==y Switched on purpose
            symbolXyzRelativeToCameraView.get(symbolArray);
            if ((symbolArray[2] < -1f) && ((x * x + y * y) < (Radar.RADIUS * Radar.RADIUS))) {
                isOnRadar = true;
            }

    }

    private synchronized void updateView() {
        isInView = false;

        symbolXyzRelativeToCameraView.get(symbolArray);
        float x1 = symbolArray[0] + (getWidth() / 2);
        float y1 = symbolArray[1] + (getHeight() / 2);
        float x2 = symbolArray[0] - (getWidth() / 2);
        float y2 = symbolArray[1] - (getHeight() / 2);
        if (x1 >= -1 && x2 <= (cam.getWidth())){ // && y1 >= -1 && y2 <= (cam.getHeight())) {
            isInView = true;
        }
    }

    public synchronized void calcRelativePosition(Location location) {

        if (location == null) throw new NullPointerException();

        updateDistance(location);

        if (physicalLocation.getAltitude() == 0.0)
            physicalLocation.setAltitude(location.getAltitude());

        PhysicalLocationUtility.convLocationToVector(location, physicalLocation, locationXyzRelativeToPhysicalLocation);
        this.initialY = locationXyzRelativeToPhysicalLocation.getY();
        updateRadar();
    }

    private synchronized void updateDistance(Location location) {
        if (location == null) throw new NullPointerException();

        Location.distanceBetween(physicalLocation.getLatitude(), physicalLocation.getLongitude(), location.getLatitude(), location.getLongitude(), distanceArray);
        distance = distanceArray[0];
    }

    public synchronized boolean handleClick(float x, float y) {
        return !(!isOnRadar || !isInView) && isPointOnMarker(x, y, this);
    }

    public synchronized boolean isMarkerOnMarker(POIMarker marker) {
        return isMarkerOnMarker(marker, true);
    }

    private synchronized boolean isMarkerOnMarker(POIMarker marker, boolean reflect) {
        marker.getScreenPosition().get(screenPositionArray);
        float x = screenPositionArray[0];
        float y = screenPositionArray[1];
        boolean middleOfMarker = isPointOnMarker(x, y, this);
        if (middleOfMarker) return true;

        float halfWidth = marker.getWidth() / 2;
        float halfHeight = marker.getHeight() / 2;

        float x1 = x - halfWidth;
        float y1 = y - halfHeight;
        boolean upperLeftOfMarker = isPointOnMarker(x1, y1, this);
        if (upperLeftOfMarker) return true;

        float x2 = x + halfWidth;
        float y2 = y1;
        boolean upperRightOfMarker = isPointOnMarker(x2, y2, this);
        if (upperRightOfMarker) return true;

        float x3 = x1;
        float y3 = y + halfHeight;
        boolean lowerLeftOfMarker = isPointOnMarker(x3, y3, this);
        if (lowerLeftOfMarker) return true;

        float x4 = x2;
        float y4 = y3;
        boolean lowerRightOfMarker = isPointOnMarker(x4, y4, this);
        return lowerRightOfMarker || (reflect) && marker.isMarkerOnMarker(this, false);

    }

    private synchronized boolean isPointOnMarker(float x, float y, POIMarker marker) {
        marker.getScreenPosition().get(screenPositionArray);
        float myX = screenPositionArray[0];
        float myY = screenPositionArray[1];
        float adjWidth = marker.getWidth() / 2;
        float adjHeight = marker.getHeight() / 2;

        float x1 = myX - adjWidth;
        float y1 = myY - adjHeight;
        float x2 = myX + adjWidth;
        float y2 = myY + adjHeight;

        return x >= x1 && x <= x2 && y >= y1 && y <= y2;

    }

    public synchronized void draw(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        if (collection == null) {
            if (isOnRadar && isInView && !ARData.CollectionInRadius(name)) {
                drawIcon(canvas);
                drawText(canvas);
            }
        }
        else{
            if (!isOnRadar || !isInView || !ARData.CollectionInRadius(collection)) return;
            if (debugTouchZone) drawTouchZone(canvas);
            if (debugCollisionZone) drawCollisionZone(canvas);
            drawIcon(canvas);
            drawText(canvas);
        }
    }

    private synchronized void drawCollisionZone(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        getScreenPosition().get(screenPositionArray);
        float x = screenPositionArray[0];
        float y = screenPositionArray[1];

        float width = getWidth();
        float height = getHeight();
        float halfWidth = width / 2;
        float halfHeight = height / 2;

        float x1 = x - halfWidth;
        float y1 = y - halfHeight;

        float x2 = x + halfWidth;
        float y2 = y1;

        float x3 = x1;
        float y3 = y + halfHeight;

        float x4 = x2;
        float y4 = y3;

        Log.w("collisionBox", "ul (x=" + x1 + " y=" + y1 + ")");
        Log.w("collisionBox", "ur (x=" + x2 + " y=" + y2 + ")");
        Log.w("collisionBox", "ll (x=" + x3 + " y=" + y3 + ")");
        Log.w("collisionBox", "lr (x=" + x4 + " y=" + y4 + ")");

        if (collisionBox == null) collisionBox = new PaintableBox(width, height, Color.WHITE, Color.RED);
        else collisionBox.set(width, height);

        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]) + 90;

        if (collisionPosition == null) collisionPosition = new PaintablePosition(collisionBox, x1, y1, currentAngle, 1);
        else collisionPosition.set(collisionBox, x1, y1, currentAngle, 1);
        collisionPosition.paint(canvas);
    }

    private synchronized void drawTouchZone(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        if (gpsSymbol == null) return;

        symbolXyzRelativeToCameraView.get(symbolArray);
        textXyzRelativeToCameraView.get(textArray);
        float x1 = symbolArray[0];
        float y1 = symbolArray[1];
        float x2 = textArray[0];
        float y2 = textArray[1];
        float width = getWidth();
        float height = getHeight();
        float adjX = (x1 + x2) / 2;
        float adjY = (y1 + y2) / 2;
        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]) + 90;
        adjX -= (width / 2);
        adjY -= (gpsSymbol.getHeight() / 2);

        Log.w("touchBox", "ul (x=" + (adjX) + " y=" + (adjY) + ")");
        Log.w("touchBox", "ur (x=" + (adjX + width) + " y=" + (adjY) + ")");
        Log.w("touchBox", "ll (x=" + (adjX) + " y=" + (adjY + height) + ")");
        Log.w("touchBox", "lr (x=" + (adjX + width) + " y=" + (adjY + height) + ")");

        if (touchBox == null) touchBox = new PaintableBox(width, height, Color.WHITE, Color.GREEN);
        else touchBox.set(width, height);

        if (touchPosition == null) touchPosition = new PaintablePosition(touchBox, adjX, adjY, currentAngle, 1);
        else touchPosition.set(touchBox, adjX, adjY, currentAngle, 1);
        touchPosition.paint(canvas);
    }

    protected synchronized void drawPointIcon(Canvas canvas) {

        //if (canvas == null) throw new NullPointerException();

        if (gpsSymbol == null) gpsSymbol = new PaintableGps(50, 50, true, getColor());

        textXyzRelativeToCameraView.get(textArray);
        symbolXyzRelativeToCameraView.get(symbolArray);

        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]);
        float angle = 0;

        if (symbolContainer == null)
            symbolContainer = new PaintablePosition(gpsSymbol, symbolArray[0], symbolArray[1], angle, 1);
        else symbolContainer.set(gpsSymbol, symbolArray[0], symbolArray[1], angle, 1);

        symbolContainer.paint(canvas);
    }

    protected synchronized void drawIcon(Canvas canvas) {
        if (canvas==null) throw new NullPointerException();

        if (bitmap==null || !ARData.getUseImageIcons()){
            drawPointIcon(canvas);
        }
        else {
            if (gpsSymbol == null) gpsSymbol = new PaintableIcon(getCircleBitmap(bitmap), 300, 300);

            textXyzRelativeToCameraView.get(textArray);
            symbolXyzRelativeToCameraView.get(symbolArray);

            float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]);
            float angle = 0;

            if (symbolContainer == null)
                symbolContainer = new PaintablePosition(gpsSymbol, symbolArray[0], symbolArray[1], angle, 1);
            else symbolContainer.set(gpsSymbol, symbolArray[0], symbolArray[1], angle, 1);

            symbolContainer.paint(canvas);
        }
    }

    private synchronized void drawText(Canvas canvas) {
        if (canvas == null) throw new NullPointerException();

        String textStr;
        if (distance < 1000.0) {
            textStr = name + " (" + DECIMAL_FORMAT.format(distance) + "m)";
        } else {
            double d = distance / 1000.0;
            textStr = name + " (" + DECIMAL_FORMAT.format(d) + "km)";
        }

        textXyzRelativeToCameraView.get(textArray);
        symbolXyzRelativeToCameraView.get(symbolArray);

        float maxHeight = Math.round(canvas.getHeight() / 10f) + 1;
        if (textBox == null) textBox = new PaintableBoxedText(textStr, Math.round(maxHeight / 2f) + 1, 300);
        else textBox.set(textStr, Math.round(maxHeight / 2f) + 1, 300);

        float currentAngle = Utilities.getAngle(symbolArray[0], symbolArray[1], textArray[0], textArray[1]);
        float angle = 0;


        float x = textArray[0] - (textBox.getWidth() / 2);
        float y = 500 + maxHeight;

        if (textContainer == null) textContainer = new PaintablePosition(textBox, x, y, angle, 1);
        else textContainer.set(textBox, x, y, angle, 1);
        textContainer.paint(canvas);
    }

    public synchronized int compareTo(POIMarker another) {
        if (another == null) throw new NullPointerException();

        return name.compareTo(another.getName());
    }

    @Override
    public synchronized boolean equals(Object marker) {
        if (marker == null || name == null) throw new NullPointerException();

        return name.equals(((POIMarker) marker).getName());
    }

    // DownloadImage AsyncTask
    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            // Set the bitmap into ImageView
            bitmap = result;
        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }



}