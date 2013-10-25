package com.android.settings.paranoid;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.os.Bundle;
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
import com.android.settings.util.CMDProcessor;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PAPieControl extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String PA_PIE_CONTROLS = "pa_pie_controls";
    private static final String PA_PIE_GRAVITY = "pa_pie_gravity";
    private static final String PA_PIE_MODE = "pa_pie_mode";
    private static final String PA_PIE_SIZE = "pa_pie_size";
    private static final String PA_PIE_TRIGGER = "pa_pie_trigger";
    private static final String PA_PIE_ANGLE = "pa_pie_angle";
    private static final String PA_PIE_GAP = "pa_pie_gap";
    private static final String PA_PIE_POWER = "pa_pie_power";
    private static final String PA_PIE_MENU = "pa_pie_menu";
    private static final String PA_PIE_SEARCH = "pa_pie_search";
    private static final String PA_PIE_LASTAPP = "pa_pie_lastapp";
    private static final String PA_PIE_KILLTASK = "pa_pie_killtask";
    //private static final String PA_PIE_APPWINDOW = "pa_pie_appwindow";
    private static final String PA_PIE_SCREENSHOT = "pa_pie_screenshot";
    private static final String PA_PIE_ACTNOTIF = "pa_pie_actnotif";
    private static final String PA_PIE_ACTQS = "pa_pie_actqs";
    private static final String PA_PIE_CENTER = "pa_pie_center";
    private static final String PA_PIE_STICK = "pa_pie_stick";
    private static final String PA_PIE_NOTIFICATIONS = "pa_pie_notifications";
    private static final String PA_PIE_RESTART = "pa_pie_restart_launcher";

    private ListPreference mPieMode;
    private ListPreference mPieSize;
    private ListPreference mPieGravity;
    private SwitchPreference mPaPieControls;
    private CheckBoxPreference mPieCenter;
    private CheckBoxPreference mPieStick;
    private ListPreference mPieTrigger;
    private ListPreference mPieAngle;
    private ListPreference mPieGap;
    private CheckBoxPreference mPieMenu;
    private CheckBoxPreference mPiePower;
    private CheckBoxPreference mPieSearch;
    private CheckBoxPreference mPieLastApp;
    private CheckBoxPreference mPieKillTask;
    private CheckBoxPreference mPieActNotif;
    private CheckBoxPreference mPieActQs;
    //private CheckBoxPreference mPieAppWindow;
    private CheckBoxPreference mPieScreenShot;
    private CheckBoxPreference mPieNotifi;
    private CheckBoxPreference mPieRestart;

    private Context mContext;
    private int mAllowedLocations;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pa_pie_controls);
        PreferenceScreen prefSet = getPreferenceScreen();
        mContext = getActivity();

        mPaPieControls = (SwitchPreference) findPreference(PA_PIE_CONTROLS);
        mPaPieControls.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_CONTROLS, 0) == 1);
        mPaPieControls.setOnPreferenceChangeListener(this); 

        mPieCenter = (CheckBoxPreference) prefSet.findPreference(PA_PIE_CENTER);
        mPieCenter.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_CENTER, 1) == 1);

        mPieStick = (CheckBoxPreference) prefSet.findPreference(PA_PIE_STICK);
        mPieStick.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_STICK, 1) == 1);

        mPieGravity = (ListPreference) prefSet.findPreference(PA_PIE_GRAVITY);
        int pieGravity = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_GRAVITY, 2);
        mPieGravity.setValue(String.valueOf(pieGravity));
        mPieGravity.setOnPreferenceChangeListener(this);

        mPieMode = (ListPreference) prefSet.findPreference(PA_PIE_MODE);
        int pieMode = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_MODE, 2);
        mPieMode.setValue(String.valueOf(pieMode));
        mPieMode.setOnPreferenceChangeListener(this);

        mPieSize = (ListPreference) prefSet.findPreference(PA_PIE_SIZE);
        mPieTrigger = (ListPreference) prefSet.findPreference(PA_PIE_TRIGGER);
        try {
            float pieSize = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.PIE_SIZE, 1.2f);
            mPieSize.setValue(String.valueOf(pieSize));
  
            float pieTrigger = Settings.System.getFloat(mContext.getContentResolver(),
                    Settings.System.PIE_TRIGGER);
            mPieTrigger.setValue(String.valueOf(pieTrigger));
        } catch(SettingNotFoundException ex) {
            // So what
        }

        mPieSize.setOnPreferenceChangeListener(this);
        mPieTrigger.setOnPreferenceChangeListener(this);

        mPieGap = (ListPreference) prefSet.findPreference(PA_PIE_GAP);
        int pieGap = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_GAP, 3);
        mPieGap.setValue(String.valueOf(pieGap));
        mPieGap.setOnPreferenceChangeListener(this);

        mPieAngle = (ListPreference) prefSet.findPreference(PA_PIE_ANGLE);
        int pieAngle = Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_ANGLE, 12);
        mPieAngle.setValue(String.valueOf(pieAngle));
        mPieAngle.setOnPreferenceChangeListener(this);

        mPieMenu = (CheckBoxPreference) prefSet.findPreference(PA_PIE_MENU);
        mPieMenu.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_MENU, 1) == 1);

        mPieSearch = (CheckBoxPreference) prefSet.findPreference(PA_PIE_SEARCH);
        mPieSearch.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_SEARCH, 1) == 1);

        mPiePower = (CheckBoxPreference) prefSet.findPreference(PA_PIE_POWER);
        mPiePower.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.PIE_POWER, 0) == 1);

        mPieLastApp = (CheckBoxPreference) prefSet.findPreference(PA_PIE_LASTAPP);
        mPieLastApp.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_LAST_APP, 0) == 1);

        mPieKillTask = (CheckBoxPreference) prefSet.findPreference(PA_PIE_KILLTASK);
        mPieKillTask.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_KILL_TASK, 0) == 1);

        //mPieAppWindow = (CheckBoxPreference) prefSet.findPreference(PA_PIE_APPWINDOW);
        //mPieAppWindow.setChecked(Settings.System.getInt(mContext.getContentResolver(),
        //        Settings.System.PIE_APP_WINDOW, 0) == 1);

        mPieScreenShot = (CheckBoxPreference) prefSet.findPreference(PA_PIE_SCREENSHOT);
        mPieScreenShot.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_SCREENSHOT, 0) == 1);

        mPieActNotif = (CheckBoxPreference) prefSet.findPreference(PA_PIE_ACTNOTIF);
        mPieActNotif.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_ACT_NOTIF, 0) == 1);

        mPieActQs = (CheckBoxPreference) prefSet.findPreference(PA_PIE_ACTQS);
        mPieActQs.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.PIE_ACT_QS, 0) == 1);

        mPieNotifi = (CheckBoxPreference) prefSet.findPreference(PA_PIE_NOTIFICATIONS);
        mPieNotifi.setChecked((Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_NOTIFICATIONS, 0) == 1)); 

        mPieRestart = (CheckBoxPreference) prefSet.findPreference(PA_PIE_RESTART);
        mPieRestart.setChecked(Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.EXPANDED_DESKTOP_RESTART_LAUNCHER, 1) == 1);
        
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mPieMenu) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_MENU, 
                    mPieMenu.isChecked() ? 1 : 0);
        } else if (preference == mPieSearch) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_SEARCH, 
                    mPieSearch.isChecked() ? 1 : 0);
        } else if (preference == mPiePower) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_POWER,
                    mPiePower.isChecked() ? 1 : 0);
        } else if (preference == mPieLastApp) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_LAST_APP,
                    mPieLastApp.isChecked() ? 1 : 0);
        } else if (preference == mPieKillTask) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_KILL_TASK, mPieKillTask.isChecked() ? 1 : 0);
        //} else if (preference == mPieAppWindow) {
        //    Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
        //            Settings.System.PIE_APP_WINDOW, mPieAppWindow.isChecked() ? 1 : 0);
        } else if (preference == mPieScreenShot) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_SCREENSHOT, mPieScreenShot.isChecked() ? 1 : 0);
        } else if (preference == mPieActNotif) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_ACT_NOTIF, mPieActNotif.isChecked() ? 1 : 0);
        } else if (preference == mPieActQs) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_ACT_QS, mPieActQs.isChecked() ? 1 : 0);
        } else if (preference == mPieCenter) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_CENTER, mPieCenter.isChecked() ? 1 : 0);
        } else if (preference == mPieStick) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.PIE_STICK, mPieStick.isChecked() ? 1 : 0);
        } else if (preference == mPieNotifi) {
            Settings.System.putInt(mContext.getContentResolver(),
                    Settings.System.PIE_NOTIFICATIONS, mPieNotifi.isChecked() ? 1 : 0);
            CMDProcessor.restartSystemUI();
        } else if (preference == mPieRestart) {
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.EXPANDED_DESKTOP_RESTART_LAUNCHER, mPieRestart.isChecked() ? 1 : 0);
            CMDProcessor.restartSystemUI();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPaPieControls) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_CONTROLS,
                    (Boolean) newValue ? 1 : 0);
            CMDProcessor.restartSystemUI();
            return true;
        } else if (preference == mPieMode) {
            int pieMode = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_MODE, pieMode);
            return true;
        } else if (preference == mPieSize) {
            float pieSize = Float.valueOf((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_SIZE, pieSize);
            return true;
        } else if (preference == mPieGravity) {
            int pieGravity = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_GRAVITY, pieGravity);
            //CMDProcessor.restartSystemUI();
            return true;
        } else if (preference == mPieAngle) {
            int pieAngle = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_ANGLE, pieAngle);
            return true; 
        } else if (preference == mPieGap) {
            int pieGap = Integer.valueOf((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_GAP, pieGap);
            return true;
        } else if (preference == mPieTrigger) {
            float pierigger = Float.valueOf((String) newValue);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_TRIGGER, pierigger);
            return true;
        }
        return false;
    }
}
