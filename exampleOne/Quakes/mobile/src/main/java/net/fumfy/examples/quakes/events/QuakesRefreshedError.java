package net.fumfy.examples.quakes.events;

/**
 * Project : Quakes
 * Created by Simon Barnes on 24/06/2017.
 *
 * @author Simon Barnes
 */

public class QuakesRefreshedError {
	public final String error_message;

	public QuakesRefreshedError(String error_message) {
		this.error_message = error_message;
	}
}
