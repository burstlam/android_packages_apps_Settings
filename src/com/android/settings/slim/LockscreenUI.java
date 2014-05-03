/*
 * Copyright (C) 2013 SlimRoms Project
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

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class LockscreenUI extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String LOCKSCREEN_GENERAL_CATEGORY = "lockscreen_general_category";
    private static final String KEY_LOCKSCREEN_BUTTONS = "lockscreen_buttons";
    private static final String KEY_LOCKSCREEN_DOUBLE_TAP_SLEEP_GESTURE = "lockscreen_double_tap_sleep_gesture";
    private static final String KEY_NOTIFICATION_PEEK = "notification_peek";

    private CheckBoxPreference mDoubleTapSleep;
    private CheckBoxPreference mNotificationPeek;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreen_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        // Find categories
        PreferenceCategory generalCategory = (PreferenceCategory)
                findPreference(LOCKSCREEN_GENERAL_CATEGORY);

        mDoubleTapSleep = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_DOUBLE_TAP_SLEEP_GESTURE);
        mDoubleTapSleep.setChecked(Settings.System.getInt(resolver,
                Settings.System.LOCKSCREEN_DOUBLE_TAP_SLEEP_GESTURE, 0) == 1);
        mDoubleTapSleep.setOnPreferenceChangeListener(this);

        mNotificationPeek = (CheckBoxPreference) findPreference(KEY_NOTIFICATION_PEEK);
        mNotificationPeek.setChecked(Settings.System.getInt(resolver,
                Settings.System.PEEK_STATE, 0) == 1);
        mNotificationPeek.setOnPreferenceChangeListener(this);

        // Remove lockscreen button actions if device doesn't have hardware keys
        if (!hasButtons()) {
            generalCategory.removePreference(findPreference(KEY_LOCKSCREEN_BUTTONS));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * Checks if the device has hardware buttons.
     * @return has Buttons
     */
    public boolean hasButtons() {
        return !getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mDoubleTapSleep) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.LOCKSCREEN_DOUBLE_TAP_SLEEP_GESTURE, value ? 1 : 0);
            return true;
        } else if (preference == mNotificationPeek) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.PEEK_STATE, value ? 1 : 0);
            return true;
        }
        return false;
    }

}
