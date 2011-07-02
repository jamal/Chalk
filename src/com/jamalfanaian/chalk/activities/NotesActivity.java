package com.jamalfanaian.chalk.activities;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.jamalfanaian.chalk.Constants;
import com.jamalfanaian.chalk.Note;
import com.jamalfanaian.chalk.R;
import com.jamalfanaian.chalk.sync.ISyncService;
import com.jamalfanaian.chalk.sync.SyncServiceFactory;
import com.jamalfanaian.chalk.sync.exception.SyncServiceInvalidResponseException;

public class NotesActivity extends ListActivity {
	
	private static final String TAG = "NotesActivity";
	
	private NotesDbAdapter mNotesDb;
	
	private ISyncService mSyncService;
	private boolean mSyncActive;

	private final static SimpleDateFormat DATE_FORMAT_HOUR = new SimpleDateFormat("h:mm a");
	private final static SimpleDateFormat DATE_FORMAT_MONTH = new SimpleDateFormat("MMM d");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
        
        mSyncActive = false;

        mNotesDb = new NotesDbAdapter(this);
        loadNotes();
        
        registerForContextMenu(getListView());
        
        SharedPreferences prefs = getSharedPreferences(Constants.PREFS_NAME, 0);
        String syncService = prefs.getString(Constants.KEY_SYNC_SERVICE, null);

        Log.d(TAG, "Sync Service: " + syncService);
        
        mSyncService = SyncServiceFactory.getSyncService(syncService, this);
        
        startSync();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        loadNotes();
    }
    
    private void loadNotes() {
    	Log.d(TAG, "Loading notes from DB");
    	
        mNotesDb.open();
        
    	Cursor cursor = mNotesDb.fetchAllNotes();
    	startManagingCursor(cursor);

        NotesAdapter adapter = new NotesAdapter(this);
        adapter.changeCursor(cursor);
        setListAdapter(adapter);
        
        mNotesDb.close();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        openNote(id);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
    	TextView note = (TextView) v.findViewById(R.id.note_title);
    	menu.setHeaderTitle(note.getText());
    	
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.context_item_note, menu);
		super.onCreateContextMenu(menu, v, menuInfo);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        
    	switch (item.getItemId()) {
    	case R.id.delete:
    		
    		mNotesDb.open();
    		mNotesDb.delete(info.id);
    		mNotesDb.close();
    		
    		loadNotes();
    		return true;
    	}
    	
    	return super.onContextItemSelected(item);
    }
    
    public void onNewNoteClick(View view) {
    	openNote(null);
    }
    
    private void openNote(Long id) {
    	Intent i = new Intent(this, NoteEditActivity.class);
    	if (id != null) {
    		i.putExtra(NotesDbAdapter.KEY_ID, id);
    	}
    	
    	startActivityForResult(i, 0);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main_activity, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.refresh:
    		startSync();
    		return true;
    		
    	case R.id.settings:
    		Intent i = new Intent(this, SettingsActivity.class);
    		startActivity(i);
    		return true;
    		
		default:
    		return super.onOptionsItemSelected(item);
    	}
    }
    
    protected void startSync() {
    	if (!mSyncActive && mSyncService != null 
    			&& mSyncService.isAuthenticated()) {
    		new SyncTask().execute(this);
    	}
    }
    
    public ISyncService getSyncService() {
    	return mSyncService;
    }
    
    protected class SyncTask extends AsyncTask<NotesActivity, Void, NotesActivity> {
    
    	// TODO: This whole class is pretty bad.. fix it! 
    	// or look into using java.nio instead.
    	
    	@Override
		protected void onPreExecute() {
    		Log.d("SyncTask", "onPreExecute");
    		mSyncActive = true;
    		findViewById(R.id.title_loading).setVisibility(View.VISIBLE);
		}

		@Override
    	protected NotesActivity doInBackground(NotesActivity... a) {
			ISyncService s = a[0].getSyncService();
            if (s != null && s.isAuthenticated()) {
            	Log.d(TAG, "Starting sync");
            	
            	try {
            		s.sync();
            	} catch (SyncServiceInvalidResponseException e) {
            		e.printStackTrace();
            	}
            }
            
			return a[0];
    	}
    	
    	protected void onPostExecute(NotesActivity a) {
    		Log.d("SyncTask", "onPostExecute");
    		mSyncActive = false;
    		findViewById(R.id.title_loading).setVisibility(View.INVISIBLE);
    		a.loadNotes();
    	}

    }
    
    private class NotesAdapter extends CursorAdapter {
    	
    	public NotesAdapter(Context context) {
    		super(context, null);
    	}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final Calendar today = Calendar.getInstance();
			today.set(Calendar.HOUR_OF_DAY, 0);
			
            final TextView titleView = (TextView) view.findViewById(R.id.note_title);
            final TextView bodyView = (TextView) view.findViewById(R.id.note_body);
            final TextView dateView = (TextView) view.findViewById(R.id.note_date);
            
            Note note = new Note(cursor);
            
            Date date = new Date(1000 * note.getDateCreate());
            
            titleView.setText(note.getTitle().toString());
            bodyView.setText(note.getContent().toString());
            
            if (today.getTime().before(date)) {
            	dateView.setText(DATE_FORMAT_HOUR.format(date));
            } else {
            	dateView.setText(DATE_FORMAT_MONTH.format(date));
            }
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return getLayoutInflater().inflate(R.layout.list_item_note, parent, false);
		}
    	
    }
    
}