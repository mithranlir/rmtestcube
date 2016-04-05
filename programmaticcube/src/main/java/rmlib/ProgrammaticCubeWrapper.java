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
import com.quartetfs.fwk.AgentException;
import com.quartetfs.fwk.query.IContinuousQueryListener;
import com.quartetfs.fwk.query.QueryException;
import rmlib.cubebuilder.CubeBuilder;
import rmlib.manager.ActivePivotManagerWrapper;

import java.util.List;
import java.util.Map;

public class ProgrammaticCubeWrapper implements IProgrammaticCube {

    private IProgrammaticCube programmaticCube;
    private ActivePivotManagerWrapper activePivotManagerWrapper;

    public ProgrammaticCubeWrapper(IProgrammaticCube programmaticCube) throws AgentException {
        this.programmaticCube = programmaticCube;
        this.activePivotManagerWrapper = new ActivePivotManagerWrapper();
        this.activePivotManagerWrapper.changeManager(programmaticCube.getManager(), false);
    }

    public void changeProgrammaticCube(IProgrammaticCube newProgrammaticCube) throws AgentException {
        this.activePivotManagerWrapper.changeManager(newProgrammaticCube.getManager(), false);
        this.programmaticCube = newProgrammaticCube;
    }

    @Override
    public IMultiVersionActivePivot getPivot() {
        return programmaticCube.getPivot();
    }

    @Override
    public IActivePivotManager getManager() {
        return programmaticCube.getManager();
    }

    @Override
    public Map<String, IMessageChannel<String, Object>> getChannelMap() {
        return programmaticCube.getChannelMap();
    }

    @Override
    public void insertTestDataInDatatoreAndCommit(String storeName, List<Map<String, Object>> mapList) {
        programmaticCube.insertTestDataInDatatoreAndCommit(storeName, mapList);
    }

    @Override
    public void insertDataInChannelWithDtoListAndCommit(ChannelData channelData) {
        programmaticCube.insertDataInChannelWithDtoListAndCommit(channelData);
    }

    @Override
    public void insertTestDataInChannelWithMapListAndCommit(ChannelData channelData) {
        programmaticCube.insertTestDataInChannelWithMapListAndCommit(channelData);
    }

    @Override
    public void registerContinuousQueryListener(IGetAggregatesQuery aggregatesQuery, IContinuousQueryListener<ICellSet, IVersionedContinuousQueryUpdate<ICellSet>> queryListener) throws QueryException {
        programmaticCube.registerContinuousQueryListener(aggregatesQuery, queryListener);
    }

    @Override
    public IDatastoreSchemaDescription getDatastoreSchemaDescription() {
        return programmaticCube.getDatastoreSchemaDescription();
    }

    @Override
    public IActivePivotManagerDescription getActivePivotManagerDescription() {
        return programmaticCube.getActivePivotManagerDescription();
    }

    @Override
    public IDatastore getDatastore() {
        return programmaticCube.getDatastore();
    }

    @Override
    public Map<String, CubeBuilder.StoreFields> getStoreFieldTypeMap() {
        return programmaticCube.getStoreFieldTypeMap();
    }

    @Override
    public boolean isManagerResetable() {
        return true;
    }

    @Override
    public void purgeDatastore() {
        programmaticCube.purgeDatastore();
    }

}
