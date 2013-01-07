
package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import com.android.settings.cyanogenmod.DailyRebootScheduleService;

public class RebootReceiver extends BroadcastReceiver {

    public static final String ACTION_REBOOT = "com.android.settings.ACTION_DAILY_REBOOT";
    public static final String ACTION_WARN = "com.android.settings.ACTION_DAILY_REBOOT_WARNING";

    static final int MIN_SCREEN_OFF_TIME_IN_MILLIS = 1000 * 20 * 20; // 20m

    static long lastScreenOffAt = System.currentTimeMillis();

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (ACTION_REBOOT.equals(action)) {
            Log.i("RC", "ROMControl daily reboot starting!");
            if (Math.abs(System.currentTimeMillis() - lastScreenOffAt) > MIN_SCREEN_OFF_TIME_IN_MILLIS) {
                PowerManager pm = (PowerManager) context.getApplicationContext()
                        .getSystemService(Context.POWER_SERVICE);
                pm.reboot(null);
            } else {
                Intent reschedule = new Intent(context, DailyRebootScheduleService.class);
                reschedule.putExtra("reschedule", true);
                context.startService(reschedule);
            }
        } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            lastScreenOffAt = System.currentTimeMillis();
        }
    }
}
