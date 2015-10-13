/*
 * Copyright (c) 2014-2015 Globo.com
 * All rights reserved.
 *
 * This source is subject to the Apache License, Version 2.0.
 * Please see the LICENSE file for more information.
 *
 * Authors: See AUTHORS file
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.galeb.undertow.scheduler;

import io.galeb.core.logging.*;
import io.galeb.core.services.*;
import io.galeb.undertow.model.*;
import io.galeb.undertow.scheduler.jobs.*;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.inject.Inject;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Default
public class UndertowProcessorScheduler implements JobListener {

    private static final String PROP_UNDERTOW_PROCESSOR_INTERVAL = "undertowProcessorInterval";

    @Inject
    public Logger logger;

    @Inject
    private FarmUndertow farmUndertow;

    private Scheduler scheduler;

    @PostConstruct
    public void init() {
        setupScheduler();
        startUndertowProcessorJob();
    }

    private void setupScheduler() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.getListenerManager().addJobListener(this);
            scheduler.start();
        } catch (SchedulerException e) {
            logger.error(e);
        }
    }

    public void startUndertowProcessorJob() {
        try {
            if (scheduler.isStarted() && farmUndertow != null) {

                int interval = Integer.parseInt(System.getProperty(PROP_UNDERTOW_PROCESSOR_INTERVAL, "10"));
                Trigger trigger = newTrigger().withIdentity(this.getName())
                        .startNow()
                        .withSchedule(simpleSchedule().withIntervalInSeconds(interval).repeatForever())
                        .build();

                JobDataMap jobdataMap = new JobDataMap();
                jobdataMap.put(AbstractService.FARM, farmUndertow);
                jobdataMap.put(AbstractService.LOGGER, logger);

                JobDetail jobDetail = newJob(UndertowProcessorJob.class).withIdentity(UndertowProcessorJob.class.getName())
                        .setJobData(jobdataMap)
                        .build();

                scheduler.scheduleJob(jobDetail, trigger);

            } else {
                String erroMsg = "FarmUndertow is NULL";
                if (logger != null) {
                    logger.error(erroMsg);
                } else {
                    System.err.println(erroMsg);
                }
            }
        } catch (SchedulerException e) {
            logger.error(e);
        }
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        logger.debug(context.getJobDetail().getKey().getName()+" to be executed");
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        logger.debug(context.getJobDetail().getKey().getName()+" vetoed");
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        logger.debug(context.getJobDetail().getKey().getName()+" was executed");
    }

}
