package rmlib.result;

import com.quartetfs.biz.pivot.ILocation;

import java.util.Map;

public class LocationConverter {

    public String toMatchingStr(ILocation location) {
        return toMatchingStr(location, null);
    }

    public String toMatchingStr(ILocation location, Map<Integer, String> hierarchyToUseMap) {
        final StringBuilder sb = new StringBuilder();
        boolean firstHierarchyToUse = true;
        for(int i=0; i<location.getHierarchyCount(); i++) {
            if(canAppendHierarchy(hierarchyToUseMap, i)) {
                firstHierarchyToUse = appendHierarchySep(sb, firstHierarchyToUse);
                appendLevels(location, sb, i);
            }
        }
        return sb.toString();
    }

    private boolean canAppendHierarchy(Map<Integer, String> hierarchyToUseMap, int i) {
        return hierarchyToUseMap == null || hierarchyToUseMap.containsKey(i);
    }

    private boolean appendHierarchySep(StringBuilder sb, boolean firstHierarchy) {
        if (!firstHierarchy) {
            sb.append("|");
        }
        firstHierarchy = false;
        return firstHierarchy;
    }

    private void appendLevels(ILocation location, StringBuilder sb, int i) {
        int depth = location.getLevelDepth(i);
        for (int j = 0; j < depth; j++) {
            if (j != 0) {
                sb.append("\\");
            }
            final Object object = location.getCoordinate(i, j);
            sb.append(object);
        }
    }

    public String toDebugString(ILocation location) {
        final StringBuilder sb = new StringBuilder();
        sb.append("location content :").append("\n");
        for(int i=0; i<location.getHierarchyCount(); i++) {
            sb.append("hierarchy " + i + " values : ");
            int depth = location.getLevelDepth(i);
            for(int j=0; j<depth; j++) {
                if(j!=0) {
                    sb.append("\\");
                }
                final Object object = location.getCoordinate(i, j);
                sb.append(object);
                if(object!=null) {
                    sb.append("(").append(object.getClass().getName()).append(")");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

}
