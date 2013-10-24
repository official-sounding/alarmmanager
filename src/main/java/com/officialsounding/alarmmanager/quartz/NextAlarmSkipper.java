package com.officialsounding.alarmmanager.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.officialsounding.alarmmanager.data.ManagedJobList;

public class NextAlarmSkipper implements TriggerListener {

	private ManagedJobList mjl;
	
	private static final Logger log = LoggerFactory.getLogger(NextAlarmSkipper.class);
	
	public NextAlarmSkipper(ManagedJobList mjl) {
		this.mjl = mjl;
	}

	@Override
	public String getName() {
		
		return "nextAlarmSkipper";
	}

	@Override
	public void triggerComplete(Trigger arg0, JobExecutionContext arg1,
			CompletedExecutionInstruction arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void triggerFired(Trigger arg0, JobExecutionContext arg1) {
		// TODO Auto-generated method stub

	}

	@Override
	public void triggerMisfired(Trigger arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean vetoJobExecution(Trigger trigger, JobExecutionContext ctx) {
		if(mjl.getJobs().isSkipnext()) {
			log.info("skipping job execution of alarm with trigger named {}",trigger.getJobKey());
			mjl.setSkipnext(false);
			return true;
		}
		return false;
	}
}
