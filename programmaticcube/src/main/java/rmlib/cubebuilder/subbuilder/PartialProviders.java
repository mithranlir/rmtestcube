package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.cube.hierarchy.impl.HierarchiesUtil;
import com.quartetfs.biz.pivot.definitions.impl.PartialProviderDefinition;

import java.util.*;

public class PartialProviders {

    public static PartialProviderBuilder builder() {
        return new PartialProviderBuilder();
    }

    public static class PartialProviderBuilder extends AbstractComponentBuilder<PartialProviderBuilder, PartialProviderDefinition> {

        private Properties properties = null;
        private String pluginKey = null;
        private Boolean swap = null;
        private final List<PartialProviderHierarchy> partialProviderHierarchyList = new ArrayList<>();

        public PartialProviderBuilder withProperty(String key, String value) {
            if(this.properties == null) {
                this.properties = new Properties();
            }
            this.properties.setProperty(key, value);
            return self();
        }

        public PartialProviderBuilder withPluginKey(String pluginKey) {
            this.pluginKey = pluginKey;
            return self();
        }

        public PartialProviderBuilder withSwap(boolean swap) {
            this.swap = swap;
            return self();
        }

        public PartialProviderBuilder withHierarchy(String name) {
            return withHierarchy(name, null);
        }

        public PartialProviderBuilder withHierarchy(String name, String dimension) {
            return withHierarchy(name, dimension, null);
        }

        public PartialProviderBuilder withHierarchy(String name, String dimension, String maxLevel) {
            partialProviderHierarchyList.add(new PartialProviderHierarchy(name, dimension, maxLevel));
            return self();
        }

        protected PartialProviderDefinition doBuild() {

            final PartialProviderDefinition partialProviderDefinition = new PartialProviderDefinition();

            partialProviderDefinition.setPluginKey(pluginKey);

            if(properties!=null) {
                partialProviderDefinition.setProperties(properties);
            }

            if(swap!=null) {
                partialProviderDefinition.setSwap(swap);
            }

            if(!partialProviderHierarchyList.isEmpty()) {
                final HashMap<String, Map<String, String>> scopeElts = new HashMap<>();
                for (PartialProviderHierarchy partialProviderHierarchy : partialProviderHierarchyList) {
                    final String dim = partialProviderHierarchy.getDimension() == null ? "NO_DIMENSION_DEFINED" : partialProviderHierarchy.getDimension();
                    HierarchiesUtil.addToMap(scopeElts, dim, partialProviderHierarchy.getName(), partialProviderHierarchy.getMaxLevel());
                }
                partialProviderDefinition.setScope(scopeElts);
            }

            return partialProviderDefinition;
        }

    }

    private static class PartialProviderHierarchy {

        private String name;
        private String dimension;
        private String maxLevel;

        public PartialProviderHierarchy(String name, String dimension, String maxLevel) {
            this.name = name;
            this.dimension = dimension;
            this.maxLevel = maxLevel;
        }

        public String getName() {
            return name;
        }

        public String getDimension() {
            return dimension;
        }

        public String getMaxLevel() {
            return maxLevel;
        }
    }
}
