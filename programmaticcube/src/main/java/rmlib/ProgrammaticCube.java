package rmlib;

import com.qfs.desc.IDatastoreSchemaDescription;
import com.qfs.msg.IMessageChannel;
import com.qfs.store.IDatastore;
import com.qfs.store.impl.Datastore;
import com.qfs.store.transaction.ITransactionalWriter;
import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.definitions.IActivePivotManagerDescription;
import com.quartetfs.biz.pivot.query.IGetAggregatesQuery;
import com.quartetfs.biz.pivot.query.IVersionedContinuousQueryUpdate;
import com.quartetfs.fwk.query.IContinuousQuery;
import com.quartetfs.fwk.query.IContinuousQueryListener;
import com.quartetfs.fwk.query.QueryException;
import rmlib.channel.ChannelFeedHelper;
import rmlib.channel.DefaultValueService;
import rmlib.cubebuilder.CubeBuilder;
import rmlib.manager.ActivePivotManagerWrapper;
import rmlib.transaction.TransactionHelper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ProgrammaticCube {

    private final IMultiVersionActivePivot pivot;
    private final IActivePivotManager manager;
    private final Map<String, IMessageChannel<String, Object>> channelMap;
    private final AtomicInteger queryIdGenerator = new AtomicInteger();
    private final Map<String, CubeBuilder.StoreFields> storeFieldTypeMap;
    private final DefaultValueService defaultValueService;
    private final IDatastoreSchemaDescription datastoreSchemaDescription;
    private final IActivePivotManagerDescription activePivotManagerDescription;
    private final IDatastore datastore;
    private final boolean resetable;

    public ProgrammaticCube(IActivePivotManager manager,
                            IMultiVersionActivePivot pivot,
                            Map<String, IMessageChannel<String, Object>> channelMap,
                            Map<String, CubeBuilder.StoreFields> storeFieldTypeMap,
                            DefaultValueService defaultValueService,
                            IDatastoreSchemaDescription datastoreSchemaDescription,
                            IActivePivotManagerDescription activePivotManagerDescription,
                            IDatastore datastore,
                            boolean resetable) {
        this.manager = manager;
        this.pivot = pivot;
        this.channelMap = channelMap;
        this.storeFieldTypeMap = storeFieldTypeMap;
        this.defaultValueService = defaultValueService;
        this.datastoreSchemaDescription = datastoreSchemaDescription;
        this.activePivotManagerDescription = activePivotManagerDescription;
        this.datastore = datastore;
        this.resetable = resetable;
    }

    public IMultiVersionActivePivot getPivot() {
        return pivot;
    }

    public IActivePivotManager getManager() {
        return manager;
    }

    public ActivePivotManagerWrapper getManagerAsWrapper() {
        return manager instanceof ActivePivotManagerWrapper ? (ActivePivotManagerWrapper) manager : null;
    }

    public Map<String, IMessageChannel<String, Object>> getChannelMap() {
        return channelMap;
    }

    public void insertTestDataInDatatoreAndCommit(String storeName, final List<Map<String, Object>> mapList) {

        final ITransactionalWriter transactionalWriter = datastore.getTransactionManager();
        final Datastore datastore = (Datastore) getManager().getDatastore();

        TransactionHelper.startTransaction(datastore);

        final List<Map<String, Object>> newList =
                ChannelFeedHelper.completeMissingFieldsInMapList(
                        mapList, null, defaultValueService);

       final CubeBuilder.StoreFields storeFields = storeFieldTypeMap.get(storeName);

        for(Map<String, Object> map : newList) {
            final Object[] tuple = new Object[storeFields.getFieldNames().size()];
            for(int i=0; i<storeFields.getFieldNames().size(); i++) {
                final String key = storeFields.getFieldNames().get(i);
                tuple[i] = map.get(key);
            }
            transactionalWriter.add(storeName, tuple);
        }

        TransactionHelper.commitTransaction(datastore);
    }

    public void insertDataInChannelWithDtoListAndCommit(ChannelData channelData) {
        final Datastore datastore = (Datastore) manager.getDatastore();
        TransactionHelper.startTransaction(datastore);
        ChannelFeedHelper.feedChannel(channelData.getData(), channelData.getChannel());
        TransactionHelper.commitTransaction(datastore);
    }

    public void insertTestDataInChannelWithMapListAndCommit(ChannelData channelData) {
        final Datastore datastore = (Datastore) manager.getDatastore();
        TransactionHelper.startTransaction(datastore);
        final CubeBuilder.StoreFields storeFields = storeFieldTypeMap.get(channelData.getStoreName());
        ChannelFeedHelper.feedChannelWithMapList(channelData.getDataAsMapList(), storeFields, defaultValueService, channelData.getChannel());
        TransactionHelper.commitTransaction(datastore);
    }

    public void registerContinuousQueryListener(IGetAggregatesQuery aggregatesQuery,
                                                IContinuousQueryListener<ICellSet, IVersionedContinuousQueryUpdate<ICellSet>> queryListener) throws QueryException {

        final IContinuousQuery<ICellSet, IVersionedContinuousQueryUpdate<ICellSet>> continuousQuery =
                pivot.registerContinuousQuery(
                        "subject-" + queryIdGenerator.incrementAndGet(),
                        aggregatesQuery,
                        null,
                        null);

        continuousQuery.addListener(queryListener);
    }


    public IDatastoreSchemaDescription getDatastoreSchemaDescription() {
        return datastoreSchemaDescription;
    }

    public IActivePivotManagerDescription getActivePivotManagerDescription() {
        return activePivotManagerDescription;
    }

    public IDatastore getDatastore() {
        return datastore;
    }

    public Map<String, CubeBuilder.StoreFields> getStoreFieldTypeMap() {
        return storeFieldTypeMap;
    }

    public boolean isResetable() {
        return resetable;
    }

}
