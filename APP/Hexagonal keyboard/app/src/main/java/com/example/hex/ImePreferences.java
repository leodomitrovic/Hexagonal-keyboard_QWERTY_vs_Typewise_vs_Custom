package com.example.hex;

import android.os.Bundle;

import android.preference.ListPreference;
import android.preference.PreferenceFragment;

import androidx.appcompat.app.AppCompatActivity;


// Prikazuje "Preferences" (postavke) za doticnu metodu unosa teksta
// TESTIRANJE:::
// Buduci da ne postoji launcher aktivnost:
// Run -> Edit Configurations...
// Run/Debug Configurations dialog: General (tab):
// Launch Options: Launch: Nothing
public class ImePreferences extends AppCompatActivity {

    static MyPreferenceFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.settings_name);

        // Postoji odgovarajuca xml datoteka koja opisuje postavke unosne metode:
        // addPreferencesFromResource(R.xml.ime_preferences); // <- deprecated
        // @Android API:
        // The preferred approach (as of API level 11) is to instantiate PreferenceFragment
        // objects to load preferences from a resource file:
        mFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction().replace(android.R.id.content, mFragment).commit();
    }


    // Preference fragment; ovdje ce se definirati listeneri koji reagiraju na
    // promjenu vrijednosti upisanu u neki EditText element (iz razloga sto novu vrijednost
    // zelimo odmah vizualizirati u UI).
    // Napomena: checkbox listenere ne treba "odradjivati", oni se implicitno-automatski vizualiziraju (true/false)
    public static class MyPreferenceFragment extends PreferenceFragment {

        @Override
        public void onCreate(final Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.ime_preferences);

            setListener("my_keyboard_layout");
        }

        protected void setListener(String preferenceKey){
            ListPreference etPref =
                    (ListPreference)getPreferenceManager().findPreference(preferenceKey);
            etPref.setSummary(etPref.getEntry());

            if (etPref != null) {
                etPref.setOnPreferenceChangeListener((preference, newValue) -> {
                    ListPreference pref = (ListPreference) preference;
                    if (newValue.toString().equals("0")) {
                        pref.setSummary("QWERTY");
                    } else if (newValue.toString().equals("1")) {
                        pref.setSummary("Typewise");
                    } else {
                        pref.setSummary("Custom");
                    }
                    return true;
                });
            }
        }
    }

}
