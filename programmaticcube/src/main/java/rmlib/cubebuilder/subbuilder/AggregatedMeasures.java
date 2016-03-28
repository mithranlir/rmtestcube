package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.definitions.impl.AggregatedMeasureDescription;

import java.util.Properties;

public class AggregatedMeasures {

    public static AggregatedMeasureDescriptionBuilder builder() {
        return new AggregatedMeasureDescriptionBuilder();
    }

    public static class AggregatedMeasureDescriptionBuilder extends AbstractComponentBuilder<AggregatedMeasureDescriptionBuilder, AggregatedMeasureDescription> {

        private String name = null;
        private String fieldName = null;
        private String folder = null;
        private Boolean visible = null;
        private String group = null;
        private Properties properties = null;
        private Boolean swap = null;
        private String preProcessedAggregation = null;

        public AggregatedMeasureDescriptionBuilder withName(String name) {
            this.name = name;
            return self();
        }

        public AggregatedMeasureDescriptionBuilder withFieldName(String fieldName) {
            this.fieldName = fieldName;
            return self();
        }

        public AggregatedMeasureDescriptionBuilder withFolder(String folder) {
            this.folder = folder;
            return self();
        }

        public AggregatedMeasureDescriptionBuilder withVisible(boolean visible) {
            this.visible = visible;
            return self();
        }

        public AggregatedMeasureDescriptionBuilder withGroup(String group) {
            this.group = group;
            return self();
        }

        public AggregatedMeasureDescriptionBuilder withProperty(String key, String value) {
            if(properties==null) {
                properties = new Properties();
            }
            this.properties.setProperty(key, value);
            return self();
        }

        public AggregatedMeasureDescriptionBuilder withSwap(boolean swap) {
            this.swap = swap;
            return self();
        }

        public AggregatedMeasureDescriptionBuilder withPreProcessedAggregation(String preProcessedAggregation) {
            this.preProcessedAggregation = preProcessedAggregation;
            return self();
        }

        protected AggregatedMeasureDescription doBuild() {

            final AggregatedMeasureDescription aggregatedMeasureDescription = new AggregatedMeasureDescription();

            aggregatedMeasureDescription.setName(name);

            if(fieldName!=null) {
                aggregatedMeasureDescription.setFieldName(fieldName);
            }

            if(folder!=null) {
                aggregatedMeasureDescription.setFolder(folder);
            }
            if(visible!=null) {
                aggregatedMeasureDescription.setVisible(visible);
            }
            if(group!=null) {
                aggregatedMeasureDescription.setGroup(group);
            }
            if(properties!=null) {
                aggregatedMeasureDescription.setProperties(properties);
            }
            if(swap!=null) {
                aggregatedMeasureDescription.setSwap(swap);
            }
            if(preProcessedAggregation!=null) {
                aggregatedMeasureDescription.setPreProcessedAggregation(preProcessedAggregation);
            }
            return aggregatedMeasureDescription;
        }
    }
}
