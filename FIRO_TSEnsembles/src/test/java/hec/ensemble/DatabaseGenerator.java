package hec.ensemble;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import hec.*;

public class DatabaseGenerator {

    /**
     * Creates a EnsembleTimeSeriesDatabase
     * with multiple locations and issue dates
     *
     * @param filename name of database to create
     * @param numberOfDates number of forecasts to include
     */
    static public void createTestDatabase(String filename, int numberOfDates) throws Exception {

        String watershedName = "Kanektok";
        String suffix = "_hefs_csv_hourly";
        ZonedDateTime issueDate1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime issueDate2 = issueDate1.plusDays(numberOfDates);

        String cacheDir = TestingPaths.instance.getCacheDir(watershedName, issueDate1, suffix);
        CsvEnsembleReader csvReader = new CsvEnsembleReader(cacheDir, suffix);
        EnsembleTimeSeries[] ets = csvReader.Read(watershedName, issueDate1, issueDate2);
        EnsembleDatabase db = new SqliteDatabase(filename, SqliteDatabase.CREATION_MODE.CREATE_NEW);

        db.write(ets);
        db.close();
    }

}
