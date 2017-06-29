package net.fumfy.examples.quakes.model;

import java.util.List;


/**
 * Project : Earthquakes
 * Created by Simon Barnes on 09/06/2017.
 *
 * @author Simon Barnes
 */
@SuppressWarnings("unused")
public class FeatureCollection {
    private String type;
    private Metadata metadata;
    private List<Double> bbox = null;
    private List<Feature> features = null;

    public String getType() {
        return type;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public List<Double> getBbox() {
        return bbox;
    }

    public List<Feature> getFeatures() {
        return features;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setFeatures(List<Feature> features) {
        this.features = features;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBbox(List<Double> bbox) {

        this.bbox = bbox;
    }

    public FeatureCollection() {
    }

    public FeatureCollection(String title) {
        metadata = new Metadata(title);
    }
}