package com.jamalfanaian.chalk.sync;

import android.content.Context;

public class SyncServiceFactory {
	
	public static ISyncService getSyncService(String name, Context ctx) {
		if (name != null) {
			if (name.equals("Ubuntu One")) {
				return new TomboyService(ctx);
			}	
		}
		
		return null;
	}

}
