/*
 * Copyright (C) 2012 Slimroms
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
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.widget.SeekBarPreference;

public class NavbarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "NavBar";
    private static final String PREF_MENU_LOCATION = "pref_navbar_menu_location";
    private static final String PREF_NAVBAR_MENU_DISPLAY = "pref_navbar_menu_display";
    private static final String ENABLE_NAVIGATION_BAR = "enable_nav_bar";
    private static final String PREF_BUTTON = "navbar_button_settings";
    private static final String PREF_RING = "navbar_targets_settings";
    private static final String PREF_STYLE_DIMEN = "navbar_style_dimen_settings";
    private static final String PREF_NAVIGATION_BAR_CAN_MOVE = "navbar_can_move";
    private static final String NAVBAR_HIDE_ENABLE = "navbar_hide_enable";
    private static final String NAVBAR_HIDE_TIMEOUT = "navbar_hide_timeout";
    private static final String DRAG_HANDLE_OPACITY = "drag_handle_opacity";
    private static final String DRAG_HANDLE_WIDTH = "drag_handle_width";
    private static final String KEY_ADVANCED_OPTIONS= "advanced_cat";

    private boolean mHasNavBarByDefault;
    private int mNavBarMenuDisplayValue;

    ListPreference mMenuDisplayLocation;
    ListPreference mNavBarMenuDisplay;
    CheckBoxPreference mEnableNavigationBar;
    CheckBoxPreference mNavigationBarCanMove;
    PreferenceScreen mButtonPreference;
    PreferenceScreen mRingPreference;
    PreferenceScreen mStyleDimenPreference;

    CheckBoxPreference mNavBarHideEnable;
    ListPreference mNavBarHideTimeout;
    SeekBarPreference mDragHandleOpacity;
    SeekBarPreference mDragHandleWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.navbar_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mMenuDisplayLocation = (ListPreference) findPreference(PREF_MENU_LOCATION);
        mMenuDisplayLocation.setOnPreferenceChangeListener(this);
        mMenuDisplayLocation.setValue(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.MENU_LOCATION,
                0) + "");

        mNavBarMenuDisplay = (ListPreference) findPreference(PREF_NAVBAR_MENU_DISPLAY);
        mNavBarMenuDisplay.setOnPreferenceChangeListener(this);
        mNavBarMenuDisplayValue = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.MENU_VISIBILITY,
                2);
        mNavBarMenuDisplay.setValue(mNavBarMenuDisplayValue + "");

        mButtonPreference = (PreferenceScreen) findPreference(PREF_BUTTON);
        mRingPreference = (PreferenceScreen) findPreference(PREF_RING);
        mStyleDimenPreference = (PreferenceScreen) findPreference(PREF_STYLE_DIMEN);

        mHasNavBarByDefault = mContext.getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        boolean enableNavigationBar = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_SHOW, mHasNavBarByDefault ? 1 : 0) == 1;
        mEnableNavigationBar = (CheckBoxPreference) findPreference(ENABLE_NAVIGATION_BAR);
        mEnableNavigationBar.setChecked(enableNavigationBar);

        mNavBarHideEnable = (CheckBoxPreference) findPreference(NAVBAR_HIDE_ENABLE);
        mNavBarHideEnable.setChecked(Settings.System.getBoolean(getActivity().getContentResolver(),
                Settings.System.NAV_HIDE_ENABLE, false));

        final int defaultDragOpacity = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.DRAG_HANDLE_OPACITY, 50);
        mDragHandleOpacity = (SeekBarPreference) findPreference(DRAG_HANDLE_OPACITY);
        mDragHandleOpacity.setInitValue((int) (defaultDragOpacity));
        mDragHandleOpacity.setOnPreferenceChangeListener(this);

        final int defaultDragWidth = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.DRAG_HANDLE_WEIGHT, 5);
        mDragHandleWidth = (SeekBarPreference) findPreference(DRAG_HANDLE_WIDTH);
        mDragHandleWidth.setInitValue((int) (defaultDragWidth));
        mDragHandleWidth.setOnPreferenceChangeListener(this);

        mNavBarHideTimeout = (ListPreference) findPreference(NAVBAR_HIDE_TIMEOUT);
        mNavBarHideTimeout.setOnPreferenceChangeListener(this);
        mNavBarHideTimeout.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NAV_HIDE_TIMEOUT, 3000) + "");

        mNavigationBarCanMove = (CheckBoxPreference) findPreference(PREF_NAVIGATION_BAR_CAN_MOVE);
        if (!Utils.isPhone(getActivity())) {
            PreferenceCategory additionalCategory = (PreferenceCategory) findPreference(KEY_ADVANCED_OPTIONS);
            if (mNavigationBarCanMove != null)
                additionalCategory.removePreference(mNavigationBarCanMove);
        } else {
            mNavigationBarCanMove.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_CAN_MOVE, 1) == 0);
        }

        updateNavbarPreferences(enableNavigationBar);
    }

    private void updateNavbarPreferences(boolean show) {
        if (mHasNavBarByDefault) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.UI_FORCE_OVERFLOW_BUTTON,
                    show ? 0 : 1);
        }
        mNavBarMenuDisplay.setEnabled(show);
        mButtonPreference.setEnabled(show);
        mRingPreference.setEnabled(show);
        mStyleDimenPreference.setEnabled(show);
        mNavigationBarCanMove.setEnabled(show);
        mMenuDisplayLocation.setEnabled(show
            && mNavBarMenuDisplayValue != 1);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mEnableNavigationBar) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            updateNavbarPreferences(((CheckBoxPreference) preference).isChecked());
            return true;
        } else if (preference == mNavigationBarCanMove) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_CAN_MOVE,
                    ((CheckBoxPreference) preference).isChecked() ? 0 : 1);
            return true;
        } else if (preference == mNavBarHideEnable) {
            Settings.System.putBoolean(getActivity().getContentResolver(),
                    Settings.System.NAV_HIDE_ENABLE,
                    ((CheckBoxPreference) preference).isChecked());
            mDragHandleOpacity
                    .setInitValue(Settings.System.getInt(getActivity().getContentResolver(),
                            Settings.System.DRAG_HANDLE_OPACITY, 50));
            mDragHandleWidth.setInitValue(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.DRAG_HANDLE_WEIGHT, 5));
            mNavBarHideTimeout.setValue(Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.NAV_HIDE_TIMEOUT, 3000) + "");
            refreshSettings();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mMenuDisplayLocation) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_LOCATION, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mNavBarMenuDisplay) {
            mNavBarMenuDisplayValue = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_VISIBILITY, mNavBarMenuDisplayValue);
            mMenuDisplayLocation.setEnabled(mNavBarMenuDisplayValue != 1);
            return true;
        } else if (preference == mNavBarHideTimeout) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAV_HIDE_TIMEOUT, val);
            return true;
        } else if (preference == mDragHandleOpacity) {
            String newVal = (String) newValue;
            int op = Integer.parseInt(newVal);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DRAG_HANDLE_OPACITY, op);
            return true;
        } else if (preference == mDragHandleWidth) {
            String newVal = (String) newValue;
            int dp = Integer.parseInt(newVal);
            //int height = mapChosenDpToPixels(dp);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.DRAG_HANDLE_WEIGHT, dp);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void refreshSettings() {
        mDragHandleOpacity.setEnabled(mNavBarHideEnable.isChecked());
        mDragHandleWidth.setEnabled(mNavBarHideEnable.isChecked());
        mNavBarHideTimeout.setEnabled(mNavBarHideEnable.isChecked());
    }

}
