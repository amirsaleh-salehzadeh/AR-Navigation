package com.honours.genar.myapplication2.app;

import android.graphics.*;
import android.location.Location;
import android.os.AsyncTask;

import java.io.InputStream;

public class POICollection implements Comparable<POICollection>{

    private static final String DEF_BIT = "http://poiinfo.csdev.nmmu.ac.za/Image/Collections/default.jpg";
    private int ID;
    private String name, description, password;
    private boolean active;
    private double lattitude;
    private double longitude;
    private String image;
    private Bitmap bitmap;


    public POICollection() {
        super();
    }

    public POICollection(int ID, String name, String description, String password, String image, boolean active) {
        this.ID = ID;
        this.name = name;
        this.description = description;
        this.password = password;
        this.active = active;
        this.image = image;
        if (image != null) new DownloadImage().execute(image);
        else
        new DownloadImage().execute(DEF_BIT);

    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getLattitude() {
        return lattitude;
    }

    public void setLattitude(double lattitude) {
        this.lattitude = lattitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistance(){
        float[] distanceArray = new float[1];
        Location.distanceBetween(ARData.getCurrentLocation().getLatitude(),ARData.getCurrentLocation().getLongitude(),lattitude,longitude,distanceArray);
        return distanceArray[0];
    }

    public Bitmap getBitmap(){
        return bitmap;
    }

    public void setImage(String image){
        if (image != null) new DownloadImage().execute(image);
        else
            new DownloadImage().execute(DEF_BIT);
    }

    @Override
    public int compareTo(POICollection poiCollection) {
        if (poiCollection==null) throw new NullPointerException();

        return name.compareTo(poiCollection.getName());
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
            bitmap = getRoundedShape(result);
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

    public Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 200;
        int targetHeight = 200;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }
}
