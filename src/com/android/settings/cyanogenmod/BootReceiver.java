/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.cyanogenmod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.settings.DisplaySettings;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.hardware.DisplayColor;
import com.android.settings.hardware.DisplayGamma;
import com.android.settings.hardware.VibratorIntensity;
import com.android.settings.location.LocationSettings;

import java.util.Arrays;
import java.util.List;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    private static final String PERF_PROFILE_SETTINGS_PROP = "sys.perf.profile.restored";
    private static final String KSM_SETTINGS_PROP = "sys.ksm.restored";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        if (SystemProperties.getBoolean(PERF_PROFILE_SETTINGS_PROP, false) == false
                && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SystemProperties.set(PERF_PROFILE_SETTINGS_PROP, "true");
            configurePerfProfile(ctx);
        } else {
            SystemProperties.set(PERF_PROFILE_SETTINGS_PROP, "false");
        }

        if (Utils.fileExists(MemoryManagement.KSM_RUN_FILE)) {
            if (SystemProperties.getBoolean(KSM_SETTINGS_PROP, false) == false
                    && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                SystemProperties.set(KSM_SETTINGS_PROP, "true");
                configureKSM(ctx);
            } else {
                SystemProperties.set(KSM_SETTINGS_PROP, "false");
            }
        }

        /* Restore the hardware tunable values */
        DisplayColor.restore(ctx);
        DisplayGamma.restore(ctx);
        VibratorIntensity.restore(ctx);
        DisplaySettings.restore(ctx);
        LocationSettings.restore(ctx);
    }

    private void configurePerfProfile(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
        final Resources res = ctx.getResources();

        if (prefs.getBoolean(PerformanceProfile.SOB_PREF, false) == false) {
            Log.i(TAG, "Performance profile restore disabled by user preference.");
            return;
        }

        String perfProfileProp = res.getString(R.string.config_perf_profile_prop);
        if (perfProfileProp == null) {
            Log.d(TAG, "Performance profiles are not supported by the device. Nothing to restore.");
        }

        String perfProfile = prefs.getString(PerformanceProfile.PERF_PROFILE_PREF, null);
        if (perfProfile == null) {
            Log.d(TAG, "No performance profile settings saved. Nothing to restore.");
        } else {
            SystemProperties.set(perfProfileProp, perfProfile);
            Log.d(TAG, "Performance profile settings restored.");
        }
    }

    private void configureKSM(Context ctx) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);

        boolean ksmDefault = (SystemProperties.get("ro.ksm.default", "0") != "0");
        boolean ksm = prefs.getBoolean(MemoryManagement.KSM_PREF, ksmDefault);

        Utils.fileWriteOneLine(MemoryManagement.KSM_RUN_FILE, ksm ? "1" : "0");
        Log.d(TAG, "KSM settings restored.");
    }
}
