package net.fumfy.examples.quakes.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import net.fumfy.examples.quakes.QuakeApplication;
import net.fumfy.examples.quakes.R;
import net.fumfy.examples.quakes.activity.PrefsActivity;
import net.fumfy.examples.quakes.activity.QuakesActivity;
import net.fumfy.examples.quakes.api.QuakeApi;
import net.fumfy.examples.quakes.events.NewQuakes;
import net.fumfy.examples.quakes.events.QuakesRefreshed;
import net.fumfy.examples.quakes.events.QuakesRefreshedError;
import net.fumfy.examples.quakes.model.Feature;
import net.fumfy.examples.quakes.model.FeatureCollection;
import net.fumfy.examples.quakes.model.Metadata;
import net.fumfy.examples.quakes.sugar.Quake;

import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static net.fumfy.examples.quakes.utility.utils.internetAvailable;
import static net.fumfy.examples.quakes.utility.utils.wifiConnected;

/**
 * Project : Quakes
 * Created by Simon Barnes on 12/06/2017.
 *
 * @author Simon Barnes
 */

public class QuakeService extends IntentService {

	public static final int ACTION_NO_ACTION = 0;
	public static final int ACTION_REFRESH_QUAKES = 1;
	public static final int ACTION_SCHEDULED_REFRESH = 2;
	public static final int ACTION_APP_ON_CREATE = 3;
	public static final int NEW_QUAKES_LIST_LIMIT=20;
	public static final String ACTION_NAME = "serviceAction";
	public static final String SERVICE_LAST_UPDATE="SERVICE_LAST_UPDATE";
	@SuppressWarnings("FieldCanBeLocal")
	private final String TAG = QuakeApplication.TAG;

	private PendingIntent quakeIntent;
	private NotificationCompat.Builder quakeNBuilder;

	@SuppressWarnings("unused")
	public QuakeService() {
		super("QuakeRetrievalService");
	}


	@SuppressWarnings("unused")
	public QuakeService(String name) {
		super(name);
	}

	private static void startGetQuakesAlarm(AlarmManager alarmManager,
	                                        int frequency,
	                                        PendingIntent pendingIntent) {
		int type = AlarmManager.ELAPSED_REALTIME_WAKEUP;
		alarmManager.cancel(pendingIntent);
		long triggerAtMillis = (frequency * 60 * 1000) + SystemClock.elapsedRealtime();
		alarmManager.setInexactRepeating(type, triggerAtMillis,
			frequency * 60 * 1000, pendingIntent);

	}

	@Override
	public void onCreate() {
		super.onCreate();

		Intent alarmIntent = new Intent(QuakeAlarmReceiver.ACTION_RETRIEVE_QUAKES_ALARM);
		quakeIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, 0);

		quakeNBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
		.setSmallIcon(R.drawable.ic_notification)
		.setContentTitle(getString(R.string.notification_title_text))
		.setContentText(getString(R.string.notification_context_text));

		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());
		SharedPreferences.OnSharedPreferenceChangeListener mListener = (SharedPreferences sharedPreferences, String key) -> {

			String frequency_key = PrefsActivity.PREF_UPDATE_FREQ;
			String autoupdate_key = PrefsActivity.PREF_AUTO_UPDATE;

			AlarmManager alarmManager =
				(AlarmManager) QuakeService.this.getSystemService(Context.ALARM_SERVICE);
			if (key.equals(frequency_key)) {
				boolean autoUpdate = sharedPreferences.getBoolean(autoupdate_key, true);
				if (autoUpdate) {
					int frequency = Integer.parseInt(
						sharedPreferences.getString(frequency_key, "60"));
					alarmManager.cancel(quakeIntent);
					QuakeService.startGetQuakesAlarm(alarmManager, frequency, quakeIntent);
				}
			} else if (key.equals(autoupdate_key)) {
				boolean autoUpdate = sharedPreferences.getBoolean(key, true);
				if (autoUpdate) {
					int frequency = Integer.parseInt(
						sharedPreferences.getString(frequency_key, "60"));
					QuakeService.startGetQuakesAlarm(alarmManager, frequency, quakeIntent);
				} else {
					alarmManager.cancel(quakeIntent);
				}

			}
		};
		prefs.registerOnSharedPreferenceChangeListener(mListener);

	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		Context context = getApplicationContext();
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(context);
		int frequency = Integer
			.parseInt(prefs.getString(PrefsActivity.PREF_UPDATE_FREQ,"60"));
		boolean autoUpdate = prefs.getBoolean(PrefsActivity.PREF_AUTO_UPDATE,false);
		boolean wifi_only = prefs.getBoolean(PrefsActivity.PREF_SCHED_IN_WIFI_ONLY, false);

		boolean scheduled=false;
		boolean refresh = false;
		boolean init = false;
		int action=ACTION_REFRESH_QUAKES;

		if (intent != null)
			action=intent.getIntExtra(QuakeService.ACTION_NAME, 0);
			switch (action) {
				case ACTION_SCHEDULED_REFRESH:
					scheduled = true;
					break;
				case ACTION_APP_ON_CREATE:
					// App on start let Auto-Update and WiFi determine
					// if Quakes should be refreshed
					init = true;
					break;
				case ACTION_REFRESH_QUAKES:
					refresh = true;
					break;
				default:
					// Assume an init
					init = true;
					Log.d(TAG, "onHandleIntent: Unknown Action");
					break;
			}

		boolean can_sched = true;

		// Safe to ignore refresh is always false warning
		//noinspection ConstantConditions
		if ((wifi_only && (scheduled||init)) && !refresh) {
			can_sched = wifiConnected(getApplicationContext());
		}

		if (autoUpdate) {
			QuakeService.startGetQuakesAlarm(alarmManager, frequency, quakeIntent);
		} else {
			alarmManager.cancel(quakeIntent);
		}

		// Retrieve the Quakes
		//noinspection ConstantConditions
		if (internetAvailable(context) && (can_sched || refresh)) {
			getQuakes(scheduled);
		} else {
			//noinspection ConstantConditions
			if (can_sched || refresh) {
				EventBus.getDefault().post(new QuakesRefreshedError(getString(R.string.no_internet)));
			}
		}
	}

	private void getQuakes(boolean scheduled) {
		SharedPreferences prefs = PreferenceManager
			.getDefaultSharedPreferences(getApplicationContext());

		long lastUpdate = prefs.getLong(SERVICE_LAST_UPDATE, 0);
		String path = QuakeApi.ALL_DAY;
		if (60*60*1000 >= System.currentTimeMillis() - lastUpdate) {
			path=QuakeApi.ALL_HOUR;
		}
		OkHttpClient okHttpClient = new OkHttpClient.Builder()
			.readTimeout(60, TimeUnit.SECONDS)
			.connectTimeout(60, TimeUnit.SECONDS)
			.build();

		Retrofit retrofit = new Retrofit.Builder()
			.baseUrl(QuakeApi.API_ENDPOINT)
			.addConverterFactory(GsonConverterFactory.create())
			.addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
			.client(okHttpClient)
			.build();

		retrofit.create(QuakeApi.class).getQuakes(path)
			.subscribeOn(Schedulers.io())
			.onErrorReturn(throwable -> new FeatureCollection(throwable.getLocalizedMessage()))
			.observeOn(Schedulers.single())
			.subscribe(Quakes -> {
				if (Quakes.getFeatures() != null) {
					SharedPreferences.Editor editor = prefs.edit();
					editor.putLong(SERVICE_LAST_UPDATE, System.currentTimeMillis());
					editor.apply();
					processEarthquakes(Quakes.getFeatures(), Quakes.getMetadata(), scheduled);
				} else {
					Log.d(QuakeApplication.TAG, "got Quakes Error? : " +
						Quakes.getMetadata().getTitle());
					EventBus.getDefault().post(new QuakesRefreshedError(Quakes.getMetadata().getTitle()));
				}
			});
	}

	private boolean quakeExists (String eid) {
		boolean exists = false;
		String where = "eid = \""+ eid +"\"";
		List<Quake> quake = Quake.find(Quake.class, where, null, null, null, null);
		if (quake.size() > 0) {
			exists = true;
		}
		return exists;
	}

	private void processEarthquakes(List<Feature> features, Metadata md, boolean scheduled) {
		ArrayList<Quake> quakeList = new ArrayList<>();
		Observable.fromIterable(features)
			.doOnComplete(() -> {
				if (quakeList.size() > 0) {
					Quake.saveInTx(quakeList);
					EventBus.getDefault()
						.post(new NewQuakes(
							quakeList.size()>NEW_QUAKES_LIST_LIMIT?null:quakeList,
							quakeList.size()));
					// Notifications are only sent when a Sceduled update finds a quake.
					if (scheduled) sendNotification();
				}
				EventBus.getDefault().post(new QuakesRefreshed(md.getTitle(),
					md.getCount(), md.getStatus(),quakeList.size()));
			})
			.subscribe(feature -> {
				if (!quakeExists(feature.getId())) {
					Quake q = new Quake(
						feature.getDescription(),
						feature.getMagnitude(),
						feature.getDate(),
						feature.getLatitude(),
						feature.getLongitude(),
						feature.getDepth(),
						feature.getUrl(),
						feature.getId());
					quakeList.add(0, q);
				}
			});
	}

	private void sendNotification() {
		Intent notificationIntent = new Intent(this, QuakesActivity.class);
		PendingIntent quakePendingIntent =
			PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);
		quakeNBuilder.setContentIntent(quakePendingIntent);
		NotificationManager mNotifyMgr =
			(NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mNotifyMgr.notify(1, quakeNBuilder.build());
	}

}
