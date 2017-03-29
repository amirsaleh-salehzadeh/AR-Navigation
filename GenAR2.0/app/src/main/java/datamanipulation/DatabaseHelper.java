package datamanipulation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Name
    private static final String DATABASE_NAME = "POIdb";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Table Names
    public static final String POI_TABLE = "POI";
    public static final String COLLECTION_TABLE = "POICollection";

    // Common Column Names
    public static final String ID_COLUMN = "id";
    public static final String NAME_COLUMN = "name";
    public static final String DESCRIPTION_COLUMN = "description";
    public static final String IMAGE_COLUMN = "image";

    // POI Table - column names
    public static final String POI_LATTITUDE = "lattitude";
    public static final String POI_LONGITUDE = "longitude";
    public static final String POI_AUDIO = "audio";
    public static final String POI_VIDEO = "video";
    public static final String POI_WEBSITE = "website";
    public static final String POI_COLLECTION_ID = "collection_id";

    // CREATE Statements
    public static final String CREATE_POI_TABLE = "CREATE TABLE "
            + POI_TABLE + "("
            + ID_COLUMN + " INTEGER PRIMARY KEY, "
            + NAME_COLUMN + " TEXT NOT NULL, "
            + POI_LATTITUDE + " DOUBLE NOT NULL, "
            + POI_LONGITUDE + " DOUBLE NOT NULL, "
            + DESCRIPTION_COLUMN + " TEXT, "
            + IMAGE_COLUMN + " TEXT, "
            + POI_AUDIO + " TEXT, "
            + POI_VIDEO + " TEXT, "
            + POI_WEBSITE + " TEXT, "
            + POI_COLLECTION_ID + " INT, " + "FOREIGN KEY(" + POI_COLLECTION_ID + ") REFERENCES "
            + COLLECTION_TABLE + "(id) " + ")";

    public static final String CREATE_COLLECTION_TABLE = "CREATE TABLE "
            + COLLECTION_TABLE + "("
            + ID_COLUMN + " INTEGER PRIMARY KEY,"
            + NAME_COLUMN + " TEXT NOT NULL, "
            + DESCRIPTION_COLUMN + " TEXT NOT NULL, "
            + IMAGE_COLUMN + " TEXT, " + ")";

    // Instance of the DB Helper
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getHelper(Context context) {
        if (instance == null)
            instance = new DatabaseHelper(context);
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COLLECTION_TABLE);
        db.execSQL(CREATE_POI_TABLE);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + POI_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + COLLECTION_TABLE);

        onCreate(db);
    }

}
