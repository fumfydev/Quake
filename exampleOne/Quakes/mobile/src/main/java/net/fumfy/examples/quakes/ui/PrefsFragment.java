package net.fumfy.examples.quakes.ui;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import net.fumfy.examples.quakes.R;

/**
 * Project : Earthquakes
 * Created by Simon Barnes on 04/06/2017.
 *
 * @author Simon Barnes
 */

public class PrefsFragment extends PreferenceFragmentCompat {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference_fragment);
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

	}
}
