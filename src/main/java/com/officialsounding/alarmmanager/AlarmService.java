package com.officialsounding.alarmmanager;

import java.io.IOException;

import com.officialsounding.alarmmanager.config.AlarmModule;
import dagger.ObjectGraph;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.officialsounding.alarmmanager.config.AlarmConfiguration;
import com.officialsounding.alarmmanager.data.ManagedJobList;
import com.officialsounding.alarmmanager.quartz.ManagedQuartz;
import com.officialsounding.alarmmanager.quartz.QuartzHealthCheck;
import com.officialsounding.alarmmanager.resources.AlarmResource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class AlarmService extends Service<AlarmConfiguration> {

	public static void main(String[] args) throws Exception {
		new AlarmService().run(args);
	}

	@Override
	public void initialize(Bootstrap<AlarmConfiguration> arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run(AlarmConfiguration config, Environment env) throws Exception {
		env.getObjectMapperFactory().registerModule(new JodaModule());
		env.getObjectMapperFactory().disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		env.getObjectMapperFactory().registerModule(new SimpleModule() {
			private static final long serialVersionUID = 3019866683654743744L;

			{
				addSerializer(LocalTime.class, new StdSerializer<LocalTime>(LocalTime.class) {

					@Override
					public void serialize(LocalTime value, JsonGenerator jgen,
							SerializerProvider provider) throws IOException,
							JsonGenerationException {
						jgen.writeString(frmt.print(value));

					}
				});
			}
		});

        ObjectGraph objectGraph = ObjectGraph.create(new AlarmModule(config));

        ManagedJobList mjl = objectGraph.get(ManagedJobList.class);
        ManagedQuartz qm = objectGraph.get(ManagedQuartz.class);

		env.manage(mjl);
		env.manage(qm);
		env.addHealthCheck(new QuartzHealthCheck(qm)); 
		env.addResource(objectGraph.get(AlarmResource.class));
	}

	private final DateTimeFormatter frmt = new DateTimeFormatterBuilder().appendHourOfDay(2).appendLiteral(':').appendMinuteOfHour(2).toFormatter();
}
