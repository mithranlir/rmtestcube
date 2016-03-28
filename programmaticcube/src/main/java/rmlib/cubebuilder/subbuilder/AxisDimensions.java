package rmlib.cubebuilder.subbuilder;

import com.quartetfs.biz.pivot.cube.dimension.IDimension;
import com.quartetfs.biz.pivot.definitions.IAxisHierarchyDescription;
import com.quartetfs.biz.pivot.definitions.impl.AxisDimensionDescription;

import java.util.ArrayList;
import java.util.List;

public class AxisDimensions {

    public static AxisDimensionDescriptionBuilder builder() {
        return new AxisDimensionDescriptionBuilder();
    }

    public static class AxisDimensionDescriptionBuilder extends AbstractComponentBuilder<AxisDimensionDescriptionBuilder, AxisDimensionDescription> {

        private String name;
        private IDimension.DimensionType dimensionType = null;
        private List<IAxisHierarchyDescription> axisHierarchyDescriptionList = new ArrayList<>();

        public AxisDimensionDescriptionBuilder withName(String name) {
            this.name = name;
            return self();
        }

        public AxisDimensionDescriptionBuilder withDimensionType(IDimension.DimensionType dimensionType) {
            this.dimensionType = dimensionType;
            return self();
        }

        public AxisDimensionDescriptionBuilder withAxisHierarchyDescription(IAxisHierarchyDescription axisHierarchyDescription) {
            axisHierarchyDescriptionList.add(axisHierarchyDescription);
            return self();
        }

        protected AxisDimensionDescription doBuild() {

            final AxisDimensionDescription axisDimensionDescription = new AxisDimensionDescription();
            axisDimensionDescription.setName(name);

            if(dimensionType!=null) {
                axisDimensionDescription.setDimensionType(dimensionType);
            }

            axisDimensionDescription.setHierarchies(axisHierarchyDescriptionList);

            return axisDimensionDescription;
        }

    }
}
