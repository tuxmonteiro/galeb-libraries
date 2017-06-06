/*
 * Copyright (c) 2014-2017 Globo.com - ATeam
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

import io.galeb.core.model.Entity;
import io.galeb.core.model.Rule;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.ServerConnection;
import io.undertow.server.handlers.ResponseCodeHandler;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class PathGlobHandlerTest {

    private final Log logger = LogFactory.getLog(this.getClass());

    @Test
    public void ruleTest() {
        Entity rule1 = new Rule().setId("x");
        rule1.putProperty(Rule.PROP_RULE_ORDER, 0);
        assertThat(rule1.compareTo(null), is(-1));

        Entity rule2 = new Rule().setId("x");
        rule2.putProperty(Rule.PROP_RULE_ORDER, 0);

        assertThat(rule1.equals(rule2), is(true));

        Entity rule3 = new Rule().setId("y");
        rule3.putProperty(Rule.PROP_RULE_ORDER, 0);

        assertThat(rule1.equals(rule3), is(false));
        assertThat(rule1.compareTo(rule3), is(-1));

        Entity rule4 = new Rule().setId("x");
        rule4.putProperty(Rule.PROP_RULE_ORDER, 1);

        assertThat(rule1.equals(rule4), is(true));
        assertThat(rule1.compareTo(rule4), is(0));
        assertThat(rule4.compareTo(rule1), is(0));

        Entity rule5 = new Rule().setId("y");
        rule5.putProperty(Rule.PROP_RULE_ORDER, 1);

        assertThat(rule5.equals(rule4), is(false));
        assertThat(rule1.compareTo(rule5), is(-1));
        assertThat(rule5.compareTo(rule1), is(1));
    }

    @Test
    public void addRemoveTest() {
        PathGlobHandler pathGlobHandler = new PathGlobHandler();
        Entity rule1 = new Rule().setId("x").putProperty(Rule.PROP_RULE_ORDER, 0);
        Entity rule2 = new Rule().setId("x").putProperty(Rule.PROP_RULE_ORDER, 0);
        Entity rule3 = new Rule().setId("y").putProperty(Rule.PROP_RULE_ORDER, 0);
        Entity rule4 = new Rule().setId("z").putProperty(Rule.PROP_RULE_ORDER, 1);
        Entity rule5 = new Rule().setId("w").putProperty(Rule.PROP_RULE_ORDER, 1);
        Entity rule6 = new Rule().setId("k").putProperty(Rule.PROP_RULE_ORDER, 0);

        pathGlobHandler.addRule((Rule) rule1, ResponseCodeHandler.HANDLE_200);
        pathGlobHandler.addRule((Rule) rule2, ResponseCodeHandler.HANDLE_200);
        pathGlobHandler.addRule((Rule) rule3, ResponseCodeHandler.HANDLE_200);
        pathGlobHandler.addRule((Rule) rule4, ResponseCodeHandler.HANDLE_200);
        pathGlobHandler.addRule((Rule) rule5, ResponseCodeHandler.HANDLE_200);

        assertThat(pathGlobHandler.contains((Rule) rule1), is(true));
        pathGlobHandler.removeRule((Rule) rule1);
        assertThat(pathGlobHandler.contains((Rule) rule1), is(false));
        assertThat(pathGlobHandler.contains((Rule) rule2), is(false));

        assertThat(pathGlobHandler.contains((Rule) rule4), is(true));
        pathGlobHandler.removeRule((Rule) rule4);
        assertThat(pathGlobHandler.contains((Rule) rule4), is(false));

        assertThat(pathGlobHandler.contains((Rule) rule3), is(true));
        pathGlobHandler.removeRule((Rule) rule3);
        assertThat(pathGlobHandler.contains((Rule) rule3), is(false));

        pathGlobHandler.removeRule((Rule) rule6);

        assertThat(pathGlobHandler.contains((Rule) rule5), is(true));
        pathGlobHandler.removeRule((Rule) rule5);
        assertThat(pathGlobHandler.contains((Rule) rule5), is(false));
    }

    @Test
    public void checkMatch() {
        final AtomicReference<String> result = new AtomicReference<>("default");

        HttpHandler defaultHandler = mock(HttpHandler.class);
        PathGlobHandler pathGlobHandler = new PathGlobHandler();
        pathGlobHandler.setDefaultHandler(defaultHandler);
        Entity rule1 = new Rule().setId("/x").putProperty(Rule.PROP_RULE_ORDER, 0).putProperty(Rule.PROP_MATCH, "/x");
        Entity rule2 = new Rule().setId("/y").putProperty(Rule.PROP_RULE_ORDER, 0).putProperty(Rule.PROP_MATCH, "/y");
        Entity rule3 = new Rule().setId("/z").putProperty(Rule.PROP_RULE_ORDER, 0).putProperty(Rule.PROP_MATCH, "/z");
        Entity rule4 = new Rule().setId("/w").putProperty(Rule.PROP_RULE_ORDER, 0).putProperty(Rule.PROP_MATCH, "/w");
        Entity rule5 = new Rule().setId("/1").putProperty(Rule.PROP_RULE_ORDER, 1).putProperty(Rule.PROP_MATCH, "/1");
        Entity rule6 = new Rule().setId("/2").putProperty(Rule.PROP_RULE_ORDER, 2).putProperty(Rule.PROP_MATCH, "/2");
        Entity rule7 = new Rule().setId("/3").putProperty(Rule.PROP_RULE_ORDER, 3).putProperty(Rule.PROP_MATCH, "/3");
        Entity rule8 = new Rule().setId("/4").putProperty(Rule.PROP_RULE_ORDER, 4).putProperty(Rule.PROP_MATCH, "/4");
        Entity rule9 = new Rule().setId("/5*").putProperty(Rule.PROP_RULE_ORDER, 4).putProperty(Rule.PROP_MATCH, "/5*");
        Entity rule10 = new Rule().setId("/6/*").putProperty(Rule.PROP_RULE_ORDER, Integer.MAX_VALUE - 1).putProperty(Rule.PROP_MATCH, "/6/*");
        Entity rule11 = new Rule().setId("/7/*.json").putProperty(Rule.PROP_RULE_ORDER, Integer.MAX_VALUE - 1).putProperty(Rule.PROP_MATCH, "/7/*.json");
        Entity rule12 = new Rule().setId("/").putProperty(Rule.PROP_RULE_ORDER, Integer.MAX_VALUE).putProperty(Rule.PROP_MATCH, "/");

        pathGlobHandler.addRule((Rule) rule1, exchange -> result.set("x"));
        pathGlobHandler.addRule((Rule) rule2, exchange -> result.set("y"));
        pathGlobHandler.addRule((Rule) rule3, exchange -> result.set("z"));
        pathGlobHandler.addRule((Rule) rule4, exchange -> result.set("w"));
        pathGlobHandler.addRule((Rule) rule5, exchange -> result.set("1"));
        pathGlobHandler.addRule((Rule) rule6, exchange -> result.set("2"));
        pathGlobHandler.addRule((Rule) rule7, exchange -> result.set("3"));
        pathGlobHandler.addRule((Rule) rule8, exchange -> result.set("4"));
        pathGlobHandler.addRule((Rule) rule9, exchange -> result.set("5"));
        pathGlobHandler.addRule((Rule) rule10, exchange -> result.set("6"));
        pathGlobHandler.addRule((Rule) rule11, exchange -> result.set("7"));
        pathGlobHandler.addRule((Rule) rule12, exchange -> result.set("slash"));

        ServerConnection serverConnection = mock(ServerConnection.class);
        try {
            HttpServerExchange exchangeNotMatch = new HttpServerExchange(serverConnection);
            exchangeNotMatch.setRelativePath(UUID.randomUUID().toString());
            HttpServerExchange exchangeX = new HttpServerExchange(serverConnection);
            exchangeX.setRelativePath("/x");
            HttpServerExchange exchangeY = new HttpServerExchange(serverConnection);
            exchangeY.setRelativePath("/y");
            HttpServerExchange exchangeZ = new HttpServerExchange(serverConnection);
            exchangeZ.setRelativePath("/z");
            HttpServerExchange exchangeW = new HttpServerExchange(serverConnection);
            exchangeW.setRelativePath("/w");
            HttpServerExchange exchange1 = new HttpServerExchange(serverConnection);
            exchange1.setRelativePath("/1");
            HttpServerExchange exchange2 = new HttpServerExchange(serverConnection);
            exchange2.setRelativePath("/2");
            HttpServerExchange exchange3 = new HttpServerExchange(serverConnection);
            exchange3.setRelativePath("/3");
            HttpServerExchange exchange4 = new HttpServerExchange(serverConnection);
            exchange4.setRelativePath("/4");
            HttpServerExchange exchange5 = new HttpServerExchange(serverConnection);
            exchange5.setRelativePath("/555");
            HttpServerExchange exchange6 = new HttpServerExchange(serverConnection);
            exchange6.setRelativePath("/6/xpto");
            HttpServerExchange exchange7 = new HttpServerExchange(serverConnection);
            exchange7.setRelativePath("/7/xpto/test.json");
            HttpServerExchange exchangeSlash = new HttpServerExchange(serverConnection);
            exchangeSlash.setRelativePath("/");

            pathGlobHandler.handleRequest(exchangeNotMatch);
            assertThat(result.get(), equalTo("default"));

            pathGlobHandler.handleRequest(exchangeX);
            assertThat(result.get(), equalTo("x"));

            pathGlobHandler.handleRequest(exchangeY);
            assertThat(result.get(), equalTo("y"));

            pathGlobHandler.handleRequest(exchangeZ);
            assertThat(result.get(), equalTo("z"));

            pathGlobHandler.handleRequest(exchangeW);
            assertThat(result.get(), equalTo("w"));

            pathGlobHandler.handleRequest(exchange1);
            assertThat(result.get(), equalTo("1"));

            pathGlobHandler.handleRequest(exchange2);
            assertThat(result.get(), equalTo("2"));

            pathGlobHandler.handleRequest(exchange3);
            assertThat(result.get(), equalTo("3"));

            pathGlobHandler.handleRequest(exchange4);
            assertThat(result.get(), equalTo("4"));

            pathGlobHandler.handleRequest(exchange2);
            assertThat(result.get(), equalTo("2"));

            pathGlobHandler.handleRequest(exchange1);
            assertThat(result.get(), equalTo("1"));

            pathGlobHandler.handleRequest(exchange5);
            assertThat(result.get(), equalTo("5"));

            pathGlobHandler.handleRequest(exchange6);
            assertThat(result.get(), equalTo("6"));

            pathGlobHandler.handleRequest(exchange7);
            assertThat(result.get(), equalTo("7"));

            pathGlobHandler.handleRequest(exchangeSlash);
            assertThat(result.get(), equalTo("slash"));

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
