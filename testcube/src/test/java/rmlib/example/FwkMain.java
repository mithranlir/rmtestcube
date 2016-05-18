package rmlib.example;

import com.qfs.store.IDatastore;
import com.qfs.store.log.impl.LogWriteException;
import com.qfs.store.transaction.DatastoreTransactionException;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.contributions.impl.ClasspathContributionProvider;
import com.quartetfs.fwk.query.QueryException;
import rmlib.IProgrammaticCube;
import rmlib.ProgrammaticCubeWrapper;
import rmlib.cubebuilder.CubeBuilder;
import rmlib.debug.HierarchiesPrintHelper;
import rmlib.example.checks.RiskResultHelper;
import rmlib.example.riskdata.RiskCsvLoadHelper;
import rmlib.example.riskdata.RiskCubeBuildHelper;
import rmlib.example.riskdata.RiskDataHelper;
import rmlib.query.SimpleQueryUtils;
import rmlib.manager.ActivePivotManagerWrapper;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

public class FwkMain {

    public static final void main(String... args) throws Exception {

        Registry.setContributionProvider(new ClasspathContributionProvider("com.quartetfs", "com.qfs"));

        final IProgrammaticCube testCube = RiskCubeBuildHelper.buildRiskCube(true);
        final ProgrammaticCubeWrapper testCubeWrapper = new ProgrammaticCubeWrapper(testCube);

        boolean dataInsertedInStoreDirectly = true;

        loadAndCheck(testCubeWrapper, dataInsertedInStoreDirectly);

        if(!dataInsertedInStoreDirectly) {
            RiskCsvLoadHelper.unregisterUpdateWhereTriggers(testCubeWrapper.getDatastore());
        }
        testCubeWrapper.purgeDatastore();

        final IProgrammaticCube newTestCube = RiskCubeBuildHelper.buildRiskCube(true);
        testCubeWrapper.changeProgrammaticCube(newTestCube);

        loadAndCheck(testCubeWrapper, dataInsertedInStoreDirectly);

        // STOP ALL
        testCubeWrapper.getManager().stop();
    }

    private static void loadAndCheck(ProgrammaticCubeWrapper testCubeWrapper, boolean dataInsertedInStoreDirectly) throws DatastoreTransactionException, LogWriteException, QueryException, ParseException {
        loadData(testCubeWrapper, dataInsertedInStoreDirectly);

        //RiskResultHelper.printHierarchies(testCubeWrapper.getPivot().getId(), testCubeWrapper.getManager());

        HierarchiesPrintHelper.printHierarchies(testCubeWrapper.getPivot().getId(), testCubeWrapper.getManager());

        // QUERY CUBE (SIMPLE)
        SimpleQueryUtils.queryCubeSimple(CubeBuilder.TEST_CUBE, testCubeWrapper.getManager());

        // QUERY CUBE (TEST FWK)
        if(dataInsertedInStoreDirectly) {
            RiskResultHelper.queryCubeAndCheckResults(CubeBuilder.TEST_CUBE, testCubeWrapper.getManager());
        }
    }

    private static void loadData(IProgrammaticCube testCube, boolean dataInsertedInStoreDirectly) throws DatastoreTransactionException, LogWriteException {
        if(dataInsertedInStoreDirectly) {
            final List<Map<String, Object>> riskEntriesMapList = RiskDataHelper.generateRiskEntriesAsMapList();
            final String storeName = RiskDataHelper.TEST_RISK_STORE;
            testCube.insertTestDataInDatatoreAndCommit(storeName, riskEntriesMapList);
        }
        else {
            final IDatastore datastore = testCube.getDatastore();
            RiskCsvLoadHelper.registerUpdateWhereTriggers(datastore);
            RiskCsvLoadHelper.loadCsv(datastore);
        }
    }


}
