package com.officialsounding.alarmmanager.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.validation.Valid;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

public class AlarmConfiguration extends Configuration {
	@Valid
	@JsonProperty
	private Map<String,String> quartzSettings = new HashMap<>();

	@NotEmpty
	@JsonProperty
	private String jobFolder;
	
	public Properties getSchedulerFactoryProperties(){
		Properties sfProps = new Properties();
		// Quartz checks for updates. This should be turned off for production systems.
		sfProps.setProperty("org.quartz.scheduler.skipUpdateCheck","true");

		// Quartz settings configured in YML file
		sfProps.setProperty("org.quartz.scheduler.instanceName", quartzSettings.get("instanceName"));
		sfProps.setProperty("org.quartz.threadPool.class", quartzSettings.get("threadPoolClass"));
		sfProps.setProperty("org.quartz.threadPool.threadCount", quartzSettings.get("threadCount"));
		sfProps.setProperty("org.quartz.threadPool.threadPriority", quartzSettings.get("threadPriority"));
		sfProps.setProperty("org.quartz.jobStore.class", quartzSettings.get("jobStoreClass"));

		return sfProps;
	}
	
	public String getJobFolder() {
		return jobFolder;
	}
}