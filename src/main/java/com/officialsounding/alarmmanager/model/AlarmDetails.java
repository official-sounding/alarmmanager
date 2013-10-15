package com.officialsounding.alarmmanager.model;

import org.joda.time.LocalTime;

public class AlarmDetails {

	private Day day;
	private String time;
	public Day getDay() {
		return day;
	}
	public void setDay(Day day) {
		this.day = day;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	
	
}
