package rmlib.example;

import com.qfs.store.IDatastore;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.contributions.impl.ClasspathContributionProvider;
import rmlib.ProgrammaticCube;
import rmlib.cubebuilder.CubeBuilder;
import rmlib.example.checks.RiskResultHelper;
import rmlib.example.riskdata.RiskCsvLoadHelper;
import rmlib.example.riskdata.RiskCubeBuildHelper;
import rmlib.example.riskdata.RiskDataHelper;

import java.util.List;
import java.util.Map;

public class FwkMain {

    public static final void main(String... args) throws Exception {

        Registry.setContributionProvider(new ClasspathContributionProvider("com.quartetfs", "com.qfs"));

        ProgrammaticCube testCube = RiskCubeBuildHelper.buildRiskCube();

        boolean dataInsertedInStoreDirectly = false;

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

        RiskResultHelper.printHierarchies(testCube.getPivot().getId(), testCube.getManager());

        // QUERY CUBE (SIMPLE)
        rmlib.query.QueryUtils.queryCubeSimple(CubeBuilder.TEST_CUBE, testCube.getManager(), 1);

        // QUERY CUBE (TEST FWK)
        if(dataInsertedInStoreDirectly) {
            RiskResultHelper.queryCubeAndCheckResults(CubeBuilder.TEST_CUBE, testCube.getManager());
        }

        // STOP ALL
        testCube.getManager().stop();
    }


}
