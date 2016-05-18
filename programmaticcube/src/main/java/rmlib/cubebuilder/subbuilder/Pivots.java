package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.context.impl.QueriesTimeLimit;
import com.quartetfs.biz.pivot.definitions.*;
import com.quartetfs.biz.pivot.definitions.impl.ActivePivotDescription;
import com.quartetfs.biz.pivot.definitions.impl.ContextValuesDescription;

import java.util.ArrayList;
import java.util.Collection;

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
        private Integer queriesTimeLimit = null;


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

        public PivotDescriptionBuilder withQueriesTimeLimit(int queriesTimeLimit) {
            this.queriesTimeLimit = queriesTimeLimit;
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

            if(queriesTimeLimit!=null) {

                final IContextValuesDescription contextValuesDescription = new ContextValuesDescription();

                final Collection<IContextValue> ctvValues = new ArrayList<>();
                final QueriesTimeLimit queriesTimeLimitCV = new QueriesTimeLimit(queriesTimeLimit);
                ctvValues.add(queriesTimeLimitCV);

                contextValuesDescription.addValues(ctvValues);

                pivotDescription.setSharedContexts(contextValuesDescription);
            }

            return pivotDescription;
        }
    }

}
