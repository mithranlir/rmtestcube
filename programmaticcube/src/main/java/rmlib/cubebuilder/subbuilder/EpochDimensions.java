package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.definitions.impl.EpochDimensionDescription;
import com.quartetfs.fwk.format.IFormatter;

public class EpochDimensions {

    public static EpochDimensionDescriptionBuilder builder() {
        return new EpochDimensionDescriptionBuilder();
    }

    public static class EpochDimensionDescriptionBuilder extends AbstractComponentBuilder<EpochDimensionDescriptionBuilder, EpochDimensionDescription> {

        private IFormatter formatter = null;
        private Boolean enabled = null;
        private String folder = null;
        private String levelComparator = null;
        private String measureGroups = null;
        private Boolean visible = null;

        public EpochDimensionDescriptionBuilder withFormatter(IFormatter formatter) {
            this.formatter = formatter;
            return self();
        }

        public EpochDimensionDescriptionBuilder withEnabled(Boolean enabled) {
            this.enabled = enabled;
            return self();
        }

        public EpochDimensionDescriptionBuilder withFolder(String folder) {
            this.folder = folder;
            return self();
        }

        public EpochDimensionDescriptionBuilder withLevelComparator(String levelComparator) {
            this.levelComparator = levelComparator;
            return self();
        }

        public EpochDimensionDescriptionBuilder withMeasureGroups(String measureGroups) {
            this.measureGroups = measureGroups;
            return self();
        }

        public EpochDimensionDescriptionBuilder withVisible(Boolean visible) {
            this.visible = visible;
            return self();
        }

        protected EpochDimensionDescription doBuild() {
            final EpochDimensionDescription epochDimensionDescription = new EpochDimensionDescription();

            if(formatter!=null) {
                epochDimensionDescription.setFormatter(formatter);
            }

            if(enabled!=null) {
                epochDimensionDescription.setEnabled(enabled);
            }

            if(folder!=null) {
                epochDimensionDescription.setFolder(folder);
            }

            if(levelComparator!=null) {
                epochDimensionDescription.setLevelComparator(levelComparator);
            }

            if(measureGroups!=null) {
                epochDimensionDescription.setMeasureGroups(measureGroups);
            }

            if(visible!=null) {
                epochDimensionDescription.setVisible(visible);
            }

            return epochDimensionDescription;
        }
    }
}
