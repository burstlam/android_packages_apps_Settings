/*
 * Copyright (C) 2013 The ChameleonOS Project
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

package com.android.settings.vanir;

import android.content.Context;
import android.graphics.Point;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.ListPreference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.WindowManager;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import static android.hardware.Sensor.TYPE_LIGHT;
import com.android.settings.widget.SeekBarPreference2;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class ActiveNotificationSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
    private static final String TAG = "ActiveDisplaySettings";

    private static final String KEY_SHOW_TEXT = "ad_text";
    private static final String KEY_REDISPLAY = "ad_redisplay";
    private static final String KEY_SHOW_DATE = "ad_show_date";
    private static final String KEY_BRIGHTNESS = "ad_brightness";
    private static final String KEY_TIMEOUT = "ad_timeout";
    private static final String KEY_THRESHOLD = "ad_threshold";
    private static final String KEY_OFFSET_TOP = "offset_top";
    private static final String KEY_EXPANDED_VIEW = "expanded_view";
    private static final String KEY_FORCE_EXPANDED_VIEW = "force_expanded_view";
    private static final String KEY_NOTIFICATIONS_HEIGHT = "notifications_height";
    private static final String KEY_WAKE_ON_NOTIFICATION = "wake_on_notification";
    private static final String KEY_NOTIFICATION_COLOR = "notification_color";
    private static final String KEY_SUNLIGHT_MODE = "ad_sunlight_mode";
    private static final String KEY_TURNOFF_MODE = "ad_turnoff_mode";

    private CheckBoxPreference mShowTextPref;
    private CheckBoxPreference mShowDatePref;
    private ListPreference mRedisplayPref;
    private SeekBarPreference2 mBrightnessLevel;
    private ListPreference mDisplayTimeout;
    private ListPreference mProximityThreshold;
    private SeekBarPreference mOffsetTop;
    private CheckBoxPreference mWakeOnNotification;
    private CheckBoxPreference mExpandedView;
    private CheckBoxPreference mForceExpandedView;
    private NumberPickerPreference mNotificationsHeight;
    private ColorPickerPreference mNotificationColor;
    private CheckBoxPreference mSunlightModePref;
    private CheckBoxPreference mTurnOffModePref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.active_notification_settings);

        boolean AmonAmarth = Settings.System.getInt(getContentResolver(),
                Settings.System.ENABLE_ACTIVE_DISPLAY, 0) == 1;

        mShowTextPref = (CheckBoxPreference) findPreference(KEY_SHOW_TEXT);
        mShowTextPref.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.ACTIVE_DISPLAY_TEXT, 0) == 1));

        PreferenceScreen prefSet = getPreferenceScreen();
        mRedisplayPref = (ListPreference) prefSet.findPreference(KEY_REDISPLAY);
        mRedisplayPref.setOnPreferenceChangeListener(this);
        long timeout = Settings.System.getLong(getContentResolver(),
                Settings.System.ACTIVE_DISPLAY_REDISPLAY, 0);
        mRedisplayPref.setValue(String.valueOf(timeout));
        updateRedisplaySummary(timeout);

        mShowDatePref = (CheckBoxPreference) findPreference(KEY_SHOW_DATE);
        mShowDatePref.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.ACTIVE_DISPLAY_SHOW_DATE, 0) == 1));

        PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        int minimumBacklight = pm.getMinimumScreenBrightnessSetting();
        int maximumBacklight = pm.getMaximumScreenBrightnessSetting();

        mBrightnessLevel = (SeekBarPreference2) prefSet.findPreference(KEY_BRIGHTNESS);
        mBrightnessLevel.setMaxValue(maximumBacklight - minimumBacklight);
        mBrightnessLevel.setMinValue(minimumBacklight);
        mBrightnessLevel.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.ACTIVE_DISPLAY_BRIGHTNESS, maximumBacklight) - minimumBacklight);
        mBrightnessLevel.setOnPreferenceChangeListener(this);

        try {
            if (Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mBrightnessLevel.setEnabled(false);
                mBrightnessLevel.setSummary(R.string.ad_autobrightness_mode_on);
            }
        } catch (SettingNotFoundException e) {
        }

        mDisplayTimeout = (ListPreference) prefSet.findPreference(KEY_TIMEOUT);
        mDisplayTimeout.setOnPreferenceChangeListener(this);
        timeout = Settings.System.getLong(getContentResolver(),
                Settings.System.ACTIVE_DISPLAY_TIMEOUT, 8000L);
        mDisplayTimeout.setValue(String.valueOf(timeout));
        updateTimeoutSummary(timeout);

        mWakeOnNotification = (CheckBoxPreference) prefSet.findPreference(KEY_WAKE_ON_NOTIFICATION);
        mWakeOnNotification.setChecked(Settings.System.getInt(getContentResolver(),
        Settings.System.LOCKSCREEN_NOTIFICATIONS_WAKE_ON_NOTIFICATION, 0) == 1);

        mProximityThreshold = (ListPreference) prefSet.findPreference(KEY_THRESHOLD);
        mProximityThreshold.setOnPreferenceChangeListener(this);
        long threshold = Settings.System.getLong(getContentResolver(),
                Settings.System.ACTIVE_DISPLAY_THRESHOLD, 5000L);
        mProximityThreshold.setValue(String.valueOf(threshold));
        updateThresholdSummary(threshold);

        mOffsetTop = (SeekBarPreference) findPreference(KEY_OFFSET_TOP);
        mOffsetTop.setProgress((int)(Settings.System.getFloat(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP, 0.3f) * 100));
        mOffsetTop.setTitle(getResources().getText(R.string.offset_top) + " " + mOffsetTop.getProgress() + "%");
        mOffsetTop.setOnPreferenceChangeListener(this);

        mExpandedView = (CheckBoxPreference) findPreference(KEY_EXPANDED_VIEW);
        mExpandedView.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW, 1) == 1);

        mForceExpandedView = (CheckBoxPreference) findPreference(KEY_FORCE_EXPANDED_VIEW);
        mForceExpandedView.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW, 0) == 1);

        mNotificationsHeight = (NumberPickerPreference) findPreference(KEY_NOTIFICATIONS_HEIGHT);
        mNotificationsHeight.setValue(Settings.System.getInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, 4));

        Point displaySize = new Point();
        ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
        int max = Math.round((float)displaySize.y * (1f - (mOffsetTop.getProgress() / 100f)) /
                (float)mContext.getResources().getDimensionPixelSize(R.dimen.notification_row_min_height));
        mNotificationsHeight.setMinValue(1);
        mNotificationsHeight.setMaxValue(max);
        mNotificationsHeight.setOnPreferenceChangeListener(this);

        mNotificationColor = (ColorPickerPreference) prefSet.findPreference(KEY_NOTIFICATION_COLOR);
        mNotificationColor.setAlphaSliderEnabled(true);
        int color = Settings.System.getInt(getContentResolver(),
        Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, 0x55555555);
        String hexColor = String.format("#%08x", (0xffffffff & color));
        mNotificationColor.setSummary(hexColor);
        mNotificationColor.setDefaultValue(color);
        mNotificationColor.setNewPreviewColor(color);
        mNotificationColor.setOnPreferenceChangeListener(this);

        if (AmonAmarth) {
            mWakeOnNotification.setEnabled(false);
            mWakeOnNotification.setSummary(R.string.wake_on_notification_disable);
        } else {
            mWakeOnNotification.setEnabled(true);
            mWakeOnNotification.setSummary(R.string.wake_on_notification_summary);
        }

        mSunlightModePref = (CheckBoxPreference) findPreference(KEY_SUNLIGHT_MODE);
        mSunlightModePref.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.ACTIVE_DISPLAY_SUNLIGHT_MODE, 0) == 1));
        if (!hasLightSensor()) {
            getPreferenceScreen().removePreference(mSunlightModePref);
        }

        mTurnOffModePref = (CheckBoxPreference) findPreference(KEY_TURNOFF_MODE);
        mTurnOffModePref.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.ACTIVE_DISPLAY_TURNOFF_MODE, 0) == 1));
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mRedisplayPref) {
            int timeout = Integer.valueOf((String) newValue);
            updateRedisplaySummary(timeout);
            return true;
        } else if (preference == mBrightnessLevel) {
            int brightness = ((Integer)newValue).intValue();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ACTIVE_DISPLAY_BRIGHTNESS, brightness);
            return true;
        } else if (preference == mNotificationsHeight) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, (Integer)newValue);
            return true;
        } else if (preference == mDisplayTimeout) {
            long timeout = Integer.valueOf((String) newValue);
            updateTimeoutSummary(timeout);
            return true;
        } else if (preference == mProximityThreshold) {
            long threshold = Integer.valueOf((String) newValue);
            updateThresholdSummary(threshold);
            return true;
        } else if (preference == mNotificationColor) {
            String hex = ColorPickerPreference.convertToARGB(
            Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
            Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, intHex);
            return true;
        } else if (preference == mOffsetTop) {
            Settings.System.putFloat(getContentResolver(), Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP,
                    (Integer)newValue / 100f);
            mOffsetTop.setTitle(getResources().getText(R.string.offset_top) + " " + (Integer)newValue + "%");
            Point displaySize = new Point();
            ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
            int max = Math.round((float)displaySize.y * (1f - (mOffsetTop.getProgress() / 100f)) /
                    (float)mContext.getResources().getDimensionPixelSize(R.dimen.notification_row_min_height));
            mNotificationsHeight.setMaxValue(max);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mShowTextPref) {
            value = mShowTextPref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ACTIVE_DISPLAY_TEXT,
                    value ? 1 : 0);
        } else if (preference == mExpandedView) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW,
                    mExpandedView.isChecked() ? 1 : 0);
            mForceExpandedView.setEnabled(mExpandedView.isChecked());
        } else if (preference == mForceExpandedView) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW,
                    mForceExpandedView.isChecked() ? 1 : 0);
        } else if (preference == mWakeOnNotification) {
            Settings.System.putInt(getContentResolver(), Settings.System.LOCKSCREEN_NOTIFICATIONS_WAKE_ON_NOTIFICATION,
                    mWakeOnNotification.isChecked() ? 1 : 0);
        } else if (preference == mShowDatePref) {
            value = mShowDatePref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ACTIVE_DISPLAY_SHOW_DATE,
                    value ? 1 : 0);
        } else if (preference == mSunlightModePref) {
            value = mSunlightModePref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ACTIVE_DISPLAY_SUNLIGHT_MODE,
                    value ? 1 : 0);
        } else if (preference == mTurnOffModePref) {
            value = mTurnOffModePref.isChecked();
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ACTIVE_DISPLAY_TURNOFF_MODE,
                    value ? 1 : 0);
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        return true;
    }

    private void updateRedisplaySummary(long value) {
        mRedisplayPref.setSummary(mRedisplayPref.getEntries()[mRedisplayPref.findIndexOfValue("" + value)]);
        Settings.System.putLong(getContentResolver(),
                Settings.System.ACTIVE_DISPLAY_REDISPLAY, value);
    }

    private void updateTimeoutSummary(long value) {
        try {
            mDisplayTimeout.setSummary(mDisplayTimeout.getEntries()[mDisplayTimeout.findIndexOfValue("" + value)]);
            Settings.System.putLong(getContentResolver(),
                    Settings.System.ACTIVE_DISPLAY_TIMEOUT, value);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }

    private boolean hasLightSensor() {
        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        return sm.getDefaultSensor(TYPE_LIGHT) != null;
    }

    private void updateThresholdSummary(long value) {
        try {
            mProximityThreshold.setSummary(mProximityThreshold.getEntries()[mProximityThreshold.findIndexOfValue("" + value)]);
            Settings.System.putLong(getContentResolver(),
                    Settings.System.ACTIVE_DISPLAY_THRESHOLD, value);
        } catch (ArrayIndexOutOfBoundsException e) {
        }
    }
}

