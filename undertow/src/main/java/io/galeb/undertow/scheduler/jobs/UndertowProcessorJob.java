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

package io.galeb.undertow.scheduler.jobs;

import io.galeb.core.logging.Logger;
import io.galeb.undertow.model.FarmUndertow;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@DisallowConcurrentExecution
public class UndertowProcessorJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobDetailMap = context.getJobDetail().getJobDataMap();
        Logger logger = (Logger) jobDetailMap.getOrDefault("", null);
        FarmUndertow farmUndertow = (FarmUndertow) jobDetailMap.getOrDefault("", null);

        try {
            farmUndertow.processAll();
        } catch (Exception e) {
            logger.error(e);
        }
    }
}
