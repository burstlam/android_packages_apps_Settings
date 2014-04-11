
package com.android.settings.slim;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Spannable;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.util.CMDProcessor;
import com.android.settings.util.Helpers;


public class UserInterface extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    public static final String TAG = "UserInterface";

    private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
    private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";
    private static final String RECENTS_STYLE = "recents_style";
    private static final String RECENT_MENU_CLEAR_ALL = "recent_menu_clear_all";
    private static final String RECENT_MENU_CLEAR_ALL_LOCATION = "recent_menu_clear_all_location";
    private static final String KEY_RECENTS_RAM_BAR = "recents_ram_bar";
    private static final String PREF_USE_ALT_RESOLVER = "use_alt_resolver";
    private static final String KEY_REVERSE_DEFAULT_APP_PICKER = "reverse_default_app_picker";
    private static final String RECENT_PANEL_LEFTY_MODE = "recent_panel_lefty_mode";
    private static final String RECENT_PANEL_SCALE = "recent_panel_scale";
    private static final String RECENT_PANEL_EXPANDED_MODE = "recent_panel_expanded_mode";

    private ListPreference mListViewAnimation;
    private ListPreference mListViewInterpolator;
    private ListPreference mRecentStyle;
    private CheckBoxPreference mRecentClearAll;
    private ListPreference mRecentClearAllPosition;
    private CheckBoxPreference mUseAltResolver;
    private CheckBoxPreference mReverseDefaultAppPicker;
    private CheckBoxPreference mRecentPanelLeftyMode;
    private ListPreference mRecentPanelScale;
    private ListPreference mRecentPanelExpandedMode;

    private Preference mRamBar;
    Preference mLcdDensity;

    int newDensityValue;

    DensityChanger densityFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.user_interface_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mLcdDensity = findPreference("lcd_density_setup");
        String currentProperty = SystemProperties.get("ro.sf.lcd_density");
        try {
            newDensityValue = Integer.parseInt(currentProperty);
        } catch (Exception e) {
            getPreferenceScreen().removePreference(mLcdDensity);
        }

        mLcdDensity.setSummary(getResources().getString(R.string.current_lcd_density) + currentProperty);

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

        mRecentStyle = (ListPreference) prefSet.findPreference(RECENTS_STYLE);
        mRecentStyle.setOnPreferenceChangeListener(this);
        mRecentStyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.RECENTS_STYLE,
                0)));
        mRecentStyle.setSummary(mRecentStyle.getEntry());

        mRecentClearAll = (CheckBoxPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL);
        mRecentClearAll.setChecked(Settings.System.getInt(resolver,
            Settings.System.SHOW_CLEAR_RECENTS_BUTTON, 0) == 1);
        mRecentClearAll.setOnPreferenceChangeListener(this);

        mRecentClearAllPosition = (ListPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL_LOCATION);
        String recentClearAllPosition = Settings.System.getString(resolver, Settings.System.CLEAR_RECENTS_BUTTON_LOCATION);
        if (recentClearAllPosition != null) {
             mRecentClearAllPosition.setValue(recentClearAllPosition);
        }
        mRecentClearAllPosition.setOnPreferenceChangeListener(this);

        mRecentPanelLeftyMode = (CheckBoxPreference) findPreference(RECENT_PANEL_LEFTY_MODE);
        mRecentPanelLeftyMode.setOnPreferenceChangeListener(this);

        mRecentPanelScale = (ListPreference) findPreference(RECENT_PANEL_SCALE);
        mRecentPanelScale.setOnPreferenceChangeListener(this);

        mRecentPanelExpandedMode = (ListPreference) findPreference(RECENT_PANEL_EXPANDED_MODE);
        mRecentPanelExpandedMode.setOnPreferenceChangeListener(this);
        final int recentExpandedMode = Settings.System.getInt(getContentResolver(),
        Settings.System.RECENT_PANEL_EXPANDED_MODE, 0);
        mRecentPanelExpandedMode.setValue(recentExpandedMode + "");

        mUseAltResolver = (CheckBoxPreference) findPreference(PREF_USE_ALT_RESOLVER);
        mUseAltResolver.setOnPreferenceChangeListener(this);
        mUseAltResolver.setChecked(Settings.System.getInt(resolver,
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, 0) == 1);

        mReverseDefaultAppPicker = (CheckBoxPreference) findPreference(KEY_REVERSE_DEFAULT_APP_PICKER);
        mReverseDefaultAppPicker.setOnPreferenceChangeListener(this);
        mReverseDefaultAppPicker.setChecked(Settings.System.getInt(resolver,
                Settings.System.REVERSE_DEFAULT_APP_PICKER, 0) == 1);
    }

    private void updateRamBar() {
        int ramBarMode = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.RECENTS_RAM_BAR_MODE, 0);
        if (ramBarMode != 0)
            mRamBar.setSummary(getResources().getString(R.string.ram_bar_color_enabled));
        else
            mRamBar.setSummary(getResources().getString(R.string.ram_bar_color_disabled));
    }

    private void updatePreference() {
        int altResolver = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, 0);
        int reverse = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.REVERSE_DEFAULT_APP_PICKER, 0);
        int recentStyle = Settings.System.getInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.RECENTS_STYLE, 0);

        if (altResolver == 1)  {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, 0);
            mReverseDefaultAppPicker.setEnabled(false);
        } else {
            mReverseDefaultAppPicker.setEnabled(true);
        }
        if (reverse == 1) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                Settings.System.ACTIVITY_RESOLVER_USE_ALT, 0);
            mUseAltResolver.setEnabled(false);
        } else {
            mUseAltResolver.setEnabled(true);
        }

        if (recentStyle == 0) {
            mRecentPanelLeftyMode.setEnabled(false);
            mRecentPanelScale.setEnabled(false);
        } else {
            mRecentPanelLeftyMode.setEnabled(true);
            mRecentPanelScale.setEnabled(true);
        }
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

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference preference) {
        if (preference == mLcdDensity) {
            ((PreferenceActivity) getActivity())
            .startPreferenceFragment(new DensityChanger(), true);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mListViewAnimation) {
            String value = (String) newValue;
            Settings.System.putString(resolver, Settings.System.LISTVIEW_ANIMATION, value);
        } else if (preference == mListViewInterpolator) {
            String value = (String) newValue;
            Settings.System.putString(resolver, Settings.System.LISTVIEW_INTERPOLATOR, value);
        } else if (preference == mRecentStyle) {
            int recentStyle = Integer.valueOf((String) newValue);
            int index = mRecentStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.RECENTS_STYLE, recentStyle);
            mRecentStyle.setSummary(mRecentStyle.getEntries()[index]);
            updatePreference();
            Helpers.restartSystemUI();
        } else if (preference == mRecentClearAll) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.SHOW_CLEAR_RECENTS_BUTTON, value ? 1 : 0);
        } else if (preference == mRecentClearAllPosition) {
            String value = (String) newValue;
            Settings.System.putString(resolver, Settings.System.CLEAR_RECENTS_BUTTON_LOCATION, value);
        } else if (preference == mUseAltResolver) {
            boolean value = (Boolean) newValue;
			Settings.System.putInt(resolver, Settings.System.ACTIVITY_RESOLVER_USE_ALT, value ? 1 : 0);
            updatePreference();
        } else if (preference == mReverseDefaultAppPicker) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.REVERSE_DEFAULT_APP_PICKER, value ? 1 : 0);
            updatePreference();
        } else if (preference == mRecentPanelScale) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_PANEL_SCALE_FACTOR, value);
            return true;
        } else if (preference == mRecentPanelLeftyMode) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.RECENT_PANEL_GRAVITY,
                    ((Boolean) newValue) ? Gravity.LEFT : Gravity.RIGHT);
            return true;
        } else if (preference == mRecentPanelExpandedMode) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getContentResolver(),
            Settings.System.RECENT_PANEL_EXPANDED_MODE, value);
            return true;
        }
        return true;
    }
}
