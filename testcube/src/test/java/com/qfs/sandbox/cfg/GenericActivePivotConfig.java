package com.qfs.sandbox.cfg;

import com.qfs.chunk.IArrayReader;
import com.qfs.chunk.IWritableArray;
import com.qfs.desc.IDatastoreSchemaDescription;
import com.qfs.desc.impl.StoreDescriptionBuilder;
import com.qfs.logging.MessagesServer;
import com.qfs.sandbox.RiskDataHelper;
import com.qfs.sandbox.model.impl.RiskEntry;
import com.qfs.store.IDatastore;
import com.qfs.store.record.IRecordFormat;
import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.cube.dimension.IDimension;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevelInfo;
import com.quartetfs.biz.pivot.definitions.IActivePivotManagerDescription;
import com.quartetfs.biz.pivot.security.IContextValueManager;
import com.quartetfs.biz.pivot.security.IRoleComparator;
import com.quartetfs.biz.pivot.security.impl.ContextValueManager;
import com.quartetfs.biz.pivot.security.impl.ContextValuePropagator;
import com.quartetfs.biz.pivot.server.impl.XMLAEnabler;
import com.quartetfs.fwk.QuartetException;
import rmlib.ProgrammaticCube;
import rmlib.cubebuilder.CubeBuilder;
import rmlib.cubebuilder.subbuilder.*;
import jsr166e.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import static com.qfs.literal.ILiteralType.DOUBLE;
import static com.qfs.literal.ILiteralType.LONG;
import static com.qfs.sandbox.cfg.DatastoreConsts.RISK__PNL;

@Configuration
public class GenericActivePivotConfig {

    private static Logger LOGGER = MessagesServer.getLogger(GenericActivePivotConfig.class);

    @Autowired
    protected Environment env;

    public GenericActivePivotConfig() {
    }

    @Bean
    public ProgrammaticCube initManager() throws Exception {

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

        return testCube;
    }

    @Bean
    public IDatastoreSchemaDescription schemaDescription(ProgrammaticCube testCube) {
        return testCube.getDatastoreSchemaDescription();
    }

    @Bean
    public IActivePivotManagerDescription activePivotManagerDescription(ProgrammaticCube testCube) {
        return testCube.getActivePivotManagerDescription();
    }

    @Bean
    public IDatastore datastore(ProgrammaticCube testCube) {
        return testCube.getDatastore();
    }

    @Bean(destroyMethod = "stop")
    public IActivePivotManager activePivotManager(ProgrammaticCube testCube) {
         return testCube.getManager();
    }



    @Bean(destroyMethod = "shutdown")
    public ContextValueManager contextValueManager(IRoleComparator roleComparator) throws QuartetException {
        ContextValueManager contextValueManager = new ContextValueManager();
        if(null != roleComparator) {
            contextValueManager.setRoleComparator(roleComparator);
        }

        return contextValueManager;
    }

    @Bean
    public IRoleComparator roleComparator() {
        return null;
    }

    @Bean
    public ContextValuePropagator contextValuePropagator(IContextValueManager contextValueManager) {
        ContextValuePropagator propagator = new ContextValuePropagator();
        propagator.setContextValueManager(contextValueManager);
        return propagator;
    }

    @Bean
    public XMLAEnabler XMLAEnabler(IActivePivotManager activePivotManager, IContextValueManager contextValueManager) throws Exception {
        XMLAEnabler xmla = new XMLAEnabler();
        xmla.setActivePivotManager(activePivotManager);
        xmla.setContextValueManager(contextValueManager);
        xmla.setLogging(false);
        xmla.setMonitoring(true);
        return xmla;
    }

    public CubeBuilder.AbstractUpdateWhereProcedure createProcedureForRiskStore() {

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
