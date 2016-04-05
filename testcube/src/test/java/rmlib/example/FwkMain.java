package rmlib.example;

import com.qfs.store.IDatastore;
import com.qfs.store.log.impl.LogWriteException;
import com.qfs.store.transaction.DatastoreTransactionException;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.contributions.impl.ClasspathContributionProvider;
import rmlib.ProgrammaticCube;
import rmlib.cubebuilder.CubeBuilder;
import rmlib.example.checks.RiskResultHelper;
import rmlib.example.riskdata.RiskCsvLoadHelper;
import rmlib.example.riskdata.RiskCubeBuildHelper;
import rmlib.example.riskdata.RiskDataHelper;
import rmlib.query.SimpleQueryUtils;
import rmlib.manager.ActivePivotManagerWrapper;

import java.util.List;
import java.util.Map;

public class FwkMain {

    public static final void main(String... args) throws Exception {

        Registry.setContributionProvider(new ClasspathContributionProvider("com.quartetfs", "com.qfs"));

        ActivePivotManagerWrapper resetableManager = new ActivePivotManagerWrapper();

        final ProgrammaticCube testCube = RiskCubeBuildHelper.buildRiskCube(true, true, resetableManager);

        boolean dataInsertedInStoreDirectly = false;

        loadData(testCube, dataInsertedInStoreDirectly);

        RiskResultHelper.printHierarchies(testCube.getPivot().getId(), testCube.getManager());

        // QUERY CUBE (SIMPLE)
        SimpleQueryUtils.queryCubeSimple(CubeBuilder.TEST_CUBE, testCube.getManager());

        // QUERY CUBE (TEST FWK)
        if(dataInsertedInStoreDirectly) {
            RiskResultHelper.queryCubeAndCheckResults(CubeBuilder.TEST_CUBE, testCube.getManager());
        }


        if(testCube.isResetable()) {

            RiskCsvLoadHelper.unregisterUpdateWhereTriggers(testCube.getDatastore());

            if (testCube.getDatastore() != null) {
                testCube.getDatastore().getLatestVersion().getEpochManager().getHistories().clear();
            }

            final ProgrammaticCube newTestCube = RiskCubeBuildHelper.buildRiskCube(true, true, resetableManager);

            loadData(newTestCube, dataInsertedInStoreDirectly);

            RiskResultHelper.printHierarchies(newTestCube.getPivot().getId(), newTestCube.getManager());

            // QUERY CUBE (SIMPLE)
            SimpleQueryUtils.queryCubeSimple(CubeBuilder.TEST_CUBE, newTestCube.getManager());

            // QUERY CUBE (TEST FWK)
            if(dataInsertedInStoreDirectly) {
                RiskResultHelper.queryCubeAndCheckResults(CubeBuilder.TEST_CUBE, newTestCube.getManager());
            }

        }

        // STOP ALL
        testCube.getManager().stop();
    }

    private static void loadData(ProgrammaticCube testCube, boolean dataInsertedInStoreDirectly) throws DatastoreTransactionException, LogWriteException {
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
