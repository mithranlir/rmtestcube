package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.definitions.impl.AxisHierarchyDescription;
import com.quartetfs.biz.pivot.definitions.impl.AxisLevelDescription;

import java.util.ArrayList;
import java.util.List;

public class AxisHierarchies {

    public static AxisHierarchyDescriptionBuilder builder() {
        return new AxisHierarchyDescriptionBuilder();
    }

    public static class AxisHierarchyDescriptionBuilder extends AbstractComponentBuilder<AxisHierarchyDescriptionBuilder, AxisHierarchyDescription> {

        private Boolean visible = null;
        private String name = null;
        private Boolean allMembersEnabled = null;
        private final List<AxisLevelDescription> axisLevelDescriptionList = new ArrayList<>();
        private Boolean defaultHierarchy = null;
        private Boolean factless = null;
        private String pluginKey = null;
        private String folder = null;

        public AxisHierarchyDescriptionBuilder withVisible(boolean visible) {
            this.visible = visible;
            return self();
        }

        public AxisHierarchyDescriptionBuilder withName(String name) {
            this.name = name;
            return self();
        }

        public AxisHierarchyDescriptionBuilder withAllMembersEnabled(boolean allMembersEnabled) {
            this.allMembersEnabled = allMembersEnabled;
            return self();
        }

        public AxisHierarchyDescriptionBuilder withDefaultHierarchy(boolean defaultHierarchy) {
            this.defaultHierarchy = defaultHierarchy;
            return self();
        }

        public AxisHierarchyDescriptionBuilder withLevel(AxisLevelDescription level) {
            this.axisLevelDescriptionList.add(level);
            return self();
        }

        public AxisHierarchyDescriptionBuilder withFactless(Boolean factless) {
            this.factless = factless;
            return self();
        }

        public AxisHierarchyDescriptionBuilder withPluginKey(String pluginKey) {
            this.pluginKey = pluginKey;
            return self();
        }

        public AxisHierarchyDescriptionBuilder withFolder(String folder) {
            this.folder = folder;
            return self();
        }

        protected AxisHierarchyDescription doBuild() {
            final AxisHierarchyDescription axisHierarchyDescription = new AxisHierarchyDescription();

            axisHierarchyDescription.setName(name);

            for(AxisLevelDescription level : axisLevelDescriptionList) {
                axisHierarchyDescription.addLevel(level);
            }

            if(visible!=null) {
                axisHierarchyDescription.setVisible(visible);
            }

            if(allMembersEnabled!=null) {
                axisHierarchyDescription.setAllMembersEnabled(allMembersEnabled);
            }

            if(defaultHierarchy!=null) {
                axisHierarchyDescription.setDefaultHierarchy(defaultHierarchy);
            }

            if(factless!=null) {
                axisHierarchyDescription.setFactless(factless);
            }

            if(pluginKey!=null) {
                axisHierarchyDescription.setPluginKey(pluginKey);
            }

            if(folder!=null) {
                axisHierarchyDescription.setFolder(folder);
            }

            return axisHierarchyDescription;
        }
    }
}
