/*
 * Copyright (C) 2013 Light Open Source Project
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

package com.android.settings.slim;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.util.CMDProcessor;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class StatusBarNetworkStyle extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBarNetworkStyle";

    private static final String STATUS_BAR_SHOW_TRAFFIC = "status_bar_show_traffic";
    private static final String STATUS_BAR_TRAFFIC_COLOR = "status_bar_traffic_color";
    private static final String STATUS_BAR_TRAFFIC_AUTOHIDE = "status_bar_traffic_autohide";
    private static final String STATUS_BAR_CARRIER_LABEL = "status_bar_carrier_label";
    private static final String STATUS_BAR_CARRIER_COLOR = "status_bar_carrier_color";

    private static final int MENU_RESET = Menu.FIRST;

    private CheckBoxPreference mStatusBarShowTraffic;
    private CheckBoxPreference mStatusBarTraffic_autohide;
    private CheckBoxPreference mStatusBarCarrierLabel;
    private ColorPickerPreference mTrafficColorPicker;
    private ColorPickerPreference mCarrierColorPicker;

    private boolean mCheckPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createCustomView();
    }
    
    private PreferenceScreen createCustomView() {
        mCheckPreferences = false;
        PreferenceScreen prefSet = getPreferenceScreen();
        if (prefSet != null) {
            prefSet.removeAll();
        }

        addPreferencesFromResource(R.xml.status_bar_network_style);
        prefSet = getPreferenceScreen();
        int defaultColor;
        int intColor;
        String hexColor;
        
        mStatusBarShowTraffic = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_SHOW_TRAFFIC);
        mStatusBarShowTraffic.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                            Settings.System.STATUS_BAR_SHOW_TRAFFIC, 0) == 1));

        mStatusBarTraffic_autohide = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC_AUTOHIDE);
        mStatusBarTraffic_autohide.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_TRAFFIC_AUTOHIDE, 0) == 1));

        mTrafficColorPicker = (ColorPickerPreference) prefSet.findPreference(STATUS_BAR_TRAFFIC_COLOR);
        mTrafficColorPicker.setOnPreferenceChangeListener(this);
        defaultColor = getResources().getColor(
                com.android.internal.R.color.holo_blue_light);
        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_TRAFFIC_COLOR, defaultColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mTrafficColorPicker.setSummary(hexColor);
        mTrafficColorPicker.setNewPreviewColor(intColor);

        mStatusBarCarrierLabel = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_CARRIER_LABEL);
        mStatusBarCarrierLabel.setChecked((Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_CARRIER, 1) == 1));

        mCarrierColorPicker = (ColorPickerPreference) prefSet.findPreference(STATUS_BAR_CARRIER_COLOR);
        mCarrierColorPicker.setOnPreferenceChangeListener(this);
        defaultColor = getResources().getColor(
                com.android.internal.R.color.holo_blue_light);
        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER_COLOR, defaultColor);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mCarrierColorPicker.setSummary(hexColor);
        mCarrierColorPicker.setNewPreviewColor(intColor);

        setHasOptionsMenu(true);
        mCheckPreferences = true;
        return prefSet;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.navbar_reset)
                .setIcon(R.drawable.ic_settings_backup) // use the backup icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.statusbar_network_style_reset_title);
        alertDialog.setMessage(R.string.statusbar_network_style_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_SHOW_TRAFFIC, 1);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_TRAFFIC_AUTOHIDE, 0);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_TRAFFIC_COLOR, 0xff33b5e5);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_CARRIER, 0);
                Settings.System.putInt(getActivity().getContentResolver(),
                        Settings.System.STATUS_BAR_CARRIER_COLOR, 0xff33b5e5);
                createCustomView();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mStatusBarShowTraffic) {
            value = mStatusBarShowTraffic.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_SHOW_TRAFFIC, value ? 1 : 0);
            CMDProcessor.restartSystemUI();
            return true;
        } else if (preference == mStatusBarTraffic_autohide) {
            value = mStatusBarTraffic_autohide.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_TRAFFIC_AUTOHIDE, value ? 1 : 0);
            return true;
        } else if (preference == mStatusBarCarrierLabel) {
            value = mStatusBarCarrierLabel.isChecked();
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER, value ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!mCheckPreferences) {
            return false;
        }

        if (preference == mTrafficColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_TRAFFIC_COLOR, intHex);
            return true;
        } else if (preference == mCarrierColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_CARRIER_COLOR, intHex);
            return true;
        }
        return false;
    }
}
