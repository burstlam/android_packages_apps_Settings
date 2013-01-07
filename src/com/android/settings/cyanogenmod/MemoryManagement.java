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

import java.io.File;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.TimePicker;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.cyanogenmod.CMDProcessor;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class MemoryManagement extends SettingsPreferenceFragment implements
        OnSharedPreferenceChangeListener, OnPreferenceChangeListener {

    public static final String TAG = "MemoryManagement";

    public static final String KEY_MINFREE = "free_memory";
    public static final String MINFREE = "/sys/module/lowmemorykiller/parameters/minfree";
    public static final String KEY_DAILY_REBOOT = "daily_reboot";

    public static final String KSM_RUN_FILE = "/sys/kernel/mm/ksm/run";

    public static final String KSM_PREF = "pref_ksm";

    public static final String KSM_PREF_DISABLED = "0";

    public static final String KSM_PREF_ENABLED = "1";

    private static final String ZRAM_PREF = "pref_zram_size";

    private static final String ZRAM_PERSIST_PROP = "persist.service.zram"; // was compcache

    private static final String ZRAM_DEFAULT = SystemProperties.get("ro.zram.default"); // was compcache

    private static final String PURGEABLE_ASSETS_PREF = "pref_purgeable_assets";

    private static final String PURGEABLE_ASSETS_PERSIST_PROP = "persist.sys.purgeable_assets";

    private static final String PURGEABLE_ASSETS_DEFAULT = "0";

    private ListPreference mzRAM;

    private CheckBoxPreference mPurgeableAssetsPref;

    private CheckBoxPreference mKSMPref;

    private int swapAvailable = -1;

    private ListPreference mFreeMem;
    private CheckBoxPreference mDailyReboot;
    private SharedPreferences preferences;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.memory_management);

            PreferenceScreen prefSet = getPreferenceScreen();
            preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            preferences.registerOnSharedPreferenceChangeListener(this);

            mzRAM = (ListPreference) prefSet.findPreference(ZRAM_PREF);
            mPurgeableAssetsPref = (CheckBoxPreference) prefSet.findPreference(PURGEABLE_ASSETS_PREF);
            mKSMPref = (CheckBoxPreference) prefSet.findPreference(KSM_PREF);

            if (isSwapAvailable()) {
                if (SystemProperties.get(ZRAM_PERSIST_PROP) == "1")
                    SystemProperties.set(ZRAM_PERSIST_PROP, ZRAM_DEFAULT);
                mzRAM.setValue(SystemProperties.get(ZRAM_PERSIST_PROP, ZRAM_DEFAULT));
                mzRAM.setOnPreferenceChangeListener(this);
            } else {
                prefSet.removePreference(mzRAM);
            }

            if (Utils.fileExists(KSM_RUN_FILE)) {
                mKSMPref.setChecked(KSM_PREF_ENABLED.equals(Utils.fileReadOneLine(KSM_RUN_FILE)));
            } else {
                prefSet.removePreference(mKSMPref);
            }

            String purgeableAssets = SystemProperties.get(PURGEABLE_ASSETS_PERSIST_PROP,
                    PURGEABLE_ASSETS_DEFAULT);
            mPurgeableAssetsPref.setChecked("1".equals(purgeableAssets));

        final int minFree = getMinFreeValue();
        final String values[] = getResources().getStringArray(R.array.minfree_values);
        String closestValue = preferences.getString(KEY_MINFREE, values[0]);

        if (minFree < 37)
            closestValue = values[0];
        else if (minFree < 62)
            closestValue = values[1];
        else if (minFree < 77)
            closestValue = values[2];
        else if (minFree < 90)
            closestValue = values[3];
        else
            closestValue = values[4];

        mFreeMem = (ListPreference) findPreference(KEY_MINFREE);
        mFreeMem.setValue(closestValue);
        mFreeMem.setSummary(getString(R.string.ps_free_memory, minFree + "mb"));

        mDailyReboot = (CheckBoxPreference) findPreference(KEY_DAILY_REBOOT);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        String key = preference.getKey();

        if (preference == mPurgeableAssetsPref) {
            SystemProperties.set(PURGEABLE_ASSETS_PERSIST_PROP,
                    mPurgeableAssetsPref.isChecked() ? "1" : "0");
            return true;
        }

        if (preference == mKSMPref) {
            Utils.fileWriteOneLine(KSM_RUN_FILE, mKSMPref.isChecked() ? "1" : "0");
            return true;
        }

        if (preference == mDailyReboot) {
            if (((CheckBoxPreference) preference).isChecked()) {
                getFragmentManager().beginTransaction()
                        .addToBackStack("timepicker").add(new TimePickerFragment(), "timepicker")
                        .commit();
            } else {
                updateRebootSummary();
                // send intent to unschedule
                Intent schedule = new Intent(getActivity(),
                        DailyRebootScheduleService.class);
                getActivity().startService(schedule);
            }
            return true;
        }

        return false;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mzRAM) {
            if (newValue != null) {
                SystemProperties.set(ZRAM_PERSIST_PROP, (String) newValue);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRebootSummary();
    }

    public void updateRebootSummary() {
        if (mDailyReboot.isChecked()) {
            int[] rebootTime = getUserSpecifiedRebootTime(mContext);
            java.text.DateFormat f = DateFormat.getTimeFormat(mContext);
            GregorianCalendar d = new GregorianCalendar();
            d.set(Calendar.HOUR_OF_DAY, rebootTime[0]);
            d.set(Calendar.MINUTE, rebootTime[1]);
            Resources res = getResources();
            mDailyReboot
                    .setSummary(String.format(
                            res.getString(R.string.performance_daily_reboot_summary),
                            f.format(d.getTime())));
        } else {
            mDailyReboot.setSummary(mContext
                    .getString(R.string.performance_daily_reboot_summary_unscheduled));
        }
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_MINFREE)) {
            String values = preferences.getString(key, null);
            if (!values.equals(null))
                new CMDProcessor().su
                        .runWaitFor("busybox echo " + values + " > " + MINFREE);
            mFreeMem.setSummary(getString(R.string.ps_free_memory, getMinFreeValue() + "mb"));
        }
    }

    private int getMinFreeValue() {
        int emptyApp = 0;
        String MINFREE_LINE = Utils.fileReadOneLine(MINFREE);
        String EMPTY_APP = MINFREE_LINE.substring(MINFREE_LINE.lastIndexOf(",") + 1);

        if (!EMPTY_APP.equals(null) || !EMPTY_APP.equals("")) {
            try {
                int mb = Integer.parseInt(EMPTY_APP.trim()) * 4 / 1024;
                emptyApp = (int) Math.ceil(mb);
            } catch (NumberFormatException nfe) {
                Log.i(TAG, "error processing " + EMPTY_APP);
            }
        }
        return emptyApp;
    }

    /**
     * Check if swap support is available on the system
     */
    private boolean isSwapAvailable() {
        if (swapAvailable < 0) {
            swapAvailable = new File("/proc/swaps").exists() ? 1 : 0;
        }
        return swapAvailable > 0;
    }

    public class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            setUserSpecifiedRebootTime(getActivity(), hourOfDay, minute);
            Intent schedule = new Intent(getActivity(),
                    DailyRebootScheduleService.class);
            getActivity().startService(schedule);
            updateRebootSummary();
        }
    }

    public static boolean isDailyRebootEnabled(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        return prefs.getBoolean(KEY_DAILY_REBOOT, false);
    }

    public static int[] getUserSpecifiedRebootTime(Context c) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        int[] time = new int[2];
        time[0] = prefs.getInt(KEY_DAILY_REBOOT + "_hour", 1);
        time[1] = prefs.getInt(KEY_DAILY_REBOOT + "_minute", 0);
        return time;
    }

    public static void setUserSpecifiedRebootTime(Context c, int hour, int minutes) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(c);
        prefs.edit().putInt(KEY_DAILY_REBOOT + "_hour", hour).
                putInt(KEY_DAILY_REBOOT + "_minute", minutes).commit();
    }
}
