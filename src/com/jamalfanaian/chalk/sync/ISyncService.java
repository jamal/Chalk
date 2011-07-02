package com.jamalfanaian.chalk.sync;

import com.jamalfanaian.chalk.sync.exception.SyncServiceInvalidResponseException;

public interface ISyncService {

	/**
	 * @return boolean True if the user is authenticated to the current service,
	 * 				   false otherwise.
	 */
	public boolean isAuthenticated();
	
	/**
	 * Starts the authorize activity for this service
	 */
	public void authorize();

	/**
	 * Starts the sync process
	 * @throws SyncServiceInvalidResponseException 
	 */
	public void sync() throws SyncServiceInvalidResponseException;
	
}
