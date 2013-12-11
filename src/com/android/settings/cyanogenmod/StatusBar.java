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

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_NETWORK_STATS = "status_bar_show_network_stats";
    private static final String STATUS_BAR_NETWORK_STATS_UPDATE = "status_bar_network_status_update";

    private ListPreference mNetStatsUpdate;
    private ColorPickerPreference mNetStatsColorPicker;
    private CheckBoxPreference mNetworkStats;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar);
        ContentResolver resolver = getActivity().getContentResolver();

        mNetworkStats = (CheckBoxPreference) findPreference(STATUS_BAR_NETWORK_STATS);
        mNetworkStats.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS, 0) == 1));

        mNetStatsColorPicker = (ColorPickerPreference) findPreference("status_bar_network_status_color");
        mNetStatsColorPicker.setOnPreferenceChangeListener(this);

        mNetStatsUpdate = (ListPreference) findPreference(STATUS_BAR_NETWORK_STATS_UPDATE);
        long statsUpdate = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, 500);
        mNetStatsUpdate.setValue(String.valueOf(statsUpdate));
        mNetStatsUpdate.setSummary(mNetStatsUpdate.getEntry());
        mNetStatsUpdate.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                         Preference preference) {
        if (preference == mNetworkStats) {
            boolean value = mNetworkStats.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS, value ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mNetStatsUpdate) {
            long updateInterval = Long.valueOf((String) newValue);
            int index = mNetStatsUpdate.findIndexOfValue((String) newValue);
            Settings.System.putLong(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, updateInterval);
            mNetStatsUpdate.setSummary(mNetStatsUpdate.getEntries()[index]);
            return true;
        } else if (preference == mNetStatsColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);

            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS_TEXT_COLOR, intHex);
            return true;
        }
        return false;
    }
}
