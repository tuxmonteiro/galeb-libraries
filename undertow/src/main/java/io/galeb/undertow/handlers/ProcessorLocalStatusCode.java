/*
 * Copyright (c) 2014-2016 Globo.com - ATeam
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

package io.galeb.undertow.handlers;

import org.apache.http.HttpStatus;

interface ProcessorLocalStatusCode {

    int OFFSET_LOCAL_ERROR = 400;
    int NOT_MODIFIED       = 0;

    default int getFakeStatusCode(final String backend,
                                  int statusCode,
                                  long responseBytesSent,
                                  Integer responseTime,
                                  int maxRequestTime,
                                  boolean forceChangeStatus) {
        int statusLogged = NOT_MODIFIED;
        if (responseTime != null &&
                forceChangeStatus &&
                statusCode == HttpStatus.SC_OK &&
                responseBytesSent <= 0 &&
                responseTime > maxRequestTime) {

            statusLogged = HttpStatus.SC_GATEWAY_TIMEOUT + OFFSET_LOCAL_ERROR;
        } else if (statusCode >= HttpStatus.SC_INTERNAL_SERVER_ERROR && backend == null) {
            statusLogged = statusCode + OFFSET_LOCAL_ERROR;
        }
        return statusLogged;
    }
}
