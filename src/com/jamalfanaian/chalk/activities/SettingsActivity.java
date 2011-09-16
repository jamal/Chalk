package com.jamalfanaian.chalk.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

import com.jamalfanaian.chalk.Constants;
import com.jamalfanaian.chalk.R;
import com.jamalfanaian.chalk.sync.ISyncService;
import com.jamalfanaian.chalk.sync.SyncServiceFactory;
import com.jamalfanaian.chalk.sync.TomboyService;

public class SettingsActivity extends PreferenceActivity {
	
	private static final String TAG = "SettingsActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
        
        ListPreference syncServicePref = (ListPreference) findPreference("syncService");
        String syncServiceName = syncServicePref.getValue();

        // Verify that the service is authenticated
        try {
	        ISyncService syncService = SyncServiceFactory.getSyncService(syncServiceName, this);
	        if (syncService.isAuthenticated()) {
	        	syncServicePref.setSummary("Authenticated with " + syncServiceName);
	        } else {
	        	syncServicePref.setSummary("Not authenticated");
	        	syncServicePref.setValue("None");
	        }
        } catch (IllegalArgumentException e) {
        	// Ignore when we could not get a sync service
        }
        
        syncServicePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				String value = (String) newValue;
				
				SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, 0);
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString(Constants.KEY_SYNC_SERVICE, value);
				editor.commit();
				
				if (value.equals("Ubuntu One")) {
					
					TomboyService sync = new TomboyService(SettingsActivity.this);
					sync.authorize();
                    
				} else if (value.equals("None")) {
					
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
