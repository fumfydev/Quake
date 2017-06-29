package net.fumfy.examples.quakes.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Project : Quakes
 * Created by Simon Barnes on 25/06/2017.
 *
 * @author Simon Barnes
 */

public class AppStartUtil {
	// EULA on AppStart values
	public enum AppStart {
		FIRST_TIME, FIRST_TIME_VERSION, NORMAL
	}

	@SuppressWarnings("FieldCanBeLocal")
	private static final String ASU_APP_VERSION="asu_app_version";
	private static final String ASU_AGREE_TO_EULA="asu_agree_to_eula";

	private static final String TAG="AppStartUtil";

	public static AppStart checkAppStart(Context context) {
		PackageInfo pInfo;
		SharedPreferences sharedPreferences = PreferenceManager
			.getDefaultSharedPreferences(context);
		AppStart appStart = AppStart.NORMAL;
		try {
			pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			boolean eula_agree = sharedPreferences.getBoolean(ASU_AGREE_TO_EULA, false);
			int lastVersionCode = sharedPreferences.getInt(ASU_APP_VERSION, 0);
			if (!eula_agree) {
				lastVersionCode=-1;
			}
			int currentVersionCode = pInfo.versionCode;
			appStart = checkAppStart(currentVersionCode, lastVersionCode);
			// Update version in preferences
			sharedPreferences.edit()
				.putInt(ASU_APP_VERSION, currentVersionCode).apply();
		} catch (PackageManager.NameNotFoundException e) {
			Log.d(TAG,
				"Unable to determine current app version from package manager. Defensively assuming normal app start.");
		}
		return appStart;
	}

	public static void eulaAgreed(Context context) {
		SharedPreferences sharedPreferences = PreferenceManager
			.getDefaultSharedPreferences(context);
		sharedPreferences.edit().putBoolean(ASU_AGREE_TO_EULA, true).apply();
	}

	private static AppStart checkAppStart(int currentVersionCode, int lastVersionCode) {
		if (lastVersionCode == -1) {
			return AppStart.FIRST_TIME;
		} else if (lastVersionCode < currentVersionCode) {
			return AppStart.FIRST_TIME_VERSION;
		} else if (lastVersionCode > currentVersionCode) {
			Log.d(TAG, "Current version code (" + currentVersionCode
				+ ") is less then the one recognized on last startup ("
				+ lastVersionCode
				+ "). Defensively assuming normal app start.");
			return AppStart.NORMAL;
		} else {
			return AppStart.NORMAL;
		}
	}

}
