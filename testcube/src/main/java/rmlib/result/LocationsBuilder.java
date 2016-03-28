package rmlib.result;

import com.quartetfs.biz.pivot.context.IContextValue;
import com.quartetfs.fwk.query.QueryException;
import rmlib.ProgrammaticCube;

public class LocationsBuilder {

    private final ProgrammaticCube testCube;
    private final GetAggregatesQueryBuilder getAggregatesQueryBuilder;
    private LocationBuilder locationBuilder;

    public LocationsBuilder(GetAggregatesQueryBuilder getAggregatesQueryBuilder, ProgrammaticCube testCube) {
        this.getAggregatesQueryBuilder = getAggregatesQueryBuilder;
        this.locationBuilder = new LocationBuilder(testCube.getPivot());
        this.locationBuilder.resetLocationArray();
        this.testCube = testCube;
    }

    public LocationsBuilder withHierarchy(String hierarchyName, Object... levels) {
        locationBuilder.withHierarchy(hierarchyName, levels);
        return this;
    }

    public LocationsBuilder withMeasure(String... measure) {
        getAggregatesQueryBuilder.withMeasure(measure);
        return this;
    }

    public LocationsBuilder withCV(IContextValue contextValue) {
        getAggregatesQueryBuilder.withCV(contextValue);
        return this;
    }

    public AggregatesQueryResult executeQuery() throws QueryException {
        return new AggregatesQueryResult(
                getAggregatesQueryBuilder.execute(), testCube.getPivot());
    }

    public LocationsBuilder addLocation() {
        getAggregatesQueryBuilder.withLocation(locationBuilder.build());
        return this;
    }
}
