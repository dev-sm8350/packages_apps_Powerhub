/*
 * Copyright (C) 2016 The Pure Nexus Project
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

package com.power.hub;

import com.android.internal.logging.nano.MetricsProto;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Surface;
import android.preference.Preference;
import com.android.settings.R;
import com.power.hub.preferences.Utils;
import com.power.hub.fragments.*;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.view.MenuItem;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import com.android.settings.SettingsPreferenceFragment;

public class powerhub extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.voltage_settings, container, false);
        BottomNavigationView navView = view.findViewById(R.id.bottom_navigation);
        FragmentManager fragmentManager = getFragmentManager();
        navView.setSelectedItemId(R.id.sb_settings);
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Fragment fragment = null;
                Class fragmentClass = null;
                int id = item.getItemId();

                if (R.id.sb_settings == id)
                    fragmentClass = StatusBarSettings.class;
                else if (R.id.button_settings == id)
                    fragmentClass = ButtonSettings.class;
                else if (R.id.lockscreen_settings == id)
                    fragmentClass = LockScreenSettings.class;
                else if (R.id.theme_settings == id)
                    fragmentClass = ThemeSettings.class;
                else
                    fragmentClass = MiscSettings.class;
                try {
                    fragment = (Fragment) fragmentClass.newInstance();
                    fragmentManager.beginTransaction().replace(R.id.frag_container, fragment).commit();
                    Log.d(this.getClass().getName(), "onNavigationItemSelected: ");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d(this.getClass().getName(), e.getMessage());
                }
                return true;
            }
        });
        return view;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.VOLTAGE;
    }
}
