package rmlib.example.riskdata;

import com.qfs.chunk.IArrayReader;
import com.qfs.chunk.IWritableArray;
import com.qfs.desc.impl.StoreDescriptionBuilder;
import com.qfs.store.record.IRecordFormat;
import com.quartetfs.biz.pivot.cube.dimension.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import jsr166e.ThreadLocalRandom;
import rmlib.IProgrammaticCube;
import rmlib.cubebuilder.CubeBuilder;
import rmlib.cubebuilder.subbuilder.*;
import rmlib.manager.ActivePivotManagerWrapper;

import java.util.Arrays;
import java.util.Random;

import static com.qfs.literal.ILiteralType.DOUBLE;
import static com.qfs.literal.ILiteralType.LONG;
import static rmlib.example.riskdata.DatastoreConsts.RISK__PNL;

public class RiskCubeBuildHelper {


    public static IProgrammaticCube buildRiskCube(boolean start) throws Exception {

        final CubeBuilder cubeBuilder = new CubeBuilder();

        final IProgrammaticCube testCube = cubeBuilder
                .withStore(
                        new StoreDescriptionBuilder()
                                .withStoreName(RiskDataHelper.TEST_RISK_STORE)
                                .withField(DatastoreConsts.RISK__FACT_ID, LONG).asKeyField()
                                .withField(DatastoreConsts.RISK__AS_OF_DATE, "date[" + RiskEntry.DATE_PATTERN + "]").asKeyField()
                                .withField(DatastoreConsts.RISK__PNL, DOUBLE)
                                .withModuloPartitioning(DatastoreConsts.RISK__FACT_ID, 8).build()
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
                .buildTestCube(start);

        return testCube;
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
