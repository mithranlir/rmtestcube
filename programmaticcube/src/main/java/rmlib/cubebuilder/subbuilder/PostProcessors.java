package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.definitions.impl.PostProcessorDescription;
import com.quartetfs.fwk.format.IFormatter;

import java.util.Properties;

public class PostProcessors {

    public static PostProcessorDescriptionBuilder builder() {
        return new PostProcessorDescriptionBuilder();
    }

    public static class PostProcessorDescriptionBuilder extends AbstractComponentBuilder<PostProcessorDescriptionBuilder, PostProcessorDescription> {

        private String name = null;
        private String pluginKey = null;
        private String underlyingMeasures = null;
        private String folder = null;
        private Boolean visible = null;
        private String group = null;
        private String continuousQueryHandlers = null;
        private Properties properties = null;
        private IFormatter formatter = null;

        public PostProcessorDescriptionBuilder withName(String name) {
            this.name = name;
            return self();
        }

        public PostProcessorDescriptionBuilder withPluginKey(String pluginKey) {
            this.pluginKey = pluginKey;
            return self();
        }

        public PostProcessorDescriptionBuilder withUnderlyingMeasures(String underlyingMeasures) {
            this.underlyingMeasures = underlyingMeasures;
            return self();
        }

        public PostProcessorDescriptionBuilder withFolder(String folder) {
            this.folder = folder;
            return self();
        }

        public PostProcessorDescriptionBuilder withVisible(boolean visible) {
            this.visible = visible;
            return self();
        }

        public PostProcessorDescriptionBuilder withGroup(String group) {
            this.group = group;
            return self();
        }

        public PostProcessorDescriptionBuilder withFormatter(IFormatter formatter) {
            this.formatter = formatter;
            return self();
        }

        public PostProcessorDescriptionBuilder withContinuousQueryHandlers(String continuousQueryHandlers) {
            this.continuousQueryHandlers = continuousQueryHandlers;
            return self();
        }

        public PostProcessorDescriptionBuilder withProperty(String key, String value) {
            if(this.properties==null) {
                this.properties = new Properties();
            }
            this.properties.setProperty(key, value);
            return self();
        }

        protected PostProcessorDescription doBuild() {

            final PostProcessorDescription postProcessorDescription = new PostProcessorDescription();
            postProcessorDescription.setName(name);
            postProcessorDescription.setPluginKey(pluginKey);

            if(underlyingMeasures!=null) {
                postProcessorDescription.setUnderlyingMeasures(underlyingMeasures);
            }

            if(folder!=null) {
                postProcessorDescription.setFolder(folder);
            }

            if(visible!=null) {
                postProcessorDescription.setVisible(visible);
            }

            if(group!=null) {
                postProcessorDescription.setGroup(group);
            }

            if(continuousQueryHandlers!=null) {
                postProcessorDescription.setContinuousQueryHandlers(continuousQueryHandlers);
            }

            if(properties!=null) {
                postProcessorDescription.setProperties(properties);
            }

            if(formatter != null) {
                postProcessorDescription.setFormatter(formatter);
            }

            return postProcessorDescription;
        }
    }

}
