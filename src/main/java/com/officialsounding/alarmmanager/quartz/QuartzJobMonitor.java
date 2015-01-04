package com.officialsounding.alarmmanager.quartz;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

public class QuartzJobMonitor implements JobListener {

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Managed Quartz Job Monitor";
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void jobToBeExecuted(JobExecutionContext arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void jobWasExecuted(JobExecutionContext arg0,
			JobExecutionException arg1) {
		// TODO Auto-generated method stub

	}

	public boolean isHealthy() {
		// TODO Auto-generated method stub
		return true;
	}

	public String getState() {
		// TODO Auto-generated method stub
		return null;
	}

}
