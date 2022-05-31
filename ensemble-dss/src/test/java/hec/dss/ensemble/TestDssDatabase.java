package hec.dss.ensemble;
import java.util.List;

import hec.RecordIdentifier;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestDssDatabase {

    private String getTestDataDirectory() {
        String path = new java.io.File(getClass().getResource(
                "/hefs_cache/2013110312_Kanektok_hefs_csv_hourly.csv").getFile()).toString();

        java.io.File f = new java.io.File(path);
        return f.getParent();
    }
    /**
     * Creates a DSS file with ensemble time series data
     * with multiple locations and issue dates
     *
     * @param dssFilename name of DSS database to create
     * @param numberOfDates number of forecasts to include
     */
    public void createDssFileFromCsv(String dssFilename, int numberOfDates) throws Exception {

        String cacheDir = getTestDataDirectory();
        java.time.ZonedDateTime issueDate1 = java.time.ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, java.time.ZoneId.of("GMT"));
        java.time.ZonedDateTime issueDate2 = issueDate1.plusDays(numberOfDates-1);

        hec.ensemble.CsvEnsembleReader csvReader = new hec.ensemble.CsvEnsembleReader(cacheDir);
        hec.ensemble.EnsembleTimeSeries[] ets = csvReader.Read("Kanektok", issueDate1, issueDate2);

        DssDatabase db = new DssDatabase(dssFilename);

        db.write(ets);
        db.close();
    }

    @Test
    public void CsvToDSS() throws Exception{

        java.io.File f = java.io.File.createTempFile("tmp-", ".dss");
        if( f.exists())
            f.delete();

        String dssFilename = f.getAbsolutePath();
        createDssFileFromCsv(dssFilename,3);

        DssDatabase db = new DssDatabase(dssFilename);
        List<hec.RecordIdentifier> recordIdentifiers= db.getEnsembleTimeSeriesIDs();

        assertEquals(23,recordIdentifiers.size());

        RecordIdentifier id  = new hec.RecordIdentifier("Kanektok.BCAC1","flow");
        List<java.time.ZonedDateTime> times = db.getEnsembleIssueDates(id);
        assertEquals(3,times.size());

//  DssDatabase.getEnsemble(),   -- ensemble
//  DssDatabase.getMetricCollection(...)  -- metrics stored in SQlite.
       // db.getEnsemble(id,)
        //db.getEnsembleTimeSeries()

        // Metrics
        // calc variations of metrics

        // time series  (like average,min,max) -- stored as collection?
        //               c:0001|T:2022|V:2022|max  ?
        // paired data  62 members --> 62 max values [0,1...61] float[]
        // scalar. [126.4]   T:2022|V:2022|scalar=max
    }

}
