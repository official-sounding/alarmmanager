package com.officialsounding.alarmmanager.quartz;

import java.io.File;
import java.io.IOException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmJob implements Job {

	private static final Logger log = LoggerFactory.getLogger(AlarmJob.class);
	@Override
	public void execute(JobExecutionContext ctx) throws JobExecutionException {
		
		String jobFolder = (String) ctx.getMergedJobDataMap().get("jobFolder");
		ProcessBuilder pb = new ProcessBuilder(jobFolder + File.separator + "pi_alarm.py");
		
		pb.directory(new File(jobFolder));
		
		try {
			log.info("Starting alarm");
			Process p = pb.start();
			p.waitFor();
		} catch (IOException | InterruptedException e) {
			log.error("error executing the alarm",e);
		} finally {
			log.info("Alarm Complete");
		}

	}

}
