package rmlib.debug;

import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.IMultiVersionHierarchy;
import com.quartetfs.biz.pivot.definitions.IAxisDimensionDescription;
import com.quartetfs.biz.pivot.definitions.IAxisHierarchyDescription;
import com.quartetfs.biz.pivot.definitions.IAxisLevelDescription;

import java.util.List;

public class HierarchiesPrintHelper {

    public static void printHierarchies(String cubeName, IActivePivotManager manager) {

        System.out.println("Hierarchies :");
        final IMultiVersionActivePivot ap = manager.getActivePivots().get(cubeName);
        final List<? extends IMultiVersionHierarchy> hierarchies = ap.getHierarchies();
        for(IMultiVersionHierarchy hierarchy : hierarchies) {
            System.out.println("hiearchyName=" + hierarchy.getName());
            for(ILevel level : hierarchy.getLevels()) {
                System.out.println("  levelName=" + level.getName());
            }
        }

        System.out.println("AxisDimensions :");
        List<IAxisDimensionDescription> dimensionDescriptionList = ap.getDescription().getAxisDimensions().getValues();
        for(int i=0; i<dimensionDescriptionList.size();i++) {
            final IAxisDimensionDescription axisDimensionDescription = dimensionDescriptionList.get(i);
            final String dimensionDescriptionName = axisDimensionDescription.getName();
            System.out.println("dimensionName=" + dimensionDescriptionName);
            for(IAxisHierarchyDescription hierarchyDescription : axisDimensionDescription.getHierarchies()) {
                final String hierarchyDescriptionName = hierarchyDescription.getName();
                System.out.println("  hierarchyName=" + hierarchyDescriptionName);
                for (IAxisLevelDescription level : hierarchyDescription.getLevels()) {
                    System.out.println("    levelName=" + level.getLevelName());
                }
            }
        }
    }

}
