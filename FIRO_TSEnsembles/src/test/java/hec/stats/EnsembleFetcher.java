package hec.stats;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.ensemble.DatabaseGenerator;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.TestingPaths;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;

public class EnsembleFetcher {
    public static Ensemble getEnsemble() throws Exception {
        String fn = TestingPaths.instance.getTempDir() + "/importCsvToDatabase.db";
        File f = new File(fn);
        if(!f.exists()) {
            DatabaseGenerator.createTestDatabase(fn, 1);
        }
        EnsembleDatabase db = new SqliteDatabase(fn, SqliteDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
        // --- READ
        RecordIdentifier tsid = new RecordIdentifier("Kanektok.SCRN2", "flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(tsid);
        List<ZonedDateTime> issueDates = ets.getIssueDates();
        return db.getEnsemble(tsid, issueDates.get(0));
    }
}
