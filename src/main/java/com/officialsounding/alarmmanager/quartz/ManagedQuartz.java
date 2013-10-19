package com.officialsounding.alarmmanager.quartz;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalTime;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.officialsounding.alarmmanager.model.AlarmDetails;
import com.officialsounding.alarmmanager.model.AlarmException;
import com.officialsounding.alarmmanager.model.Day;
import com.officialsounding.alarmmanager.model.JobList;
import com.yammer.dropwizard.lifecycle.Managed;


import static org.quartz.JobBuilder.*;

public class ManagedQuartz implements Managed {

	private Scheduler scheduler;
	private QuartzSchedulerMonitor schedulerMonitor;
	private QuartzJobMonitor jobMonitor;

	private JobList jobs;
	private String jobFolder;
	private File jobfile;


	private static final Logger log = LoggerFactory.getLogger(ManagedQuartz.class);

	public ManagedQuartz(SchedulerFactory sf,String jobFolder) throws SchedulerException {
		scheduler = sf.getScheduler();
		schedulerMonitor = new QuartzSchedulerMonitor(); // Implements SchedulerListener
		scheduler.getListenerManager().addSchedulerListener(schedulerMonitor);
		jobMonitor = new QuartzJobMonitor(); // Implements JobListener
		scheduler.getListenerManager().addJobListener(jobMonitor);

		this.jobFolder = jobFolder;
		jobfile = new File(jobFolder,"jobs.json");

	}

	@Override
	public void start() throws Exception {
		scheduler.start();
		// Make our Job listener cover all scheduled jobs
		scheduler.getListenerManager().addJobListener(jobMonitor, EverythingMatcher.allJobs());	

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JodaModule());


		if(!jobfile.exists()) {
			log.info("job file doesn't exist yet");
			jobs = new JobList();
		} else {
			log.info("loading jobs from job file");
			jobs = mapper.readValue(jobfile,JobList.class);
			for(Day day: Day.values()) {
				for(LocalTime time: jobs.getJobs().get(day)) {
					addAlarm(day,time);
				}
			}
		}

	}

	@Override
	public void stop() throws Exception {
		scheduler.getListenerManager().removeJobListener(jobMonitor.getName());
		scheduler.shutdown(true);

		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JodaModule());

		if(!jobfile.exists()) {
			log.info("creating job file at {}",jobfile);
			jobfile.createNewFile();
		}
		log.info("persisting job list to filesystem");
		mapper.writeValue(jobfile, jobs);
		
	}

	public boolean isHealthy(){
		return schedulerMonitor.isHealthy() && jobMonitor.isHealthy();
	}


	public String getState() {
		return "Scheduler: " + schedulerMonitor.getState() + " Jobs:" + jobMonitor.getState();
	}


	public AlarmDetails addAlarm(Day day, LocalTime time) throws AlarmException {
		JobDetail job = buildJob(day, time);
		log.info("adding alarm for {} @ {}",day.getPretty(),time);

		Trigger trigger;
		CronScheduleBuilder cron;

		if(day == Day.ALL) {
			cron = CronScheduleBuilder.dailyAtHourAndMinute(time.getHourOfDay(), time.getMinuteOfHour());

		} else {
			cron = CronScheduleBuilder.weeklyOnDayAndHourAndMinute(day.getCron(),time.getHourOfDay(), time.getMinuteOfHour());
		}

		trigger = TriggerBuilder.newTrigger()
				.withIdentity(time.toString(), day.toString())
				.withSchedule(cron)
				.build();

		try {
			if(!scheduler.checkExists(job.getKey())) {
				scheduler.scheduleJob(job,trigger);
				jobs.addJob(day, time);
				log.info("alarm added");
				return new AlarmDetails(day,time);
			} else {
				throw new AlarmException("job already exists");
			}
		}catch(SchedulerException e) {
			log.error("exception on job creation",e);
			throw new AlarmException("exception on job creation",e);
		}
	}

	public void deleteAlarm(Day day, LocalTime time) throws AlarmException{
		JobKey key = new JobKey(time.toString(),day.toString());
		log.debug("deleteing job for {} @ {}",day.getPretty(),time);

		try {
			if(scheduler.checkExists(key)) {
				if(scheduler.deleteJob(key)) {
					jobs.deleteJob(day, time);
					log.debug("job deleted successfully");
					return;
				} else {
					throw new AlarmException("job with key "+key+" was not deleted successfully");
				}
			} else {
				throw new AlarmException("job with key "+key+" did not exist");
			}
		} catch(SchedulerException e) {
			log.error("exception on job delete",e);
			throw new AlarmException("exception on job delete",e);
		}
	}

	private JobDetail buildJob(Day day, LocalTime time) {
		JobDetail job = newJob(AlarmJob.class)
				.withIdentity(time.toString(), day.toString())
				.usingJobData("jobFolder",jobFolder)
				.build();
		return job;
	}

	public JobList getJobs() {
		return jobs;
	}

	public Map<String,List<String>> getJobsFromScheduler()  {
		Map<String,List<String>> jobs = new HashMap<>();
		try {
				for(JobKey key: scheduler.getJobKeys(GroupMatcher.anyJobGroup())) {
					log.debug("found {} triggers for key {}",scheduler.getTriggersOfJob(key).size(),key);
							List<String> triggers = new ArrayList<String>();
							for(Trigger trig: scheduler.getTriggersOfJob(key)) {
								triggers.add(trig.getNextFireTime().toString());
							}
							jobs.put(key.toString(),triggers);
				}
			
		} catch (SchedulerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jobs;
	}

}
