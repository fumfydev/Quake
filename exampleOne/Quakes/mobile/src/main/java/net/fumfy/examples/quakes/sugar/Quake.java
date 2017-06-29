package net.fumfy.examples.quakes.sugar;


import com.orm.SugarRecord;
import com.orm.dsl.Unique;


/**
 * Project : Earthquakes
 * Created by Simon Barnes on 05/06/2017.
 *
 * @author Simon Barnes
 */

public class Quake extends SugarRecord {
    public String description;
    public Double magnitude;
    public long date;
    public Double latitude;
    public Double longitude;
	public Double depth;
    public String link;
    @SuppressWarnings("WeakerAccess")
    @Unique
    public String eid;

    public Quake() {

    }

    public Quake(String description, Double magnitude, long date,
                      Double latitude, Double longitude, Double depth, String link, String eid) {
        this.description = description;
        this.magnitude = magnitude;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
	    this.depth = depth;
        this.link = link;
        this.eid = eid;
    }
}
