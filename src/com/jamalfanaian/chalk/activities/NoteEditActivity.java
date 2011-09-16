package com.jamalfanaian.chalk.activities;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.jamalfanaian.chalk.Note;
import com.jamalfanaian.chalk.R;

public class NoteEditActivity extends Activity {
	
	private static final String TAG = "NoteEditActivity";
	
	private NotesDbAdapter mNotesDb;
	
	private String mTitle;
	private Long mNoteId;
	private Note mNote;
	
	private EditText mNoteEdit;
	private TextView mTitleView;
	private SpannableStringBuilder mSpanText;
	
	private boolean mModified = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_edit);
		
		mNotesDb = new NotesDbAdapter(this);
		
		mNoteId = null;
		mNoteEdit = (EditText) findViewById(R.id.note_edit);
		mTitleView = (TextView) findViewById(R.id.note_edit_title);

		mSpanText = new SpannableStringBuilder();
		
		if (savedInstanceState != null) {
			// Load from bundle
			mNoteId = (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ID);
		}
		
		if (mNoteId == null) {
			// Check if an ID was passed from the intent
			Bundle extras = getIntent().getExtras();
			if (extras != null) {
				mNoteId = extras.getLong(NotesDbAdapter.KEY_ID);
			}
		}

		if (mNoteId == null) {
			mTitleView.setText(R.string.new_note);
			
			// Set default styles for new note
			mSpanText.setSpan(new StyleSpan(Typeface.BOLD), 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			mSpanText.setSpan(new UnderlineSpan(), 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
			mNoteEdit.setText(mSpanText, BufferType.SPANNABLE);
		} else {
			mTitleView.setText(R.string.edit_note);
		}
		
		// Register text change listener to update the title of the view
		mNoteEdit.addTextChangedListener(new TextWatcher() {
			
			public void afterTextChanged(Editable s) {
				mModified = true;
				
				Log.d(TAG, "afterTextChanged()");

				Spannable content = mNoteEdit.getText();
				int endl = content.toString().indexOf("\n");
				
				if (endl > 0) {
					StyleSpan[] boldSpans = content.getSpans(0, endl, StyleSpan.class);
					UnderlineSpan[] underlineSpans = content.getSpans(0, endl, UnderlineSpan.class);
					
					if (boldSpans.length > 0 && underlineSpans.length > 0 
							&& endl < content.getSpanEnd(boldSpans[0])) {
						content.removeSpan(boldSpans[0]);
						content.removeSpan(underlineSpans[0]);
						
						content.setSpan(new StyleSpan(Typeface.BOLD), 0, endl, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
						content.setSpan(new UnderlineSpan(), 0, endl, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
					}
				}
				
				updateTitle(content.toString());
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
			
		});
	}
	
	private void updateTitle(String body) {
		int limit = body.indexOf("\n");
		if (body.indexOf("\n") <= 0) {
			limit = body.length();
		}
		
		String firstLine = body.substring(0, limit).trim();
		
		// Only update the view when the title changes
		if (firstLine != null && mTitle != firstLine) {
			mTitle = firstLine;
			mTitleView.setText(firstLine);
		}
	}
	
	private void loadNote() {
		if (mNoteId != null) {
			mNotesDb.open();
			
			Log.d(TAG, "Loading note " + Long.toString(mNoteId));
			Cursor cursor = mNotesDb.fetchNote(mNoteId);
    		
    		mNote = new Note(cursor);
    		
    		mSpanText.clear();
    		mSpanText.append(mNote.getTitle() + "\n" + mNote.getContent());
    		mSpanText.setSpan(new StyleSpan(Typeface.BOLD), 0, mNote.getTitle().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
    		mSpanText.setSpan(new UnderlineSpan(), 0, mNote.getTitle().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        	mNoteEdit.setText(mSpanText, BufferType.SPANNABLE);

            cursor.close();
            mNotesDb.close();
		} else {
			mNote = new Note();
		}
	}
	
	private void saveNote() {
		if (mModified) {
			Spanned content = mNoteEdit.getText();
			
			if (content.length() > 0) {
				mNote.setContentWithTitle(content);
				mNote.setModified(true);
				
				mNotesDb.open();
				long id = mNotesDb.save(mNote);
				mNotesDb.close();
				
				if (id > 0) {
					mNoteId = id;
					mModified = false;
				}
			}
		}
	}
	
	public void onSaveClick(View v) {
		saveNote();
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveNote();
	}

	@Override
	protected void onResume() {
		super.onResume();
		loadNote();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(NotesDbAdapter.KEY_ID, mNoteId);
	}

}
