package com.officialsounding.alarmmanager.model;

public class AlarmException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9189378052462169117L;

	public AlarmException(String reason, Throwable cause) {
		super(reason, cause);
	}
	
	public AlarmException(String reason) {
		super(reason);
	}
}
