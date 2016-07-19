/*
 * Copyright (C) 2016 Jens Reimann <jreimann@redhat.com> and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.camel.component.milo;

import org.apache.camel.EndpointInject;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.milo.server.MiloServerComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.Test;

/**
 * Unit tests for milo server component which require an actual connection
 */
public class MonitorItem2Test extends AbstractMiloServerTest {

	private static final String DIRECT_START_1 = "direct:start1";

	private static final String MILO_SERVER_ITEM_1 = "milo-server:myitem1";

	private static final String MILO_CLIENT_ITEM_C1_1 = "milo-client:tcp://foo:bar@localhost:12685?nodeId=items-myitem1&namespaceUri="
			+ MiloServerComponent.DEFAULT_NAMESPACE_URI;

	private static final String MOCK_TEST_1 = "mock:test1";

	@EndpointInject(uri = MOCK_TEST_1)
	protected MockEndpoint test1Endpoint;

	@Produce(uri = DIRECT_START_1)
	protected ProducerTemplate producer1;

	@Override
	protected RoutesBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(DIRECT_START_1).to(MILO_SERVER_ITEM_1);

				from(MILO_CLIENT_ITEM_C1_1).to(MOCK_TEST_1);
			}
		};
	}

	/**
	 * Monitor multiple events
	 */
	@Test
	public void testMonitorItem1() throws Exception {
		/*
		 * we will wait 2 * 1_000 milliseconds between server updates since the
		 * default server update rate is 1_000 milliseconds
		 */

		// set server values
		this.producer1.sendBody("Foo");
		Thread.sleep(2_000);
		this.producer1.sendBody("Bar");
		Thread.sleep(2_000);
		this.producer1.sendBody("Baz");
		Thread.sleep(2_000);

		// item 1 ... only this one receives
		this.test1Endpoint.setExpectedCount(3);

		// tests
		testBody(this.test1Endpoint.message(0), assertGoodValue("Foo"));
		testBody(this.test1Endpoint.message(1), assertGoodValue("Bar"));
		testBody(this.test1Endpoint.message(2), assertGoodValue("Baz"));

		// assert
		this.assertMockEndpointsSatisfied();
	}
}