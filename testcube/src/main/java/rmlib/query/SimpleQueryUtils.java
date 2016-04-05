package rmlib.query;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import rmlib.result.GetAggregatesQueryBuilder;
import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.context.IContextSnapshot;
import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.impl.Location;
import com.quartetfs.biz.pivot.query.impl.GetAggregatesQuery;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.pivot.mdx.utils.impl.ServicesUtil;
import rmlib.ProgrammaticCube;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SimpleQueryUtils {

    public static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH);

    public static GetAggregatesQueryBuilder createAggregatesQueryBuilder(ProgrammaticCube testCube) {
        return new GetAggregatesQueryBuilder(testCube);
    }

    public static void queryCubeSimple(String cubeName, IActivePivotManager manager) throws QueryException {
        System.out.println("queryCubeSimple");
        final IMultiVersionActivePivot ap = manager.getActivePivots().get(cubeName);
        final GetAggregatesQuery query = createSimpleQuery(ap);
        final ICellSet cellSet = executeQuery(ap, query);
        System.out.println("LocationCount=" + cellSet.getLocationCount());
    }

    public static ICellSet executeQuery(IMultiVersionActivePivot ap, GetAggregatesQuery query) throws QueryException {
        final IContextSnapshot oldContext = ServicesUtil.applyContextValues(ap, query.getContextValues(), true);
        final ICellSet cellSet;
        try {
            cellSet = ap.execute(query);
        }
        finally {
            ServicesUtil.replaceContextValues(ap, oldContext);
        }
        return cellSet;
    }

    public static GetAggregatesQuery createSimpleQuery(IMultiVersionActivePivot ap) {
        final ILocation location = createLocation(ap);
        final Collection<ILocation> locations = ImmutableSet.of(location);
        final Collection<String> measureSelections = Arrays.asList("contributors.COUNT");
        final GetAggregatesQuery query = new GetAggregatesQuery(locations, measureSelections);
        final List<IContextValue> contextValues = Lists.newArrayList();
        query.setContextValues(contextValues);
        return query;
    }

    private static ILocation createLocation(IMultiVersionActivePivot ap) {
        final int cptWildCards = ap.getHierarchies().size() - 1;
        String locationStr = "";
        for(int i=0; i<cptWildCards; i++) {
            if(i>0) {
                locationStr+="|";
            }
            locationStr+=ILocation.WILDCARD;
        }
        return new Location(locationStr);
    }

}
