package com.officialsounding.alarmmanager.resources;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.joda.time.LocalTime;
import org.quartz.Trigger;

import com.officialsounding.alarmmanager.model.AlarmDetails;
import com.officialsounding.alarmmanager.model.AlarmException;
import com.officialsounding.alarmmanager.model.Day;
import com.officialsounding.alarmmanager.model.JobList;
import com.officialsounding.alarmmanager.quartz.ManagedQuartz;
import com.yammer.metrics.annotation.Timed;

@Path("/alarms")
@Produces(MediaType.APPLICATION_JSON)
public class AlarmResource {

	ManagedQuartz mq;
	
	public AlarmResource(ManagedQuartz mq) {
		this.mq = mq;
	}
	
	@GET
	@Timed
	public JobList getJobs() {
		return mq.getJobs();
	}
	
	@DELETE
	@Timed
	public Response deleteJob(@Valid AlarmDetails job) {
		try {
			mq.deleteAlarm(job.getDay(),  getTimeFromString(job.getTime()));
			return Response.ok().build();
		}catch(AlarmException e) {
			return Response.notModified().build();
		}
		
	}
	
	@POST
	@Timed
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response addJob(@FormParam("day") String day, @FormParam("time") String time) {
		try {
			
			mq.addAlarm(Day.valueOf(day), getTimeFromString(time));
			return Response.created(UriBuilder.fromPath("/").build()).build();
		}catch(AlarmException | NumberFormatException e) {
			return Response.notModified().build();
		}
	}
	
	@GET
	@Path("/quartz")
	public Map<String, List<String>> getQuartzJobs() {
		return mq.getJobsFromScheduler();
	}
	
	private LocalTime getTimeFromString(String timeString) throws AlarmException{
		if(timeString == null || timeString.isEmpty()) {
			throw new AlarmException("time string is null or empty");
		}
		String[] timeparts = timeString.split(":");
		if(timeparts.length != 2) {
			throw new AlarmException("time sent not in the proper format");
		}
		return new LocalTime(Integer.parseInt(timeparts[0]),Integer.parseInt(timeparts[1]));
	}
}
