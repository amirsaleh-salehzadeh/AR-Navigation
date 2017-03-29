package com.honours.genar.myapplication2.app;

import android.content.SharedPreferences;
import android.preference.PreferenceActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;
import augmentation.AugmentedActivity;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String PREF_KEY_HIDE_RADAR = "pref_key_hide_radar";
    public static final String PREF_KEY_HIDE_ZOOM_BAR = "pref_key_hide_zoom_bar";
    public static final String PREF_KEY_DETAILED_RADIUS = "detailed_radius_preference";
    public static final String PREF_KEY_MAXIMUM_RADIUS = "max_radius_preference";
    public static final String PREF_KEY_DISABLE_IMAGE_ICONS = "pref_key_disable_image_icons";
    public static final String PREF_KEY_REMEMBER = "pref_key_remember";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(PREF_KEY_HIDE_RADAR)){
            MainActivity.showRadar = !MainActivity.showRadar;
        }
        else if (key.equals(PREF_KEY_HIDE_ZOOM_BAR)){
            MainActivity.showZoomBar = !MainActivity.showZoomBar;
        }
        else if (key.equals(PREF_KEY_DISABLE_IMAGE_ICONS)){
            ARData.setUseImageIcons(!ARData.getUseImageIcons());
        }
        else if (key.equals(PREF_KEY_DETAILED_RADIUS)){
            ARData.setDetailedRadius(Float.parseFloat(sharedPreferences.getString(key, "10000")));
            Toast.makeText(getApplicationContext(), "Detailed radius changed in Settings", Toast.LENGTH_SHORT).show();
        }
        else if (key.equals(PREF_KEY_MAXIMUM_RADIUS)){
            AugmentedActivity.setMaxZoom(Float.parseFloat(sharedPreferences.getString(key, "100000")));
        }
        else if (key.equals(PREF_KEY_REMEMBER)){

            if (sharedPreferences.getBoolean(PREF_KEY_REMEMBER, false)){
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("email",
                        ARData.getCurrentAccount().getEmail()).commit();
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("password",
                        ARData.getCurrentAccount().getPassword()).commit();
                Toast.makeText(getApplicationContext(), "Login Details Saved",Toast.LENGTH_SHORT).show();
            }
            else{
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().clear().commit();
                Toast.makeText(getApplicationContext(),"Login Details Removed",Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}
