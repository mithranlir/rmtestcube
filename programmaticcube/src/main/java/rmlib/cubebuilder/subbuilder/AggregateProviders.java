package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.definitions.IPartialProviderDefinition;
import com.quartetfs.biz.pivot.definitions.impl.AggregateProviderDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AggregateProviders {

    public static AggregateProviderBuilder builder() {
        return new AggregateProviderBuilder();
    }

    public static class AggregateProviderBuilder extends AbstractComponentBuilder<AggregateProviderBuilder, AggregateProviderDefinition> {

        private String pluginKey = null;
        private Properties properties = null;
        private List<IPartialProviderDefinition> partialProviders = null;

        public AggregateProviderBuilder withPluginKey(String pluginKey) {
            this.pluginKey = pluginKey;
            return self();
        }

        public AggregateProviderBuilder withProperty(String key, String value) {
            if(this.properties == null) {
                this.properties = new Properties();
            }
            this.properties.put(key, value);
            return self();
        }

        public AggregateProviderBuilder withPartialProvider(IPartialProviderDefinition partialProviderDefinition) {

            if(this.partialProviders == null) {
                this.partialProviders = new ArrayList<>();
            }

            this.partialProviders.add(partialProviderDefinition);
            return self();
        }


        protected AggregateProviderDefinition doBuild() {

            final AggregateProviderDefinition aggregateProviderDefinition = new AggregateProviderDefinition();

            aggregateProviderDefinition.setPluginKey(pluginKey);

            if(properties!=null) {
                aggregateProviderDefinition.setProperties(properties);
            }

            if(partialProviders!=null) {
                aggregateProviderDefinition.setPartialProviders(partialProviders);
            }

            return aggregateProviderDefinition;
        }
    }

}
