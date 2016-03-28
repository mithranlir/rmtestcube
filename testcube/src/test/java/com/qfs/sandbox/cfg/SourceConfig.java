/*
 * (C) Quartet FS 2013-2015
 * ALL RIGHTS RESERVED. This material is the CONFIDENTIAL and PROPRIETARY
 * property of Quartet Financial Systems Limited. Any unauthorized use,
 * reproduction or transfer of this material is strictly prohibited
 */
package com.qfs.sandbox.cfg;

import com.qfs.condition.impl.BaseConditions;
import com.qfs.logging.MessagesSandbox;
import com.qfs.msg.IColumnCalculator;
import com.qfs.msg.csv.ICSVSourceConfiguration;
import com.qfs.msg.csv.ICSVTopic;
import com.qfs.msg.csv.IFileInfo;
import com.qfs.msg.csv.ILineReader;
import com.qfs.msg.csv.impl.CSVSource;
import com.qfs.msg.impl.EmptyCalculator;
import com.qfs.sandbox.RiskDataHelper;
import com.qfs.sandbox.source.impl.RiskCalculator;
import com.qfs.source.impl.CSVMessageChannelFactory;
import com.qfs.source.impl.Fetch;
import com.qfs.store.IDatastore;
import com.qfs.store.IDatastoreSchemaMetadata;
import com.qfs.store.impl.StoreUtils;
import com.qfs.store.log.impl.LogWriteException;
import com.qfs.store.selection.impl.Selection;
import com.qfs.store.transaction.DatastoreTransactionException;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.contributions.impl.ClasspathContributionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import static com.qfs.sandbox.cfg.DatastoreConsts.*;


@PropertySource(value="classpath:sandbox.properties")
@Configuration
public class SourceConfig {

	protected static Logger LOGGER = MessagesSandbox.getLogger(SourceConfig.class);

	static {
		Registry.setContributionProvider(new ClasspathContributionProvider("com.qfs", "com.quartetfs"));
	}

	@Autowired
	protected Environment env;

	@Autowired
	protected IDatastore datastore;

	public final static String RISKENTRIES_STORE_FILE = "riskentries.csv";
	public final static char CSV_SEPARATOR = '|';

	public static final String RISK_STORE = RiskDataHelper.TEST_RISK_STORE;
	public static final String RISK_TOPIC = RiskDataHelper.TEST_RISK_STORE;

	public static boolean loadCsv = false;

	@Bean
	@DependsOn({"startManager", "registerUpdateWhereTriggers"})
	public Void csvLoad() throws LogWriteException {

		if(loadCsv) {

			final String riskTopicName = RISK_TOPIC;
			final String riskStoreName = RISK_STORE;

			final CSVSource csvSource = new CSVSource();
			final Properties sourceProps = new Properties();
			final String parserThreads = env.getProperty("csvSource.parserThreads", "4");
			sourceProps.setProperty(ICSVSourceConfiguration.PARSER_THREAD_PROPERTY, parserThreads);
			csvSource.configure(sourceProps);

			final String filePath = env.getProperty("csvSource.dataset") + System.getProperty("file.separator");
			final List<String> riskFileColumns = Arrays.asList(RISK__TRADE_ID, RISK__AS_OF_DATE);
			final ICSVTopic riskTopic =
					csvSource.createTopic(
							riskTopicName,
							filePath + RISKENTRIES_STORE_FILE,
							csvSource.createParserConfiguration(riskFileColumns));
			riskTopic.getParserConfiguration().setSeparator(CSV_SEPARATOR);
			csvSource.addTopic(riskTopic);

			final CSVMessageChannelFactory channelFactory =
					new CSVMessageChannelFactory(csvSource, datastore);
			channelFactory.setCalculatedColumns(
					riskTopicName,
					riskStoreName,
					Arrays.<IColumnCalculator<ILineReader>>asList(
							new EmptyCalculator<ILineReader>(DatastoreConsts.RISK__PNL) // because calculated in trigger...
					)
			);

			final Collection<String> stores = datastore.getSchemaMetadata().getStoreNames();
			final Fetch<IFileInfo, ILineReader> fetch = new Fetch<>(channelFactory, stores);
			final long before = System.nanoTime();
			fetch.fetch(csvSource);
			final long elapsed = System.nanoTime() - before;
			LOGGER.info("CSV data load completed in " + elapsed / 1_000_000L + "ms.");
		}

		return null;
	}

	@Bean
	public Void registerUpdateWhereTriggers(IDatastore datastore) throws DatastoreTransactionException {

		IDatastoreSchemaMetadata datastoreSchemaMetadata = datastore.getSchemaMetadata();

		int pnlIndex = StoreUtils.getFieldIndex(datastoreSchemaMetadata, RISK_STORE, RISK__PNL);

		datastore.getTransactionManager().registerUpdateWhereTrigger(
				"Risk Calculator Trigger",
				0,
				new Selection(RISK_STORE,  DatastoreConsts.RISK__PNL),
				BaseConditions.TRUE, // We want to match all facts
				new RiskCalculator(pnlIndex)
		);

		return null;
	}

}