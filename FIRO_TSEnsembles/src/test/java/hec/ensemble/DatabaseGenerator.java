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
    static public EnsembleDatabase createTestDatabase(String filename, int numberOfDates) throws Exception {

        String cacheDir = TestingPaths.instance.getCacheDir();
        ZonedDateTime issueDate1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime issueDate2 = issueDate1.plusDays(numberOfDates);

        CsvEnsembleReader csvReader = new CsvEnsembleReader(cacheDir);
        EnsembleTimeSeries[] ets = csvReader.Read("Kanektok", issueDate1, issueDate2);
        EnsembleDatabase db = new JdbcDatabase(filename, JdbcDatabase.CREATION_MODE.CREATE_NEW);

        db.write(ets);
        return db;
    }

}
