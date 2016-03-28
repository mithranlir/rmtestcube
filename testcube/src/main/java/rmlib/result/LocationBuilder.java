package rmlib.result;

import com.google.common.collect.Sets;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.IMultiVersionHierarchy;
import com.quartetfs.biz.pivot.cube.hierarchy.axis.impl.AAnalysisHierarchy;
import com.quartetfs.biz.pivot.definitions.IAxisDimensionDescription;
import com.quartetfs.biz.pivot.definitions.IAxisHierarchyDescription;
import com.quartetfs.biz.pivot.impl.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LocationBuilder {

    private final List<IAxisDimensionDescription> dimensionDescriptionList;
    private final List<IMultiVersionHierarchy> hierarchiesList;
    private final Set<Integer> usedHierarchies = Sets.newHashSet();
    private Object[][] locationArray;

    public LocationBuilder(IMultiVersionActivePivot pivot) {
        dimensionDescriptionList = pivot.getDescription().getAxisDimensions().getValues();
        hierarchiesList = removeFirstElement(pivot.getHierarchies());
        resetLocationArray();
    }

    public void resetLocationArray() {
        locationArray = new Object[hierarchiesList.size()][];
    }

    private List<IMultiVersionHierarchy> removeFirstElement(List<? extends IMultiVersionHierarchy> list) {
        final List<IMultiVersionHierarchy> result = new ArrayList<>();
        for(int i=1; i<list.size();i++) {
            result.add(list.get(i));
        }
        return result;
    }

    public LocationBuilder withHierarchy(String hierarchyName, Object... levels) {

        // not take account first element of hierarchiesList because MEASURES hierarchy...
        for(int i=0; i<hierarchiesList.size();i++) {
            final IMultiVersionHierarchy hierarchy = hierarchiesList.get(i);
            if (hierarchyName.equals(hierarchy.getName())) {
                locationArray[i] = levels;
                usedHierarchies.add(i);
            }
            else if (!usedHierarchies.contains(i)) {
                if(hierarchy instanceof AAnalysisHierarchy) {
                    locationArray[i] = generateDefaultLocation((AAnalysisHierarchy) hierarchy);
                }
                else {
                    final String dimensionName = hierarchy.getHierarchyInfo().getDimensionInfo().getName();
                    final IAxisHierarchyDescription hierarchyDescription = findHierarchyDescription(dimensionName, hierarchy.getName());
                    if(hierarchyDescription!=null) {
                        if(hierarchyDescription.isAllMembersEnabled()) {
                            locationArray[i] = new Object[] { ILevel.ALLMEMBER };
                        }
                        else {
                            // depth = 0 -> first level
                            //final String retrievedFirstMember = hierarchy.getLatestVersion().retrieveMembers(0).get(0).getName();
                            //locationArray[i] = new Object[] { retrievedFirstMember };
                            //locationArray[i] = new Object[] { null };
                            locationArray[i] = null;
                        }
                    }
                }
            }
        }
        return this;
    }

    private IAxisHierarchyDescription findHierarchyDescription(String dimensionName, String hierarchyName) {
        for(int i=0; i<dimensionDescriptionList.size();i++) {
            final IAxisDimensionDescription axisDimensionDescription = dimensionDescriptionList.get(i);
            if(dimensionName.equals(axisDimensionDescription.getName())) {
                for(IAxisHierarchyDescription hierarchyDescription : axisDimensionDescription.getHierarchies()) {
                    if(hierarchyName.equals(hierarchyDescription.getName())) {
                        return hierarchyDescription;
                    }
                }
            }
        }
        return null;
    }

    private Object[] generateDefaultLocation(AAnalysisHierarchy analysisHierarchy) {
        final int levelCount = analysisHierarchy.getLevels().size();
        Object[] path = new Object[levelCount];
        for(int i=0; i< levelCount; i++) {
            path[i] =  analysisHierarchy.getDefaultDiscriminator(i);
        }
        return path;
    }

    public ILocation build() {
        final ILocation location = new Location(locationArray);
        usedHierarchies.clear();
        resetLocationArray();
        return location;
    }

}
