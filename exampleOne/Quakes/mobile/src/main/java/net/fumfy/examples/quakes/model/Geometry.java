package net.fumfy.examples.quakes.model;

import java.util.List;

/**
 * Project : Earthquakes
 * Created by Simon Barnes on 09/06/2017.
 *
 * @author Simon Barnes
 */

@SuppressWarnings("unused")
public class Geometry {
    private String type;
    private List<Double> coordinates = null;

    public String getType() {
        return type;
    }

    public List<Double> getCoordinates() {
        return coordinates;
    }

}