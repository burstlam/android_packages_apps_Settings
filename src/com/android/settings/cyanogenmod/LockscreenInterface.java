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

import android.app.ActivityManager;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ChooseLockSettingsHelper;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class LockscreenInterface extends SettingsPreferenceFragment {
    private static final String TAG = "LockscreenInterface";

    private static final String KEY_ENABLE_WIDGETS = "keyguard_enable_widgets";
    private static final String LOCKSCREEN_WIDGETS_CATEGORY = "lockscreen_widgets_category";
    private static final String KEY_LOCKSCREEN_CAMERA_WIDGET = "lockscreen_camera_widget";
    private static final String KEY_LOCKSCREEN_DISABLE_HINTS = "lockscreen_disable_hints";
    private static final String PREF_LOCKSCREEN_USE_CAROUSEL = "lockscreen_use_widget_container_carousel";

    private CheckBoxPreference mEnableKeyguardWidgets;
    private CheckBoxPreference mCameraWidget;
    private CheckBoxPreference mLockscreenHints;
    private CheckBoxPreference mLockscreenUseCarousel;

    private ChooseLockSettingsHelper mChooseLockSettingsHelper;
    private DevicePolicyManager mDPM;
    private boolean mIsPrimary;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mChooseLockSettingsHelper = new ChooseLockSettingsHelper(getActivity());
        mDPM = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

        addPreferencesFromResource(R.xml.lockscreen_interface_settings);
        PreferenceCategory widgetsCategory = (PreferenceCategory) findPreference(LOCKSCREEN_WIDGETS_CATEGORY);

        // Determine which user is logged in
        mIsPrimary = UserHandle.myUserId() == UserHandle.USER_OWNER;
        if (mIsPrimary) {
            // Its the primary user, show all the settings
            if (!Utils.isPhone(getActivity())) {
                if (widgetsCategory != null) {
                    widgetsCategory.removePreference(
                            findPreference(Settings.System.LOCKSCREEN_MAXIMIZE_WIDGETS));
                }
            }

        } else {
            // Secondary user is logged in, remove all primary user specific preferences
        }

        // This applies to all users
        // Enable or disable keyguard widget checkbox based on DPM state
        mEnableKeyguardWidgets = (CheckBoxPreference) findPreference(KEY_ENABLE_WIDGETS);
        if (mEnableKeyguardWidgets != null) {
            if (ActivityManager.isLowRamDeviceStatic()) {
                    /*|| mLockPatternUtils.isLockScreenDisabled()) {*/
                // Widgets take a lot of RAM, so disable them on low-memory devices
                if (widgetsCategory != null) {
                    widgetsCategory.removePreference(findPreference(KEY_ENABLE_WIDGETS));
                    mEnableKeyguardWidgets = null;
                }
            } else {
                final boolean disabled = (0 != (mDPM.getKeyguardDisabledFeatures(null)
                        & DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL));
                if (disabled) {
                    mEnableKeyguardWidgets.setSummary(
                            R.string.security_enable_widgets_disabled_summary);
                } else {
                    mEnableKeyguardWidgets.setSummary(R.string.lockscreen_enable_widgets_summary);
                }
                mEnableKeyguardWidgets.setEnabled(!disabled);
            }
        }

        final boolean cameraDefault = keyguardResources != null
                ? keyguardResources.getBoolean(keyguardResources.getIdentifier(
                "com.android.keyguard:bool/kg_enable_camera_default_widget", null, null)) : false;

        DevicePolicyManager dpm = (DevicePolicyManager)getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        mCameraWidget = (CheckBoxPreference) findPreference(KEY_LOCKSCREEN_CAMERA_WIDGET);
        if (dpm.getCameraDisabled(null)
                || (dpm.getKeyguardDisabledFeatures(null)
                    & DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA) != 0) {
            prefSet.removePreference(mCameraWidget);
        } else {
            mCameraWidget.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_CAMERA_WIDGET, cameraDefault ? 1 : 0) == 1);
            mCameraWidget.setOnPreferenceChangeListener(this);
        }


        mLockscreenHints = (CheckBoxPreference)findPreference(KEY_LOCKSCREEN_DISABLE_HINTS);
        mLockscreenHints.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_DISABLE_HINTS, 1) == 1);
        mLockscreenHints.setOnPreferenceChangeListener(this);

        mLockscreenUseCarousel = (CheckBoxPreference)findPreference(PREF_LOCKSCREEN_USE_CAROUSEL);
        mLockscreenUseCarousel.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL, 0) == 1);
        mLockscreenUseCarousel.setOnPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
        if (mEnableKeyguardWidgets != null) {
            mEnableKeyguardWidgets.setChecked(lockPatternUtils.getWidgetsEnabled());
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mCameraWidget) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_CAMERA_WIDGET,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mLockscreenHints) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_DISABLE_HINTS,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mLockscreenUseCarousel) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_USE_WIDGET_CONTAINER_CAROUSEL,
                    (Boolean) newValue ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        final String key = preference.getKey();

        final LockPatternUtils lockPatternUtils = mChooseLockSettingsHelper.utils();
        if (KEY_ENABLE_WIDGETS.equals(key)) {
            lockPatternUtils.setWidgetsEnabled(mEnableKeyguardWidgets.isChecked());
            return true;
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public static class DeviceAdminLockscreenReceiver extends DeviceAdminReceiver {}

}
