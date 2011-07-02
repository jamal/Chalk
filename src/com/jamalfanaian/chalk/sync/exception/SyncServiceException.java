package com.jamalfanaian.chalk.sync.exception;

@SuppressWarnings("serial")
public class SyncServiceException extends Exception {

	public SyncServiceException() {
		super();
	}

	public SyncServiceException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public SyncServiceException(String detailMessage) {
		super(detailMessage);
	}

	public SyncServiceException(Throwable throwable) {
		super(throwable);
	}
	
}
