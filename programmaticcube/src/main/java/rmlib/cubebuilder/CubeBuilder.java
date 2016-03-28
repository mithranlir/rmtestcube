package rmlib.cubebuilder;

import com.qfs.condition.impl.BaseConditions;
import com.qfs.desc.IReferenceDescription;
import com.qfs.desc.IStoreDescription;
import com.qfs.desc.impl.DatastoreSchemaDescription;
import com.qfs.msg.IColumnCalculator;
import com.qfs.msg.IMessageChannel;
import com.qfs.msg.impl.EmptyCalculator;
import com.qfs.multiversion.IAdvancedEpochManager;
import com.qfs.multiversion.IEpochManagementPolicy;
import com.qfs.source.impl.POJOMessageChannelFactory;
import com.qfs.store.IDatastore;
import com.qfs.store.IDatastoreSchemaMetadata;
import com.qfs.store.IStoreFormat;
import com.qfs.store.IStoreMetadata;
import com.qfs.store.impl.Datastore;
import com.qfs.store.impl.StoreUtils;
import com.qfs.store.log.ILogConfiguration;
import com.qfs.store.record.IByteRecordFormat;
import com.qfs.store.selection.impl.Selection;
import com.qfs.store.transaction.DatastoreTransactionException;
import com.qfs.store.transaction.ITransactionManager;
import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.cube.hierarchy.IMultiVersionHierarchy;
import com.quartetfs.biz.pivot.definitions.*;
import com.quartetfs.biz.pivot.definitions.impl.*;
import com.quartetfs.biz.pivot.impl.ActivePivotManagerBuilder;
import com.quartetfs.biz.pivot.postprocessing.IPostProcessor;
import com.quartetfs.biz.pivot.query.aggregates.IAggregatesContinuousHandler;
import com.quartetfs.fwk.types.impl.ExtendedPluginInjector;
import rmlib.ProgrammaticCube;
import rmlib.channel.ChannelCreationHelper;
import rmlib.channel.DefaultValueService;
import rmlib.channel.SimpleDefaultValueService;
import rmlib.cubebuilder.subbuilder.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CubeBuilder {

    public static final String TEST_CALALOG_NAME = "TestCatalog";
    public static final String TEST_SCHEMA = "TestSchema";
    public static final String TEST_CUBE = "TestCube";

    private final Dimensions.AxisDimensionsDescriptionBuilder axisDimensionsDescriptionBuilder;
    private final Measures.MeasuresDescriptionBuilder measuresDescriptionBuilder;
    private final Managers.ManagerDescriptionBuilder managerDescriptionBuilder;
    private final List<IStoreDescription> storeDescriptionList = new ArrayList<>();
    private final List<IReferenceDescription> referenceDescriptionList = new ArrayList<>();
    private ISelectionDescription selectionDescription = null;
    private IEpochManagementPolicy epochManagementPolicy = null;
    private IAdvancedEpochManager epochManager = null;
    private ILogConfiguration logConfiguration = null;
    private List<ChannelBuildInfo> channelBuildInfoList = new ArrayList<>();
    private final List<ObjectInjection> objectsToInject = new ArrayList<>();
    private IEpochDimensionDescription epochDimensionDescription = null;
    private Integer aggregatesCacheSize = null;
    private IAggregateProviderDefinition aggregateProviderDefinition = null;
    private Map<String, StoreFields> storeFieldTypeMap = new HashMap<>();
    private final DefaultValueService defaultValueService;


    public CubeBuilder() {
        axisDimensionsDescriptionBuilder = Dimensions.builder();
        measuresDescriptionBuilder = Measures.builder();
        managerDescriptionBuilder = Managers.builder();
        defaultValueService = new SimpleDefaultValueService();
    }

    public CubeBuilder withAxisDimension(IAxisDimensionDescription axisDimensionDescription) {
        axisDimensionsDescriptionBuilder.withAxisDimensionDescription(axisDimensionDescription);
        return this;
    }

    public CubeBuilder withEpochDimension(IEpochDimensionDescription epochDimensionDescription) {
        this.epochDimensionDescription = epochDimensionDescription;
        return this;
    }

    public CubeBuilder withAggregatedMeasure(IAggregatedMeasureDescription aggregatedMeasureDescription) {
        measuresDescriptionBuilder.withAggregatedMeasure(aggregatedMeasureDescription);
        return this;
    }

    public CubeBuilder withNativeMeasure(INativeMeasureDescription nativeMeasureDescription) {
        measuresDescriptionBuilder.withNativeMeasure(nativeMeasureDescription);
        return this;
    }

    public CubeBuilder withPostProcessor(IPostProcessorDescription postProcessorDescription) {
        measuresDescriptionBuilder.withPostProcessor(postProcessorDescription);
        return this;
    }

    public CubeBuilder withSelection(ISelectionDescription selectionDescription) {
        this.selectionDescription = selectionDescription;
        return this;
    }

    public CubeBuilder withStore(IStoreDescription storeDescription) {
        storeDescriptionList.add(storeDescription);
        return this;
    }

    public CubeBuilder withStoreReference(IReferenceDescription referenceDescription) {
        referenceDescriptionList.add(referenceDescription);
        return this;
    }

    public CubeBuilder withEpochManagementPolicy(IEpochManagementPolicy epochManagementPolicy) {
        this.epochManagementPolicy = epochManagementPolicy;
        return this;
    }

    public CubeBuilder withEpochManager(IAdvancedEpochManager epochManager) {
        this.epochManager = epochManager;
        return this;
    }

    public CubeBuilder withLogConfiguration(ILogConfiguration logConfiguration) {
        this.logConfiguration = logConfiguration;
        return this;
    }

    public CubeBuilder withChannel(String topicName, String storeName) {
        return withChannel(topicName, storeName, new ArrayList<String>());
    }

    public CubeBuilder withChannel(String topicName, String storeName, List<String> emptyCalculatedColumns) {
        return withChannel(topicName, storeName, emptyCalculatedColumns, false);
    }

    public CubeBuilder withChannel(String topicName, String storeName, List<String> emptyCalculatedColumns, boolean autocommit) {
        return withChannel(topicName, storeName, emptyCalculatedColumns, autocommit, null);
    }

    public CubeBuilder withChannel(String topicName, String storeName, List<String> emptyCalculatedColumns, boolean autocommit, AbstractUpdateWhereProcedure procedure) {
        final ChannelBuildInfo channelBuildInfo = new ChannelBuildInfo(topicName, storeName, autocommit, emptyCalculatedColumns, procedure);
        channelBuildInfoList.add(channelBuildInfo);
        return this;
    }

    public CubeBuilder withPostProcessorDependencyInjection(String pluginType, String property, Object object) {
        objectsToInject.add(new ObjectInjection(IPostProcessor.class, pluginType, property, object));
        return this;
    }

    public CubeBuilder withHierarchyDependencyInjection(String pluginType, String property, Object object) {
        objectsToInject.add(new ObjectInjection(IMultiVersionHierarchy.class, pluginType, property, object));
        return this;
    }

    public CubeBuilder withHierarchyDependencyInjection(String pluginType, Object object) {
        objectsToInject.add(new ObjectInjection(IMultiVersionHierarchy.class, pluginType, null, object));
        return this;
    }

    public CubeBuilder withContinuousHandlerDependencyInjection(String pluginType, String property, Object object) {
        objectsToInject.add(new ObjectInjection(IAggregatesContinuousHandler.class, pluginType, property, object));
        return this;
    }

    public CubeBuilder withAggregatesCacheSize(Integer aggregatesCacheSize) {
        this.aggregatesCacheSize = aggregatesCacheSize;
        return this;
    }

    public CubeBuilder withAggregateProvider(IAggregateProviderDefinition aggregateProviderDefinition) {
        this.aggregateProviderDefinition = aggregateProviderDefinition;
        return this;
    }

    private void configureInjections() {
        for(ObjectInjection objectInjection : objectsToInject) {
            if(objectInjection.getProperty()!=null) {
                ExtendedPluginInjector.inject(
                        objectInjection.getInterfaceClass(),
                        objectInjection.getPluginKey(),
                        objectInjection.getProperty(),
                        objectInjection.getObject());
            }
            else  {
                ExtendedPluginInjector.inject(
                        objectInjection.getInterfaceClass(),
                        objectInjection.getPluginKey(),
                        objectInjection.getObject());
            }
        }

    }

    public ProgrammaticCube buildTestCube(boolean start) throws Exception {

        final ActivePivotInstanceDescription instanceDescription =
                buildInstanceDescription();

        final ActivePivotManagerDescription managerDescription =
                buildManagerDescription(instanceDescription);

        final ActivePivotDatastorePostProcessor activePivotDatastorePostProcessor =
                ActivePivotDatastorePostProcessor.createFrom(managerDescription);

        final DataStoreBuildResult datastoreBuildResult =
                buildDatastore(activePivotDatastorePostProcessor);

        final Datastore datastore = datastoreBuildResult.getDatastore();

        final DatastoreSchemaDescription datastoreSchemaDescription = datastoreBuildResult.getDatastoreSchemaDescription();

        configureStoreFieldTypeMap(datastore);

        configureInjections();

        final IActivePivotManager manager =
                buildManager(managerDescription, datastore);

        if(start) {
            manager.init(null);
            manager.start();
        }

        final Map<String, IMessageChannel<String, Object>> channelMap =
                configureChannels(datastore);

        final IMultiVersionActivePivot pivot = manager.getActivePivots().get(TEST_CUBE);

        return new ProgrammaticCube(manager, pivot, channelMap, storeFieldTypeMap, defaultValueService,
                datastoreSchemaDescription, managerDescription, datastore);
    }

    private void configureStoreFieldTypeMap(Datastore datastore) {
        for(String storeName : datastore.getSchemaMetadata().getStoreNames()) {

            final StoreFields storeFields = new StoreFields();
            storeFieldTypeMap.put(storeName, storeFields);

            final IStoreMetadata storeMetadata = datastore.getSchemaMetadata().getStoreMetadata(storeName);
            final IStoreFormat storeFormat = storeMetadata.getStoreFormat();
            final IByteRecordFormat recordFormat = storeFormat.getRecordFormat();

            for(int i=0; i<recordFormat.getFieldCount(); i++) {
                final String fieldName = recordFormat.getFieldName(i);
                final int type = recordFormat.getType(i);
                storeFields.addField(fieldName, type);
            }
        }
    }


    public class StoreFields {
        private final HashMap<String, Integer> fieldTypeMap = new HashMap<>();
        private final List<String> fields = new ArrayList<>();

        public StoreFields() {
        }

        public void addField(String fieldName, Integer type) {
            fieldTypeMap.put(fieldName, type);
            fields.add(fieldName);
        }

        public HashMap<String, Integer> getFieldTypeMap() {
            return fieldTypeMap;
        }

        public List<String> getFieldNames() {
            return fields;
        }
    }

    private IActivePivotManager buildManager(ActivePivotManagerDescription managerDescription, Datastore datastore) {
        final ActivePivotManagerBuilder managerBuilder = new ActivePivotManagerBuilder();
        managerBuilder.setDatastore(datastore);
        managerBuilder.setDescription(managerDescription);
        return managerBuilder.build();
    }

    private DataStoreBuildResult buildDatastore(ActivePivotDatastorePostProcessor activePivotDatastorePostProcessor) {

        final Datastores.CubeDatastoreBuilder cubeDatastoreBuilder = Datastores.builder();

        cubeDatastoreBuilder.withSchemaDescriptionPostProcessors(activePivotDatastorePostProcessor);

        for(IReferenceDescription referenceDescription : referenceDescriptionList) {
            cubeDatastoreBuilder.withReferencesDescription(referenceDescription);
        }

        for(IStoreDescription storeDescription : storeDescriptionList) {
            cubeDatastoreBuilder.withStoreDescription(storeDescription);
        }

        if(epochManagementPolicy!=null) {
            cubeDatastoreBuilder.withEpochManagementPolicy(epochManagementPolicy);
        }
        if(epochManager!=null) {
            cubeDatastoreBuilder.withEpochManager(epochManager);
        }
        if(logConfiguration!=null) {
            cubeDatastoreBuilder.withLogConfiguration(logConfiguration);
        }

        final Datastore datastore = cubeDatastoreBuilder.build();

        DatastoreSchemaDescription datastoreSchemaDescription = cubeDatastoreBuilder.getDatastoreSchemaDescriptionAfterBuild();

        return new DataStoreBuildResult(datastore, datastoreSchemaDescription);
    }

    private class DataStoreBuildResult {

        private Datastore datastore;
        private DatastoreSchemaDescription datastoreSchemaDescription;

        public DataStoreBuildResult(Datastore datastore, DatastoreSchemaDescription datastoreSchemaDescription) {
            this.datastore = datastore;
            this.datastoreSchemaDescription = datastoreSchemaDescription;
        }


        public Datastore getDatastore() {
            return datastore;
        }

        public DatastoreSchemaDescription getDatastoreSchemaDescription() {
            return datastoreSchemaDescription;
        }

    }

    private ActivePivotInstanceDescription buildInstanceDescription() {

        final AxisDimensionsDescription axisDimensionsDescription = axisDimensionsDescriptionBuilder.build();
        final MeasuresDescription measuresDescription = measuresDescriptionBuilder.build();

        final Pivots.PivotDescriptionBuilder pivotDescriptionBuilder = Pivots.builder();
        pivotDescriptionBuilder.withAxisDimensionsDescription(axisDimensionsDescription);
        pivotDescriptionBuilder.withMeasuresDescription(measuresDescription);

        if(epochDimensionDescription!=null) {
            pivotDescriptionBuilder.withEpochDimensionDescription(epochDimensionDescription);
        }

        if(aggregateProviderDefinition!=null) {
            pivotDescriptionBuilder.withAggregateProviderDefinition(aggregateProviderDefinition);
        }

        if(aggregatesCacheSize!=null) {
            pivotDescriptionBuilder.withAggregatesCacheSize(aggregatesCacheSize);
        }

        final ActivePivotDescription pivotDescription = pivotDescriptionBuilder.build();

        final ActivePivotInstanceDescription instanceDescription =  new ActivePivotInstanceDescription();
        instanceDescription.setId(TEST_CUBE);
        instanceDescription.setActivePivotDescription(pivotDescription);

        return instanceDescription;
    }

    private ActivePivotManagerDescription buildManagerDescription(ActivePivotInstanceDescription instanceDescription) {

        managerDescriptionBuilder.withActivePivotId(TEST_CUBE);
        managerDescriptionBuilder.withCatalogId(TEST_CALALOG_NAME);
        managerDescriptionBuilder.withSchemaInstanceId(TEST_SCHEMA);
        managerDescriptionBuilder.withSelectionDescription(selectionDescription);
        managerDescriptionBuilder.withInstanceDescription(instanceDescription);
        // no distributedInstanceDescription for managerDescription

        return managerDescriptionBuilder.build();
    }

    private Map<String, IMessageChannel<String, Object>> configureChannels(Datastore datastore) throws DatastoreTransactionException {
        final Map<String, IMessageChannel<String, Object>> channels = new HashMap<>();
        final POJOMessageChannelFactory factory = new POJOMessageChannelFactory(datastore);
        for(ChannelBuildInfo channelBuildInfo : channelBuildInfoList) {

            final IMessageChannel<String, Object> channel =
                    ChannelCreationHelper.createAndConfigurePOJOChannel(
                            factory,
                            datastore,
                            channelBuildInfo.getTopicName(),
                            channelBuildInfo.getStoreName(),
                            channelBuildInfo.isAutocommit(),
                            getEmptyCalculator(channelBuildInfo.getEmptyCalculatedColumns())
                    );

            if(channelBuildInfo.getProcedure()!=null) {

                channelBuildInfo.getProcedure().configure(
                        datastore, channelBuildInfo.getStoreName());

                registerUpdateWhereTrigger(
                        channelBuildInfo.getProcedure(), "Trigger for " + channelBuildInfo.getStoreName());
            }

            channels.put(channelBuildInfo.getStoreName(), channel);
        }

        return channels;
    }

    private List<IColumnCalculator<Object>> getEmptyCalculator(List<String> columnNames) {
        final List<IColumnCalculator<Object>> list = new ArrayList<>();
        for(String columnName : columnNames) {
            list.add(new EmptyCalculator<>(columnName));
        }
        return list;
    }

    public static class ChannelBuildInfo {

        private String topicName;
        private String storeName;
        private boolean autocommit;
        private List<String> emptyCalculatedColumns;
        private AbstractUpdateWhereProcedure procedure;

        public ChannelBuildInfo(String topicName, String storeName, boolean autocommit, List<String> emptyCalculatedColumns, AbstractUpdateWhereProcedure procedure) {
            this.topicName = topicName;
            this.storeName = storeName;
            this.autocommit = autocommit;
            this.emptyCalculatedColumns = emptyCalculatedColumns;
            this.procedure = procedure;
        }

        public String getTopicName() {
            return topicName;
        }

        public String getStoreName() {
            return storeName;
        }

        public boolean isAutocommit() {
            return autocommit;
        }

        public List<String> getEmptyCalculatedColumns() {
            return emptyCalculatedColumns;
        }

        public AbstractUpdateWhereProcedure getProcedure() {
            return procedure;
        }
    }

    private void registerUpdateWhereTrigger(AbstractUpdateWhereProcedure procedure, String triggerName) throws DatastoreTransactionException {
        procedure.getDatastore().getTransactionManager().registerUpdateWhereTrigger(
                triggerName,
                0, // Priority is not important, as we have only one trigger.
                new Selection(procedure.getStoreName()), // The fields required to perform the computation
                BaseConditions.TRUE, // We want to match all facts
                procedure
        );
    }

    public static abstract class AbstractUpdateWhereProcedure implements ITransactionManager.IUpdateWhereProcedure {

        protected Datastore datastore = null;
        protected String storeName = null;

        protected int getFieldIndex(IDatastore datastore, String storeName, String fieldName) {
            final IDatastoreSchemaMetadata datastoreSchemaMetadata = datastore.getSchemaMetadata();
            return StoreUtils.getFieldIndex(datastoreSchemaMetadata, storeName, fieldName);
        }

        public void configure(Datastore datastore, String storeName) {
            this.datastore = datastore;
            this.storeName = storeName;
        }

        public Datastore getDatastore() {
            return datastore;
        }

        public String getStoreName() {
            return storeName;
        }
    }

    private class ObjectInjection {

        private final Class interfaceClass;
        private final String pluginKey;
        private final String property;
        private final Object object;

        public ObjectInjection(Class interfaceClass, String pluginKey, String property, Object object) {
            this.interfaceClass = interfaceClass;
            this.pluginKey = pluginKey;
            this.property = property;
            this.object = object;
        }

        public Class getInterfaceClass() {
            return interfaceClass;
        }

        public String getPluginKey() {
            return pluginKey;
        }

        public String getProperty() {
            return property;
        }

        public Object getObject() {
            return object;
        }
    }


}
