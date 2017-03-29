package datamanipulation;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import com.honours.genar.myapplication2.app.POICollection;

import java.util.ArrayList;
import java.util.List;

public class POICollectionDAO  extends POI_DB_DAO{


    private static final String WHERE_ID_EQUALS = DatabaseHelper.ID_COLUMN
            + " =?";

    public POICollectionDAO(Context context) {
        super(context);
    }

    public long save(POICollection POICollection) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ID_COLUMN, POICollection.getID());
        values.put(DatabaseHelper.NAME_COLUMN, POICollection.getName());
        values.put(DatabaseHelper.DESCRIPTION_COLUMN, POICollection.getDescription());
        values.put(DatabaseHelper.IMAGE_COLUMN, POICollection.getImage());

        return database.insert(DatabaseHelper.COLLECTION_TABLE, null, values);
    }

    public long update(POICollection POICollection) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.NAME_COLUMN, POICollection.getName());
        values.put(DatabaseHelper.DESCRIPTION_COLUMN, POICollection.getDescription());
        values.put(DatabaseHelper.IMAGE_COLUMN, POICollection.getImage());

        long result = database.update(DatabaseHelper.COLLECTION_TABLE, values,
                WHERE_ID_EQUALS,
                new String[] { String.valueOf(POICollection.getID()) });
        Log.d("Update Result:", "=" + result);
        return result;

    }

    public int deleteCollection(POICollection POICollection) {
        return database.delete(DatabaseHelper.COLLECTION_TABLE,
                WHERE_ID_EQUALS, new String[] { POICollection.getID() + "" });
    }

    public List<POICollection> getPOICollections() {
        List<POICollection> POICollections = new ArrayList<POICollection>();
        Cursor cursor = database.query(DatabaseHelper.COLLECTION_TABLE,
                new String[] { DatabaseHelper.ID_COLUMN,
                        DatabaseHelper.NAME_COLUMN,
                        DatabaseHelper.DESCRIPTION_COLUMN,
                        DatabaseHelper.IMAGE_COLUMN}, null, null, null, null,
                null);

        while (cursor.moveToNext()) {
            POICollection POICollection = new POICollection();
            POICollection.setID(cursor.getInt(0));
            POICollection.setName(cursor.getString(1));
            POICollection.setDescription(cursor.getString(2));
            POICollection.setImage(cursor.getString(3));
            POICollections.add(POICollection);
        }
        return POICollections;
    }

    public void loadCollections() {

        List<POICollection> POICollections = new ArrayList<POICollection>();



        for (POICollection col : POICollections) {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.NAME_COLUMN, col.getName());
            database.insert(DatabaseHelper.COLLECTION_TABLE, null, values);
        }

    }


}
