package com.example.android.todolist;

/**
 * Created by Zsolt on 02.02.2018.
 */

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings_main);

        Preference orderBy = findPreference(getString(R.string.pref_key_order));
        bindPreferenceSummaryToValueString(orderBy);

        Preference notification = findPreference(getString(R.string.pref_key_notification));
        bindPreferenceSummaryToValueBoolean(notification);
    }

    // onPreferenceChange() method is invoked with the key of the preference that was changed
    // in oder to save changes.
    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();
        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                CharSequence[] labels = listPreference.getEntries();
                preference.setSummary(labels[prefIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    }

    // Helper method for current String value of the "order by" preference stored in the SharedPreferences on the
    // device, and display that in the preference summary.
    private void bindPreferenceSummaryToValueString(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        String preferenceString = preferences.getString(preference.getKey(), "");
        onPreferenceChange(preference, preferenceString);
    }

    // Helper method for current boolean value of the "set reminder" preference stored in the SharedPreferences on the
    // device, and display that in the preference summary.
    private void bindPreferenceSummaryToValueBoolean(Preference preference) {
        preference.setOnPreferenceChangeListener(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        boolean preferenceBoolean = preferences.getBoolean(preference.getKey(), false);
        onPreferenceChange(preference, preferenceBoolean);
    }
}

