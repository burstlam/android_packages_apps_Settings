/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.settings.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class AdvancedWifiSettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "AdvancedWifiSettings";
    private static final String KEY_MAC_ADDRESS = "mac_address";
    private static final String KEY_CURRENT_IP_ADDRESS = "current_ip_address";
    private static final String KEY_FREQUENCY_BAND = "frequency_band";
    private static final String KEY_COUNTRY_CODE = "wifi_countrycode";
    private static final String KEY_NOTIFY_OPEN_NETWORKS = "notify_open_networks";
    private static final String KEY_SLEEP_POLICY = "sleep_policy";
    private static final String KEY_POOR_NETWORK_DETECTION = "wifi_poor_network_detection";
    private static final String KEY_WIFI_IDLE_MN = "wifi_idle_mn";

    private WifiManager mWifiManager;

    private static final long DEFAULT_IDLE_MS = 15 * 60 * 1000; /* 15 minutes */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.wifi_advanced_settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onResume() {
        super.onResume();
        initPreferences();
        refreshWifiInfo();
    }

    private void initPreferences() {
        CheckBoxPreference notifyOpenNetworks =
            (CheckBoxPreference) findPreference(KEY_NOTIFY_OPEN_NETWORKS);
        notifyOpenNetworks.setChecked(Secure.getInt(getContentResolver(),
                Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON, 0) == 1);
        notifyOpenNetworks.setEnabled(mWifiManager.isWifiEnabled());

        CheckBoxPreference poorNetworkDetection =
            (CheckBoxPreference) findPreference(KEY_POOR_NETWORK_DETECTION);
        if (poorNetworkDetection != null) {
            if (Utils.isWifiOnly(getActivity())) {
                getPreferenceScreen().removePreference(poorNetworkDetection);
            } else {
                poorNetworkDetection.setChecked(Secure.getInt(getContentResolver(),
                        Secure.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED, 1) == 1);
            }
        }

        ListPreference frequencyPref = (ListPreference) findPreference(KEY_FREQUENCY_BAND);

        if (mWifiManager.isDualBandSupported()) {
            frequencyPref.setOnPreferenceChangeListener(this);
            int value = mWifiManager.getFrequencyBand();
            if (value != -1) {
                frequencyPref.setValue(String.valueOf(value));
            } else {
                Log.e(TAG, "Failed to fetch frequency band");
            }
        } else {
            if (frequencyPref != null) {
                // null if it has already been removed before resume
                getPreferenceScreen().removePreference(frequencyPref);
            }
        }

        ListPreference sleepTimeoutPref = (ListPreference) findPreference(KEY_WIFI_IDLE_MN);
        if (sleepTimeoutPref != null) {
            sleepTimeoutPref.setOnPreferenceChangeListener(this);
            long value = Secure.getLong(getContentResolver(),
                    Secure.WIFI_IDLE_MS, DEFAULT_IDLE_MS);
            String stringValue = String.valueOf(value / (1000 * 60));
            sleepTimeoutPref.setValue(stringValue);
            updateSleepTimeoutSummary(sleepTimeoutPref, stringValue);
        }

        ListPreference ccodePref = (ListPreference) findPreference(KEY_COUNTRY_CODE);
        if (ccodePref != null) {
            ccodePref.setOnPreferenceChangeListener(this);
            String value = mWifiManager.getCountryCode();
            if (value != null) {
                ccodePref.setValue(value);
            } else {
                Log.e(TAG, "Failed to fetch country code");
            }
        }

        ListPreference sleepPolicyPref = (ListPreference) findPreference(KEY_SLEEP_POLICY);
        if (sleepPolicyPref != null) {
            if (Utils.isWifiOnly(getActivity())) {
                sleepPolicyPref.setEntries(R.array.wifi_sleep_policy_entries_wifi_only);
            }
            sleepPolicyPref.setOnPreferenceChangeListener(this);
            int value = Settings.System.getInt(getContentResolver(),
                    Settings.System.WIFI_SLEEP_POLICY,
                    Settings.System.WIFI_SLEEP_POLICY_NEVER);
            String stringValue = String.valueOf(value);
            sleepPolicyPref.setValue(stringValue);
            updateSleepPolicySummary(sleepPolicyPref, stringValue);
        }
    }

    private void updateSleepPolicySummary(Preference sleepPolicyPref, String value) {
        if (value != null) {
            String[] values = getResources().getStringArray(R.array.wifi_sleep_policy_values);
            final int summaryArrayResId = Utils.isWifiOnly(getActivity()) ?
                    R.array.wifi_sleep_policy_entries_wifi_only : R.array.wifi_sleep_policy_entries;
            String[] summaries = getResources().getStringArray(summaryArrayResId);
            for (int i = 0; i < values.length; i++) {
                if (value.equals(values[i])) {
                    if (i < summaries.length) {
                        sleepPolicyPref.setSummary(summaries[i]);
                        Preference sleepTimeoutPref = findPreference(KEY_WIFI_IDLE_MN);
                        if (sleepTimeoutPref != null) {
                            sleepTimeoutPref.setEnabled(!value.equals(String.valueOf(
                                    Settings.System.WIFI_SLEEP_POLICY_NEVER)));
                        }
                        return;
                    }
                }
            }
        }

        sleepPolicyPref.setSummary("");
        Log.e(TAG, "Invalid sleep policy value: " + value);
    }

    private void updateSleepTimeoutSummary(Preference sleepTimeoutPref, String value) {
        if (value != null) {
            String[] values = getResources().getStringArray(R.array.wifi_idle_mn_values);
            final int summaryArrayId = R.array.wifi_idle_mn_entries;
            String[] summaries = getResources().getStringArray(summaryArrayId);
            for (int i = 0; i < values.length; i++) {
                 if (value.equals(values[i]) && i < summaries.length) {
                     sleepTimeoutPref.setSummary(summaries[i]);
                     return;
                 }
            }
        }

        sleepTimeoutPref.setSummary("");
        Log.e(TAG, "Invalid wifi idle timeout value: " + value);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {
        String key = preference.getKey();

        if (KEY_NOTIFY_OPEN_NETWORKS.equals(key)) {
            Secure.putInt(getContentResolver(),
                    Secure.WIFI_NETWORKS_AVAILABLE_NOTIFICATION_ON,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
        } else if (KEY_POOR_NETWORK_DETECTION.equals(key)) {
            Secure.putInt(getContentResolver(),
                    Secure.WIFI_WATCHDOG_POOR_NETWORK_TEST_ENABLED,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
        } else {
            return super.onPreferenceTreeClick(screen, preference);
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();

        if (KEY_FREQUENCY_BAND.equals(key)) {
            try {
                mWifiManager.setFrequencyBand(Integer.parseInt((String) newValue), true);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), R.string.wifi_setting_frequency_band_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (KEY_COUNTRY_CODE.equals(key)) {
            try {
                mWifiManager.setCountryCode((String) newValue, true);
            } catch (IllegalArgumentException e) {
                Toast.makeText(getActivity(), R.string.wifi_setting_countrycode_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (KEY_SLEEP_POLICY.equals(key)) {
            try {
                String stringValue = (String) newValue;
                Settings.System.putInt(getContentResolver(), Settings.System.WIFI_SLEEP_POLICY,
                        Integer.parseInt(stringValue));
                updateSleepPolicySummary(preference, stringValue);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), R.string.wifi_setting_sleep_policy_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        if (KEY_WIFI_IDLE_MN.equals(key)) {
            try {
                String stringValue = (String) newValue;
                Settings.Secure.putLong(getContentResolver(), Settings.Secure.WIFI_IDLE_MS,
                        Integer.parseInt(stringValue) * 60 * 1000);
                updateSleepTimeoutSummary(preference, stringValue);
            } catch (NumberFormatException e) {
                Toast.makeText(getActivity(), R.string.wifi_setting_sleep_policy_error,
                        Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }

    private void refreshWifiInfo() {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

        Preference wifiMacAddressPref = findPreference(KEY_MAC_ADDRESS);
        String macAddress = wifiInfo == null ? null : wifiInfo.getMacAddress();
        wifiMacAddressPref.setSummary(!TextUtils.isEmpty(macAddress) ? macAddress
                : getActivity().getString(R.string.status_unavailable));

        Preference wifiIpAddressPref = findPreference(KEY_CURRENT_IP_ADDRESS);
        String ipAddress = Utils.getWifiIpAddresses(getActivity());
        wifiIpAddressPref.setSummary(ipAddress == null ?
                getActivity().getString(R.string.status_unavailable) : ipAddress);
    }

}
