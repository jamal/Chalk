package com.jamalfanaian.chalk.sync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;
import android.text.format.Time;
import android.util.Log;

import com.jamalfanaian.chalk.Constants;
import com.jamalfanaian.chalk.Note;
import com.jamalfanaian.chalk.activities.NotesDbAdapter;
import com.jamalfanaian.chalk.sync.exception.SyncServiceInvalidResponseException;

/**
 * Sync with tomboy based services
 * @author jamal
 * 
 */
public class TomboyService implements ISyncService {
	
	private static final String TAG = "TomboyService";
	
	private static final String AUTH_URL = "https://one.ubuntu.com/notes/api/1.0/";
	private static final String API_URL = "https://one.ubuntu.com/notes/api/1.0/op/";
	
	private static final String CONSUMER_KEY = "anyone";
	private static final String CONSUMER_SECRET = "anyone";
	
	public static final String KEY_OAUTH_TOKEN = "oauth_request_token";
	public static final String KEY_OAUTH_TOKEN_SECRET = "oauth_request_token_secret";
	public static final String KEY_OAUTH_AUTHORIZE_URL = "oauth_authorize_url";
	public static final String KEY_OAUTH_REQUEST_TOKEN_URL = "oauth_request_token_url";
	public static final String KEY_OAUTH_ACCESS_TOKEN_URL = "oauth_access_token_url";
	public static final String KEY_OAUTH_TOKEN_TYPE = "oauth_token_type";
	public static final String KEY_API_URL = "tomboy_api_url";
	
	public String mTokenType;
	
	private Context mContext;
	
	private OAuthConsumer mConsumer;
	private OAuthProvider mProvider;
	
	public TomboyService(Context context) {
		mContext = context;
		mConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		mProvider = null;
		mTokenType = null;
		
		SharedPreferences prefs = getPrefs();
		String token = prefs.getString(KEY_OAUTH_TOKEN, null);
		String tokenSecret = prefs.getString(KEY_OAUTH_TOKEN_SECRET, null);
		String tokenType = prefs.getString(KEY_OAUTH_TOKEN_TYPE, null);
		
		if (token != null && tokenSecret != null) {
			mConsumer.setTokenWithSecret(token, tokenSecret);
			mTokenType = tokenType;
		}
	}
	
	public boolean isAuthenticated() {
		return mTokenType != null && mTokenType.equals("access")
			&& mConsumer.getToken() != null && mConsumer.getTokenSecret() != null;
	}
	
