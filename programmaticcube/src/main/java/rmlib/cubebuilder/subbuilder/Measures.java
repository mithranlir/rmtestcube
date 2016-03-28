package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.definitions.IAggregatedMeasureDescription;
import com.quartetfs.biz.pivot.definitions.INativeMeasureDescription;
import com.quartetfs.biz.pivot.definitions.IPostProcessorDescription;
import com.quartetfs.biz.pivot.definitions.impl.MeasuresDescription;

import java.util.ArrayList;
import java.util.List;

public class Measures {

    public static MeasuresDescriptionBuilder builder() {
        return new MeasuresDescriptionBuilder();
    }

    public static class MeasuresDescriptionBuilder extends AbstractComponentBuilder<MeasuresDescriptionBuilder, MeasuresDescription> {

        private final List<INativeMeasureDescription> nativeMeasureDescriptionList = new ArrayList<>();
        private final List<IAggregatedMeasureDescription> aggregatedMeasureDescriptionList = new ArrayList<>();
        private final List<IPostProcessorDescription> postProcessorDescriptionList = new ArrayList<>();

        public MeasuresDescriptionBuilder withAggregatedMeasure(IAggregatedMeasureDescription aggregatedMeasureDescription) {
            aggregatedMeasureDescriptionList.add(aggregatedMeasureDescription);
            return self();
        }

        public MeasuresDescriptionBuilder withPostProcessor(IPostProcessorDescription postProcessorDescription) {
            postProcessorDescriptionList.add(postProcessorDescription);
            return self();
        }

        public MeasuresDescriptionBuilder withNativeMeasure(INativeMeasureDescription nativeMeasureDescription) {
            nativeMeasureDescriptionList.add(nativeMeasureDescription);
            return self();
        }

        protected MeasuresDescription doBuild() {

            final MeasuresDescription measuresDescription = new MeasuresDescription();

            measuresDescription.setNativeMeasures(nativeMeasureDescriptionList);
            measuresDescription.setAggregatedMeasuresDescription(aggregatedMeasureDescriptionList);
            measuresDescription.setPostProcessorsDescription(postProcessorDescriptionList);

            return measuresDescription;
        }
    }

}
