package net.fumfy.examples.quakes.events;

/**
 * Project : Quakes
 * Created by Simon Barnes on 21/06/2017.
 *
 * @author Simon Barnes
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public class QuakesRefreshed {

	public final String title;
	public final String count;
	public final String status;
	public final int new_quakes;

	public QuakesRefreshed(String title, String count, String status, int new_quakes) {
		this.title = title;
		this.count = count;
		this.status = status;
		this.new_quakes = new_quakes;
	}
}
