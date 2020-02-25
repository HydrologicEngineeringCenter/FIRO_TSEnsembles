package hec.ensemble;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import hec.*;

public class DatabaseGenerator {

    /**
     * Creates a EnsembleTimeSeriesDatabase
     * with multiple locations and issue dates
     *
     * @param filename name of database to create
     * @param numberOfDates number of forecasts to include
     */
    static public TimeSeriesDatabase createTestDatabase(String filename, int numberOfDates) throws Exception {

        String cacheDir = TestingPaths.instance.getCacheDir();
        ZonedDateTime issueDate1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime issueDate2 = issueDate1.plusDays(numberOfDates);

        CsvEnsembleReader csvReader = new CsvEnsembleReader(cacheDir);
        EnsembleTimeSeries[] ets = csvReader.Read("Kanektok", issueDate1, issueDate2);
        TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(filename, JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW);

        db.write(ets);
        return db;
    }
    static public TimeSeriesDatabase create1997TestDatabase(String filename) throws Exception {

        int numberOfDates = 30;
        String cacheDir = "c:/temp/hefs_cache"; // Karl's has 67 GB of ensemble data in this dir for experiments.

        ZonedDateTime issueDate1 = ZonedDateTime.of(2015, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime issueDate2 = issueDate1.plusDays(numberOfDates);

        CsvEnsembleReader csvReader = new CsvEnsembleReader(cacheDir.toString());
        EnsembleTimeSeries[] ets = csvReader.Read("RussianNapa", issueDate1, issueDate2);
        TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(filename, JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW);
        ZonedDateTime startDateTime = ZonedDateTime.of(1996, 12, 24, 12, 0, 0, 0, ZoneId.of("GMT"));

         // modify start/issue dates/ location
        TimeSeriesIdentifier tsid = new TimeSeriesIdentifier("Coyote.fake_forecast","flow");
            EnsembleTimeSeries a = ets[0]; // take first location
            List<ZonedDateTime> dates = a.getIssueDates();

            EnsembleTimeSeries ets2 = new EnsembleTimeSeries(db,tsid,a.getUnits(),a.getDataType(),a.getVersion());
            ZonedDateTime newIssueDate = startDateTime;
            for (int j = 0; j < a.getCount(); j++) {
                Ensemble e = a.getEnsemble(dates.get(j));
                Ensemble e2 = new Ensemble(newIssueDate, e.getValues(), newIssueDate, e.getInterval());
                newIssueDate = newIssueDate.plusDays(1);
                ets2.addEnsemble(e2);
            }

        db.write(ets2);
        return db;
    }
}
