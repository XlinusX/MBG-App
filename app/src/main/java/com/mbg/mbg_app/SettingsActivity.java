package com.mbg.mbg_app;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */

public class SettingsActivity extends AppCompatActivity {
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */

    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);
            }

            Preference pref_letter = preference.getPreferenceManager().findPreference("pref_class_letter");

            if (preference == preference.getPreferenceManager().findPreference("pref_class_level")) {
                Log.d("linus", "onPreferenceChange: " + stringValue);
                if (stringValue.equals("Jahrgangsstufe 1") || stringValue.equals("Jahrgangsstufe 2")) {
                    Log.d("linus", "onPreferenceChange: J1/2");
                    pref_letter.setEnabled(false);
                    pref_letter.setSummary("");
                } else {
                    pref_letter.setEnabled(true);
                }
            }

            if(preference == pref_letter){
                if(!preference.isEnabled()){
                    preference.setSummary("");
                }
            }

            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();

    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            bindPreferenceSummaryToValue(findPreference("pref_class_level"));
            bindPreferenceSummaryToValue(findPreference("pref_class_letter"));

            SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.shared_pref_filename), Context.MODE_PRIVATE);

            ((CheckBoxPreference) findPreference("pref_staySingedIn")).setChecked(sharedPref.getBoolean("staySingedIn", false));

        }

        @Override
        public void onStop() {
            super.onStop();

            SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.shared_pref_filename), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            editor.putBoolean("staySingedIn", ((CheckBoxPreference) findPreference("pref_staySingedIn")).isChecked()).apply();

            editor.putString("selected_class_level", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_class_level", "")).apply();
            editor.putString("selected_class_letter", PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_class_letter", "")).apply();

        }
    }
}
