package rmlib.example.checks;

import com.quartetfs.biz.pivot.IActivePivotManager;
import com.quartetfs.biz.pivot.IMultiVersionActivePivot;
import com.quartetfs.biz.pivot.cube.hierarchy.ILevel;
import com.quartetfs.biz.pivot.cube.hierarchy.IMultiVersionHierarchy;
import com.quartetfs.fwk.query.QueryException;
import rmlib.ProgrammaticCube;
import rmlib.query.SimpleQueryUtils;
import rmlib.result.AggregatesQueryResult;

import java.text.ParseException;
import java.util.List;

public class RiskResultHelper {

    public static void queryCubeAndCheckResults(String cubeName, IActivePivotManager manager) throws QueryException, ParseException {

        System.out.println("queryCubeAndCheckResults");

        final IMultiVersionActivePivot ap = manager.getActivePivots().get(cubeName);
        final ProgrammaticCube testCube = new ProgrammaticCube(manager, ap, null, null, null, null, null, null, false);

        final AggregatesQueryResult result2 = SimpleQueryUtils.createAggregatesQueryBuilder(testCube)
                .withHierarchy("HistoricalDates", new Object[]{ null })
                .addLocation()
                .withMeasure("contributors.COUNT")
                .executeQuery();

        final String refResult = "" +
                "[MEASURE:VALUE]|HistoricalDates\n" +
                "[contributors.COUNT:1]Fri Jan 01 00:00:00 CET 2016\n" +
                "[contributors.COUNT:1]Thu Dec 31 00:00:00 CET 2015\n" +
                "[contributors.COUNT:1]Wed Dec 30 00:00:00 CET 2015\n" +
                "[contributors.COUNT:1]Tue Dec 29 00:00:00 CET 2015\n" +
                "[contributors.COUNT:1]Mon Dec 28 00:00:00 CET 2015";
        result2.assertSameCellSetString(refResult, "HistoricalDates");


        result2.assertionLocationBuilder()
                .withHierarchy("HistoricalDates", SimpleQueryUtils.sdf.parse("Fri Jan 01 00:00:00 CET 2016"))
                .measureHasValue("contributors.COUNT", 1L);

    }


    public static void printHierarchies(String cubeName, IActivePivotManager manager) {

        System.out.println("printHierarchies");

        final IMultiVersionActivePivot ap = manager.getActivePivots().get(cubeName);
        final List<? extends IMultiVersionHierarchy> hierarchies = ap.getHierarchies();
        for(IMultiVersionHierarchy hierarchy : hierarchies) {
            System.out.println("hiearchyName=" + hierarchy.getName());
            for(ILevel level : hierarchy.getLevels()) {
                System.out.println("  levelName=" + level.getName());
            }
        }
    }


}
