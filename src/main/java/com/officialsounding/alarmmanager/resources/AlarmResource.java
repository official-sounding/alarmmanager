package com.officialsounding.alarmmanager.resources;


import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	Logger log = LoggerFactory.getLogger(AlarmResource.class);
	
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
	@Path("{day}/{time}")
	public Response deleteJob(@PathParam("day") String day, @PathParam("time") String time) {
		try {
			mq.deleteAlarm(Day.valueOf(day),  getTimeFromString(time));
			//should probably return a 205 response code here, but the Response object doesn't have that
			return Response.ok().build();
		}catch(AlarmException e) {
			return Response.noContent().build();
		} 	
			
	}
	
	@POST
	@Timed
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response addJob(@FormParam("day") String day, @FormParam("time") String time) {
		try {
			AlarmDetails details = mq.addAlarm(Day.valueOf(day), getTimeFromString(time));
			return Response.ok(details).build();
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
