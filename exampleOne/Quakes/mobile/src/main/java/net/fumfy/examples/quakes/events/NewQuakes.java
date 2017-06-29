package net.fumfy.examples.quakes.events;

import android.support.annotation.Nullable;

import net.fumfy.examples.quakes.sugar.Quake;

import java.util.ArrayList;

/**
 * Project : Quakes
 * Created by Simon Barnes on 13/06/2017.
 *
 * @author Simon Barnes
 *
 * If list size is greater than 20 then the list will be null.
 * This is to prevent corruption the the data due to internal limits
 * on EventBus post.
 */

public class NewQuakes {
	public final ArrayList<Quake> list;
	public final int size;

	public NewQuakes(@Nullable ArrayList<Quake> list, int size) {
		this.list = list;
		this.size = size;
	}
}
