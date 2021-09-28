package hec.ensemble;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.metrics.MetricCollectionTimeSeries;
import hec.stats.MultiComputable;
import hec.stats.MultiStatComputable;
import hec.stats.Statistics;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static hec.stats.Statistics.*;
import static org.junit.jupiter.api.Assertions.*;

class EnsembleTimeSeriesTest {

    private SqliteDatabase prepareNewDatabase(String fn, int numberOfDates) throws Exception {

        File f = new File(fn);
        f.delete();
        String cacheDir = TestingPaths.instance.getCacheDir();
        ZonedDateTime issueDate1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime issueDate2 = issueDate1.plusDays(numberOfDates);

        CsvEnsembleReader csvReader = new CsvEnsembleReader(cacheDir);
        EnsembleTimeSeries[] ets = csvReader.Read("Kanektok", issueDate1, issueDate2);
        SqliteDatabase db = new SqliteDatabase(fn, SqliteDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);

        db.write(ets);
        return db;
    }

    @Test
    void iterateAcrossTimestepsOfEnsemblesWithMultiComputable() {
        try {
            //create a new clean database
            String fn = TestingPaths.instance.getTempDir()+"/importCsvToDatabaseMutable.db";
            SqliteDatabase db = prepareNewDatabase(fn, 3);
            //define a location and variable
            RecordIdentifier tsid = new RecordIdentifier("Kanektok.SCRN2", "flow");
            //get an ensemble time series
            EnsembleTimeSeries ets = db.getEnsembleTimeSeries(tsid);
            //create a computable statistic
            MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, MEAN, MAX});
            //compute the statistics for the entire ensemble time series
            MetricCollectionTimeSeries output = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(test);
            //verify at the data is properly computing for a set of known values
            float[] value1 = output.iterator().next().getValues()[3];
            assertEquals(-4000, value1[0]);//min
            assertEquals(-10.0833740234375, value1[1]);//mean
            assertEquals(11.159436225891113, value1[2]);//max
            //write result
            db.write(output);
        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }
    @AfterAll
    static void cleanUp(){
        String fn = TestingPaths.instance.getTempDir()+"/importCsvToDatabaseMutable.db";
        File f = new File(fn);
        f.delete();
    }
}