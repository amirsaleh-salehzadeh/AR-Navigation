package datamanipulation;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.honours.genar.myapplication2.app.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class is used to access the SQL Server
 * Used to fetch POI's Roles and Account information
 */
public abstract class ServerDataSource extends DataSource {

    // Listener to allow communication of fragments
    public static OnRolesRefreshListener roleListener;
    // Interface for RolesActivity to make use of in order to swap fragments
    public interface OnRolesRefreshListener {
        void onRolesRefreshed();
    }

    // Listener to allow communication of fragments
    public static OnCollectionsRefreshListener collectionListener;
    // Interface for RolesActivity to make use of in order to swap fragments
    public interface OnCollectionsRefreshListener {
        void onCollectionsRefreshed();
    }

    public static LoginListener loginListener;

    public interface LoginListener{
        void userAuthentication(Boolean authentic, Boolean credError);
        void accountCreated(Boolean success);
    }

    private static boolean passwordError = false;


    private static final String url = "jdbc:jtds:sqlserver://postgrad.nmmu.ac.za:1433/POIDB";
    private static final String driver = "net.sourceforge.jtds.jdbc.Driver";
    private static final String userName = "POIDBuser";
    private static final String password = "POIDB1";

    private static String TAG = "ServerDataSource";

    public static Boolean COLLECTION_REFRESH = false;
    public static Boolean ROLE_REFRESH = false;

    private static List<POIMarker> cachedPOIMarkers = new ArrayList<POIMarker>();

    private static List<POICollection> cachedPOICollections = new ArrayList<POICollection>();
    private static ArrayList<POIMarker> collectionMarkers = new ArrayList<POIMarker>();

    private static List<Role> cachedRoles = new ArrayList<Role>();

    private static Account cachedAccount = null;
    private static List<String> cachedAccessedRoles = new ArrayList<String>();


    public static Context applicationConext;

