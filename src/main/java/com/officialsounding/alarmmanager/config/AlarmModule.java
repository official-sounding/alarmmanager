package com.officialsounding.alarmmanager.config;

import com.officialsounding.alarmmanager.data.ManagedJobList;
import com.officialsounding.alarmmanager.quartz.ManagedQuartz;
import com.officialsounding.alarmmanager.resources.AlarmResource;
import dagger.Module;
import dagger.Provides;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import javax.inject.Named;
import javax.inject.Singleton;

@Module ( library = true, injects = {ManagedJobList.class, ManagedQuartz.class, AlarmResource.class} )
public class AlarmModule {

    private AlarmConfiguration config;

    public AlarmModule(AlarmConfiguration config) {
        this.config = config;
    }

    @Provides @Singleton
    @Named("JobFolder") String provideJobFolder() {
        return config.getJobFolder();
    }

    @Provides @Singleton
    SchedulerFactory providesSchedulerFactory() {
        try {
            return new StdSchedulerFactory(config.getSchedulerFactoryProperties());
        } catch (SchedulerException e) {
            return null;
        }
    }

    @Provides @Singleton ManagedJobList providesManagedJobList() {
        return new ManagedJobList(config.getJobFolder());
    }
}


