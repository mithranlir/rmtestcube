package run;

import com.qfs.chunk.IArrayReader;
import com.qfs.chunk.IWritableArray;
import com.qfs.desc.impl.StoreDescriptionBuilder;
import com.qfs.sandbox.RiskDataHelper;
import com.qfs.sandbox.RiskResultHelper;
import com.qfs.sandbox.cfg.CustomEpochPolicy;
import com.qfs.sandbox.cfg.DatastoreConsts;
import com.qfs.sandbox.cfg.SourceConfig;
import com.qfs.sandbox.model.impl.RiskEntry;
import com.qfs.store.record.IRecordFormat;
import com.quartetfs.biz.pivot.cube.dimension.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.fwk.Registry;
import com.quartetfs.fwk.contributions.impl.ClasspathContributionProvider;
import jsr166e.ThreadLocalRandom;
import rmlib.cubebuilder.subbuilder.*;
import rmlib.ProgrammaticCube;
import rmlib.cubebuilder.CubeBuilder;

import java.util.Arrays;
import java.util.Random;

import static com.qfs.literal.ILiteralType.DOUBLE;
import static com.qfs.literal.ILiteralType.LONG;
import static com.qfs.sandbox.cfg.DatastoreConsts.RISK__PNL;

public class FwkMain {

    public static final void main(String... args) throws Exception {

        Registry.setContributionProvider(new ClasspathContributionProvider("com.quartetfs", "com.qfs"));

        final CubeBuilder cubeBuilder = new CubeBuilder();

        final ProgrammaticCube testCube = cubeBuilder
                .withStore(
                        new StoreDescriptionBuilder()
                                .withStoreName(RiskDataHelper.TEST_RISK_STORE)
                                .withField(DatastoreConsts.RISK__TRADE_ID, LONG).asKeyField()
                                .withField(DatastoreConsts.RISK__AS_OF_DATE, "date[" + RiskEntry.DATE_PATTERN + "]").asKeyField()
                                .withField(DatastoreConsts.RISK__PNL, DOUBLE)
                                .withModuloPartitioning(DatastoreConsts.RISK__TRADE_ID, 8).build()
                )
                .withSelection(Selections.builder()
                                .withBaseStore(RiskDataHelper.TEST_RISK_STORE)
                                .withSelection("AsOfDate")
                                .withSelection("pnl")
                                .build()
                )
                .withAxisDimension(AxisDimensions.builder()
                                .withName("Time")
                                .withDimensionType(IDimension.DimensionType.TIME)
                                .withAxisHierarchyDescription(
                                        AxisHierarchies.builder()
                                                .withName("HistoricalDates")
                                                .withAllMembersEnabled(false)
                                                .withLevel(
                                                        AxisLevels.level("AsOfDate", ILevelInfo.LevelType.TIME)
                                                                .withFormatter(Formatters.formatter("DATE[yyyy-MM-dd]"))
                                                                .withComparator(
                                                                        Comparators.builder().withPluginkey("ReverseOrder").build()
                                                                )
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .withEpochDimension(
                        EpochDimensions.builder()
                                .withEnabled(true)
                                .withLevelComparator("REVERSE_EPOCH")
                                .withFormatter(
                                        Formatters.builder().withFormatter("EPOCH[HH:mm:ss]").build())
                                .build())
                .withNativeMeasure(NativeMeasures.builder()
                                .withName("contributors.COUNT")
                                .withAlias("Count")
                                .build()
                )
                .withAggregatedMeasure(AggregatedMeasures.builder()
                                .withFieldName("pnl")
                                .withFolder("PnL")
                                .build()
                )
                .withAggregateProvider(AggregateProviders.builder()
                                .withPluginKey("JUST_IN_TIME")
                                .build()
                )
                .withChannel(RiskDataHelper.TOPIC_RISK, RiskDataHelper.TEST_RISK_STORE, Arrays.asList("pnl"), false, createProcedureForRiskStore())
                .withEpochManagementPolicy(new CustomEpochPolicy(5 * 60_000, 5 * 60_000, 30 * 60_000))
                        // Retain the latest 5 minutes of history and retain one version each 5 minutes, until the last 30 minutes.
                .buildTestCube(false);


        RiskResultHelper.printHierarchies(testCube.getPivot().getId(), testCube.getManager());

        // QUERY CUBE (SIMPLE)
        rmlib.query.QueryUtils.queryCubeSimple(CubeBuilder.TEST_CUBE, testCube.getManager(), 1);

        if(!SourceConfig.loadCsv) {
            // QUERY CUBE (TEST FWK)
            RiskResultHelper.queryCubeAndCheckResults(CubeBuilder.TEST_CUBE, testCube.getManager());
        }

        // STOP ALL
        testCube.getManager().stop();
    }

    public static CubeBuilder.AbstractUpdateWhereProcedure createProcedureForRiskStore() {

        final CubeBuilder.AbstractUpdateWhereProcedure riskProcedure = new
                CubeBuilder.AbstractUpdateWhereProcedure() {
                    private int pnlIndex;
                    @Override
                    public void init(IRecordFormat selectionFormat) {
                        pnlIndex = getFieldIndex(datastore, storeName, RISK__PNL);
                    }

                    @Override
                    public void execute(IArrayReader selectedRecord, IWritableArray recordWriter) {
                        final Random random = ThreadLocalRandom.current();
                        final double pnl = 1000 + random.nextInt(4000); // just for resultcheck...
                        recordWriter.writeDouble(pnlIndex, pnl);
                    }
                };
        return riskProcedure;
    }

}
