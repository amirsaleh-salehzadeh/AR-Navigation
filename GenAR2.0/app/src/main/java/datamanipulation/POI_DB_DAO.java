package datamanipulation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

public class POI_DB_DAO {

    protected SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private Context mContext;

    public POI_DB_DAO(Context context) {
        this.mContext = context;
        dbHelper = DatabaseHelper.getHelper(mContext);
        try {
            open();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void open() throws SQLException {
        if(dbHelper == null)
            dbHelper = DatabaseHelper.getHelper(mContext);
        database = dbHelper.getWritableDatabase();
    }


}
