package hec.ensemble;

import hec.JdbcTimeSeriesDatabase;
import hec.TimeSeriesDatabase;
import jdk.nashorn.internal.runtime.ECMAException;

import java.time.ZonedDateTime;
import java.util.List;

public class TestData {

    private static TimeSeriesDatabase s_db = null;

    private static TimeSeriesDatabase getDatabase() {
        try {
            String fn = "src/test/resources/database/importCsvToDatabase.db";
            TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fn, JdbcTimeSeriesDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
            return db;
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static Ensemble getSampleEnsemble() {
        if(s_db == null)
            s_db = getDatabase();

        TimeSeriesIdentifier tsid = new TimeSeriesIdentifier("Kanektok.SCRN2", "flow");
        EnsembleTimeSeries ets = s_db.getEnsembleTimeSeries(tsid);
        List<ZonedDateTime> issueDates = ets.getIssueDates();
        return s_db.getEnsemble(tsid, issueDates.get(0));
    }
}
