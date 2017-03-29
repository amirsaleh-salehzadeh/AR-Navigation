package datamanipulation;

import com.honours.genar.myapplication2.app.POICollection;

public class POI {

    private int id;
    private String  name, description, image, audio, video, website;
    private double lattitude, longitude;
    // POICollection variable which defines foreign key constraint
    private POICollection collection;

    public POI(int id, String name, double lattitude, double longitude, String description, String image, String audio, String video,String website, POICollection collection) {
        this.id = id;
        this.name = name;
        this.lattitude = lattitude;
        this.longitude = longitude;
        this.description = description;
        this.image = image;
        this.audio = audio;
        this.video = video;
        this.website = website;
        this.collection = collection;
    }

    public POI(){
        super();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public void setImage(String image) {
        this.image = image;
    }

    public String getAudio() {
        return audio;
    }

    public void setAudio(String audio) {
        this.audio = audio;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public POICollection getCollection() {
        return collection;
    }

    public void setCollection(POICollection collection) {
        this.collection = collection;
    }
}
