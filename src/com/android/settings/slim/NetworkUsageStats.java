package com.android.settings.slim;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
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

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class NetworkUsageStats extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String STATUS_BAR_NETWORK_STATS = "status_bar_show_network_stats";
    private static final String STATUS_BAR_NETWORK_STATS_UPDATE = "status_bar_network_status_update";
    private static final String STATUS_BAR_NETWORK_COLOR = "status_bar_network_color";
    //private static final String STATUS_BAR_NETWORK_HIDE = "status_bar_network_hide";
    
    private ListPreference mStatusBarNetStatsUpdate;
    private ListPreference mStatusBarNetworkStats;
    private ColorPickerPreference mStatusBarNetworkColor;
    //private CheckBoxPreference mStatusBarNetworkHide;
    
    private static final int MENU_RESET = Menu.FIRST;

    static final int DEFAULT_NETWORK_USAGE_COLOR = 0xffffffff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	refreshSettings();
    }

    public void refreshSettings() {
	PreferenceScreen prefSet = getPreferenceScreen();
        if (prefSet != null) {
            prefSet.removeAll();
        }

        addPreferencesFromResource(R.xml.network_usage_stats);
        prefSet = getPreferenceScreen();

        mStatusBarNetworkStats = (ListPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS);
        int trafficStyle = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS, 0);
        mStatusBarNetworkStats.setValue(String.valueOf(trafficStyle));
        mStatusBarNetworkStats.setSummary( mStatusBarNetworkStats.getEntry());
        mStatusBarNetworkStats.setOnPreferenceChangeListener(this);

        mStatusBarNetStatsUpdate = (ListPreference) prefSet.findPreference(STATUS_BAR_NETWORK_STATS_UPDATE);
        long statsUpdate = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, 500);
        mStatusBarNetStatsUpdate.setValue(String.valueOf(statsUpdate));
        mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntry());
        mStatusBarNetStatsUpdate.setOnPreferenceChangeListener(this);

    	// custom colors
	    mStatusBarNetworkColor = (ColorPickerPreference) prefSet.findPreference(STATUS_BAR_NETWORK_COLOR);
      	mStatusBarNetworkColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getActivity().getContentResolver(),
                   Settings.System.STATUS_BAR_NETWORK_COLOR, 0xffffffff);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
        mStatusBarNetworkColor.setSummary(hexColor);
        mStatusBarNetworkColor.setNewPreviewColor(intColor);

      	setHasOptionsMenu(true);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.status_bar_network_usage_color_reset)
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
        alertDialog.setTitle(R.string.status_bar_network_usage_color_reset);
        alertDialog.setMessage(R.string.status_bar_network_usage_color_reset_message);
        alertDialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                NetworkStatsColorReset();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void NetworkStatsColorReset() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.STATUS_BAR_NETWORK_COLOR, DEFAULT_NETWORK_USAGE_COLOR);
        
        mStatusBarNetworkColor.setNewPreviewColor(DEFAULT_NETWORK_USAGE_COLOR);
        String hexColor = String.format("#%08x", (0xffffffff & DEFAULT_NETWORK_USAGE_COLOR));
        mStatusBarNetworkColor.setSummary(hexColor);
    } 

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mStatusBarNetStatsUpdate) {
            long updateInterval = Long.valueOf((String) newValue);
            int index = mStatusBarNetStatsUpdate.findIndexOfValue((String) newValue);
            Settings.System.putLong(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, updateInterval);
            mStatusBarNetStatsUpdate.setSummary(mStatusBarNetStatsUpdate.getEntries()[index]);
            return true;
    	} else if (preference == mStatusBarNetworkStats) {
            int trafficStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarNetworkStats.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_STATS, trafficStyle);
            mStatusBarNetworkStats.setSummary(mStatusBarNetworkStats.getEntries()[index]);
            return true;
    	} else if (preference == mStatusBarNetworkColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_NETWORK_COLOR, intHex);
            return true;  
        }
        return false;
    }

}
