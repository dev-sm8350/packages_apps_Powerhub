package com.cherish.settings.fragments;

import com.android.internal.logging.nano.MetricsProto;

import static android.os.UserHandle.USER_SYSTEM;
import android.app.UiModeManager;
import android.graphics.Color;
import android.os.Bundle;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.os.SystemProperties;
import android.os.ServiceManager;
import android.content.om.IOverlayManager;
import android.os.RemoteException;
import android.content.ContentResolver;
import android.content.res.Resources;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.PreferenceManager;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.SwitchPreference;
import com.cherish.settings.preferences.CustomSeekBarPreference;
import android.provider.Settings;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Locale;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

import com.android.internal.util.cherish.ThemesUtils;
import com.android.internal.util.cherish.CherishUtils;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class ThemeSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {
			
			private static final String PREF_THEME_SWITCH = "theme_switch";
    private static final String SYSUI_ROUNDED_SIZE = "sysui_rounded_size";
    private static final String SYSUI_ROUNDED_CONTENT_PADDING = "sysui_rounded_content_padding";
    private static final String SYSUI_STATUS_BAR_PADDING = "sysui_status_bar_padding";
    private static final String SYSUI_ROUNDED_FWVALS = "sysui_rounded_fwvals";
    private static final String SWITCH_STYLE = "switch_style";
    private static final String QS_TILE_STYLE = "qs_tile_style";
    private static final String QS_HEADER_STYLE = "qs_header_style";
    private static final String BRIGHTNESS_SLIDER_STYLE = "brightness_slider_style";

    private static final String UI_STYLE = "ui_style";

    private UiModeManager mUiModeManager;
    private IOverlayManager mOverlayService;
    private ListPreference mThemeSwitch;
    private CustomSeekBarPreference mCornerRadius;
    private CustomSeekBarPreference mContentPadding;
    private CustomSeekBarPreference mSBPadding;
    private SwitchPreference mRoundedFwvals;
    private ListPreference mSwitchStyle;
    private ListPreference mQsTileStyle;
    private ListPreference mQsHeaderStyle;
    private ListPreference mBrightnessSliderStyle;
    private ListPreference mUIStyle;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.cherish_settings_theme);

        PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        Resources res = null;
        Context ctx = getContext();
        float density = Resources.getSystem().getDisplayMetrics().density;

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        mUIStyle = (ListPreference) findPreference(UI_STYLE);
        int UIStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.UI_STYLE, 0);
        int UIStyleValue = getOverlayPosition(ThemesUtils.UI_THEMES);
        if (UIStyleValue != 0) {
            mUIStyle.setValue(String.valueOf(UIStyle));
        }
        mUIStyle.setSummary(mUIStyle.getEntry());
        mUIStyle.setOnPreferenceChangeListener(this);

        mBrightnessSliderStyle = (ListPreference) findPreference(BRIGHTNESS_SLIDER_STYLE);
        int BrightnessSliderStyle = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.BRIGHTNESS_SLIDER_STYLE, 0);
        int BrightnessSliderStyleValue = getOverlayPosition(ThemesUtils.BRIGHTNESS_SLIDER_THEMES);
        if (BrightnessSliderStyleValue != 0) {
            mBrightnessSliderStyle.setValue(String.valueOf(BrightnessSliderStyle));
        }
        mBrightnessSliderStyle.setSummary(mBrightnessSliderStyle.getEntry());
        mBrightnessSliderStyle.setOnPreferenceChangeListener(this);

        // Rounded Corner Radius
        mCornerRadius = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_SIZE);
        int resourceIdRadius = (int) ctx.getResources().getDimension(com.android.internal.R.dimen.rounded_corner_radius);
        int cornerRadius = Settings.Secure.getIntForUser(ctx.getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                ((int) (resourceIdRadius / density)), UserHandle.USER_CURRENT);
        mCornerRadius.setValue(cornerRadius);
        mCornerRadius.setOnPreferenceChangeListener(this);

        // Rounded Content Padding
        mContentPadding = (CustomSeekBarPreference) findPreference(SYSUI_ROUNDED_CONTENT_PADDING);
        int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null,
                null);
        int contentPadding = Settings.Secure.getIntForUser(ctx.getContentResolver(),
                Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
                (int) (res.getDimension(resourceIdPadding) / density), UserHandle.USER_CURRENT);
        mContentPadding.setValue(contentPadding);
        mContentPadding.setOnPreferenceChangeListener(this);

        // Status Bar Content Padding
        mSBPadding = (CustomSeekBarPreference) findPreference(SYSUI_STATUS_BAR_PADDING);
        int resourceIdSBPadding = res.getIdentifier("com.android.systemui:dimen/status_bar_extra_padding", null,
                null);
        int sbPadding = Settings.Secure.getIntForUser(ctx.getContentResolver(),
                Settings.Secure.SYSUI_STATUS_BAR_PADDING,
                (int) (res.getDimension(resourceIdSBPadding) / density), UserHandle.USER_CURRENT);
        mSBPadding.setValue(sbPadding);
        mSBPadding.setOnPreferenceChangeListener(this);

        // Rounded use Framework Values
        mRoundedFwvals = (SwitchPreference) findPreference(SYSUI_ROUNDED_FWVALS);
        mRoundedFwvals.setOnPreferenceChangeListener(this);

        mSwitchStyle = (ListPreference) findPreference(SWITCH_STYLE);
        int switchStyle = Settings.System.getInt(resolver,
                Settings.System.SWITCH_STYLE, 1);
        int switchValueIndex = mSwitchStyle.findIndexOfValue(String.valueOf(switchStyle));
        mSwitchStyle.setValueIndex(switchValueIndex >= 0 ? switchValueIndex : 0);
        mSwitchStyle.setSummary(mSwitchStyle.getEntry());
        mSwitchStyle.setOnPreferenceChangeListener(this);

        mQsTileStyle = (ListPreference) findPreference(QS_TILE_STYLE);
        int qsTileStyle = Settings.System.getIntForUser(resolver,
                Settings.System.QS_TILE_STYLE, 0, UserHandle.USER_CURRENT);
        int valueIndex = mQsTileStyle.findIndexOfValue(String.valueOf(qsTileStyle));
        mQsTileStyle.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
        mQsTileStyle.setSummary(mQsTileStyle.getEntry());
        mQsTileStyle.setOnPreferenceChangeListener(this);

        mQsHeaderStyle = (ListPreference)findPreference(QS_HEADER_STYLE);
        int qsHeaderStyle = Settings.System.getInt(resolver,
                Settings.System.QS_HEADER_STYLE, 0);
        int qsvalueIndex = mQsHeaderStyle.findIndexOfValue(String.valueOf(qsHeaderStyle));
        mQsHeaderStyle.setValueIndex(qsvalueIndex >= 0 ? qsvalueIndex : 0);
        mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntry());
        mQsHeaderStyle.setOnPreferenceChangeListener(this);


		mOverlayService = IOverlayManager.Stub
                .asInterface(ServiceManager.getService(Context.OVERLAY_SERVICE));
		mUiModeManager = getContext().getSystemService(UiModeManager.class);
		setupThemeSwitchPref();
        }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
           ContentResolver resolver = getActivity().getContentResolver();
           if (preference == mThemeSwitch) {
            String theme_switch = (String) objValue;
            final Context context = getContext();
            switch (theme_switch) {
                case "1":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.CHOCO_X);
					handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_NO, ThemesUtils.CLEAR_SPRING);
                    break;
                case "2":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "3":
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "4":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "5":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "6":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "7":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "8":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
                case "9":
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.SOLARIZED_DARK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.BAKED_GREEN);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CHOCO_X);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.PITCH_BLACK);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.DARK_GREY);
                    handleBackgrounds(false, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.MATERIAL_OCEAN);
                    handleBackgrounds(true, context, UiModeManager.MODE_NIGHT_YES, ThemesUtils.CLEAR_SPRING);
                    break;
            }
            try {
                 mOverlayService.reloadAndroidAssets(UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                 mOverlayService.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
             } catch (RemoteException ignored) {
             }
        } else if (preference == mCornerRadius) {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_ROUNDED_SIZE,
                    (int) objValue, UserHandle.USER_CURRENT);
        } else if (preference == mContentPadding) {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_ROUNDED_CONTENT_PADDING,
                    (int) objValue, UserHandle.USER_CURRENT);
        } else if (preference == mSBPadding) {
            Settings.Secure.putIntForUser(getContext().getContentResolver(), Settings.Secure.SYSUI_STATUS_BAR_PADDING,
                    (int) objValue, UserHandle.USER_CURRENT);
        } else if (preference == mRoundedFwvals) {
            restoreCorners();
        } else if (preference == mSwitchStyle) {
            String value = (String) objValue;
            Settings.System.putInt(resolver, Settings.System.SWITCH_STYLE, Integer.valueOf(value));
            int valueIndex = mSwitchStyle.findIndexOfValue(value);
            mSwitchStyle.setSummary(mSwitchStyle.getEntries()[valueIndex]);
         } else if (preference == mQsTileStyle) {
            int qsTileStyleValue = Integer.valueOf((String) objValue);
            Settings.System.putIntForUser(resolver,
                    Settings.System.QS_TILE_STYLE, qsTileStyleValue, UserHandle.USER_CURRENT);
            mQsTileStyle.setSummary(mQsTileStyle.getEntries()[qsTileStyleValue]);
        } else if (preference == mQsHeaderStyle) {
            String value = (String) objValue;
            Settings.System.putInt(resolver,
			    Settings.System.QS_HEADER_STYLE, Integer.valueOf(value));
            int newIndex = mQsHeaderStyle.findIndexOfValue(value);
            mQsHeaderStyle.setSummary(mQsHeaderStyle.getEntries()[newIndex]);
        } else if (preference == mBrightnessSliderStyle) {
                    String value = (String) objValue;
                    Settings.System.putInt(resolver, Settings.System.BRIGHTNESS_SLIDER_STYLE, Integer.valueOf(value));
                    int valueIndex = mBrightnessSliderStyle.findIndexOfValue(value);
                    mBrightnessSliderStyle.setSummary(mBrightnessSliderStyle.getEntries()[valueIndex]);
                    String overlayName = getOverlayName(ThemesUtils.BRIGHTNESS_SLIDER_THEMES);
                    if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayService);
                    }
                    if (valueIndex > 0) {
                        handleOverlays(ThemesUtils.BRIGHTNESS_SLIDER_THEMES[valueIndex],
                                true, mOverlayService);
                    }
        }
        } else if (preference == mUIStyle) {
                    String value = (String) newValue;
                    Settings.System.putInt(getActivity().getContentResolver(), Settings.System.UI_STYLE, Integer.valueOf(value));
                    int valueIndex = mUIStyle.findIndexOfValue(value);
                    mUIStyle.setSummary(mUIStyle.getEntries()[valueIndex]);
                    String overlayName = getOverlayName(ThemesUtils.UI_THEMES);
                    if (overlayName != null) {
                    handleOverlays(overlayName, false, mOverlayService);
                    }
                    if (valueIndex > 0) {
                        handleOverlays(ThemesUtils.UI_THEMES[valueIndex],
                                true, mOverlayService);
                    }
                }				
        return true;
    }
	
	private void setupThemeSwitchPref() {
        mThemeSwitch = (ListPreference) findPreference(PREF_THEME_SWITCH);
        mThemeSwitch.setOnPreferenceChangeListener(this);
        if (CherishUtils.isThemeEnabled("com.android.theme.clearspring.system")) {
            mThemeSwitch.setValue("9");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.darkgrey.system")) {
            mThemeSwitch.setValue("7");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.pitchblack.system")) {
            mThemeSwitch.setValue("6");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.materialocean.system")) {
            mThemeSwitch.setValue("8");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.chocox.system")) {
            mThemeSwitch.setValue("5");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.bakedgreen.system")) {
            mThemeSwitch.setValue("4");
        } else if (CherishUtils.isThemeEnabled("com.android.theme.solarizeddark.system")) {
            mThemeSwitch.setValue("3");
        } else if (mUiModeManager.getNightMode() == UiModeManager.MODE_NIGHT_YES) {
            mThemeSwitch.setValue("2");
        } else {
            mThemeSwitch.setValue("1");
        }
    }

    private void handleBackgrounds(Boolean state, Context context, int mode, String[] overlays) {
        if (context != null) {
            Objects.requireNonNull(context.getSystemService(UiModeManager.class))
                    .setNightMode(mode);
        }
        for (int i = 0; i < overlays.length; i++) {
            String background = overlays[i];
            try {
                mOverlayService.setEnabled(background, state, USER_SYSTEM);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void restoreCorners() {
        Resources res = null;
        float density = Resources.getSystem().getDisplayMetrics().density;
        Context ctx = getContext();

        try {
            res = ctx.getPackageManager().getResourcesForApplication("com.android.systemui");
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        int resourceIdRadius = (int) ctx.getResources().getDimension(com.android.internal.R.dimen.rounded_corner_radius);
        int resourceIdPadding = res.getIdentifier("com.android.systemui:dimen/rounded_corner_content_padding", null,
                null);
        int resourceIdSBPadding = res.getIdentifier("com.android.systemui:dimen/status_bar_extra_padding", null,
                null);
        mCornerRadius.setValue((int) (resourceIdRadius / density));
        mContentPadding.setValue((int) (res.getDimension(resourceIdPadding) / density));
        mSBPadding.setValue((int) (res.getDimension(resourceIdSBPadding) / density));
    }
	
	private String getOverlayName(String[] overlays) {
            String overlayName = null;
            for (int i = 0; i < overlays.length; i++) {
                String overlay = overlays[i];
                if (CherishUtils.isThemeEnabled(overlay)) {
                    overlayName = overlay;
                }
            }
            return overlayName;
        }

    private int getOverlayPosition(String[] overlays) {
            int position = -1;
            for (int i = 0; i < overlays.length; i++) {
                String overlay = overlays[i];
                if (CherishUtils.isThemeEnabled(overlay)) {
                    position = i;
                }
            }
            return position;
        }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CHERISH_SETTINGS;
    }

}