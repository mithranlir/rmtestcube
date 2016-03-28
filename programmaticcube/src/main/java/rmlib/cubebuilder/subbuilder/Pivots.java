package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.definitions.IAggregateProviderDefinition;
import com.quartetfs.biz.pivot.definitions.IAxisDimensionsDescription;
import com.quartetfs.biz.pivot.definitions.IEpochDimensionDescription;
import com.quartetfs.biz.pivot.definitions.IMeasuresDescription;
import com.quartetfs.biz.pivot.definitions.impl.ActivePivotDescription;

public class Pivots {

    public static PivotDescriptionBuilder builder() {
        return new PivotDescriptionBuilder();
    }

    public static class PivotDescriptionBuilder extends AbstractComponentBuilder<PivotDescriptionBuilder, ActivePivotDescription> {

        private IAxisDimensionsDescription axisDimensionsDescription;
        private IMeasuresDescription measuresDescription;
        private IEpochDimensionDescription epochDimensionDescription = null;
        private Integer aggregatesCacheSize = null;
        private Boolean autoFactlessHierarchies = null;
        private IAggregateProviderDefinition aggregateProvider = null;

        public PivotDescriptionBuilder withAxisDimensionsDescription(IAxisDimensionsDescription axisDimensionsDescription) {
            this.axisDimensionsDescription = axisDimensionsDescription;
            return self();
        }

        public PivotDescriptionBuilder withMeasuresDescription(IMeasuresDescription measuresDescription) {
            this.measuresDescription = measuresDescription;
            return self();
        }

        public PivotDescriptionBuilder withEpochDimensionDescription(IEpochDimensionDescription epochDimensionDescription) {
            this.epochDimensionDescription = epochDimensionDescription;
            return self();
        }

        public PivotDescriptionBuilder withAggregatesCacheSize(Integer aggregatesCacheSize) {
            this.aggregatesCacheSize = aggregatesCacheSize;
            return self();
        }

        public PivotDescriptionBuilder withAggregateProviderDefinition(IAggregateProviderDefinition aggregateProvider) {
            this.aggregateProvider = aggregateProvider;
            return self();
        }

        protected ActivePivotDescription doBuild() {
            final ActivePivotDescription pivotDescription = new ActivePivotDescription();
            pivotDescription.setAxisDimensions(axisDimensionsDescription);
            pivotDescription.setMeasuresDescription(measuresDescription);

            if(epochDimensionDescription!=null) {
                pivotDescription.setEpochDimensionDescription(epochDimensionDescription);
            }

            if(aggregateProvider!=null) {
                pivotDescription.setAggregateProvider(aggregateProvider);
            }

            if(aggregatesCacheSize!=null) {
                pivotDescription.setAggregatesCacheSize(aggregatesCacheSize);
            }

            if(autoFactlessHierarchies!=null) {
                pivotDescription.setAutoFactlessHierarchies(autoFactlessHierarchies);
            }

            return pivotDescription;
        }
    }

}