    public static void fetchPOIMarkers() {

        new AsyncTask<Void, Void, Boolean>() {

            Connection connection;

            @Override
            protected Boolean doInBackground(Void... voids) {

                try {
                    // Establish the connection.
                    Class.forName(driver);
                    connection = DriverManager.getConnection(url, userName, password);
                    Log.d(TAG, "Connected");

                    String audio;
                    String video;
                    String website;

                    Statement stmt = connection.createStatement();

                    cachedPOIMarkers.clear();

                    for (POICollection c : ARData.getPOICollections()) {

                        if (c.isActive()) {

                            ResultSet rs = stmt.executeQuery("SELECT DISTINCT POI.Name, POI.Lattitude, POI.Longitude, POI_Details.Description, POI_Details.Image, POI_Details.Audio, POI_Details.Video, POI_Details.Website, Collection.Name, POI.POI_ID " +
                                    "FROM POI, Role, Collection, POI_Details " +
                                    "WHERE Role.Title = '"+ARData.getCurrentRole().getName()+"' " +
                                    "AND Role.Role_ID = POI_Details.Role_ID " +
                                    "AND POI.Collection_ID = Collection.Collection_ID " +
                                    "AND POI.POI_ID = POI_Details.POI_ID " +
                                    "AND Collection.Name = '" + c.getName() + "' " +
                                    "GROUP BY POI.Name, POI.Lattitude, POI.Longitude, POI_Details.Description, POI_Details.Image, POI_Details.Audio, POI_Details.Video, POI_Details.Website, Collection.Name, POI.POI_ID;");

                            Random random = new Random();
                            int r = random.nextInt(256);
                            int g = random.nextInt(256);
                            int b = random.nextInt(256);

                            while (rs.next()) {

                                String name = rs.getString(1);
                                double lat = rs.getDouble(2);
                                double lon = rs.getDouble(3);
                                String desc = rs.getString(4);
                                String image = rs.getString(5);
                                audio = rs.getString(6);
                                video = rs.getString(7);
                                website = rs.getString(8);
                                String col = rs.getString(9);
                                int id = rs.getInt(10);
                                POIMarker DE = new POIMarker(name, lat, lon, 0, desc, image, audio, video, website,col, Color.rgb(r, g, b));
                                DE.setID(id);
                                cachedPOIMarkers.add(DE);

                            }

                            POIMarker m = new POIMarker(c.getName(), c.getLattitude(), c.getLongitude(), 0, c.getDescription(),c.getImage() , null, null, null, null, Color.RED);

                            collectionMarkers.add(m);


                        }
                    }
                    return true;

                } catch (Exception ex) {
                    Log.d(TAG, "SQL Error: " + ex.getMessage());
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean bool) {
                if (bool) {
                    Toast.makeText(applicationConext, "Connected to Server", Toast.LENGTH_SHORT).show();
                    ARData.clearMarkers();
                    ARData.addMarkers(collectionMarkers);
                    ARData.addMarkers(cachedPOIMarkers);
                    collectionMarkers.clear();
                } else {
                    Toast.makeText(applicationConext, "Unable to connect to Server", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public static void fetchPOICollections() {
        new AsyncTask<Void, Void, Boolean>() {

            Connection connection;

            @Override
            protected Boolean doInBackground(Void... voids) {

                try {
                    // Establish the connection.
                    Class.forName(driver);
                    connection = DriverManager.getConnection(url, userName, password);
                    Log.d(TAG, "Connected");

                    Statement stmt = connection.createStatement();
                    Statement stmt2 = connection.createStatement();

                    ARData.clearPOICollections();
                    cachedPOICollections.clear();
                    collectionMarkers.clear();

                    ResultSet rs = stmt.executeQuery("SELECT DISTINCT Collection.Collection_ID, Collection.Name, Collection.Description, Collection.Image " +
                            "FROM Collection " +
                            "INNER JOIN POI ON  Collection.Collection_ID = POI.Collection_ID " +
                            "INNER JOIN POI_Details ON POI.POI_ID = POI_Details.POI_ID " +
                            "INNER JOIN Role ON POI_Details.Role_ID = Role.Role_ID " +
                            "AND Role.Title = '" + ARData.getCurrentRole().getName() + "'; ");

                    while (rs.next()) {

                        int id = rs.getInt(1);
                        String name = rs.getString(2);
                        String desc = rs.getString(3);
                        String image = rs.getString(4);

                        ResultSet rss = stmt2.executeQuery("SELECT AVG(POI.Lattitude), AVG(POI.Longitude) " +
                                "FROM POI, Collection " +
                                "WHERE POI.Collection_ID = Collection.Collection_ID " +
                                "AND Collection.Name = '" + name + "';");

                        Double lat = 0.0;
                        Double lon = 0.0;
                        Boolean valid = false;

                        while (rss.next()){
                            lat = rss.getDouble(1);
                            lon = rss.getDouble(2);
                            valid = true;
                        }

                        if (valid) {

                            POICollection col = new POICollection(id, name, desc, null,image, true);
                            col.setLattitude(lat);
                            col.setLongitude(lon);

                            cachedPOICollections.add(col);
                        }

                    }

                    return true;

                } catch (Exception ex) {
                    Log.d(TAG, "SQL Error: " + ex.getMessage());
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean bool) {
                if (bool) {
                    //Toast.makeText(applicationConext, "Connected to Server - Collections", Toast.LENGTH_SHORT).show();
                    ARData.clearPOICollections();
                    ARData.clearMarkers();
                    ARData.addPOICollections(cachedPOICollections);
                    ARData.addMarkers(collectionMarkers);

                    if (COLLECTION_REFRESH){
                        collectionListener.onCollectionsRefreshed();
                        COLLECTION_REFRESH = false;
                    }
                    fetchPOIMarkers();
                } else {
                    Toast.makeText(applicationConext, "Unable to connect to Server - Collections", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public static void fetchRoles(){

        new AsyncTask<Void, Void, Boolean>() {

            Connection connection;

            @Override
            protected Boolean doInBackground(Void... voids) {



                try {
                    // Establish the connection.
                    Class.forName(driver);
                    connection = DriverManager.getConnection(url, userName, password);
                    Log.d(TAG, "Connected");

                    Statement stmt = connection.createStatement();

                    cachedRoles.clear();

                    ResultSet rs = stmt.executeQuery("SELECT Role.Title, Role.Description, Role.Password " +
                            "FROM ROLE;");

                    while (rs.next()) {
                        String name = rs.getString(1);
                        String desc = rs.getString(2);
                        String pass = rs.getString(3);

                        Role role = new Role(name, desc, pass);

                        if (ARData.getCurrentRole() == null && role.getName().equals("NMMU Student")){
                            ARData.setCurrentRole(role);
                        }
                        else if (role.getName().equals(ARData.getCurrentRole().getName()))
                            ARData.setCurrentRole(role);

                        cachedRoles.add(role);
                    }

                    return true;

                } catch (Exception ex) {
                    Log.d(TAG, "SQL Error: " + ex.getMessage());
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean bool) {
                if (bool) {
                    //Toast.makeText(applicationConext, "Connected to Server - Roles", Toast.LENGTH_SHORT).show();
                    ARData.clearRoles();
                    ARData.addRoles(cachedRoles);
                    if (ROLE_REFRESH){
                        roleListener.onRolesRefreshed();
                        ROLE_REFRESH = false;
                    }
                    fetchPOICollections();
                    fetchAccessedRoles();
                } else {
                    Toast.makeText(applicationConext, "Unable to connect to Server - Collections", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public static void verifyAccount(final String email, final String pass) {
        new AsyncTask<Void, Void, Boolean>() {

            Connection connection;

            @Override
            protected Boolean doInBackground(Void... voids) {

                try {
                    // Establish the connection.
                    Class.forName(driver);
                    connection = DriverManager.getConnection(url, userName, password);
                    Log.d(TAG, "Connected");

                    Statement stmt = connection.createStatement();

                    ResultSet rs = stmt.executeQuery("SELECT COUNT(Email) " +
                            "FROM Account " +
                            "WHERE Email = '" + email + "' " +
                            "AND Password = '" + pass + "';");

                    cachedAccount = null;

                    while (rs.next()) {
                        if (rs.getInt(1) == 1){
                            cachedAccount = new Account(email,pass);
                            return true;
                        }
                    }

                    rs = stmt.executeQuery("SELECT COUNT(Email) " +
                            "FROM Account " +
                            "WHERE Email = '" + email + "';");

                    while (rs.next()) {
                        if (rs.getInt(1) == 1) {
                            passwordError = true;
                            return false;
                        }
                    }

                    return false;

                } catch (Exception ex) {
                    Log.d(TAG, "SQL Error: " + ex.getMessage());
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean bool) {

                if (bool) {
                    //Toast.makeText(applicationConext, "Connected to Server - Login", Toast.LENGTH_SHORT).show();
                    ARData.setCurrentAccount(cachedAccount);
                } else {
                    Toast.makeText(applicationConext, "Unable to connect to Server - Login", Toast.LENGTH_SHORT).show();
                }

                String er = null;


                loginListener.userAuthentication(bool, passwordError);

            }
        }.execute();
    }

    public static void createAccount(final String email, final String pass){
        new AsyncTask<Void, Void, Boolean>() {

            Connection connection;
            Boolean dupEmail = false;

            @Override
            protected Boolean doInBackground(Void... voids) {

                try {
                    // Establish the connection.
                    Class.forName(driver);
                    connection = DriverManager.getConnection(url, userName, password);
                    Log.d(TAG, "Connected");

                    Statement stmt = connection.createStatement();

                    ResultSet rs = stmt.executeQuery("SELECT TOP 1 Email " +
                            "FROM Account " +
                            "WHERE Email = '" + email + "';");

                    if (rs.next()) {
                        dupEmail = true;
                        return false;
                    }


                    String insert_stmt = "INSERT INTO Account " +
                                " VALUES ('"+email+"', '"+pass+"');";

                    stmt.execute(insert_stmt);

                    cachedAccount = new Account(email,pass);

                   return true;

                } catch (Exception ex) {
                    Log.d(TAG, "SQL Error: " + ex.getMessage());
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean bool) {

                if (bool) {
                    Toast.makeText(applicationConext, "Account Created", Toast.LENGTH_SHORT).show();
                    loginListener.accountCreated(true);
                    ARData.setCurrentAccount(cachedAccount);
                } else {
                    if (dupEmail){
                        loginListener.accountCreated(false);
                    }
                    Toast.makeText(applicationConext, "Unable to create account", Toast.LENGTH_SHORT).show();
                }



            }
        }.execute();
    }

    public static void fetchAccessedRoles(){
        new AsyncTask<Void, Void, Boolean>() {

            Connection connection;

            @Override
            protected Boolean doInBackground(Void... voids) {

                try {
                    // Establish the connection.
                    Class.forName(driver);
                    connection = DriverManager.getConnection(url, userName, password);
                    Log.d(TAG, "Connected");

                    Statement stmt = connection.createStatement();

                    cachedAccessedRoles.clear();

                    ResultSet rs = stmt.executeQuery("SELECT Role.Title " +
                                    "FROM Role, Account_Role, Account  " +
                                    "WHERE Role.Role_ID = Account_Role.Role_ID " +
                                    "AND Account_Role.Account_ID = Account.Account_ID " +
                                    "AND Account.Email = '"+ARData.getCurrentAccount().getEmail()+"';");

                    while (rs.next()){
                        cachedAccessedRoles.add(rs.getString(1));
                    }
                    return true;

                } catch (Exception ex) {
                    Log.d(TAG, "SQL Error: " + ex.getMessage());
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean bool) {

                if (bool) {
                    //Toast.makeText(applicationConext, "Accessed Roles Obtained", Toast.LENGTH_SHORT).show();
                    ARData.clearAccessedRoles();
                    ARData.addAccessedRoles(cachedAccessedRoles);

                } else {
                    Toast.makeText(applicationConext, "Unable to obtain Accessed Roles", Toast.LENGTH_SHORT).show();
                }



            }
        }.execute();
    }

    public static void grantRoleAccess(final Role role){
        new AsyncTask<Void, Void, Boolean>() {

            Connection connection;

            @Override
            protected Boolean doInBackground(Void... voids) {

                try {
                    // Establish the connection.
                    Class.forName(driver);
                    connection = DriverManager.getConnection(url, userName, password);
                    Log.d(TAG, "Connected");

                    Statement stmt = connection.createStatement();

                    cachedAccessedRoles.clear();

                    ResultSet rs = stmt.executeQuery("SELECT Account_ID FROM Account WHERE Account.Email = '"+ARData.getCurrentAccount().getEmail()+"';");

                    int AccountID = -1;

                    while (rs.next()){
                        AccountID = rs.getInt(1);
                    }

                    rs = stmt.executeQuery("SELECT Role_ID FROM Role WHERE Role.Title = '"+role.getName()+"';");

                    int RoleID = -1;

                    while (rs.next()){
                        RoleID = rs.getInt(1);
                    }

                    String insert_stmt = "INSERT INTO Account_Role " +
                            " VALUES ("+AccountID+", "+RoleID+");";

                    stmt.execute(insert_stmt);

                    return true;

                } catch (Exception ex) {
                    Log.d(TAG, "SQL Error: " + ex.getMessage());
                    return false;
                }

            }

            @Override
            protected void onPostExecute(Boolean bool) {

                if (bool) {
                    Toast.makeText(applicationConext, "New Accessed Role Added", Toast.LENGTH_SHORT).show();
                    fetchAccessedRoles();

                } else {
                    Toast.makeText(applicationConext, "Unable to add Accessed Role", Toast.LENGTH_SHORT).show();
                }



            }
        }.execute();
    }


    /**
     * This methods is overridden from the parent class DataSource
     *
     * @return A list of POI's applicable to the 'General' Role.
     */
    @Override
    public List<POIMarker> getMarkers() {
        return cachedPOIMarkers;
    }

    /**
     * This methods is overridden from the parent class DataSource
     *
     * @return A list of Collections applicable to the 'General' Role.
     */
    @Override
    public List<POICollection> getPOICollections() {
        return cachedPOICollections;
    }

}
