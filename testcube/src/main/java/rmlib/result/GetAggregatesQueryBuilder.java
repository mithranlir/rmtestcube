package rmlib.result;

import com.quartetfs.biz.pivot.ILocation;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.cellset.ICellSet;
import com.quartetfs.biz.pivot.context.IContextSnapshot;
import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.biz.pivot.query.impl.GetAggregatesQuery;
import com.quartetfs.fwk.query.QueryException;
import com.quartetfs.pivot.mdx.utils.impl.ServicesUtil;
import rmlib.IProgrammaticCube;

import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

public class GetAggregatesQueryBuilder {

    private final IProgrammaticCube testCube;
    private List<ILocation> locations = newArrayList();
    private List<String> measures = newArrayList();
    private List<IContextValue> contextValues = newArrayList();
    private LocationsBuilder locationsBuilder;

    public GetAggregatesQueryBuilder(IProgrammaticCube testCube) {
        this.testCube = testCube;
        this.locationsBuilder = new LocationsBuilder(this, testCube);
    }

    public GetAggregatesQueryBuilder withLocation(ILocation location) {
        locations.add(location);
        return this;
    }

    public GetAggregatesQueryBuilder withMeasure(String... measure) {
        measures.addAll(Arrays.asList(measure));
        return this;
    }

    public GetAggregatesQueryBuilder withCV(IContextValue contextValue) {
        this.contextValues.add(contextValue);
        return this;
    }

    public ICellSet execute() throws QueryException {
        final GetAggregatesQuery query = createGetAggregateQuery();
        query.setContextValues(contextValues);

        final IMultiVersionActivePivot pivot = testCube.getPivot();
        final IContextSnapshot oldContext = ServicesUtil.applyContextValues(pivot, query.getContextValues(), true);
        final ICellSet cellSet;
        try {
            cellSet = pivot.execute(query);
        }
        finally {
            ServicesUtil.replaceContextValues(pivot, oldContext);
        }
        return cellSet;
    }

    public GetAggregatesQuery createGetAggregateQuery() {
        return new GetAggregatesQuery(locations, measures);
    }

    public LocationsBuilder withHierarchy(String hierarchyName, Object[] levels) {
        locationsBuilder.withHierarchy(hierarchyName, levels);
        return locationsBuilder;
    }

    public GetAggregatesQueryBuilder clear() {
        locationsBuilder = new LocationsBuilder(this, testCube);
        locations.clear();
        measures.clear();
        return this;
    }


}
