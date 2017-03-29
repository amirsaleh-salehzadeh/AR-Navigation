package datamanipulation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import com.honours.genar.myapplication2.app.ARData;
import com.honours.genar.myapplication2.app.POICollection;
import com.honours.genar.myapplication2.app.POIMarker;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class POIDAO extends POI_DB_DAO {

    public static final String POI_ID_WITH_PREFIX = "poi.id";
    public static final String POI_NAME_WITH_PREFIX = "poi.name";
    public static final String POI_DESCRIPTION_WITH_PREFIX = "poi.description";
    public static final String POI_IMAGE_WITH_PREFIX = "poi.image";

    public static final String COLLECTION_NAME_WITH_PREFIX = "collection.name";
    public static final String COLLECTION_IMAGE_WITH_PREFIX = "collection.image";
    public static final String COLLECTION_DESCRIPTION_WITH_PREFIX = "collection.description";

    private static final String WHERE_ID_EQUALS = DatabaseHelper.ID_COLUMN + " =?";

    private static final SimpleDateFormat formatter = new SimpleDateFormat(
            "yyyy-MM-dd", Locale.ENGLISH);

    public POIDAO(Context context) {
        super(context);
    }

    public long save(POIMarker poi) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ID_COLUMN,poi.getID());
        values.put(DatabaseHelper.NAME_COLUMN, poi.getName());
        values.put(DatabaseHelper.POI_LATTITUDE, poi.getLattitude());
        values.put(DatabaseHelper.POI_LONGITUDE, poi.getLongitude());
        values.put(DatabaseHelper.DESCRIPTION_COLUMN, poi.getDescription());
        values.put(DatabaseHelper.IMAGE_COLUMN, poi.getImage());
        values.put(DatabaseHelper.POI_AUDIO, poi.getAudio());
        values.put(DatabaseHelper.POI_VIDEO, poi.getVideoLink());
        values.put(DatabaseHelper.POI_WEBSITE, poi.getWebsiteLink());
        values.put(DatabaseHelper.POI_COLLECTION_ID, poi.getCollection());

        return database.insert(DatabaseHelper.POI_TABLE, null, values);
    }

    public long update(POIMarker poi) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ID_COLUMN,poi.getID());
        values.put(DatabaseHelper.NAME_COLUMN, poi.getName());
        values.put(DatabaseHelper.POI_LATTITUDE, poi.getLattitude());
        values.put(DatabaseHelper.POI_LONGITUDE, poi.getLongitude());
        values.put(DatabaseHelper.DESCRIPTION_COLUMN, poi.getDescription());
        values.put(DatabaseHelper.IMAGE_COLUMN, poi.getImage());
        values.put(DatabaseHelper.POI_AUDIO, poi.getAudio());
        values.put(DatabaseHelper.POI_VIDEO, poi.getVideoLink());
        values.put(DatabaseHelper.POI_WEBSITE, poi.getWebsiteLink());
        values.put(DatabaseHelper.POI_COLLECTION_ID, poi.getCollection());

        long result = database.update(DatabaseHelper.POI_TABLE, values,
                WHERE_ID_EQUALS,
                new String[] { String.valueOf(poi.getID()) });
        Log.d("Update Result:", "=" + result);
        return result;
    }

    public int deletePOI(POIMarker poi) {
        return database.delete(DatabaseHelper.POI_TABLE, WHERE_ID_EQUALS,
                new String[] { poi.getID() + "" });
    }

    // METHOD 1
    // Uses rawQuery() to query multiple tables
    public ArrayList<POIMarker> getPOIs() {
        ArrayList<POIMarker> pois = new ArrayList<POIMarker>();

        for (POICollection c : ARData.getPOICollections()) {

            if (c.isActive()) {

                String query = "SELECT " + POI_ID_WITH_PREFIX + ","
                        + POI_NAME_WITH_PREFIX + ","
                        + DatabaseHelper.POI_LATTITUDE + ","
                        + DatabaseHelper.POI_LONGITUDE + ","
                        + POI_DESCRIPTION_WITH_PREFIX + ","
                        + POI_IMAGE_WITH_PREFIX + ","
                        + DatabaseHelper.POI_AUDIO + ","
                        + DatabaseHelper.POI_VIDEO + ","
                        + DatabaseHelper.POI_WEBSITE + ","
                        + DatabaseHelper.POI_COLLECTION_ID + ","
                        + COLLECTION_NAME_WITH_PREFIX + ","
                        + COLLECTION_DESCRIPTION_WITH_PREFIX + ","
                        + COLLECTION_IMAGE_WITH_PREFIX + " FROM "
                        + DatabaseHelper.POI_TABLE + " poi, "
                        + DatabaseHelper.COLLECTION_TABLE + " collection WHERE poi." + DatabaseHelper.POI_COLLECTION_ID + " = collection." + DatabaseHelper.ID_COLUMN
                        + " AND " + COLLECTION_NAME_WITH_PREFIX + " = " + c.getName();

                Random random = new Random();
                int r = random.nextInt(256);
                int g = random.nextInt(256);
                int b = random.nextInt(256);

                Log.d("query", query);
                Cursor cursor = database.rawQuery(query, null);
                while (cursor.moveToNext()) {

                    POIMarker poi = new POIMarker();
                    poi.setID(cursor.getInt(0));
                    poi.setName(cursor.getString(1));
                    poi.setLatLong(cursor.getDouble(2), cursor.getDouble(3));
                    poi.setDescription(cursor.getString(4));
                    poi.setImage(cursor.getString(5));
                    poi.setAudio(cursor.getString(6));
                    poi.setVideoLink(cursor.getString(7));
                    poi.setWebsiteLink(cursor.getString(8));
                    poi.setColor(Color.rgb(r, g, b));

                    POICollection poiCollection = new POICollection();
                    poiCollection.setID(cursor.getInt(9));
                    poiCollection.setName(cursor.getString(10));
                    poiCollection.setDescription(cursor.getString(11));
                    poiCollection.setImage(cursor.getString(12));

                    poi.setCollection(poiCollection.getName());

                    pois.add(poi);
                }
            }
        }




        String query = "SELECT " + POI_ID_WITH_PREFIX + ","
                + POI_NAME_WITH_PREFIX + ","
                + DatabaseHelper.POI_LATTITUDE + ","
                + DatabaseHelper.POI_LONGITUDE + ","
                + POI_DESCRIPTION_WITH_PREFIX + ","
                + POI_IMAGE_WITH_PREFIX + ","
                + DatabaseHelper.POI_AUDIO + ","
                + DatabaseHelper.POI_VIDEO + ","
                + DatabaseHelper.POI_WEBSITE + ","
                + DatabaseHelper.POI_COLLECTION_ID + ","
                + COLLECTION_NAME_WITH_PREFIX + ","
                + COLLECTION_DESCRIPTION_WITH_PREFIX + ","
                + COLLECTION_IMAGE_WITH_PREFIX + " FROM "
                + DatabaseHelper.POI_TABLE + " poi, "
                + DatabaseHelper.COLLECTION_TABLE + " collection WHERE poi."
                + DatabaseHelper.POI_COLLECTION_ID + " = collection."
                + DatabaseHelper.ID_COLUMN;


        return pois;
    }
}
