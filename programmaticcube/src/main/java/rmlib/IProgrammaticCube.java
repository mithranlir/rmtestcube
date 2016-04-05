package rmlib;

import com.qfs.desc.IDatastoreSchemaDescription;
import com.qfs.msg.IMessageChannel;
import com.qfs.store.IDatastore;
import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.definitions.IActivePivotManagerDescription;
import com.quartetfs.biz.pivot.query.IGetAggregatesQuery;
import com.quartetfs.biz.pivot.query.IVersionedContinuousQueryUpdate;
import com.quartetfs.fwk.query.IContinuousQueryListener;
import com.quartetfs.fwk.query.QueryException;
import rmlib.cubebuilder.CubeBuilder;
import rmlib.manager.ActivePivotManagerWrapper;

import java.util.List;
import java.util.Map;

public interface IProgrammaticCube {

    IMultiVersionActivePivot getPivot();

    IActivePivotManager getManager();

    Map<String, IMessageChannel<String, Object>> getChannelMap();

    void insertTestDataInDatatoreAndCommit(String storeName, List<Map<String, Object>> mapList);

    void insertDataInChannelWithDtoListAndCommit(ChannelData channelData);

    void insertTestDataInChannelWithMapListAndCommit(ChannelData channelData);

    void registerContinuousQueryListener(IGetAggregatesQuery aggregatesQuery,
                                         IContinuousQueryListener<ICellSet, IVersionedContinuousQueryUpdate<ICellSet>> queryListener) throws QueryException;

    IDatastoreSchemaDescription getDatastoreSchemaDescription();

    IActivePivotManagerDescription getActivePivotManagerDescription();

    IDatastore getDatastore();

    Map<String, CubeBuilder.StoreFields> getStoreFieldTypeMap();

    boolean isManagerResetable();

    void purgeDatastore();
}
