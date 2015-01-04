package com.officialsounding.alarmmanager.quartz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
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

import com.officialsounding.alarmmanager.data.ManagedJobList;
import com.officialsounding.alarmmanager.model.AlarmDetails;
import com.officialsounding.alarmmanager.model.AlarmException;
import com.officialsounding.alarmmanager.model.Day;
import com.yammer.dropwizard.lifecycle.Managed;


import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static org.quartz.JobBuilder.*;
@Singleton
public class ManagedQuartz implements Managed {


	private QuartzSchedulerMonitor schedulerMonitor;
	private QuartzJobMonitor jobMonitor;

	
	private String jobFolder;
	private NextAlarmSkipper skipper;
	private ManagedJobList mjl;
    private Scheduler scheduler;


	private static final Logger log = LoggerFactory.getLogger(ManagedQuartz.class);

    @Inject
	public ManagedQuartz(SchedulerFactory sf, @Named("JobFolder") String jobFolder, ManagedJobList mjl) {
		try {
            scheduler = sf.getScheduler();
            schedulerMonitor = new QuartzSchedulerMonitor(); // Implements SchedulerListener
            scheduler.getListenerManager().addSchedulerListener(schedulerMonitor);
            jobMonitor = new QuartzJobMonitor(); // Implements JobListener
            scheduler.getListenerManager().addJobListener(jobMonitor);
            this.jobFolder = jobFolder;
            this.mjl = mjl;
        } catch(SchedulerException ex) {
            //ignored

        }
	}

	@Override
	public void start() throws Exception {
		scheduler.start();
		// Make our Job listener cover all scheduled jobs
		scheduler.getListenerManager().addJobListener(jobMonitor, EverythingMatcher.allJobs());	

		for(Day day: Day.values()) {
			for(LocalTime time: mjl.getJobs().getJobs().get(day)) {
				try {
				addAlarm(day,time);
				} catch(Exception e) {
					
				}
			}
		}
		
	}

	@Override
	public void stop() throws Exception {
		scheduler.getListenerManager().removeJobListener(jobMonitor.getName());
		scheduler.shutdown(true);
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
		job.getJobDataMap().put("jobList",mjl.getJobs());
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
				log.info("alarm added");
				AlarmDetails details = new AlarmDetails(day,time);
				mjl.addJob(details);
				return details;
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
					log.debug("job deleted successfully");
					mjl.deleteJob(new AlarmDetails(day,time));
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
			log.error("error getting quartz jobs",e);
		}

		return jobs;
	}

}
