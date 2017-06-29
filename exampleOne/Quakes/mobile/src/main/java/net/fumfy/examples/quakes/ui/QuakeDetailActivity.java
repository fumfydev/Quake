package net.fumfy.examples.quakes.ui;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.fumfy.examples.quakes.QuakeApplication;
import net.fumfy.examples.quakes.R;
import net.fumfy.examples.quakes.sugar.Quake;
import net.fumfy.examples.quakes.utility.utils;

import java.text.DateFormat;
import java.util.Date;



import static java.text.DateFormat.getDateTimeInstance;
import static net.fumfy.examples.quakes.utility.utils.quakeHue;

@SuppressWarnings("WeakerAccess")
public class QuakeDetailActivity extends AppCompatActivity
implements OnMapReadyCallback {

	public static Bundle makeBundle(Quake quake) {
		Bundle bundle = new Bundle();
		String title = Double.toString(quake.magnitude) + " " + quake.description;
		bundle.putString(QuakeDetailActivity.TITLE, title);
		bundle.putString(QuakeDetailActivity.URL, quake.link);
		bundle.putLong(QuakeDetailActivity.DATE, quake.date);
		bundle.putDouble(QuakeDetailActivity.MAGNITUDE, quake.magnitude);
		bundle.putDouble(QuakeDetailActivity.LATITUDE, quake.latitude);
		bundle.putDouble(QuakeDetailActivity.LONGITUDE, quake.longitude);
		bundle.putDouble(QuakeDetailActivity.DEPTH, quake.depth);
		return bundle;
	}

	private MapView mMapView;

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			this.finishAfterTransition();
		} else {
			this.finish();
		}
	}

	@Override
	public boolean navigateUpTo(Intent upIntent) {
		Log.d(QuakeApplication.TAG, "in QuakeDetailActivity:navigateUpTo");
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			this.finishAfterTransition();
			return false;
		}
		return super.navigateUpTo(upIntent);
	}

	// Bundle value names
	public static final String TITLE="title";
	public static final String DATE="date";
	public static final String MAGNITUDE="mag";
	public static final String URL="url";
	public static final String LATITUDE="latitude";
	public static final String LONGITUDE="longitude";
	public static final String DEPTH="depth";

	private LatLng mLocation;
	private String mMarker_title;
	private Double mMagnitude;

	public void onMapReady(GoogleMap googleMap) {
		float color=quakeHue(mMagnitude);
		Marker marker = googleMap.addMarker(new MarkerOptions()
			.icon(BitmapDescriptorFactory.defaultMarker(color))
			.position(mLocation)
			.title(mMarker_title)
			.visible(true));
		marker.showInfoWindow();

		CameraPosition cameraPosition = new CameraPosition.Builder()
			.target(mLocation).zoom(5).build();

		googleMap.animateCamera(CameraUpdateFactory
			.newCameraPosition(cameraPosition));

	}

	@Override
	protected void onStart() {
		super.onStart();
		if (mMapView != null) {
			mMapView.onStart();
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mMapView != null) {
			mMapView.onStop();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mMapView != null) {
			mMapView.onDestroy();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mMapView != null) {
			mMapView.onSaveInstanceState(outState);
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (mMapView != null) {
			mMapView.onLowMemory();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mMapView != null) {
			mMapView.onPause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mMapView != null) {
			mMapView.onResume();
		}
	}

	private String formatLocation(Double latitude, Double longitude) {
		return "Latitude: " + latitude +
			" Longitude: " +
			longitude;
	}

	private String formatDepth(Double depth) {
		return "" + depth + " km depth";
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_quake_detail);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		Intent startIntent = getIntent();

		String title = startIntent.getStringExtra(TITLE);
		mMarker_title = title;
		final String url = startIntent.getStringExtra(URL);
		long date = startIntent.getLongExtra(DATE, 0);
		mMagnitude = startIntent.getDoubleExtra(MAGNITUDE, 0.0);
		Double longitude = startIntent.getDoubleExtra(LONGITUDE, 0.0);
		Double latitude = startIntent.getDoubleExtra(LATITUDE, 0.0);
		Double depth = startIntent.getDoubleExtra(DEPTH, 0.0);
		mLocation = new LatLng(latitude, longitude);

		TextView title_view = (TextView) findViewById(R.id.detail_title);
		title_view.setText(title);

		TextView location_view = (TextView) findViewById(R.id.detail_location);
		location_view.setText(formatLocation(latitude, longitude));

		TextView depth_view = (TextView) findViewById(R.id.detail_depth);
		depth_view.setText(formatDepth(depth));

		DateFormat format = getDateTimeInstance();
		TextView date_view = (TextView) findViewById(R.id.detail_date);
		date_view.setText(format.format(new Date(date)));
		Drawable icon;

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			icon = getResources().getDrawable(R.mipmap.ic_quake, getTheme());
		} else {
			icon = ContextCompat.getDrawable(this, R.mipmap.ic_quake);
		}
		int color = utils.quakeColor(mMagnitude);
		icon.mutate().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
		((ImageView)findViewById(R.id.detail_quake_image)).setImageDrawable(icon);

		mMapView = (MapView) findViewById(R.id.mapView);
		if (mMapView != null) {
			mMapView.onCreate(null);
			mMapView.getMapAsync(this);
		}

		Button usgs = (Button) findViewById(R.id.detail_button);
		usgs.setOnClickListener(v -> {
			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(webIntent);
		});

	}

}
