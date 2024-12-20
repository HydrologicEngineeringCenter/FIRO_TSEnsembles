package hec.ensemble;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.SqliteDatabase;

import java.time.ZonedDateTime;
import java.util.List;

public class TestData {

    private static EnsembleDatabase s_db = null;

    private static EnsembleDatabase getDatabase() {
        try {
            String fn = "src/test/resources/database/importCsvToDatabase.db";
            EnsembleDatabase db = new SqliteDatabase(fn, SqliteDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
            return db;
        }catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }
    public static Ensemble getSampleEnsemble() {
     return getSampleEnsemble("Kanektok.SCRN2", "flow");
    }
    public static Ensemble getSampleEnsemble(String location, String parameter) {
        if(s_db == null)
            s_db = getDatabase();

        RecordIdentifier tsid = new RecordIdentifier(location,parameter);
        EnsembleTimeSeries ets = s_db.getEnsembleTimeSeries(tsid);
        List<ZonedDateTime> issueDates = ets.getIssueDates();
        return s_db.getEnsemble(tsid, issueDates.get(0));
    }
}
