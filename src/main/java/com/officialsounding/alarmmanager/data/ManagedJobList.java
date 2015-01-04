package com.officialsounding.alarmmanager.data;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.officialsounding.alarmmanager.model.AlarmDetails;
import com.officialsounding.alarmmanager.model.JobList;
import com.yammer.dropwizard.lifecycle.Managed;

public class ManagedJobList implements Managed {

	private static final Logger log = LoggerFactory.getLogger(ManagedJobList.class);

	private JobList jobs;
	private File jobfile;
	private ObjectMapper mapper;

	public ManagedJobList(String jobFolder) {
		jobfile = new File(jobFolder,"jobs.json");
		mapper = new ObjectMapper();
		mapper.registerModule(new JodaModule());
	}


	@Override
	public void start() throws Exception {
		if(!jobfile.exists()) {
			log.info("job file doesn't exist yet");
			jobs = new JobList();
		} else {
			log.info("loading jobs from job file");
			jobs = mapper.readValue(jobfile,JobList.class);
		}

	}

	@Override
	public void stop() throws Exception {
		saveFile();
	}
	
	public JobList getJobs() {
		return jobs;
	}

    public void saveFile() {
        boolean skipNextState = jobs.isSkipnext();
        try {
            jobs.setSkipnext(false);    //never persist skip next
            if (!jobfile.exists()) {
                log.info("creating job file at {}", jobfile);
                jobfile.createNewFile();
            }
            log.info("persisting job list to filesystem");

            mapper.writeValue(jobfile, jobs);

        }catch(IOException e ){
            log.error("Failed to save job list",e);
        }finally {
            jobs.setSkipnext(skipNextState);
        }
    }
	
	public void addJob(AlarmDetails details) {
		jobs.addJob(details.getDay(), details.getTime());
        saveFile();
	}
	
	public void deleteJob(AlarmDetails details) {
		jobs.deleteJob(details.getDay(), details.getTime());
        saveFile();
	}
	
	public void setEnabled(boolean enabled) {
		jobs.setEnabled(enabled);
	}
	
	public void setFalloff(boolean falloff) {
		jobs.setFalloff(falloff);
	}

    public void setSkipnext(boolean skipNext) { jobs.setSkipnext(skipNext);}
}