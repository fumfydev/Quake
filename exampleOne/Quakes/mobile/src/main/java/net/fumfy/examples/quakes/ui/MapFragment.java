package net.fumfy.examples.quakes.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import net.fumfy.examples.quakes.QuakeApplication;
import net.fumfy.examples.quakes.R;
import net.fumfy.examples.quakes.activity.PrefsActivity;
import net.fumfy.examples.quakes.events.NewQuakes;
import net.fumfy.examples.quakes.sugar.Quake;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static net.fumfy.examples.quakes.utility.utils.quakeHue;


public class MapFragment extends Fragment implements
	OnMapReadyCallback,
	SharedPreferences.OnSharedPreferenceChangeListener {

	private GoogleMap mMap;

	private final String TAG = QuakeApplication.TAG;

	private OnMapFragmentInteractionListener mListener;
	private List<Quake> mQuakes;
	private Double magnitude;

	public MapFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setUpPrefs();
		initQuakeList(magnitude);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		View view = null;
		// Inflate the layout for this fragment
		try {
			view = inflater.inflate(R.layout.fragment_map, container, false);
		} catch (Exception e) {
			Log.e(TAG, "in onCreateView inflating fragment Map caught exception: " + e.getMessage());
		}

		return view;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_map_fragment, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == R.id.action_refresh) {
			mListener.mapFragRefreshQuakes();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof OnMapFragmentInteractionListener) {
			mListener = (OnMapFragmentInteractionListener) context;
		} else {
			throw new RuntimeException(context.toString()
				+ " must implement OnListFragmentInteractionListener");
		}
	}

	private void setUpPrefs() {

		SharedPreferences sharedPreferences = PreferenceManager
			.getDefaultSharedPreferences(getContext());
		magnitude = (double) Integer.parseInt(sharedPreferences
			.getString(PrefsActivity.PREF_MIN_MAG, "3"));
	}

	private void initQuakeList(Double magnitude) {
		String select = "magnitude >= ?";
		String[] args  = { magnitude.toString() };
		String orderBy = "date DESC";
		mQuakes = Quake.find(Quake.class,select, args, null, orderBy, null);

	}


	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		try {
			((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map))
				.getMapAsync(this);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}


	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onNewQuakes(NewQuakes event) {
		if (mMap != null) {
			if (event.list != null) {
				//noinspection Convert2streamapi
				for (Quake quake : event.list) {
					if (quake.magnitude >= magnitude) {
						mQuakes.add(quake);
						loadQuakeToMap(quake);
					}
				}
			} else if (event.size > 20) {
				initQuakeList(magnitude);
				loadQuakesToMap();
			}
		}
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		loadQuakesToMap();
	}

	public void clearQuakes() {
		if (mQuakes != null) {
			mQuakes.clear();
		}
		if (mMap != null) {
			mMap.clear();
		}
	}

	private void loadQuakesToMap() {
		if (mMap != null) {
			mMap.clear();
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
				//noinspection Convert2streamapi
				for (Quake quake : mQuakes) {
					loadQuakeToMap(quake);
				}
			} else {
				mQuakes.forEach(this::loadQuakeToMap);
			}
		}
	}

	private void loadQuakeToMap(Quake quake) {
		@SuppressLint("SimpleDateFormat")
		SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		String date = fmt.format(new Date(quake.date));
		String snippet = date + " " + quake.magnitude + " " + quake.description;
		String title = Double.toString(quake.magnitude) + " " + quake.description;
		LatLng latLng = new LatLng(quake.latitude, quake.longitude);
		float color=quakeHue(quake.magnitude);
		mMap.addMarker(new MarkerOptions()
			.icon(BitmapDescriptorFactory.defaultMarker(color))
			.position(latLng)
			.title(title)
			.visible(true)
			.snippet(snippet));
	}

	@Override
	public void onStart() {
		super.onStart();
		EventBus.getDefault().register(this);
		PreferenceManager
			.getDefaultSharedPreferences(getContext())
			.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onStop() {
		EventBus.getDefault().unregister(this);
		super.onStop();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (getActivity() != null) {
			if (key.equals(PrefsActivity.PREF_MIN_MAG)) {
				magnitude = (Double) (double) Integer.parseInt(sharedPreferences
					.getString(PrefsActivity.PREF_MIN_MAG, "3"));
				MapFragment.this.initQuakeList(magnitude);
				if (mMap != null) {
					MapFragment.this.loadQuakesToMap();
				}
			}
		}
	}

	public interface OnMapFragmentInteractionListener {

		void mapFragRefreshQuakes();
	}
}
