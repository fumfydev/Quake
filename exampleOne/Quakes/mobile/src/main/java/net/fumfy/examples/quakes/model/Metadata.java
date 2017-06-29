package net.fumfy.examples.quakes.model;


/**
 * Project : Earthquakes
 * Created by Simon Barnes on 09/06/2017.
 *
 * @author Simon Barnes
 */
@SuppressWarnings("unused")
public class Metadata {

    private Double generated;
    private String url;
    private String title;
    private String api;
    private String count;
    private String status;

    public Double getGenerated() {
        return generated;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getApi() {
        return api;
    }

    public Metadata() {
    }

    public String getCount() {
        return count;
    }

    public String getStatus() {
        return status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Metadata(String title) {
        this.title = title;
    }
}