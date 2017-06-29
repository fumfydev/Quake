package net.fumfy.examples.quakes.utility;

import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Project : Quakes
 * Created by Simon Barnes on 17/06/2017.
 *
 * @author Simon Barnes
 */

public class utils {

	public static int quakeColor(Double magnitude) {
		int color;
		if (magnitude < 2.9) {
			color = Color.GREEN;
		} else if (magnitude < 4) {
			color = Color.BLUE;
		} else if (magnitude < 5) {
			color = Color.YELLOW;
		} else if (magnitude < 6) {
			// Orange
			color = Color.rgb(255, 165,0);
		} else {
			color= Color.RED;
		}
		return color;
	}

	public static float quakeHue(Double magnitude) {
		float color;
		if (magnitude < 2.9) {
			color = BitmapDescriptorFactory.HUE_GREEN;
		} else if (magnitude < 4) {
			color = BitmapDescriptorFactory.HUE_BLUE;
		} else if (magnitude < 5) {
			color = BitmapDescriptorFactory.HUE_YELLOW;
		} else if (magnitude < 6) {
			color = BitmapDescriptorFactory.HUE_ORANGE;
		} else {
			color= BitmapDescriptorFactory.HUE_RED;
		}
		return color;
	}


	public static boolean internetAvailable(Context context) {
		ConnectivityManager cm =
			(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ani = cm.getActiveNetworkInfo();
		return ani != null && ani.isConnected();
	}

	public static boolean wifiConnected(Context context) {
		boolean connected = false;
		ConnectivityManager cm =
			(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ani = cm.getActiveNetworkInfo();
		if (ani!= null) {
			connected = ani.getType() == ConnectivityManager.TYPE_WIFI;
			Log.d("Quakes", "wifiConnected: Type = " + ani.getTypeName());
		}
		return connected;
	}

}
