package com.android.settings.pa;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.vanir.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PAPieControl2 extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String PIE2_STATE = "pie2_state";
    private static final String PIE2_GRAVITY = "pie2_gravity";
    private static final String PIE2_MODE = "pie2_mode";

    private ListPreference mPie2Mode;
    private ListPreference mPie2Gravity;
    private SwitchPreference mPie2State;

    private Context mContext;

    protected Handler mHandler;
    private SettingsObserver mSettingsObserver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pie2_control);
        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity().getApplicationContext();
        ContentResolver resolver = mContext.getContentResolver();

        mSettingsObserver = new SettingsObserver(new Handler());

        mPie2State = (SwitchPreference) findPreference(PIE2_STATE);
        mPie2State.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE2_STATE, 0) == 1);
        mPie2State.setOnPreferenceChangeListener(this); 

        mPie2Gravity = (ListPreference) prefSet.findPreference(PIE2_GRAVITY);
        int pieGravity = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE2_GRAVITY, 0);
        mPie2Gravity.setValue(String.valueOf(pieGravity));
        mPie2Gravity.setSummary(mPie2Gravity.getEntry());
        mPie2Gravity.setOnPreferenceChangeListener(this);

        mPie2Mode = (ListPreference) prefSet.findPreference(PIE2_MODE);
        int pieMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE2_MODE, 0);
        mPie2Mode.setValue(String.valueOf(pieMode));
        mPie2Mode.setSummary(mPie2Mode.getEntry());
        mPie2Mode.setOnPreferenceChangeListener(this);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPie2State) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE2_STATE,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mPie2Mode) {
            int pieMode = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE2_MODE, pieMode);
            return true;
        } else if (preference == mPie2Gravity) {
            int pieGravity = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE2_GRAVITY, pieGravity);
            return true;
        }
        return false;
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
            observe();
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.PIE2_STATE), false,
                    this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.PIE2_GRAVITY), false,
                    this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.NAVIGATION_BAR_SHOW), false,
                    this);
        }

        @Override
        public void onChange(boolean selfChange) {
            update();
            Helpers.restartSystemUI();
        }

        void update() {
            ContentResolver resolver = mContext.getContentResolver();
            boolean hasNavBarByDefault = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);

            boolean pieOn = Settings.System.getBoolean(resolver, 
                Settings.System.PIE2_STATE, true);
            int navbarOn = Settings.System.getInt(resolver,
                Settings.System.NAVIGATION_BAR_SHOW, 1);
            int pieGravity = Settings.System.getInt(resolver,
                Settings.System.PIE_GRAVITY, 0);

            if (hasNavBarByDefault && navbarOn == 1) {
                if (pieOn && pieGravity == 0) {
                    Settings.System.putInt(resolver,
                        Settings.System.NAVIGATION_BAR_SHOW, 0);
                }
            }
            
            if (hasNavBarByDefault && !pieOn) {
                Settings.System.putInt(resolver,
                    Settings.System.NAVIGATION_BAR_SHOW, 1);
            }
        }
    }
}
