package net.fumfy.examples.quakes;

import android.app.Application;

import com.orm.SugarContext;

/**
 * Project : Quakes
 * Created by Simon Barnes on 12/06/2017.
 *
 * @author Simon Barnes
 */

public class QuakeApplication extends Application {
	public static final String TAG="Quake";
	@Override
	public void onTerminate() {
		super.onTerminate();
		SugarContext.terminate();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		SugarContext.init(getApplicationContext());
	}
}
