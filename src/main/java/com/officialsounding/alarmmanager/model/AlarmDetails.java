package com.officialsounding.alarmmanager.model;

import org.joda.time.LocalTime;

public class AlarmDetails {

	public AlarmDetails(Day day, LocalTime time) {
		super();
		this.day = day;
		this.time = time;
	}
	private Day day;
	private LocalTime time;
	
	public Day getDay() {
		return day;
	}
	public void setDay(Day day) {
		this.day = day;
	}
	public LocalTime getTime() {
		return time;
	}
	public void setTime(LocalTime time) {
		this.time = time;
	}
	
	
}
