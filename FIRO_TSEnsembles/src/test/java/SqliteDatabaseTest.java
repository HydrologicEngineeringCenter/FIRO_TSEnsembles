import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.VersionIdentifier;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.*;
import hec.metrics.MetricCollectionTimeSeries;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SqliteDatabaseTest {
    private File get_test_file(String test_database_resource ) throws Exception{
        InputStream testdb_stream = this.getClass().getResourceAsStream(test_database_resource);
        Path tmpdbfile = Files.createTempFile("updatetests", "db");
        FileOutputStream file = new FileOutputStream(tmpdbfile.toFile());
        byte buffer[] = new byte[4096];
        int length;
        while((length = testdb_stream.read(buffer,0,4096))> 0){
            file.write(buffer,0,length);
        }
        file.close();
        return tmpdbfile.toFile();
    }

    @Test
    public void testMetricCollectionAsTimeSeries_TwoStep() throws Exception{
        String sourceData = "src/test/resources/database/ResSimTest_20200101.db";
        String copiedData = "C:\\Temp\\testdb.db";
        File sourcefile = new File(sourceData);
        File copiedFile = new File(copiedData);
        copyFileUsingStream(sourcefile,copiedFile);

        SqliteDatabase db = new SqliteDatabase(copiedData, SqliteDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);

        RecordIdentifier id  = new hec.RecordIdentifier("Kanektok.BCAC1","flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);

        MultiComputable cumulativeComputable = new CumulativeComputable();
        Computable cumulative = new NDayMultiComputable(cumulativeComputable, new float[]{2});
        Computable percentileCompute = new PercentilesComputable(0.95f);
        SingleComputable twoStep = new TwoStepComputable(cumulative,percentileCompute,false);
        MetricCollectionTimeSeries output = ets.computeSingleValueSummary(twoStep);
        db.write(output);

        List<RecordIdentifier> ids = db.getMetricTimeSeriesIDs();
        for(hec.RecordIdentifier mid: ids){
            List<String> stats = db.getMetricStatistics(mid);
            MetricCollectionTimeSeries mcts = db.getMetricCollectionTimeSeries(mid, stats.get(0));
            assertEquals(3, mcts.getIssueDates().size());
        }
        db.close();
    }

    @Test
    public void testDeleteAllEnsembles() throws Exception {
        String sourceData = "src/test/resources/database/ResSimTest_20200101.db";
        String copiedData = "C:\\Temp\\testdb_delete.db";
        File sourcefile = new File(sourceData);
        File copiedFile = new File(copiedData);
        copyFileUsingStream(sourcefile,copiedFile);

        SqliteDatabase db = new SqliteDatabase(copiedData, SqliteDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);
        db.deleteAllEnsemblesFromDB();
        Integer testVal = db.getEnsembleTimeSeriesIDs().size();
        assertEquals(0, testVal);
        db.close();
    }

    @Test
    public void testVersionRead() throws Exception{
        String sourceData = "src/test/resources/database/versionTest.db";
        SqliteDatabase db = new SqliteDatabase(sourceData, SqliteDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);
        VersionIdentifier id  = new hec.VersionIdentifier("american.FOLSOM","flow", "test2");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);
        ZonedDateTime date = ZonedDateTime.of(1986,2,13,12,0,0,0, ZoneId.of("GMT"));
        Ensemble e = ets.getEnsemble(date);
        Float firstVal = e.getValues()[0][0];
        assertEquals(0.123f, firstVal );

        VersionIdentifier id2  = new hec.VersionIdentifier("american.FOLSOM","flow", "test");
        EnsembleTimeSeries ets2 = db.getEnsembleTimeSeries(id2);
        Ensemble e2 = ets2.getEnsemble(date);
        Float firstVal2 = e2.getValues()[0][0];
        assertEquals(123.0f, firstVal2 );
        db.close();
    }

    @Test
    public static void testMetricStatistics() throws Exception {
        // open ensemble.db, read metric statistics, confirm that there is the correct number of them.
        String sourceData = "src/test/resources/database/synthetic_ensembles.db";

        File sourcefile = new File(sourceData);
        File copiedFile = File.createTempFile("syntemp",".db");
        copyFileUsingStream(sourcefile,copiedFile);
        SqliteDatabase db = new SqliteDatabase(copiedFile.getAbsolutePath(), SqliteDatabase.CREATION_MODE.CREATE_NEW_OR_OPEN_EXISTING_UPDATE);

        RecordIdentifier recID = new RecordIdentifier("ADOC", "FLOW");
        List<String> stats = db.getMetricStatistics(recID);
        assertEquals(24, stats.size());

        // check sizes of results
        Map<RecordIdentifier, List<String>> statsMap = db.getMetricStatistics();
        assertEquals(1, statsMap.keySet().size());
        assertEquals(24, statsMap.get(recID).size());

        // check that we can read with a statistic
        MetricCollectionTimeSeries mcts = db.getMetricCollectionTimeSeries(recID, "CUMULATIVE(2DAY),PERCENTILE(0.05)");
        assertNotNull(mcts);
        assertEquals(65, mcts.getIssueDates().size());

        // get the whole list without naming a stat
        List<MetricCollectionTimeSeries> mctsList = db.getMetricCollectionTimeSeries(recID);
        assertEquals(24, mctsList.size());

        db.close();
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }




}
