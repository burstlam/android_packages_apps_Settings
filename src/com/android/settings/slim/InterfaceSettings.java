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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

import android.text.Editable;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.List;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class InterfaceSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "InterfaceSettings";
    private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
    private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";
    private static final String RECENT_MENU_CLEAR_ALL = "recent_menu_clear_all";
    private static final String RECENT_MENU_CLEAR_ALL_LOCATION = "recent_menu_clear_all_location";
    private static final String KEY_RECENTS_RAM_BAR = "recents_ram_bar";
    private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";

    private static final String KEY_LCD_DENSITY = "lcd_density";
    private static final int DIALOG_CUSTOM_DENSITY = 101;
    private static final String DENSITY_PROP = "persist.sys.lcd_density";

    private static ListPreference mLcdDensity;
    private static Activity mActivity;

    private ListPreference mListViewAnimation;
    private ListPreference mListViewInterpolator;
    private CheckBoxPreference mRecentClearAll;
    private ListPreference mRecentClearAllPosition;
    private CheckBoxPreference mUseAltResolver;

    private Preference mRamBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = getActivity();
        updateSettings();
    }

    private void updateSettings() {
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.slim_interface_settings);

        //ListView Animations

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mRamBar = findPreference(KEY_RECENTS_RAM_BAR);
        updateRamBar();

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

        mRecentClearAll = (CheckBoxPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL);
        mRecentClearAll.setChecked(Settings.System.getInt(resolver,
            Settings.System.SHOW_CLEAR_RECENTS_BUTTON, 1) == 1);
        mRecentClearAll.setOnPreferenceChangeListener(this);
        mRecentClearAllPosition = (ListPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL_LOCATION);
        String recentClearAllPosition = Settings.System.getString(resolver, Settings.System.CLEAR_RECENTS_BUTTON_LOCATION);
        if (recentClearAllPosition != null) {
             mRecentClearAllPosition.setValue(recentClearAllPosition);
        }
        mRecentClearAllPosition.setOnPreferenceChangeListener(this);

        mUseAltResolver = (CheckBoxPreference) findPreference(PREF_USE_ALT_RESOLVER);
        mUseAltResolver.setChecked(Settings.System.getInt(resolver,
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, 0) == 1);

        mLcdDensity = (ListPreference) findPreference(KEY_LCD_DENSITY);
        String current = SystemProperties.get(DENSITY_PROP,
                SystemProperties.get("ro.sf.lcd_density"));
        final ArrayList<String> array = new ArrayList<String>(
                Arrays.asList(getResources().getStringArray(R.array.lcd_density_entries)));
        if (array.contains(current)) {
            mLcdDensity.setValue(current);
        } else {
            mLcdDensity.setValue("custom");
        }
        mLcdDensity.setSummary(getResources().getString(R.string.current_lcd_density) + current);
        mLcdDensity.setOnPreferenceChangeListener(this);
    }

    private void updateRamBar() {
        int ramBarMode = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.RECENTS_RAM_BAR_MODE, 0);
        if (ramBarMode != 0)
            mRamBar.setSummary(getResources().getString(R.string.ram_bar_color_enabled));
        else
            mRamBar.setSummary(getResources().getString(R.string.ram_bar_color_disabled));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateRamBar();
    }

    @Override
    public void onPause() {
        super.onResume();
        updateRamBar();
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mListViewAnimation) {
            String value = (String) newValue;
            Settings.System.putString(resolver, Settings.System.LISTVIEW_ANIMATION, value);
        } else if (preference == mListViewInterpolator) {
            String value = (String) newValue;
            Settings.System.putString(resolver, Settings.System.LISTVIEW_INTERPOLATOR, value);
        } else if (preference == mRecentClearAll) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.SHOW_CLEAR_RECENTS_BUTTON, value ? 1 : 0);
        } else if (preference == mRecentClearAllPosition) {
            String value = (String) newValue;
            Settings.System.putString(resolver, Settings.System.CLEAR_RECENTS_BUTTON_LOCATION, value);
        } else if (preference == mUseAltResolver) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.ACTIVITY_RESOLVER_USE_ALT, value ? 1 : 0);
        } else if (preference == mLcdDensity) {
            String density = (String) newValue;
            if (SystemProperties.get(DENSITY_PROP) != density) {
                if ((density).equals(getResources().getString(R.string.custom_density))) {
                    showDialogInner(DIALOG_CUSTOM_DENSITY);
                } else {
                    setDensity(Integer.parseInt(density));
                }
            }
        } else {
            return false;
        }
        return true;
    }

    private static void setDensity(int density) {
        int max = mActivity.getResources().getInteger(R.integer.lcd_density_max);
        int min = mActivity.getResources().getInteger(R.integer.lcd_density_min);
        int navbarHeight = Settings.System.getIntForUser(mActivity.getContentResolver(),
                Settings.System.NAVIGATION_BAR_HEIGHT, mActivity.getResources()
                .getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height),
                UserHandle.USER_CURRENT);
        if (density < min && density > max) {
            mLcdDensity.setSummary(mActivity.getResources().getString(
                                            R.string.custom_density_summary_invalid));
        }
        SystemProperties.set(DENSITY_PROP, Integer.toString(density));
        Settings.System.putInt(mActivity.getContentResolver(),
                Settings.System.LCD_DENSITY, density);

        killCurrentLauncher();
        Configuration mConfiguration = new Configuration();
        mConfiguration.setToDefaults();
        try {
            ActivityManagerNative.getDefault().updateConfiguration(mConfiguration);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failure communicating with activity manager", e);
        }
        mActivity.recreate();
        try {
            Thread.sleep(2000);
        } catch (Exception e){}
        Settings.System.putInt(mActivity.getContentResolver(),
                Settings.System.NAVIGATION_BAR_HEIGHT, navbarHeight);
    }

    private static void killCurrentLauncher() {
        ComponentName defaultLauncher = mActivity.getPackageManager().getHomeActivities(
                        new ArrayList<ResolveInfo>());
                ActivityManager am = (ActivityManager) mActivity.getSystemService(
                        Context.ACTIVITY_SERVICE);
                am.killBackgroundProcesses(defaultLauncher.getPackageName());
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        InterfaceSettings getOwner() {
            return (InterfaceSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater factory = LayoutInflater.from(getActivity());
            int id = getArguments().getInt("id");
            switch (id) {
                case DIALOG_CUSTOM_DENSITY:
                    final View textEntryView = factory.inflate(
                            R.layout.alert_dialog_text_entry, null);
                    return new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.set_custom_density_title))
                            .setView(textEntryView)
                            .setPositiveButton(getResources().getString(
                                    R.string.set_custom_density_set),
                                    new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    EditText dpi = (EditText)
                                            textEntryView.findViewById(R.id.dpi_edit);
                                    Editable text = dpi.getText();
                                    dialog.dismiss();
                                    setDensity(Integer.parseInt(text.toString()));

                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
        }
    }
}
