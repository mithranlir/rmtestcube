package rmlib.cubebuilder.subbuilder;

import com.google.common.collect.ImmutableList;
import com.quartetfs.biz.pivot.definitions.*;
import com.quartetfs.biz.pivot.definitions.impl.ActivePivotManagerDescription;
import com.quartetfs.biz.pivot.definitions.impl.ActivePivotSchemaDescription;
import com.quartetfs.biz.pivot.definitions.impl.ActivePivotSchemaInstanceDescription;
import com.quartetfs.biz.pivot.definitions.impl.CatalogDescription;
import com.quartetfs.biz.pivot.distribution.IDistributedActivePivotInstanceDescription;

import java.util.ArrayList;
import java.util.List;

public class Managers {

    public static ManagerDescriptionBuilder builder() {
        return new ManagerDescriptionBuilder();
    }

    public static class ManagerDescriptionBuilder extends AbstractComponentBuilder<ManagerDescriptionBuilder, ActivePivotManagerDescription> {

        private String activePivotId = null;
        private String catalogId = null;
        private String schemaInstanceId = null;
        private final List<IActivePivotInstanceDescription> instanceDescriptionList = new ArrayList<>();
        private ISelectionDescription selectionDescription;
        private final List<IDistributedActivePivotInstanceDescription> distributedInstanceDescriptions = new ArrayList<>();

        public ManagerDescriptionBuilder withActivePivotId(String activePivotId) {
            this.activePivotId = activePivotId;
            return self();
        }

        public ManagerDescriptionBuilder withCatalogId(String catalogId) {
            this.catalogId = catalogId;
            return self();
        }

        public ManagerDescriptionBuilder withSchemaInstanceId(String schemaInstanceId) {
            this.schemaInstanceId = schemaInstanceId;
            return self();
        }

        public ManagerDescriptionBuilder withInstanceDescription(IActivePivotInstanceDescription instance) {
            this.instanceDescriptionList.add(instance);
            return self();
        }

        public ManagerDescriptionBuilder withSelectionDescription(ISelectionDescription selectionDescription) {
            this.selectionDescription = selectionDescription;
            return self();
        }

        public ManagerDescriptionBuilder withDistributedInstanceDescription(IDistributedActivePivotInstanceDescription distributedInstanceDescription) {
            this.distributedInstanceDescriptions.add(distributedInstanceDescription);
            return self();
        }

        protected ActivePivotManagerDescription doBuild() {

            final IActivePivotSchemaDescription schemaDescription = new ActivePivotSchemaDescription();
            schemaDescription.setActivePivotInstanceDescriptions(instanceDescriptionList);
            schemaDescription.setDatastoreSelection(selectionDescription);
            schemaDescription.setDistributedActivePivotInstanceDescriptions(distributedInstanceDescriptions);

            final ICatalogDescription catalogDescription = new CatalogDescription();
            catalogDescription.setId(catalogId);
            catalogDescription.setActivePivotIds(ImmutableList.of(activePivotId));

            final List<ICatalogDescription> catalogDescriptionList = new ArrayList<>();
            catalogDescriptionList.add(catalogDescription);

            final IActivePivotSchemaInstanceDescription schemaInstanceDescription = new ActivePivotSchemaInstanceDescription();
            schemaInstanceDescription.setId(schemaInstanceId);
            schemaInstanceDescription.setActivePivotSchemaDescription(schemaDescription);

            final List<IActivePivotSchemaInstanceDescription> schemaInstanceDescriptionList = new ArrayList<>();
            schemaInstanceDescriptionList.add(schemaInstanceDescription);

            final ActivePivotManagerDescription managerDescription = new ActivePivotManagerDescription();
            managerDescription.setCatalogs(catalogDescriptionList);
            managerDescription.setSchemas(schemaInstanceDescriptionList);

            return managerDescription;
        }
    }

}
