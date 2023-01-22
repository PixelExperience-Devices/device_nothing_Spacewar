/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
 *               2020-2022 Paranoid Android
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

package org.pixelexperience.glyph.Settings;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings;
import android.widget.Switch;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragment;
import androidx.preference.SeekBarPreference;
import androidx.preference.SwitchPreference;

import com.android.settingslib.PrimarySwitchPreference;
import com.android.settingslib.widget.MainSwitchPreference;
import com.android.settingslib.widget.OnMainSwitchChangeListener;

import org.pixelexperience.glyph.R;
import org.pixelexperience.glyph.Constants.Constants;
import org.pixelexperience.glyph.Manager.SettingsManager;
import org.pixelexperience.glyph.Utils.ServiceUtils;

public class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener,
        OnMainSwitchChangeListener {

    private MainSwitchPreference mSwitchBar;

    private SwitchPreference mFlipPreference;
    private SeekBarPreference mBrightnessPreference;
    private PrimarySwitchPreference mNotifsPreference;
    private SwitchPreference mCallPreference;
    private SwitchPreference mChargingLevelPreference;

    private ContentResolver mContentResolver;
    private SettingObserver mSettingObserver;

    private Handler mHandler = new Handler();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.glyph_settings);

        mContentResolver = getActivity().getContentResolver();
        mSettingObserver = new SettingObserver();
        mSettingObserver.register(mContentResolver);

        boolean glyphEnabled = SettingsManager.isGlyphEnabled(getActivity());

        mSwitchBar = (MainSwitchPreference) findPreference(Constants.GLYPH_ENABLE);
        mSwitchBar.addOnSwitchChangeListener(this);
        mSwitchBar.setChecked(glyphEnabled);

        mFlipPreference = (SwitchPreference) findPreference(Constants.GLYPH_FLIP_ENABLE);
        mFlipPreference.setEnabled(glyphEnabled);
        mFlipPreference.setOnPreferenceChangeListener(this);

        mBrightnessPreference = (SeekBarPreference) findPreference(Constants.GLYPH_BRIGHTNESS);
        mBrightnessPreference.setEnabled(glyphEnabled);
        mBrightnessPreference.setMin(0);
        mBrightnessPreference.setMax(4095);
        mBrightnessPreference.setOnPreferenceChangeListener(this);

        mNotifsPreference = (PrimarySwitchPreference) findPreference(Constants.GLYPH_NOTIFS_ENABLE);
        mNotifsPreference.setChecked(SettingsManager.isGlyphNotifsEnabled(getActivity()));
        mNotifsPreference.setEnabled(glyphEnabled);
        mNotifsPreference.setSwitchEnabled(glyphEnabled);
        mNotifsPreference.setOnPreferenceChangeListener(this);

        mCallPreference = (SwitchPreference) findPreference(Constants.GLYPH_CALL_ENABLE);
        mCallPreference.setEnabled(glyphEnabled);
        mCallPreference.setOnPreferenceChangeListener(this);

        mChargingLevelPreference = (SwitchPreference) findPreference(Constants.GLYPH_CHARGING_LEVEL_ENABLE);
        mChargingLevelPreference.setEnabled(glyphEnabled);
        mChargingLevelPreference.setOnPreferenceChangeListener(this);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String preferenceKey = preference.getKey();

        if (preferenceKey.equals(Constants.GLYPH_NOTIFS_ENABLE)) {
            SettingsManager.setGlyphNotifsEnabled(getActivity(), !mNotifsPreference.isChecked() ? true : false);
        }

        mHandler.post(() -> ServiceUtils.checkGlyphService(getActivity()));

        return true;
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        SettingsManager.enableGlyph(getActivity(), isChecked);
        ServiceUtils.checkGlyphService(getActivity());

        mSwitchBar.setChecked(isChecked);

        mFlipPreference.setEnabled(isChecked);

        mBrightnessPreference.setEnabled(isChecked);

        mNotifsPreference.setEnabled(isChecked);
        mNotifsPreference.setSwitchEnabled(isChecked);

        mCallPreference.setEnabled(isChecked);

        mChargingLevelPreference.setEnabled(isChecked);
    }

    @Override
    public void onDestroy() {
        mSettingObserver.unregister(mContentResolver);
        super.onDestroy();
    }

    private class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(new Handler());
        }

        public void register(ContentResolver cr) {
            cr.registerContentObserver(Settings.Secure.getUriFor(
                Constants.GLYPH_NOTIFS_ENABLE), false, this);
        }

        public void unregister(ContentResolver cr) {
            cr.unregisterContentObserver(this);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (uri.equals(Settings.Secure.getUriFor(Constants.GLYPH_NOTIFS_ENABLE))) {
                mNotifsPreference.setChecked(SettingsManager.isGlyphNotifsEnabled(getActivity()));
            }
        }
    }
}
