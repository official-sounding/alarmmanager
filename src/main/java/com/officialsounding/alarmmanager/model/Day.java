package com.officialsounding.alarmmanager.model;

import org.quartz.DateBuilder;

public enum Day {

	ALL("All",-1),
	MON("Monday",DateBuilder.MONDAY),
	TUE("Tuesday",DateBuilder.TUESDAY),
	WED("Wednesday",DateBuilder.WEDNESDAY),
	THU("Thursday",DateBuilder.THURSDAY),
	FRI("Friday",DateBuilder.FRIDAY),
	SAT("Saturday",DateBuilder.SATURDAY),
	SUN("Sunday",DateBuilder.SUNDAY);
	
	private String pretty;
	private int cron;
	
	Day(String pretty, int cron) {
		this.pretty = pretty;
		this.cron = cron;
	}
	
	public String getPretty() {
		return pretty;
	}
	public int getCron() {
		return cron;
	}
	
	
}
