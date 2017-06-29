package net.fumfy.examples.quakes.api;

import net.fumfy.examples.quakes.model.FeatureCollection;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;


@SuppressWarnings("unused")
public interface QuakeApi {
	String API_ENDPOINT = "https://earthquake.usgs.gov";
	String ALL_MONTH = "all_month.geojson";
	String ALL_WEEK = "all_week.geojson";
	String ALL_DAY = "all_day.geojson";
	String ALL_HOUR = "all_hour.geojson";

	@GET("earthquakes/feed/v1.0/summary/{feed}")
	Observable<FeatureCollection> getQuakes(@Path("feed") String feed);

}