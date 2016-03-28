package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.definitions.IAxisDimensionDescription;
import com.quartetfs.biz.pivot.definitions.impl.AxisDimensionsDescription;

import java.util.ArrayList;
import java.util.List;

public class Dimensions {

    public static AxisDimensionsDescriptionBuilder builder() {
        return new AxisDimensionsDescriptionBuilder();
    }

    public static class AxisDimensionsDescriptionBuilder extends AbstractComponentBuilder<AxisDimensionsDescriptionBuilder, AxisDimensionsDescription> {

        private final List<IAxisDimensionDescription> axisDimensionDescriptionList = new ArrayList<>();

        public AxisDimensionsDescriptionBuilder withAxisDimensionDescription(IAxisDimensionDescription axisDimension) {
            axisDimensionDescriptionList.add(axisDimension);
            return self();
        }

        protected AxisDimensionsDescription doBuild() {
            final AxisDimensionsDescription axisDimensionsDescription = new AxisDimensionsDescription();
            axisDimensionsDescription.addValues(axisDimensionDescriptionList);
            return axisDimensionsDescription;
        }
    }

}
