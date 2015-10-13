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

package io.galeb.core.services;

import io.galeb.core.logging.Logger;
import io.galeb.core.model.Farm;

public interface ProcessorScheduler  {

    String PROP_PROCESSOR_INTERVAL = ProcessorScheduler.class.getName() + ".ProcessorInterval";

    void setupScheduler(Logger logger, Farm farm);
    void startProcessorJob();

}
