package rmlib.result;


import com.google.common.collect.Maps;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellProcedure;
import com.quartetfs.biz.pivot.cellset.ICellSet;

import java.util.*;

import static com.google.common.collect.Maps.newHashMap;

public class CellSetConverter {

    public Map<String, Map<ILocation, Object>> convertCellSetToMap(ICellSet result) {
        final Map<String, Map<ILocation, Object>> measureMap = newHashMap();
        result.forEachCell((new ICellProcedure() {
            @Override
            public boolean execute(ILocation location, String measure, Object value) {
                if(!measureMap.containsKey(measure)) {
                    measureMap.put(measure, Maps.<ILocation, Object>newHashMap());
                }
                measureMap.get(measure).put(location, value);
                return true;
            }
        }));
        return measureMap;
    }

    public String convertCellSetToMatchingStr(ICellSet result, LocationConverter locationConverter, Map<Integer, String> hierarchyToUseMap) {
        final Map<String, Map<ILocation, Object>> measureMap = convertCellSetToMap(result);
        final StringBuilder sb = new StringBuilder();
        boolean headerDone = false;
        for(String measureName : new TreeSet<>(measureMap.keySet())) {
            final Map<ILocation, Object> locationMap = measureMap.get(measureName);
            final List<ILocation> sortedList = createLocationOrderedList(locationMap);
            for(ILocation location : sortedList) {
                headerDone = appendHeader(hierarchyToUseMap, sb, headerDone);
                appendLocation(locationConverter, hierarchyToUseMap, sb, measureName, locationMap, location);
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private boolean appendHeader(Map<Integer, String> hierarchyToUseMap, StringBuilder sb, boolean headerDone) {
        if(!headerDone) {
            sb.append(getHeader(hierarchyToUseMap));
            headerDone = true;
        }
        return headerDone;
    }

    private void appendLocation(LocationConverter locationConverter, Map<Integer, String> hierarchyToUseMap, StringBuilder sb, String measureName, Map<ILocation, Object> locationMap, ILocation location) {
        sb.append("[" + measureName + ":" + locationMap.get(location) + "]");
        sb.append(locationConverter.toMatchingStr(location, hierarchyToUseMap));
    }

    public String getHeader(Map<Integer, String> hierarchyToUseMap) {
        final StringBuilder sb = new StringBuilder();
        if(hierarchyToUseMap!=null) {
            sb.append("[MEASURE:VALUE]");
            final TreeSet<Integer> hierarchyOrdinalSet = new TreeSet<>(hierarchyToUseMap.keySet());
            for (Integer hierarchyOrdinal : hierarchyOrdinalSet) {
                sb.append("|").append(hierarchyToUseMap.get(hierarchyOrdinal));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private List<ILocation> createLocationOrderedList(Map<ILocation, Object> locationMap) {
        final Comparator<ILocation> comparator = new Comparator<ILocation>() {
            @Override
            public int compare(ILocation o1, ILocation o2) {
                return o1.hashCode() - o2.hashCode();
            }
        };
        final List<ILocation> sortedList = new ArrayList<>(locationMap.keySet());
        Collections.sort(sortedList, comparator);
        return sortedList;
    }
}
