package hec.ensemble;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import hec.*;

public class ResSimTest {

    public void CreateResSimTestFile() throws Exception {
        String fn = "ResSim.db";
        File f = new File(fn);
        f.delete();

        // get an ensembleTimeSeries from the database
        // in initialization code somewhere.
        // database layer (base/interface ) = jdbc/sqlite instance

        TimeSeriesIdentifier tsid = new TimeSeriesIdentifier("Coyote.fake_forecast", "flow");

        // DatabaseGenerator.createTestDatabase(fn,20);
        DatabaseGenerator.create1997TestDatabase(fn);
    }

    @Test
    public void simulateResSim() throws Exception {

        // CreateResSimTestFile();

        String fn = "ResSimTest.db";
        File f = new File(fn);
        f.delete();

        // get an ensembleTimeSeries from the database
        // in initialization code somewhere.
        // database layer (base/interface ) = jdbc/sqlite instance
        DatabaseGenerator.createTestDatabase(fn, 2);
        try (TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fn,JdbcTimeSeriesDatabase.CREATION_MODE.OPEN_EXISTING_NO_UPDATE);) {
            // InMemoryEnsembleTimeSeriesDatabase

            TimeSeriesIdentifier tsid = new TimeSeriesIdentifier("Kanektok.FARC1F", "flow");

            EnsembleTimeSeries ets = db.getEnsembleTimeSeries(tsid);
            if (ets == null)
                throw new Exception("could not find " + tsid.toString());
            Object R = null; // Represents result of ResSim script processing an ensemble.
            // -- end initialization

            Ensemble e = null;
            // t represents ResSim timestep (RunTimeStep)
            ZoneId pst = ZoneId.of("America/Los_Angeles");
            ZonedDateTime t = ZonedDateTime.of(2019, 12, 25, 0, 0, 0, 0, pst);
            ZonedDateTime timeOfPreviousEnsemble = t;
            int numSteps = 168; // hourly time steps
            int tolerance = 24; // require new ensemble at least every 24 hours

            for (int i = 0; i < numSteps; i++) {

                // assuming issue_times might not be exactly regular.
                if (i == 0 || (t.isAfter(timeOfPreviousEnsemble) && ets.issueDateExists(t, tolerance))) {
                    e = ets.getEnsemble(t, tolerance);// gets nearest ensemble at or before time t
                    R = ProcessEnsemble(e);// process the new ensemble
                    timeOfPreviousEnsemble = t;
                }

                // Do smart stuff with R

                t = t.plusHours(1);
            }
        }
    }

    private Object ProcessEnsemble(Ensemble e) {
        return new Object();
    }
}
