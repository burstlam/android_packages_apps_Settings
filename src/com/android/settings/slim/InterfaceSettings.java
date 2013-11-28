/*
 * Copyright (C) 2013 SlimRoms
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

public class InterfaceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
    private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";

    private ListPreference mListViewAnimation;
    private ListPreference mListViewInterpolator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.slim_interface_settings);

        //ListView Animations

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mListViewAnimation = (ListPreference) findPreference(KEY_LISTVIEW_ANIMATION);
        String listViewAnimation = Settings.System.getString(resolver, Settings.System.LISTVIEW_ANIMATION);
        if (listViewAnimation != null) {
             mListViewAnimation.setValue(listViewAnimation);
        }
        mListViewAnimation.setOnPreferenceChangeListener(this);

        mListViewInterpolator = (ListPreference) findPreference(KEY_LISTVIEW_INTERPOLATOR);
        String listViewInterpolator = Settings.System.getString(resolver, Settings.System.LISTVIEW_INTERPOLATOR);
        if (listViewInterpolator != null) {
             mListViewInterpolator.setValue(listViewInterpolator);
        }
        mListViewInterpolator.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mListViewAnimation) {
            String value = (String) newValue;
            Settings.System.putString(resolver, Settings.System.LISTVIEW_ANIMATION, value);
        } else if (preference == mListViewInterpolator) {
            String value = (String) newValue;
            Settings.System.putString(resolver, Settings.System.LISTVIEW_INTERPOLATOR, value);
        } else {
            return false;
        }
        return true;
    }
}
