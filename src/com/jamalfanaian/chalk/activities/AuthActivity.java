package com.jamalfanaian.chalk.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.jamalfanaian.chalk.sync.TomboyService;

public class AuthActivity extends Activity {

	@Override
	protected void onResume() {
		super.onResume();
		
		Intent intent = getIntent();

		Log.w("AuthActivity", "Intent URI: " + intent.getData().toString());
		
		if (intent != null && intent.getData() != null &&
				intent.getData().toString().startsWith("whisper://")) {
			
			// TODO: Check the current pref for which service to sync against
			TomboyService service = new TomboyService(this);
		
			// Get data from URI and store it in the app prefs
            Uri uri = intent.getData();
            String verifier = uri.getQueryParameter("oauth_verifier");
            
            if (verifier != null) {
            	// TODO: Show an error dialog if the auth fails
            	service.getAccessToken(verifier);
            	
            	// TODO: Show a success dialog
            	Intent i = new Intent(this, NotesActivity.class);
            	startActivity(i);
            }
		}

		finish();
	}
	
}
