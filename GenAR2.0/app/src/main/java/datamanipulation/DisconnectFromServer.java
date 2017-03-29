package datamanipulation;

import android.os.AsyncTask;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.SQLException;

public class DisconnectFromServer extends AsyncTask<Connection,Void,Boolean> {

    @Override
    protected Boolean doInBackground(Connection... cons) {

        Connection con = cons[0];

        if (con == null){
            return true;
        }

        try {
            con.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean bool) {

        if (bool){
            Toast.makeText(null, "Disconnected from Server", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(null,"Unable to disconnect from Server",Toast.LENGTH_LONG).show();
        }
    }


}
