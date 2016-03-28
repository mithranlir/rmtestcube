package com.qfs.sandbox.cfg;

import com.qfs.logging.MessagesSandbox;
import com.qfs.monitoring.HealthCheckAgent;
import com.qfs.pivot.content.IActivePivotContentService;
import com.qfs.sandbox.RiskDataHelper;
import com.qfs.sandbox.RiskResultHelper;
import com.qfs.server.cfg.ActivePivotRestServicesConfig;
import com.qfs.server.cfg.ActivePivotWebSocketServicesConfig;
import com.qfs.store.IDatastore;
import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.cube.hierarchy.IBucketer;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.impl.DefaultTimeBucketer;
import com.quartetfs.biz.pivot.distribution.security.IDistributedSecurityManager;
import com.quartetfs.biz.pivot.impl.PeriodicActivePivotSchemaRebuilder;
import com.quartetfs.biz.pivot.monitoring.impl.JMXEnabler;
import com.quartetfs.biz.pivot.security.IContextValueManager;
import com.quartetfs.biz.pivot.security.impl.UserDetailsServiceWrapper;
import com.quartetfs.biz.pivot.spring.ActivePivotCxfServicesConfig;
import com.quartetfs.biz.pivot.spring.ActivePivotRemotingServicesConfig;
import com.quartetfs.biz.pivot.spring.ActivePivotServicesConfig;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.contributions.impl.ClasspathContributionProvider;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.tech.distribution.messenger.IDistributedMessenger;
import rmlib.ProgrammaticCube;
import rmlib.cubebuilder.CubeBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.security.provisioning.UserDetailsManager;
import rmlib.query.QueryUtils;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.quartetfs.fwk.types.impl.ExtendedPluginInjector.inject;

//import QueryUtils;

@PropertySource(value = "classpath:sandbox.properties")
@Configuration
@Import(value = {
		GenericActivePivotConfig.class,
		SourceConfig.class,

		ActivePivotServicesConfig.class,
		ActivePivotCxfServicesConfig.class,
		ActivePivotRemotingServicesConfig.class,
		SecurityConfig.class,
		ActivePivotRestServicesConfig.class,
		ActivePivotWebSocketServicesConfig.class
})
public class DynSandboxConfig {

	/** Logger **/
	protected static Logger LOGGER = MessagesSandbox.getLogger(DynSandboxConfig.class);

	/** Before anything else we statically initialize the Quartet FS Registry. */
	static {
		Registry.setContributionProvider(new ClasspathContributionProvider("com.qfs", "com.quartetfs"));
	}

	/** Spring environment, automatically wired */
	@Autowired
	protected Environment env;

	/** Application datastore, automatically wired */
	@Autowired
	protected IDatastore datastore;

	/**
	 * Create a time bucketer that buckets dates into time buckets
	 * @return the time bucketer that buckets dates into time buckets
	 */
	@Bean
	public IBucketer<Long> timeBucketer() {
		DefaultTimeBucketer bucketer = new DefaultTimeBucketer();
		return bucketer;
	}

	/**
	 *
	 * Initialize and start the ActivePivot Manager, after performing all the injections into the
	 * ActivePivot plug-ins.
	 *
	 * @param activePivotManager the {@link IActivePivotManager} to start
	 * @param contextValueManager the {@link IContextValueManager} to inject in some plug-ins
	 * @param userDetailsManager the {@link UserDetailsManager} to inject in some plug-ins
	 * @return void
	 * @throws Exception any exception that occurred during the manager's start up
	 */
	@Bean
	public Void startManager(
			IActivePivotManager activePivotManager,
			IContextValueManager contextValueManager,
			UserDetailsManager userDetailsManager) throws Exception {

		/* ********************************************************************** */
		/* Inject dependencies before the ActivePivot components are initialized. */
		/* ********************************************************************** */

		// Inject the distributed messenger with security services
		for (Object key : Registry.getExtendedPlugin(IDistributedMessenger.class).keys()) {
			inject(IDistributedMessenger.class, String.valueOf(key), contextValueManager);
		}

		// Inject the distributed security manager with security services
		final UserDetailsServiceWrapper userDetailsService = new UserDetailsServiceWrapper();
		userDetailsService.setUserDetailsService(userDetailsManager);
		for (Object key : Registry.getExtendedPlugin(IDistributedSecurityManager.class).keys()) {
			inject(IDistributedSecurityManager.class, String.valueOf(key), userDetailsService);
		}

		/* *********************************************** */
		/* Initialize the ActivePivot Manager and start it */
		/* *********************************************** */

		activePivotManager.init(null);
		activePivotManager.start();

		return null;
	}


	@Bean
	@DependsOn(value = "startManager")
	public Void insertDataInStoreDirectly(ProgrammaticCube testCube) throws QueryException, ParseException {

		if(!SourceConfig.loadCsv) {
			final String storeName = RiskDataHelper.TEST_RISK_STORE;
			final List<Map<String, Object>> riskEntriesMapList = RiskDataHelper.generateRiskEntriesAsMapList();
			testCube.insertTestDataInDatatoreAndCommit(storeName, riskEntriesMapList);

		}

		return null;
	}

	@Bean
	@DependsOn(value = { "csvLoad", "insertDataInStoreDirectly"})
	public Void checkResults(ProgrammaticCube testCube) throws QueryException, ParseException {

		RiskResultHelper.printHierarchies(testCube.getPivot().getId(), testCube.getManager());

		// QUERY CUBE (SIMPLE)
		QueryUtils.queryCubeSimple(CubeBuilder.TEST_CUBE, testCube.getManager(), 1);

		if(!SourceConfig.loadCsv) {
			// QUERY CUBE (TEST FWK)
			RiskResultHelper.queryCubeAndCheckResults(CubeBuilder.TEST_CUBE, testCube.getManager());
		}

		return null;
	}

	/**
	 * Enable JMX Monitoring for the Datastore
	 *
	 * @param activePivotManager the {@link IActivePivotManager} that contains the datastore
	 * @return the {@link JMXEnabler} attached to the datastore
	 */
	@Bean
	public JMXEnabler JMXDatastoreEnabler(IActivePivotManager activePivotManager) {
		return new JMXEnabler(activePivotManager.getDatastore());
	}

	/**
	 * Enable JMX Monitoring for ActivePivot Components
	 * @param activePivotManager the {@link IActivePivotManager} to monitor
	 * @return the {@link JMXEnabler} attached to the activePivotManager
	 */
	@Bean
	@DependsOn(value = "startManager")
	public JMXEnabler JMXActivePivotEnabler(IActivePivotManager activePivotManager) {
		return new JMXEnabler(activePivotManager);
	}

	@Bean(initMethod = "start", destroyMethod = "stop")
	public PeriodicActivePivotSchemaRebuilder rebuild(IActivePivotManager manager) {
		return new PeriodicActivePivotSchemaRebuilder()
				.setManager(manager)
				.setSchemaName(CubeBuilder.TEST_SCHEMA)
				.setPeriod(30, TimeUnit.MINUTES);
	}

	/**
	 * Health Check Agent bean
	 * @return the health check agent
	 */
	@Bean(initMethod = "start", destroyMethod = "interrupt")
	public HealthCheckAgent healthCheckAgent() {
		HealthCheckAgent agent = new HealthCheckAgent(60);  // One trace per minute
		return agent;
	}

	@Bean
	public IActivePivotContentService activePivotContentService() {
		return null;
	}

}
