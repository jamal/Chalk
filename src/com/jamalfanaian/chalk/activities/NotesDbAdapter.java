package com.jamalfanaian.chalk.activities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.jamalfanaian.chalk.Note;

public class NotesDbAdapter {
	
	public static final String TAG = "NotesDbAdapter";
	
	public static final String KEY_ID = "_id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_CONTENT = "content";
	public static final String KEY_PINNED = "pinned";
	public static final String KEY_MODIFIED = "modified";
	public static final String KEY_DELETED = "deleted";
	public static final String KEY_REMOTE_ID = "remote_id";
	public static final String KEY_REMOTE_REV = "remote_rev";
	public static final String KEY_DATE_REMOTE_UPDATE = "date_remote_update";
	public static final String KEY_DATE_CREATE = "date_create";
	public static final String KEY_DATE_UPDATE = "date_update";
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private final Context mContext;
	
	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE = "notes";
	private static final int DATABASE_VERSION = 8;	// Public release at version 7
	
	private static final String DATABASE_CREATE = 
		"CREATE TABLE " + DATABASE_TABLE + " (" +
			KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
			KEY_TITLE + " TEXT NOT NULL," +
			KEY_CONTENT + " TEXT NOT NULL," +
			KEY_PINNED + " INTEGER DEFAULT 0," +
			KEY_MODIFIED + " INTEGER DEFAULT 0," +
			KEY_DELETED + " INTEGER DEFAULT 0," +
			KEY_REMOTE_ID + " TEXT NULL," + 
			KEY_REMOTE_REV + " INTEGER DEFAULT 0," + 
			KEY_DATE_REMOTE_UPDATE + " INTEGER NULL," + 
			KEY_DATE_CREATE + " INTEGER NOT NULL," +
			KEY_DATE_UPDATE + " INTEGER NOT NULL);";
	
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);	
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// Upgrade 7 -> 8
			if (oldVersion <= 7 && newVersion == 8) {
				db.execSQL("ALTER TABLE " + DATABASE_TABLE + " ADD COLUMN " + KEY_DELETED + " INTEGER DEFAULT 0");		
			}
		}
		
	}
	
	
	public NotesDbAdapter(Context context) {
		mContext = context;
	}
	
	public NotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
	}
	
	public void close() {
		mDbHelper.close();
	}
	
	public long save(Note note) {
		Long id = note.getId();
		long timestamp = System.currentTimeMillis() / 1000L;
		
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, note.getTitleHtml());
		values.put(KEY_CONTENT, note.getContentHtml());
		values.put(KEY_PINNED, note.getPinned());
		values.put(KEY_MODIFIED, note.getModified());
		values.put(KEY_DELETED, note.getDeleted());
		values.put(KEY_REMOTE_ID, note.getRemoteId());
		values.put(KEY_REMOTE_REV, note.getRemoteRev());
		values.put(KEY_DATE_REMOTE_UPDATE, note.getDateRemoteUpdate());
		values.put(KEY_DATE_UPDATE, timestamp);

		if (id != null) {
			mDb.update(DATABASE_TABLE, values, KEY_ID + " = " + id, null);
			return id;
		} else {
			values.put(KEY_DATE_CREATE, timestamp);
	        return mDb.insert(DATABASE_TABLE, null, values);
		}
	}

	/**
	 * Create a new note
	 * 
	 * @param title the title of the new note
	 * @param body the body of the new note
	 * @return the row ID for the new created note
	 */
	public long create(String content) {
		long timestamp = System.currentTimeMillis() / 1000L;
		
        ContentValues values = new ContentValues();
        values.put(KEY_CONTENT, content);
        values.put(KEY_DATE_CREATE, timestamp);
        values.put(KEY_DATE_UPDATE, timestamp);

        return mDb.insert(DATABASE_TABLE, null, values);
	}
	
	/**
	 * Update an existing note
	 * 
	 * @param id the id of the note being updated
	 * @param title the title of the note
	 * @param body the body of the note
	 * @return true if the note was successfully updated, false otherwise
	 */
	public boolean update(long id, String content) {
		long timestamp = System.currentTimeMillis() / 1000L;
		
        ContentValues values = new ContentValues();
        values.put(KEY_CONTENT, content);
        values.put(KEY_DATE_UPDATE, timestamp);

        return mDb.update(DATABASE_TABLE, values, KEY_ID + " = " + id, null) > 0;
	}
	
	/**
	 * Delete a note
	 * 
	 * @param id the id of the note being deleted
	 * @return true if the note was successfully deleted, false otherwise
	 */
    public boolean delete(long id) {
    	long timestamp = System.currentTimeMillis() / 1000L;
		
    	ContentValues values = new ContentValues();
    	values.put(KEY_DELETED, 1);
        values.put(KEY_DATE_UPDATE, timestamp);
    	
        return mDb.update(DATABASE_TABLE, values, KEY_ID + "=" + id, null) > 0;
    }
	
	/**
	 * Toggle the pinned status of a note
	 * 
	 * @param id the id of the note being updated
	 * @param boolean pinned the new pinned value
	 * @return true if the note was successfully updated, false otherwise
	 */
	public boolean setPinned(long id, boolean pinned) {
		long timestamp = System.currentTimeMillis() / 1000L;
		
		ContentValues values = new ContentValues();
		values.put(KEY_PINNED, pinned);
		values.put(KEY_DATE_UPDATE, timestamp);
		
		return mDb.update(DATABASE_TABLE, values, KEY_ID + " = " + id, null) > 0;
	}
	
	/**
	 * Return a cursor for all notes, sorted by pinned status and date updated
	 * 
	 * @return Cursor for all notes
	 */
	public Cursor fetchAllNotes() {
		return mDb.query(DATABASE_TABLE,  
				new String[] {KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_PINNED, 
				KEY_MODIFIED, KEY_DELETED, KEY_REMOTE_ID, KEY_REMOTE_REV, 
				KEY_DATE_REMOTE_UPDATE, KEY_DATE_CREATE, KEY_DATE_UPDATE}, 
				KEY_DELETED + " = 0", null, null, null, 
				KEY_PINNED + " DESC, " + KEY_DATE_CREATE + " DESC");
	}
	
	/**
	 * Return a cursor positioned at the note with the given ID
	 * 
	 * @param id the id of the note
	 * @return Cursor position to matching note, if found
	 */
	public Cursor fetchNote(long id) {
		Cursor cursor = mDb.query(DATABASE_TABLE,  
				new String[] {KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_PINNED, 
				KEY_MODIFIED, KEY_DELETED, KEY_REMOTE_ID, KEY_REMOTE_REV, 
				KEY_DATE_REMOTE_UPDATE,  KEY_DATE_CREATE, KEY_DATE_UPDATE}, 
				KEY_ID + " = " + id, null, null, null, null);
		
		if (cursor != null) {
			cursor.moveToFirst();
		}
		
		return cursor;
	}
	
	public Cursor fetchNoteByRemoteId(String remoteId) {
		Cursor cursor = mDb.query(DATABASE_TABLE,  
				new String[] {KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_PINNED, 
				KEY_MODIFIED, KEY_DELETED, KEY_REMOTE_ID, KEY_REMOTE_REV, 
				KEY_DATE_REMOTE_UPDATE, KEY_DATE_CREATE, KEY_DATE_UPDATE}, 
				KEY_REMOTE_ID + " = '" + remoteId + "'", 
				null, null, null, null);
		
		if (cursor != null) {
			cursor.moveToFirst();
		}
		
		return cursor;
	}
	
	public Cursor fetchLocalNotes() {
		Cursor cursor = mDb.query(DATABASE_TABLE,  
				new String[] {KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_PINNED, 
				KEY_MODIFIED, KEY_DELETED, KEY_REMOTE_ID, KEY_DATE_REMOTE_UPDATE, 
				KEY_DATE_CREATE, KEY_DATE_UPDATE}, 
				KEY_REMOTE_ID + " IS NULL" + " AND " + KEY_DELETED + " = 0", 
				null, null, null, null);
		
		return cursor;
	}
	
	public Cursor fetchModifiedNotes() {
		Cursor cursor = mDb.query(DATABASE_TABLE,  
				new String[] {KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_PINNED, 
				KEY_MODIFIED, KEY_DELETED, KEY_REMOTE_ID, KEY_REMOTE_REV, 
				KEY_DATE_REMOTE_UPDATE, KEY_DATE_CREATE, KEY_DATE_UPDATE}, 
				KEY_MODIFIED + " = 1", 
				null, null, null, null);
		
		return cursor;
	}
	
	public Cursor fetchDeletedNotes() {
		Cursor cursor = mDb.query(DATABASE_TABLE,  
				new String[] {KEY_ID, KEY_TITLE, KEY_CONTENT, KEY_PINNED, 
				KEY_MODIFIED, KEY_DELETED, KEY_REMOTE_ID, KEY_REMOTE_REV, 
				KEY_DATE_REMOTE_UPDATE, KEY_DATE_CREATE, KEY_DATE_UPDATE}, 
				KEY_DELETED + " = 1", 
				null, null, null, null);
		
		return cursor;
	}
	
}
