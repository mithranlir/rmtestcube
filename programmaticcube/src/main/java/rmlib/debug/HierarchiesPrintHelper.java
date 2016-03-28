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
    private static void printHierarchies(String cubeName, IActivePivotManager manager) {
        System.out.println("Hierarchies :");
        final IMultiVersionActivePivot ap = manager.getActivePivots().get(cubeName);
        final List<? extends IMultiVersionHierarchy> hierarchies = ap.getHierarchies();
        for(IMultiVersionHierarchy hierarchy : hierarchies) {
            System.out.println("hiearchyName=" + hierarchy.getName());
            for(ILevel level : hierarchy.getLevels()) {
                System.out.println("  levelName=" + level.getName());
            }

            final String retrievedFirstMemberForDepth0 = hierarchy.getLatestVersion().retrieveMembers(-1).get(0).getName();
            System.out.println("retrievedFirstMember for depth 0 : " + retrievedFirstMemberForDepth0);

            final String retrievedFirstMemberForDepth1 = hierarchy.getLatestVersion().retrieveMembers(0).get(0).getName();
            System.out.println("retrievedFirstMember for depth 1 : " + retrievedFirstMemberForDepth1);
        }

        System.out.println("AxisDimensions :");
        List<IAxisDimensionDescription> dimensionDescriptionList = ap.getDescription().getAxisDimensions().getValues();
        for(int i=0; i<dimensionDescriptionList.size();i++) {
            final IAxisDimensionDescription axisDimensionDescription = dimensionDescriptionList.get(i);
            final String dimensionDescriptionName = axisDimensionDescription.getName();
            System.out.println("dimensionName=" + dimensionDescriptionName);
            for(IAxisHierarchyDescription hierarchyDescription : axisDimensionDescription.getHierarchies()) {
                final String hierarchyDescriptionName = hierarchyDescription.getName();
                System.out.println("  hieararchyName=" + hierarchyDescriptionName);
                for (IAxisLevelDescription level : hierarchyDescription.getLevels()) {
                    System.out.println("    levelName=" + level.getLevelName());
                }
            }
        }
    }

}
