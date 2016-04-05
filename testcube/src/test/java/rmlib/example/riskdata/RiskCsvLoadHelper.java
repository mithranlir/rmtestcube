package rmlib.example.riskdata;

import com.qfs.condition.impl.BaseConditions;
import com.qfs.msg.IColumnCalculator;
import com.qfs.msg.csv.ICSVSourceConfiguration;
import com.qfs.msg.csv.ICSVTopic;
import com.qfs.msg.csv.IFileInfo;
import com.qfs.msg.csv.ILineReader;
import com.qfs.msg.csv.impl.CSVSource;
import com.qfs.msg.impl.EmptyCalculator;
import com.qfs.source.impl.CSVMessageChannelFactory;
import com.qfs.source.impl.Fetch;
import com.qfs.store.IDatastore;
import com.qfs.store.IDatastoreSchemaMetadata;
import com.qfs.store.impl.StoreUtils;
import com.qfs.store.log.impl.LogWriteException;
import com.qfs.store.selection.impl.Selection;
import com.qfs.store.transaction.DatastoreTransactionException;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class RiskCsvLoadHelper {

    public final static String RISK_ENTRIES_STORE_FILE = "riskentries.csv";
    public final static char CSV_SEPARATOR = '|';

    public static final String RISK_STORE = RiskDataHelper.TEST_RISK_STORE;
    public static final String RISK_TOPIC = RiskDataHelper.TEST_RISK_STORE;

    public static void loadCsv(IDatastore datastore) throws LogWriteException {

        final String riskTopicName = RISK_TOPIC;
        final String riskStoreName = RISK_STORE;

        final CSVSource csvSource = new CSVSource();
        final Properties sourceProps = new Properties();
        final String parserThreads = "4";
        sourceProps.setProperty(ICSVSourceConfiguration.PARSER_THREAD_PROPERTY, parserThreads);
        csvSource.configure(sourceProps);

        final String filePath = "data" + System.getProperty("file.separator");
        final List<String> riskFileColumns = Arrays.asList(DatastoreConsts.RISK__TRADE_ID, DatastoreConsts.RISK__AS_OF_DATE);
        final ICSVTopic riskTopic =
                csvSource.createTopic(
                        riskTopicName,
                        filePath + RISK_ENTRIES_STORE_FILE,
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

        System.out.println("CSV data load completed in " + elapsed / 1_000_000L + "ms.");
    }

    public static void registerUpdateWhereTriggers(IDatastore datastore) throws DatastoreTransactionException {

        IDatastoreSchemaMetadata datastoreSchemaMetadata = datastore.getSchemaMetadata();

        int pnlIndex = StoreUtils.getFieldIndex(datastoreSchemaMetadata, RISK_STORE, DatastoreConsts.RISK__PNL);

        datastore.getTransactionManager().registerUpdateWhereTrigger(
                "Risk Calculator Trigger",
                0,
                new Selection(RISK_STORE,  DatastoreConsts.RISK__PNL),
                BaseConditions.TRUE, // We want to match all facts
                new RiskCalculator(pnlIndex)
        );

    }

    public static void unregisterUpdateWhereTriggers(IDatastore datastore) throws DatastoreTransactionException {
        datastore.getTransactionManager().unregisterUpdateWhereTrigger(RISK_STORE, "Risk Calculator Trigger");
    }
}
