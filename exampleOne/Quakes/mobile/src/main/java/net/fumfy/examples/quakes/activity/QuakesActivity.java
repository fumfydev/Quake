package net.fumfy.examples.quakes.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import net.fumfy.examples.quakes.R;
import net.fumfy.examples.quakes.events.QuakesRefreshed;
import net.fumfy.examples.quakes.events.QuakesRefreshedError;
import net.fumfy.examples.quakes.ui.MapFragment;
import net.fumfy.examples.quakes.ui.QuakeDetailActivity;
import net.fumfy.examples.quakes.ui.QuakeFragment;
import net.fumfy.examples.quakes.service.QuakeService;
import net.fumfy.examples.quakes.sugar.Quake;
import net.fumfy.examples.quakes.utility.AppStartUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class QuakesActivity extends AppCompatActivity implements
	QuakeFragment.OnListFragmentInteractionListener,
	MapFragment.OnMapFragmentInteractionListener
{

	private QuakeFragment mQuakeFrament;
	private MapFragment mMapFragment;
	private boolean informSRL = false;


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onQuakesRefreshed(QuakesRefreshed event) {
		if (informSRL) {
			if (mQuakeFrament != null) {
				mQuakeFrament.onQuakesRefreshed(event);
				informSRL = false;
			}
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onQuakesRefreshedError(QuakesRefreshedError event) {
		if (informSRL) {
			if (mQuakeFrament != null) {
				mQuakeFrament.onQuakesRefreshedError(event);
				informSRL = false;
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quakes);

		// Initialise the the Preference manager
		PreferenceManager.setDefaultValues(this, R.xml.preference_fragment, true);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		QuakesPagerAdapter mQuakesPagerAdapter = new QuakesPagerAdapter(getSupportFragmentManager());

		ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
		mViewPager.setAdapter(mQuakesPagerAdapter);

		int action;
		switch (AppStartUtil.checkAppStart(this)) {
			case FIRST_TIME:
				// no EULA yet
				AppStartUtil.eulaAgreed(this);
				action=QuakeService.ACTION_REFRESH_QUAKES;
				break;
			case FIRST_TIME_VERSION:
				action=QuakeService.ACTION_APP_ON_CREATE;
				break;
			case NORMAL:
				action=QuakeService.ACTION_APP_ON_CREATE;
				break;
			default:
				action=QuakeService.ACTION_NO_ACTION;
				break;
		}

		startQuakeService(action);
	}


	private void startQuakeService(int action) {
		Intent serviceIntent = new Intent(this, QuakeService.class);
		serviceIntent.putExtra(QuakeService.ACTION_NAME, action);
		startService(serviceIntent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_quakes, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();

		switch (id) {
			case R.id.action_settings:
				Intent i = new Intent(this, PrefsActivity.class);
				startActivityForResult(i, 1);
				overridePendingTransition(R.anim.enter, R.anim.exit);
				return true;
			case R.id.action_delete_data:
				deleteDataDialog();
				return true;
			case R.id.action_search:
				return super.onSearchRequested();
			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void deleteDataDialog() {
		AlertDialog.Builder builder =
			new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
		builder.setTitle(R.string.action_delete_data_title);
		builder.setMessage(R.string.delete_data_dialog_message);
		builder.setNegativeButton(R.string.negative_button_text, null);
		builder.setPositiveButton(R.string.positive_button_text, ((dialog, which) -> {
			Quake.deleteAll(Quake.class);
			if (mMapFragment != null) {
				mMapFragment.clearQuakes();
			}
			if (mQuakeFrament != null) {
				mQuakeFrament.clearQuakes();
			}
			SharedPreferences.Editor editor = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext()).edit();
			editor.putLong(QuakeService.SERVICE_LAST_UPDATE, 0);
			editor.apply();

		}) );
		AlertDialog dialog = builder.create();
		Window window = dialog.getWindow();
		if (window != null) {
			window.getAttributes().windowAnimations = R.style.DialogAnimationTheme;
		}
		dialog.show();
	}

	@Override
	public void refreshQuakes() {
		startQuakeService(QuakeService.ACTION_REFRESH_QUAKES);
		informSRL = true;
	}

	@Override
	public void onListFragmentInteraction(Quake quake, View view) {

		ImageView imageView =  (ImageView) view.findViewById(R.id.quakeView);
		Intent intent = new Intent(this, QuakeDetailActivity.class);
		Bundle bundle = QuakeDetailActivity.makeBundle(quake);
		intent.putExtras(bundle);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			ActivityOptionsCompat options = ActivityOptionsCompat.
				makeSceneTransitionAnimation(this, imageView, getString(R.string.detail_transition_name));
			startActivity(intent, options.toBundle());
		}
		else {
			startActivity(intent);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
	}

	@Override
	protected void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	@Override
	public void mapFragRefreshQuakes() {
		refreshQuakes();
	}

	private class QuakesPagerAdapter extends FragmentPagerAdapter {
		static final int LIST_SECTION=0;
		static final int MAP_SECTION=1;

		private QuakesPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
				case LIST_SECTION:
					if (mQuakeFrament == null) {
						mQuakeFrament = new QuakeFragment();
					}
					fragment = mQuakeFrament;
					break;
				case MAP_SECTION:
					if (mMapFragment == null) {
						mMapFragment = new MapFragment();
					}
					fragment = mMapFragment;
					break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
				case LIST_SECTION:
					return getString(R.string.section_title_list);
				case MAP_SECTION:
					return getString(R.string.section_title_map);
			}
			return null;
		}
	}
}
