package rmlib.result;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.cube.hierarchy.IMultiVersionHierarchy;

import java.util.HashMap;
import java.util.Map;

public class AggregatesQueryResult {

    private final ICellSet result;
    private final IMultiVersionActivePivot pivot;
    private LocationBuilder locationBuilder;
    private Map<String, IMultiVersionHierarchy> hierarchyMap;

    public AggregatesQueryResult(ICellSet result, IMultiVersionActivePivot pivot) {
        this.result = result;
        this.pivot = pivot;
        this.hierarchyMap = buildHierarchyMap(pivot);
    }

    private Map<String, IMultiVersionHierarchy> buildHierarchyMap(IMultiVersionActivePivot pivot) {
        final Map<String, IMultiVersionHierarchy> map = new HashMap<>();
        for(IMultiVersionHierarchy hierarchy : pivot.getHierarchies()) {
            map.put(hierarchy.getName(), hierarchy);
        }
        return map;
    }

    public AggregatesQueryResult assertionLocationBuilder() {
        this.locationBuilder = new LocationBuilder(pivot);
        return this;
    }

    public AggregatesQueryResult withHierarchy(String hierarchyName, Object... levels) {
        locationBuilder.withHierarchy(hierarchyName, levels);
        return this;
    }

    public AggregatesQueryResult measureHasValue(String measure, Object value) {
        CubeMatchers.assertMeasureValue(result, measure, locationBuilder.build(), value);
        return this;
    }

    public AggregatesQueryResult assertDoubleMeasureValueGreaterThan(String measure, Double value) {
        CubeMatchers.assertDoubleMeasureValueGreaterThan(result, measure, locationBuilder.build(), value);
        return this;
    }

    public AggregatesQueryResult assertDoubleMeasureValuLowerThan(String measure, Double value) {
        CubeMatchers.assertDoubleMeasureValueLowerThan(result, measure, locationBuilder.build(), value);
        return this;
    }

    public AggregatesQueryResult hasNumberOfLocationsForMeasure(String measure, int size) {
        CubeMatchers.assertCellSetSizeForMeasure(result, measure, size);
        return this;
    }

    public AggregatesQueryResult hasNoValueForMeasure(String measure) {
        CubeMatchers.assertNoMeasureValue(result, measure);
        return this;
    }

    public Double getMeasureValue(String measure) {
        final ILocation location = locationBuilder.build();
        return CubeMatchers.getMeasureValue(result, measure, location);
    }

    public AggregatesQueryResult assertSameCellSetString(String referenceStr, String... hierarchyNames) {
        Map<Integer, String> hierarchyToUseMap = buildHierarchyToUseMap(hierarchyNames);
        CubeMatchers.assertSameCellSetString(result, referenceStr, hierarchyToUseMap);
        return this;
    }

    private Map<Integer, String> buildHierarchyToUseMap(String[] hierarchyNames) {
        if(hierarchyNames==null) {
            return null;
        }
        final Map<Integer, String> hierarchyToUseMap = new HashMap<>();
        for(String hierarchyName : hierarchyNames) {
            final IMultiVersionHierarchy hierarchy = hierarchyMap.get(hierarchyName);
            final int ordinal = hierarchy.getHierarchyInfo().getOrdinal() - 1;
            hierarchyToUseMap.put(ordinal, hierarchyName);
        }
        return hierarchyToUseMap;
    }

}
