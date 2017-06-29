package net.fumfy.examples.quakes.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import net.fumfy.examples.quakes.R;
import net.fumfy.examples.quakes.ui.PrefsFragment;

/**
 * Project : Earthquakes
 * Created by Simon Barnes on 04/06/2017.
 *
 * @author Simon Barnes
 */

public class PrefsActivity extends AppCompatActivity {

	public static final String PREF_AUTO_UPDATE = "PREF_AUTO_UPDATE";
	public static final String PREF_MIN_MAG = "PREF_MIN_MAG";
	public static final String PREF_UPDATE_FREQ = "PREF_UPDATE_FREQ";
	public static final String PREF_SCHED_IN_WIFI_ONLY = "PREF_SCHED_IN_WIFI_ONLY";

	/**
	 * Take care of popping the fragment back stack or finishing the activity
	 * as appropriate.
	 */
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.enter, R.anim.exit);
	}

	@Override
	public boolean navigateUpTo(Intent upIntent) {
		Boolean retVal=super.navigateUpTo(upIntent);
		overridePendingTransition(R.anim.enter, R.anim.exit);
		return retVal;
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

		getSupportFragmentManager().beginTransaction()
			.replace(android.R.id.content, new PrefsFragment())
			.commit();
	}
}