	public HttpResponse execute(HttpUriRequest request) {
		HttpClient client = new DefaultHttpClient();
		HttpResponse response;
		
		try {
			response = client.execute(request);
			
			return response;
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String httpPut(String uri, String body, boolean signed) {
		// TODO: This should be moved to it's own class

		StringBuilder sb = new StringBuilder();
		
		HttpPut request = new HttpPut(uri);
		HttpResponse response;
	
		try {
			request.setEntity(new StringEntity(body, "UTF-8"));
			
			if (signed && isAuthenticated()) {
				
				try {
					mConsumer.sign(request);
				} catch (OAuthMessageSignerException e) {
					e.printStackTrace();
				} catch (OAuthExpectationFailedException e) {
					e.printStackTrace();
				} catch (OAuthCommunicationException e) {
					e.printStackTrace();
				}
				
			}
			
			Log.d(TAG, "HTTP Put " + uri);
			response = execute(request);
			Log.d(TAG, response.getStatusLine().toString());

			InputStream is = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			String line = null;
		
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		
			is.close();
			
			Log.d(TAG, sb.toString());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return sb.toString();
	}
	
	public String httpRequest(String uri, boolean signed) {
		// TODO: This should be moved to it's own class

		StringBuilder sb = new StringBuilder();
		
		HttpGet request = new HttpGet(uri);
		HttpResponse response;
	
		try {
			if (signed && isAuthenticated()) {
				
				try {
					mConsumer.sign(request);
					Log.d(TAG, mConsumer.getConsumerKey());
					Log.d(TAG, mConsumer.getConsumerSecret());
					Log.d(TAG, mConsumer.getToken());
					Log.d(TAG, mConsumer.getTokenSecret());
				} catch (OAuthMessageSignerException e) {
					e.printStackTrace();
				} catch (OAuthExpectationFailedException e) {
					e.printStackTrace();
				} catch (OAuthCommunicationException e) {
					e.printStackTrace();
				}
				
			}
			
			Header[] headers = request.getAllHeaders();
			
			for (int i = 0; i < headers.length; i++) {
				Log.d(TAG, headers[i].getName() + ": " + headers[i].getValue());
			}
			
			Log.d(TAG, "HTTP Request " + uri);
			response = execute(request);
			
			InputStream is = response.getEntity().getContent();
			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			
			String line = null;
		
			while ((line = reader.readLine()) != null) {
				sb.append(line).append("\n");
			}
		
			is.close();
		
			Log.d(TAG, sb.toString());
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		
		return sb.toString();
	}
	
	public OAuthProvider getProvider() {

		if (mProvider == null) {
			
			boolean update = false;
			
			try {
				SharedPreferences prefs = getPrefs();
				String accessTokenUrl = prefs.getString(KEY_OAUTH_ACCESS_TOKEN_URL, null);
				String authorizeUrl = prefs.getString(KEY_OAUTH_AUTHORIZE_URL, null);
				String requestTokenUrl = prefs.getString(KEY_OAUTH_REQUEST_TOKEN_URL, null);
				
				if (accessTokenUrl == null || authorizeUrl == null || requestTokenUrl == null) {

					String response = httpRequest(AUTH_URL, false);
					Log.d(TAG, response);
					
					JSONObject jsonResponse = new JSONObject(response);
					
					accessTokenUrl = jsonResponse.getString("oauth_access_token_url");
					authorizeUrl = jsonResponse.getString("oauth_authorize_url");
					requestTokenUrl = jsonResponse.getString("oauth_request_token_url");

					update = true;
				}
				
				mProvider = new CommonsHttpOAuthProvider(requestTokenUrl,
						accessTokenUrl, authorizeUrl);
				mProvider.setOAuth10a(true);

				if (update) {
					savePrefs();
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return mProvider;
	}
	
	/**
	 * Get authorization URL
	 * @return String authorization URL
	 */
	public String getAuthorizationUrl() {
		String url;
		OAuthProvider provider = getProvider();
		
		try {
			Log.d(TAG, "Retrieving request token");
			
			url = provider.retrieveRequestToken(mConsumer, "whisper://sync");
			mTokenType = "request";
			
			savePrefs();

			return url;
			
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			Log.d(TAG, "Response: " + e.getResponseBody());
			e.printStackTrace();
		}
		
		return null;
	}
	
	public void authorize() {
        String url = getAuthorizationUrl();
        
		Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        mContext.startActivity(intent);
	}
	
	public boolean getAccessToken(String verifier) {
		if (mConsumer.getToken() != null && mConsumer.getTokenSecret() != null) {
			try {
				Log.d(TAG, "Retrieving access token");
				
				getProvider().retrieveAccessToken(mConsumer, verifier);
				mTokenType = "access";
				
				Log.d(TAG, "Received access token: " + mConsumer.getToken() + ", secret: " + mConsumer.getTokenSecret());
				
				savePrefs();
				return true;
				
			} catch (OAuthMessageSignerException e) {
				e.printStackTrace();
				return false;
			} catch (OAuthNotAuthorizedException e) {
				e.printStackTrace();
				return false;
			} catch (OAuthExpectationFailedException e) {
				e.printStackTrace();
				return false;
			} catch (OAuthCommunicationException e) {
				Log.d(TAG, "OAuth response: " + e.getResponseBody());
				e.printStackTrace();
				return false;
			}
		}
		
		return false;
	}
	
	private SharedPreferences getPrefs() {
		SharedPreferences prefs = mContext.getSharedPreferences(Constants.PREFS_NAME, 0);
		return prefs;
	}
	
	private void savePrefs() {
		Log.d(TAG, "Save prefs");
		
		if (mProvider != null && mConsumer != null) {
			SharedPreferences.Editor editor = getPrefs().edit();
			Log.d(TAG, "Saving token: " + mConsumer.getToken());
			editor.putString(KEY_OAUTH_TOKEN_TYPE, mTokenType);
			editor.putString(KEY_OAUTH_TOKEN, mConsumer.getToken());
			editor.putString(KEY_OAUTH_TOKEN_SECRET, mConsumer.getTokenSecret());
			editor.putString(KEY_OAUTH_REQUEST_TOKEN_URL, mProvider.getRequestTokenEndpointUrl());
			editor.putString(KEY_OAUTH_AUTHORIZE_URL, mProvider.getAuthorizationWebsiteUrl());
			editor.putString(KEY_OAUTH_ACCESS_TOKEN_URL, mProvider.getAccessTokenEndpointUrl());
			editor.commit();
		}
	}
	
	private void put(Note note, int revision) {
		put(note, revision, null);
	}

	// TODO: Return boolean for success
	private void put(Note note, int revision, String command) {
		// TODO: Make a put that takes mulitple notes

		String guid = note.getRemoteId();
		
		Log.d(TAG, "in put()");
	
		JSONObject jsonRequest = new JSONObject();
		JSONObject jsonNote = new JSONObject();
		JSONArray jsonNoteChanges = new JSONArray();
		
		try {
			if (guid != null) {
				jsonNote.put("guid", guid);
			}
			
			if (command != null) {
				jsonNote.put("command", command);
			}
			
			Time time = new Time();
			time.set(note.getDateUpdate() * 1000);
			time.switchTimezone("GMT");
			
			jsonNote.put("title", note.getTitle());
			jsonNote.put("note-content", note.getContent());
			jsonNote.put("last-change-date", time.format3339(false));
			jsonNote.put("latest-sync-revision", revision);
			jsonNoteChanges.put(0, jsonNote);

			jsonRequest.put("latest-sync-revision", revision);
			jsonRequest.put("note-changes", jsonNoteChanges);
			
			// TODO: Response should be an object that contains status and response string
			String response = httpPut(API_URL, jsonRequest.toString(), true);
			Log.d(TAG, response);
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
	
	// TODO: Return boolean for success
	private void delete(Note note, int revision) {
		put(note, revision, "delete");
	}
	
	public void sync() throws SyncServiceInvalidResponseException {
		int countCreated = 0;
		int countUpdated = 0;
		int countMerged = 0;
		int countUploaded = 0;
		int countDeleted = 0;
		
        NotesDbAdapter notesDb = new NotesDbAdapter(mContext);
        notesDb.open();
        
		Time time = new Time();
		
		Log.d(TAG, "Downloading updates");
		String response = httpRequest(API_URL + "?include_notes=true", true);
		
		Log.d(TAG, response);
		
		// Download remote updates
		JSONObject jsonResponse;
		try {
			jsonResponse = new JSONObject(response);
			
			JSONArray remoteNotes = jsonResponse.getJSONArray("notes");
			
			for (int i = 0; i < remoteNotes.length(); i++) {
				JSONObject jsonNote = remoteNotes.getJSONObject(i);
				
				// Get remote note data
				String remoteId = jsonNote.getString("guid");
				String title = Html.fromHtml(jsonNote.getString("title")).toString();
				String content = Html.fromHtml(jsonNote.getString("note-content")).toString();
				Integer remoteRev = jsonNote.getInt("last-sync-revision");
				
				time.parse3339(jsonNote.getString("last-change-date"));
				time.switchTimezone(Time.getCurrentTimezone());
				
				Long remoteTime = new Long(time.format("%s"));
				
				Cursor cursor = notesDb.fetchNoteByRemoteId(remoteId);
				if (cursor == null || cursor.getCount() == 0) {
					
					// Note doesn't exist locally
					Note remoteNote = new Note();
					remoteNote.setTitle(title);
					remoteNote.setContent(content);
					remoteNote.setRemoteId(remoteId);
					remoteNote.setDateRemoteUpdate(remoteTime);
					remoteNote.setRemoteRev(remoteRev);
					notesDb.save(remoteNote);
					
					countCreated++;
					
				} else {
					
					// Note exists locally
					Note localNote = new Note(cursor);

					if (remoteRev > localNote.getRemoteRev() && !localNote.getModified()) {
						
						// Remote note is newer
						localNote.setTitle(title);
						localNote.setContent(content);
						localNote.setRemoteRev(remoteRev);
						localNote.setDateRemoteUpdate(remoteTime);
						notesDb.save(localNote);
						
						countUpdated++;
						
					} else if (remoteRev > localNote.getRemoteRev() && localNote.getModified()) {
						// TODO: Allow the user to choose conflict resolution
						
						Log.d(TAG, "Overwriting note " + title);
						
						// Overwrite
						localNote.setTitle(title);
						localNote.setContent(content);
						localNote.setRemoteRev(remoteRev);
						localNote.setDateRemoteUpdate(remoteTime);
						notesDb.save(localNote);
						
						countMerged++;
						
					}
					
				}
				
				cursor.close();
				
			} // END looping over remote notes
			
			int revision = jsonResponse.getInt("latest-sync-revision");
			int nextRevision = revision + 1;
			
			// Delete notes
			Cursor deletedNotes = notesDb.fetchDeletedNotes();
			Log.d(TAG, "Uploading " + Integer.toString(deletedNotes.getCount()) + " deleted notes");
			if (deletedNotes.getCount() > 0) {
				deletedNotes.moveToFirst();
				
				while (!deletedNotes.isAfterLast()) {
					Note note = new Note(deletedNotes);
					
					delete(note, nextRevision);
					
					countDeleted++;
					
					deletedNotes.moveToNext();
				}
			}
			deletedNotes.close();
			
			// Upload modified notes (if any)
			Cursor modifiedNotes = notesDb.fetchModifiedNotes();
			Log.d(TAG, "Uploading " + Integer.toString(modifiedNotes.getCount()) + " modified notes");
			if (modifiedNotes.getCount() > 0) {
				modifiedNotes.moveToFirst();
				
				while (!modifiedNotes.isAfterLast()) {
					Note note = new Note(modifiedNotes);
					
					put(note, nextRevision);

					// Update note
					note.setModified(false);
					note.setRemoteRev(nextRevision);
					note.setDateRemoteUpdate(note.getDateUpdate());
					notesDb.save(note);
					
					countUploaded++;
					
					modifiedNotes.moveToNext();
				}
			}
			modifiedNotes.close();
			
			Log.d(TAG, "Created " + countCreated + ", updated " + countUpdated + 
					   ", merged " + countMerged + ", and uploaded " + 
					   countUploaded + ", and deleted " + countDeleted);
			
		} catch (JSONException e) {
			throw new SyncServiceInvalidResponseException(response);
		}
		
		notesDb.close();
	}
	
}