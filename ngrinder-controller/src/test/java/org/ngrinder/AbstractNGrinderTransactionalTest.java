/* 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.ngrinder;

import net.grinder.AgentControllerDaemon;
import org.hyperic.jni.ArchLoaderException;
import org.junit.Before;
import org.ngrinder.common.constant.NGrinderConstants;
import org.ngrinder.infra.AgentConfig;
import org.ngrinder.infra.ArchLoaderInit;
import org.ngrinder.model.User;
import org.ngrinder.monitor.MonitorConstants;
import org.ngrinder.monitor.agent.AgentMonitorServer;
import org.ngrinder.user.repository.UserRepository;
import org.ngrinder.user.service.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

import javax.sql.DataSource;
import java.util.Set;


/**
 * This class is used as base class for test case,and it will initialize the DB
 * related config, like datasource, and it will start a transaction for every
 * test function, and rollback after the execution.
 *
 * @author Mavlarn
 */
@ContextConfiguration({"classpath:applicationContext-test.xml"})
abstract public class AbstractNGrinderTransactionalTest extends AbstractTransactionalJUnit4SpringContextTests implements
		NGrinderConstants {
	protected static final Logger LOG = LoggerFactory.getLogger(AbstractNGrinderTransactionalTest.class);

	@Autowired
	protected UserRepository userRepository;

	protected User testUser = null;

	static {

		System.setProperty("unit-test", "true");
		LOG.info("* Start nGrinder Agent *");

		AgentConfig agentConfig = new AgentConfig.NullAgentConfig(1).init();
		try {
			new ArchLoaderInit().init(agentConfig.getHome().getNativeDirectory());
		} catch (Exception e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		AgentControllerDaemon agentControllerDaemon = new AgentControllerDaemon();
		agentControllerDaemon.run();

		LOG.info("* Start nGrinder Monitor *");
		try {
			Set<String> collector = MonitorConstants.SYSTEM_DATA_COLLECTOR;
			AgentMonitorServer.getInstance().init(MonitorConstants.DEFAULT_MONITOR_PORT, collector, agentConfig);
			AgentMonitorServer.getInstance().start();
		} catch (Exception e) {
			LOG.error("ERROR: {}", e.getMessage());
			LOG.debug("Error while starting Monitor", e);
		}
	}

	@Before
	public void beforeSetSecurity() {
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("admin", null);
		SecurityContextImpl context = new SecurityContextImpl();
		context.setAuthentication(token);
		SecurityContextHolder.setContext(context);
	}

	@Autowired
	private UserContext userContext;

	@Autowired
	@Override
	public void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
		super.setDataSource(dataSource);
	}

	public User getUser(String userId) {
		return userRepository.findOneByUserId(userId);
	}

	public User getTestUser() {
		if (testUser == null) {
			testUser = userContext.getCurrentUser();
		}
		return testUser;
	}

	public User getAdminUser() {
		return userRepository.findOneByUserId("admin");
	}

	public void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			LOG.error("error:", e);
		}
	}

}
