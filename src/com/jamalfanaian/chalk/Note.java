package com.jamalfanaian.chalk;

import android.database.Cursor;
import android.text.Html;
import android.text.Spanned;

import com.jamalfanaian.chalk.activities.NotesDbAdapter;

public class Note {
	
	private Long mId;
	private String mTitle;
	private String mContent;
	private boolean mPinned;
	private boolean mModified;
	private boolean mDeleted;
	private String mRemoteId;
	private Integer mRemoteRev;
	private Long mDateRemoteUpdate;
	private Long mDateCreate;
	private Long mDateUpdate;
	
	public Note() {
		mId = null;
		mTitle = null;
		mContent = null;
		mPinned = false;
		mModified = false;
		mDeleted = false;
		mRemoteId = null;
		mRemoteRev = null;
		mDateRemoteUpdate = null;
		mDateCreate = null;
		mDateUpdate = null;
	}
	
	public Note(Cursor cursor) {
		mId = cursor.getLong(cursor.getColumnIndex(NotesDbAdapter.KEY_ID));
		mTitle = cursor.getString(cursor.getColumnIndex(NotesDbAdapter.KEY_TITLE));
		mContent = cursor.getString(cursor.getColumnIndex(NotesDbAdapter.KEY_CONTENT));
		mPinned = cursor.getInt(cursor.getColumnIndex(NotesDbAdapter.KEY_PINNED)) != 0;
		mModified = cursor.getInt(cursor.getColumnIndex(NotesDbAdapter.KEY_MODIFIED)) != 0;
		mDeleted = cursor.getInt(cursor.getColumnIndex(NotesDbAdapter.KEY_DELETED)) != 0;
		mRemoteId = cursor.getString(cursor.getColumnIndex(NotesDbAdapter.KEY_REMOTE_ID));
		mRemoteRev = cursor.getInt(cursor.getColumnIndex(NotesDbAdapter.KEY_REMOTE_REV));
		mDateRemoteUpdate = cursor.getLong(cursor.getColumnIndex(NotesDbAdapter.KEY_DATE_REMOTE_UPDATE));
		mDateCreate = cursor.getLong(cursor.getColumnIndex(NotesDbAdapter.KEY_DATE_CREATE));
		mDateUpdate = cursor.getLong(cursor.getColumnIndex(NotesDbAdapter.KEY_DATE_UPDATE));
	}
	
	public void setTitle(String title) {
		mTitle = title;
	}
	
	public void setTitle(Spanned title) {
		mTitle = Html.toHtml(title);
	}
	
	public void setContent(String content) {
		mContent = content;
	}
	
	public void setContent(Spanned content) {
		mContent = Html.toHtml(content);
	}
	
	public void setContentWithTitle(String content) {
		int limit = content.indexOf("\n");
		if (content.indexOf("\n") <= 0) {
			limit = content.length();
		}
		
		setTitle(content.substring(0, limit).trim());
		setContent(content.substring(limit).trim());
	}
	
	public void setContentWithTitle(Spanned content) {
		String htmlContent = Html.toHtml(content);
		
		int limit = htmlContent.indexOf("\n");
		if (htmlContent.indexOf("\n") <= 0) {
			limit = content.length();
		}
		
		setTitle(htmlContent.substring(0, limit).trim());
		setContent(htmlContent.substring(limit).trim());
	}
	
	public void setPinned(boolean pinned) {
		mPinned = pinned;
	}
	
	public void setModified(boolean modified) {
		mModified = modified;
	}
	
	public void setDeleted(boolean deleted) {
		mDeleted = deleted;
	}
	
	public void setRemoteId(String remoteId) {
		mRemoteId = remoteId;
	}
	
	public void setRemoteRev(Integer remoteRev) {
		mRemoteRev = remoteRev;
	}
	
	public void setDateRemoteUpdate(Long date) {
		mDateRemoteUpdate = date;
	}
	
	public Long getId() {
		return mId;
	}
	
	public Spanned getTitle() {
		return Html.fromHtml(mTitle);
	}
	
	public String getTitleHtml() {
		return mTitle;
	}
	
	public Spanned getContent() {
		return Html.fromHtml(mContent);
	}
	
	public String getContentHtml() {
		return mContent;
	}
	
	public boolean getPinned() {
		return mPinned;
	}
	
	public boolean getModified() {
		return mModified;
	}
	
	public boolean getDeleted() {
		return mDeleted;
	}
	
	public String getRemoteId() {
		return mRemoteId;
	}
	
	public Integer getRemoteRev() {
		return mRemoteRev;
	}
	
	public Long getDateRemoteUpdate() {
		return mDateRemoteUpdate;
	}
	
	public Long getDateCreate() {
		return mDateCreate;
	}
	
	public long getDateUpdate() {
		return mDateUpdate;
	}
	
}
