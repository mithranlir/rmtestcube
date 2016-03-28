package rmlib.cubebuilder.subbuilder;

import com.qfs.desc.IDatastoreSchemaDescriptionPostProcessor;
import com.qfs.desc.IReferenceDescription;
import com.qfs.desc.IStoreDescription;
import com.qfs.desc.impl.DatastoreSchemaDescription;
import com.qfs.multiversion.IAdvancedEpochManager;
import com.qfs.multiversion.IEpochManagementPolicy;
import com.qfs.store.build.IDatastoreBuilder;
import com.qfs.store.build.impl.DatastoreBuilder;
import com.qfs.store.impl.Datastore;
import com.qfs.store.log.ILogConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Datastores {

    public static CubeDatastoreBuilder builder() {
        return new CubeDatastoreBuilder();
    }

    public static class CubeDatastoreBuilder extends AbstractComponentBuilder<CubeDatastoreBuilder, Datastore> {

        private final List<IStoreDescription> storeDescriptionList = new ArrayList<>();
        private final List<IReferenceDescription> referenceDescriptionList = new ArrayList<>();
        private final List<IDatastoreSchemaDescriptionPostProcessor> schemaDescriptionPostProcessorList = new ArrayList<>();
        private ILogConfiguration logConfiguration = null;
        private IEpochManagementPolicy epochManagementPolicy = null;
        private IAdvancedEpochManager epochManager = null;
        private DatastoreSchemaDescription datastoreSchemaDescription = null;

        public CubeDatastoreBuilder withStoreDescription(IStoreDescription storeDescription) {
            storeDescriptionList.add(storeDescription);
            return self();
        }

        public CubeDatastoreBuilder withReferencesDescription(IReferenceDescription referenceDescription) {
            referenceDescriptionList.add(referenceDescription);
            return self();
        }

        public CubeDatastoreBuilder withSchemaDescriptionPostProcessors(IDatastoreSchemaDescriptionPostProcessor... schemaDescriptionPostProcessors) {
            schemaDescriptionPostProcessorList.addAll(Arrays.asList(schemaDescriptionPostProcessors));
            return self();
        }

        public CubeDatastoreBuilder withLogConfiguration(ILogConfiguration logConfiguration) {
            this.logConfiguration = logConfiguration;
            return self();
        }

        public CubeDatastoreBuilder withEpochManagementPolicy(IEpochManagementPolicy epochManagementPolicy) {
            this.epochManagementPolicy = epochManagementPolicy;
            return self();
        }

        public CubeDatastoreBuilder withEpochManager(IAdvancedEpochManager epochManager) {
            this.epochManager = epochManager;
            return self();
        }

        protected Datastore doBuild() {

            this.datastoreSchemaDescription =
                    new DatastoreSchemaDescription(
                            storeDescriptionList,
                            referenceDescriptionList);

            final DatastoreBuilder builder = new DatastoreBuilder();

            final IDatastoreBuilder.IBuildableDatastore buildDatastoreDatastore =
                    builder.setSchemaDescription(datastoreSchemaDescription);

            if (logConfiguration != null) {
                buildDatastoreDatastore.setLogConfiguration(logConfiguration);
            }

            buildDatastoreDatastore.addSchemaDescriptionPostProcessors(
                    schemaDescriptionPostProcessorList.toArray(new IDatastoreSchemaDescriptionPostProcessor[0]));

            buildDatastoreDatastore.setEpochManagementPolicy(epochManagementPolicy);

            if (epochManager != null) {
                buildDatastoreDatastore.setEpochManager(epochManager);
            }

            return (Datastore) buildDatastoreDatastore.build();
        }


        public DatastoreSchemaDescription getDatastoreSchemaDescriptionAfterBuild() {
            return datastoreSchemaDescription;
        }
    }
}
