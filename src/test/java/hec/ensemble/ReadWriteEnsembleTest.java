package hec.ensemble;

import hec.JdbcTimeSeriesDatabase;
import hec.TimeSeriesDatabase;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReadWriteEnsembleTest {
    @Test
    public void ReadWrite() throws Exception {

        String fn = "src/test/resources/database/ResSim.db";

        TimeSeriesDatabase db = new JdbcTimeSeriesDatabase(fn, JdbcTimeSeriesDatabase.CREATION_MODE.OPEN_EXISTING_UPDATE);
        List<TimeSeriesIdentifier> locations = db.getTimeSeriesIDs();

        ArrayList<EnsembleTimeSeries> etsList = new ArrayList<>();
        int count = 0;
        for(TimeSeriesIdentifier tsid: locations) {
            System.out.println(tsid.toString());
            hec.ensemble.EnsembleTimeSeries ets = db.getEnsembleTimeSeries(tsid);
            for (ZonedDateTime t: ets.getIssueDates()){
                Ensemble e = ets.getEnsemble(t);
                for(float[] v : e.getValues()){
                    for (int i = 0; i <v.length ; i++) {
                        v[i] = v[i]+10 ;// offset by 10
                    }
                }
            }
            etsList.add(ets);
            count += ets.getCount();
        }
        String outputPath = TestingPaths.instance.getTempDir()+"/out_delete_me.db";
        File f = new File(outputPath);
        f.delete();
        hec.JdbcTimeSeriesDatabase dbaseOut = new hec.JdbcTimeSeriesDatabase(outputPath, JdbcTimeSeriesDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);
        dbaseOut.write(etsList.toArray(new hec.ensemble.EnsembleTimeSeries[0]));

    }

}
