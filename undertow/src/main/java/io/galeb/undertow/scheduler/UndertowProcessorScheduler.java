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

import io.galeb.core.model.Farm;
import io.galeb.core.services.AbstractService;
import io.galeb.core.services.ProcessorScheduler;
import io.galeb.undertow.model.FarmUndertow;
import io.galeb.undertow.scheduler.jobs.UndertowProcessorJob;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import javax.enterprise.inject.Default;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

@Default
public class UndertowProcessorScheduler implements JobListener, ProcessorScheduler {

    private static final Logger LOGGER = LogManager.getLogger();

    private FarmUndertow farmUndertow;
    private Scheduler scheduler;

    @Override
    public void setupScheduler(final Farm farm) {
        try {
            if (farm instanceof FarmUndertow) {
                this.farmUndertow = (FarmUndertow) farm;
            } else {
                throw new RuntimeException("Farm isnt instance of FarmUndertow");
            }
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.getListenerManager().addJobListener(this);
            scheduler.start();
        } catch (SchedulerException e) {
            LOGGER.error(e);
        }
    }

    @Override
    public void startProcessorJob() {
        try {
            if (scheduler.isStarted() && farmUndertow != null) {

                int interval = Integer.parseInt(System.getProperty(PROP_PROCESSOR_INTERVAL, "1"));
                Trigger trigger = newTrigger().withIdentity(this.getName())
                        .startNow()
                        .withSchedule(simpleSchedule().withIntervalInSeconds(interval).repeatForever())
                        .build();

                JobDataMap jobdataMap = new JobDataMap();
                jobdataMap.put(AbstractService.FARM_KEY, farmUndertow);

                JobDetail jobDetail = newJob(UndertowProcessorJob.class).withIdentity(UndertowProcessorJob.class.getName())
                        .setJobData(jobdataMap)
                        .build();

                scheduler.scheduleJob(jobDetail, trigger);

            } else {
                String erroMsg = "FarmUndertow is NULL";
                LOGGER.error(erroMsg);
            }
        } catch (SchedulerException e) {
            LOGGER.error(e);
        }
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        LOGGER.debug(context.getJobDetail().getKey().getName()+" to be executed");
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        LOGGER.debug(context.getJobDetail().getKey().getName()+" vetoed");
    }

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        LOGGER.debug(context.getJobDetail().getKey().getName()+" was executed");
    }

}
