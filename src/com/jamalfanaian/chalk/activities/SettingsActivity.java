package com.jamalfanaian.chalk.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.jamalfanaian.chalk.Constants;
import com.jamalfanaian.chalk.R;
import com.jamalfanaian.chalk.sync.TomboyService;

public class SettingsActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, 0);
        String username = prefs.getString(Constants.KEY_USERNAME, null);
        
        ListPreference syncService = (ListPreference) findPreference("syncService");

        if (username == null) {
        	syncService.setSummary("Not authenticated");
        	syncService.setValue("none");
        } else {
        	syncService.setSummary("Authenticated as " + username);
        }
        	
        syncService.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String value = (String) newValue;
				
				SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, 0);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(Constants.KEY_SYNC_SERVICE, value);
				editor.commit();
				
				if (value.equals("Ubuntu One")) {
					
					TomboyService sync = new TomboyService(SettingsActivity.this);
					sync.authorize();
                    
				} else if (value.equals("none")) {
					
					// Make sure we clear the user/pass
					String username = prefs.getString(Constants.KEY_USERNAME, null);
					String password = prefs.getString(Constants.KEY_PASSWORD, null);
					editor = prefs.edit();
					
					if (username != null || password != null) {
						editor.putString(Constants.KEY_USERNAME, "");
						editor.putString(Constants.KEY_PASSWORD, null);
						editor.commit();
					}
					
				}
				
				// Save the preference
				return true;
			}
			
		});
	}

}
