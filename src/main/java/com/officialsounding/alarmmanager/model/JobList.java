package com.officialsounding.alarmmanager.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalTime;

public class JobList {

	private Map<Day,Set<LocalTime>> jobs;
	private boolean enabled;
	private boolean falloff;
	
	private boolean skipnext;
	
	
	public JobList() {
		jobs = new HashMap<>();
		for(Day d: Day.values()) {
			jobs.put(d,new HashSet<LocalTime>());
		}
		enabled = true;
		falloff = true;
		skipnext = false;
	}
	
	public Map<Day, Set<LocalTime>> getJobs() {
		return jobs;
	}
	public void setJobs(Map<Day, Set<LocalTime>> jobs) {
		this.jobs = jobs;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean isFalloff() {
		return falloff;
	}
	public void setFalloff(boolean falloff) {
		this.falloff = falloff;
	}
	public boolean isSkipnext() {
		return skipnext;
	}

	public void setSkipnext(boolean skipnext) {
		this.skipnext = skipnext;
	}
	
	public Day[] getDays() {
		return Day.values();
	}
	
	
	public boolean addJob(Day day, LocalTime time) {
		
		return jobs.get(day).add(time);
	}
	
	public boolean deleteJob(Day day, LocalTime time) {
	
		
		return jobs.get(day).remove(time);
	}
	
}
