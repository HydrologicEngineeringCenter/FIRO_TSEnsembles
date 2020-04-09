package hec.ensemble;

import hec.JdbcTimeSeriesDatabase;
import hec.TimeSeriesDatabase;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class ResSimTest {


    @Test
    public void simulateResSim() throws Exception {

        String fn ="src/test/resources/database/ResSim.db";

        // get an ensembleTimeSeries from the database
        // in initialization code somewhere.
        // database layer (base/interface ) = jdbc/sqlite instance
        try (TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fn,JdbcTimeSeriesDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);) {
            // InMemoryEnsembleTimeSeriesDatabase

            EnsembleIdentifier tsid = new EnsembleIdentifier("Coyote.fake_forecast", "flow");

            EnsembleTimeSeries ets = db.getEnsembleTimeSeries(tsid);
            if (ets == null)
                throw new Exception("could not find " + tsid.toString());
            Object R = null; // Represents result of ResSim script processing an ensemble.
            // -- end initialization

            List<ZonedDateTime> issueDates = ets.getIssueDates();
            Ensemble e;
            // t represents ResSim timestep (RunTimeStep)
            ZoneId pst = ZoneId.of("America/Los_Angeles");
            ZonedDateTime t = issueDates.get(0);
            int numSteps = 168; // hourly time steps
            ZonedDateTime timeOfNextEnsemble = issueDates.get(0);
            for (ZonedDateTime z: issueDates) { // find first forecast
                if( z.compareTo(t) >0 )
                    break;
                timeOfNextEnsemble = z;

            }

            for (int i = 0; i < numSteps; i++) {

                // assuming issue_times might not be exactly regular.
                if (i == 0 || (t.equals(timeOfNextEnsemble) || t.isAfter(timeOfNextEnsemble))) {
                    e = ets.getEnsemble(timeOfNextEnsemble);// gets nearest ensemble at or before time t
                    ZonedDateTime issueDate =  e.getIssueDate();
                    int idx = issueDates.indexOf(issueDate);
                    if( idx+1  < issueDates.size())
                        timeOfNextEnsemble = issueDates.get(idx+1);
                    else
                        System.out.println("no more ensembles...");

                    R = ProcessEnsemble(e);// process the new ensemble
                }

                // Do smart stuff with R

                t = t.plusHours(1);
            }
        }
    }

    private Object ProcessEnsemble(Ensemble e) {
        System.out.println("processing...");
        return new Object();
    }
}
