package com.jamalfanaian.chalk.sync;

import android.content.Context;

public class SyncServiceFactory {
	
	/**
	 * Get a sync service by name
	 * @throws IllegalArgumentException
	 * @param name The name of the service
	 * @param ctx Current context
	 * @return ISyncService that implements name
	 */
	public static ISyncService getSyncService(String name, Context ctx) {
		if (name != null) {
			if (name.equals("Ubuntu One")) {
				return new TomboyService(ctx);
			}	
		}
		
		throw new IllegalArgumentException("No sync service with name: " + name);
	}

}
