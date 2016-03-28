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

public class QueryUtils {

    public static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzzz yyyy", Locale.ENGLISH);

    public static GetAggregatesQueryBuilder createAggregatesQueryBuilder(ProgrammaticCube testCube) {
        return new GetAggregatesQueryBuilder(testCube);
    }

    public static void queryCubeSimple(String cubeName, IActivePivotManager manager, int nbWildCards) throws QueryException {
        final GetAggregatesQuery query = createSimpleQuery(nbWildCards);
        final ICellSet cellSet = executeQuery(cubeName, manager, query);
        System.out.println("LocationCount=" + cellSet.getLocationCount());
    }

    public static ICellSet executeQuery(String cubeName, IActivePivotManager manager, GetAggregatesQuery query) throws QueryException {
        final IMultiVersionActivePivot ap = manager.getActivePivots().get(cubeName);
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

    public static GetAggregatesQuery createSimpleQuery(int cptWildCards) {

        final Collection<ILocation> locations = ImmutableSet.of(
                (ILocation) new Location(ILocation.WILDCARD));

        final Collection<String> measureSelections = Arrays.asList("contributors.COUNT");
        final GetAggregatesQuery query = new GetAggregatesQuery(locations, measureSelections);
        final List<IContextValue> contextValues = Lists.newArrayList();
        query.setContextValues(contextValues);
        return query;
    }

}
