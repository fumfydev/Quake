package net.fumfy.examples.quakes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Project : Quakes
 * Created by Simon Barnes on 12/06/2017.
 *
 * @author Simon Barnes
 */

public class QuakeAlarmReceiver extends BroadcastReceiver {

	public static final String ACTION_RETRIEVE_QUAKES_ALARM =
		"net.fumfy.examples.quakes.service.ALARM_RETRIEVE_QUAKES";

	@Override
	public void onReceive(Context context, Intent intent) {
		Intent startIntent = new Intent(context, QuakeService.class);
		startIntent.putExtra(QuakeService.ACTION_NAME, QuakeService.ACTION_SCHEDULED_REFRESH);
		context.startService(startIntent);
	}
}
