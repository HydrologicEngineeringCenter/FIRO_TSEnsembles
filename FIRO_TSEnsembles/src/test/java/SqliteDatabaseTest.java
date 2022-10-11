import hec.RecordIdentifier;
import hec.SqliteDatabase;
import hec.VersionableDatabase;
import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.*;
import hec.metrics.MetricCollectionTimeSeries;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        Computable cumulative = new NDayMultiComputable(cumulativeComputable,2);
        Computable percentileCompute = new PercentilesComputable(0.95f);
        SingleComputable twoStep = new TwoStepComputable(cumulative,percentileCompute,false);
        MetricCollectionTimeSeries output = ets.computeSingleValueSummary(twoStep);
        db.write(output);

        List<RecordIdentifier> ids = db.getMetricTimeSeriesIDs();
        for(hec.RecordIdentifier mid: ids){
            MetricCollectionTimeSeries mcts = db.getMetricCollectionTimeSeries(mid);
            assertEquals(3, mcts.getIssueDates().size());
        }
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
