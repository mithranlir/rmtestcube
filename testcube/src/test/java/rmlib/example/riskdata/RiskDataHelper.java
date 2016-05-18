package rmlib.example.riskdata;

import com.qfs.msg.IColumnCalculator;
import com.qfs.msg.IMessageChannel;
import com.qfs.msg.csv.ILineReader;
import com.qfs.msg.csv.translator.impl.AColumnCalculator;
import com.qfs.store.impl.Datastore;
import rmlib.channel.ChannelFeedHelper;
import rmlib.channel.DefaultValueService;
import rmlib.cubebuilder.CubeBuilder;
import rmlib.transaction.TransactionHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;


public class RiskDataHelper {

    public static final String TEST_RISK_STORE = "RiskStore";
    public static final String TOPIC_RISK = "Risk";

    public static void populateRiskChannelWithTestDataUsingDtoList(
                                                IMessageChannel<String, Object> riskChannel,
                                                Datastore datastore) {

        TransactionHelper.startTransaction(datastore);

        final List<RiskEntry> riskEntryList = generateRiskEntriesAsDTOList();

        ChannelFeedHelper.feedChannel(
                riskEntryList,
                riskChannel);

        TransactionHelper.commitTransaction(datastore);
    }

    public static void populateRiskChannelWithTestDataUsingMapList(
            IMessageChannel<String, Object> riskChannel,
            Datastore datastore,
            CubeBuilder.StoreFields storeFields,
            DefaultValueService defaultValueService) {

        TransactionHelper.startTransaction(datastore);

        final List<Map<String, Object>> riskEntryMapList = generateRiskEntriesAsMapList();

        ChannelFeedHelper.feedChannelWithMapList(
                riskEntryMapList,
                storeFields,
                defaultValueService,
                riskChannel);

        TransactionHelper.commitTransaction(datastore);
    }

    public static List<RiskEntry> generateRiskEntriesAsDTOList() {
        final List<RiskEntry> riskEntryList = new ArrayList<>();
        final int elmCount = 10; //number of generated objects
        final RiskEntryGenerator riskEntryGenerator = new RiskEntryGenerator(4);
        for (int i = 0; i < elmCount; i++) {
            final List<RiskEntry> riskEntries = riskEntryGenerator.generateRiskEntries(1L);
            riskEntryList.addAll(riskEntries);
        }
        return riskEntryList;
    }


    public static List<Map<String, Object>> generateRiskEntriesAsMapList() {
        final List<Map<String, Object>> riskEntryMapList = new ArrayList<>();
        final int elmCount = 10; //number of generated objects
        final AtomicLong keyGenerator = new AtomicLong();
        final RiskEntryGenerator riskEntryGenerator = new RiskEntryGenerator(4);
        for (int i = 0; i < elmCount; i++) {
            final List<RiskEntry> riskEntries = riskEntryGenerator.generateRiskEntries(1L);
            riskEntryMapList.addAll(convertRiskListToMapList(riskEntries, keyGenerator));
        }
        return riskEntryMapList;
    }

    private static List<Map<String, Object>> convertRiskListToMapList(List<RiskEntry> riskEntryList, AtomicLong keyGenerator) {
        final List<Map<String, Object>> list = new ArrayList<>();
        for(RiskEntry riskEntry : riskEntryList) {
            final Map<String, Object> riskMap = new HashMap<>();
            riskMap.put(DatastoreConsts.RISK__FACT_ID, keyGenerator.incrementAndGet());
            riskMap.put(DatastoreConsts.RISK__AS_OF_DATE, riskEntry.getAsOfDate());
            riskMap.put("HostName", riskEntry.getHostName());
            riskMap.put(DatastoreConsts.RISK__TRADE_ID, riskEntry.getTradeId());
            list.add(riskMap);
        }
        return list;
    }
}
