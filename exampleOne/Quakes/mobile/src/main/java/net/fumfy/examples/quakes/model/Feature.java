package net.fumfy.examples.quakes.model;


/**
 * Project : Earthquakes
 * Created by Simon Barnes on 09/06/2017.
 *
 * @author Simon Barnes
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class Feature {

    private String type;
    private Properties properties;
    private Geometry geometry;
    private String id;

    public String getType() {
        return type;
    }

    public Properties getProperties() {
        return properties;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getId() {
        return id;
    }

    public long getDate() {
        return getProperties().getTime();
    }

    public String getDescription() {
        return getProperties().getPlace();
    }

    public Double getMagnitude() {
        return getProperties().getMag();
    }

    public String getUrl() {
        return getProperties().getUrl();
    }

    public Double getLatitude() {
        // Latitude at index 1 of Coordinates
        return getGeometry().getCoordinates().get(1);
    }

    public Double getLongitude() {
        // Longitude at index 0 of Coordinates
        return getGeometry().getCoordinates().get(0);
    }

    public Double getDepth() {
        // Depth at index 2 of Coordinates
	    return getGeometry().getCoordinates().get(2);
    }

}
