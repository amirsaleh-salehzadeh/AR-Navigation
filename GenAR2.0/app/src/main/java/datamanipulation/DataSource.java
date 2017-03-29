package datamanipulation;

import com.honours.genar.myapplication2.app.POIMarker;
import com.honours.genar.myapplication2.app.POICollection;

import java.util.List;

public abstract class DataSource {
    public abstract List<POIMarker> getMarkers();
    public abstract List<POICollection> getPOICollections();
}
