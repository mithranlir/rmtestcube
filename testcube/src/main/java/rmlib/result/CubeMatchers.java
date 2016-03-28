package rmlib.result;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.cellset.ICellSet;

import java.util.Map;

public class CubeMatchers {

    private static CellSetConverter cellSetConverter = new CellSetConverter();
    private static LocationConverter locationConverter = new LocationConverter();

    public static void assertMeasureValue(ICellSet result, String measure, ILocation location, Object expectedValue) {
        final Map<String, Map<ILocation, Object>> measureMap = cellSetConverter.convertCellSetToMap(result);
        checkMeasureAndLocation(measure, location, measureMap);
        if(!expectedValue.equals(measureMap.get(measure).get(location))) {
            throw new AssertionError("wrong value for location " + location + " expected : " + expectedValue
                    + ", was: " + measureMap.get(measure).get(location));
        }
    }

    private static void checkMeasureAndLocation(String measure, ILocation location, Map<String, Map<ILocation, Object>> measureMap) {
        if(!measureMap.containsKey(measure)) {
            throw new AssertionError("Measure " + measure + " doesn't exist in results");
        }
        if(!measureMap.get(measure).containsKey(location)) {
            throw new AssertionError("Result doesn't contain location " + location);
        }
    }

    public static void assertDoubleMeasureValueGreaterThan(ICellSet result, String measure, ILocation location, Double value) {
        final Map<String, Map<ILocation, Object>> measureMap = cellSetConverter.convertCellSetToMap(result);
        checkMeasureAndLocation(measure, location, measureMap);
        if( ((Double)measureMap.get(measure).get(location)).compareTo(value)<=0) {
            throw new AssertionError("wrong value for location " + location
                    + ", " + measureMap.get(measure).get(location) + " was not greater than value : " + value);
        }
    }

    public static void assertDoubleMeasureValueLowerThan(ICellSet result, String measure, ILocation location, Double value) {
        final Map<String, Map<ILocation, Object>> measureMap = cellSetConverter.convertCellSetToMap(result);
        checkMeasureAndLocation(measure, location, measureMap);
        if( ((Double)measureMap.get(measure).get(location)).compareTo(value)>=0) {
            throw new AssertionError("wrong value for location " + location
                    + ", " + measureMap.get(measure).get(location) + " was not lower than value : " + value);
        }
    }

    public static void assertNoMeasureValue(ICellSet result, String measure) {
        final Map<String, Map<ILocation, Object>> measureMap = cellSetConverter.convertCellSetToMap(result);
        if(measureMap.containsKey(measure)) {
            throw new AssertionError("Results contain measure " + measure);
        }
    }

    public static void assertCellSetSizeForMeasure(ICellSet result, String measure, int size) {
        final Map<String, Map<ILocation, Object>> measureMap = cellSetConverter.convertCellSetToMap(result);
        if(size == 0) {
            throw new AssertionError("Result doesn't contain measure " + measure);
        }
        else {
            if(!measureMap.containsKey(measure)) {
                throw new AssertionError("Result doesn't contain measure" + measure);
            }
            //assertThat(measureMap.get(measure).size(), equalTo(size));

            if(measureMap.get(measure).size()!=size) {
                throw new AssertionError("assertCellSetSizeForMeasure");
            }
        }
    }

    public static Double getMeasureValue(ICellSet result, String measure, ILocation location) {
        final Map<String, Map<ILocation, Object>> measureMap = cellSetConverter.convertCellSetToMap(result);
        if(!measureMap.containsKey(measure)) {
            return null;
        }
        if(!measureMap.get(measure).containsKey(location)) {
            return null;
        }
        return (Double) measureMap.get(measure).get(location);
    }

    public static void assertSameCellSetString(ICellSet result, String strReference, Map<Integer, String> hierarchyToUseMap) {
        final String resultStr = cellSetConverter.convertCellSetToMatchingStr(result, locationConverter, hierarchyToUseMap);
        if(!strReference.trim().equals(resultStr.trim())) {
            throw new AssertionError("Result is not equals to reference.\nResult=\n" + resultStr + "\n\nReference=\n" + strReference);
        }
    }

}
