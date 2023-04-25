package hec.dss.ensemble;
import java.time.ZonedDateTime;
import java.util.List;

import hec.RecordIdentifier;
import hec.ensemble.Ensemble;

import hec.ensemble.EnsembleTimeSeries;
import hec.ensemble.stats.*;
import hec.heclib.dss.DssDataType;
import hec.heclib.dss.HecPairedData;
import hec.heclib.dss.HecTimeSeries;
import hec.io.PairedDataContainer;
import hec.io.TimeSeriesContainer;
import hec.metrics.MetricCollection;
import hec.metrics.MetricCollectionTimeSeries;
import mil.army.usace.hec.data.timeseries.TimeSeries;
import org.junit.jupiter.api.Test;

import static hec.ensemble.stats.Statistics.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestDssDatabase {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(hec.dss.ensemble.TestDssDatabase.class.getName());

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
    private void createDssFileFromCsv(String dssFilename, int numberOfDates) throws Exception {

        String cacheDir = getTestDataDirectory();
        java.time.ZonedDateTime issueDate1 = java.time.ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, java.time.ZoneId.of("GMT"));
        java.time.ZonedDateTime issueDate2 = issueDate1.plusDays(numberOfDates-1);

        hec.ensemble.CsvEnsembleReader csvReader = new hec.ensemble.CsvEnsembleReader(cacheDir);
        hec.ensemble.EnsembleTimeSeries[] ets = csvReader.Read("Kanektok", issueDate1, issueDate2);

        LOGGER.info(System.getenv("java.library.path"));

        DssDatabase db = new DssDatabase(dssFilename);

        db.write(ets);
        db.close();
    }

    private static String getNewDssFileName() throws Exception{
        java.io.File f = java.io.File.createTempFile("tmp-", ".dss");
        if( f.exists())
            f.delete();
        return f.getAbsolutePath();
    }

    private DssDatabase getNewTestDssDatabase() throws Exception{
        String dssFilename = getNewDssFileName();
        createDssFileFromCsv(dssFilename,3);
        DssDatabase db =  new hec.dss.ensemble.DssDatabase(dssFilename);
        return db;
    }

    @Test
    public void testEnsemble() throws Exception{

        DssDatabase db = getNewTestDssDatabase();
        System.out.println("testEnsemble: "+db.getFileName());
        List<hec.RecordIdentifier> recordIdentifiers= db.getEnsembleTimeSeriesIDs();

        assertEquals(23,recordIdentifiers.size());
        RecordIdentifier id  = new hec.RecordIdentifier("Kanektok.BCAC1","flow");
        List<java.time.ZonedDateTime> times = db.getEnsembleIssueDates(id);
        assertEquals(3,times.size());

        Ensemble e = db.getEnsemble(id,times.get(1));
        assertEquals(59, e.getValues().length);
        assertEquals(337, e.getValues()[0].length);

        // read a record using the DSS API
        HecTimeSeries dss = new HecTimeSeries(db.getFileName());
        TimeSeriesContainer tsc = new TimeSeriesContainer();
        tsc.fullName = "//Kanektok.BCAC1/flow/01Nov2013/1Hour/C:000007|T:20131103-1200|V:20131103-120000|/";
        int status = dss.read(tsc, true);
        assertEquals(0, status);
        assertEquals(DssDataType.PER_AVER.toString(),tsc.type);
        dss.done();
    }

    @Test
    public void testEnsembleTimeSeries() throws Exception{

        DssDatabase db = getNewTestDssDatabase();
        RecordIdentifier id  = new hec.RecordIdentifier("Kanektok.BCAC1","flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);
        assertEquals(3, ets.getCount());
        List<ZonedDateTime> zdts = ets.getIssueDates();
        for (ZonedDateTime zdt : zdts) {
            assertEquals(59, ets.getEnsemble(zdt).getValues().length);
            assertEquals(337, ets.getEnsemble(zdt).getValues()[0].length);
        }
    }

    @Test
    public void testMetricCollectionAsTimeSeries_DSS_API() throws Exception{

        DssDatabase db = getNewTestDssDatabase();
        RecordIdentifier id  = new hec.RecordIdentifier("Kanektok.BCAC1","flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);
        MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, MAX, AVERAGE});
        MetricCollectionTimeSeries output = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(test);
        db.write(output);

        HecTimeSeries dss = new HecTimeSeries(db.getFileName());
        String[] pathsToFind = new String[] {
            "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-MAX/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|/",
                "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-AVERAGE/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|/",
                "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-MIN/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|/",
                "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-MAX/01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|/",
                "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-AVERAGE/01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|/",
                "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-MIN/01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|/",
                "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-MAX/01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|/",
                "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-AVERAGE/01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|/",
                "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-MIN/01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|/"
        };

        for (String path : pathsToFind) {
            TimeSeriesContainer tsc = new TimeSeriesContainer();
            tsc.fullName = path;
            int status = dss.read(tsc, true);
            assertEquals(0, status);
            assertEquals(337, tsc.values.length);
            assertEquals(337, tsc.times.length);
        }
        dss.done();

    }

    @Test
    public void testMetricCollectionAsTimeSeries_TwoStep() throws Exception{

        DssDatabase db = getNewTestDssDatabase();
        RecordIdentifier id  = new hec.RecordIdentifier("Kanektok.BCAC1","flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);

        MultiComputable cumulativeComputable = new CumulativeComputable();
        Computable cumulative = new NDayMultiComputable(cumulativeComputable,2);
        Computable percentileCompute = new PercentilesComputable(0.95f);
        SingleComputable twoStep = new TwoStepComputable(cumulative,percentileCompute,false);
        MetricCollectionTimeSeries output = ets.computeSingleValueSummary(twoStep);
        db.write(output);

        HecTimeSeries dss = new HecTimeSeries(db.getFileName());
        String[] pathsToFind = new String[] {
                "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-CUMULATIVE(2DAY),PERCENTILE(0.95)/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|/",
                "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-CUMULATIVE(2DAY),PERCENTILE(0.95)/01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|/",
                "//Kanektok.BCAC1/" +DssDatabase.metricTimeseriesIdentifier+ "-flow-CUMULATIVE(2DAY),PERCENTILE(0.95)/01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|/",
        };

        for (String path : pathsToFind) {
            TimeSeriesContainer tsc = new TimeSeriesContainer();
            tsc.fullName = path;
            int status = dss.read(tsc, true);
            assertEquals(0, status);
            assertEquals(1, tsc.values.length);
            assertEquals(1, tsc.times.length);
        }
        //db.catalog.update();
        /* commented out until getMetricStatistics(RecordIdentifer) and getMetricCollectionTimeSeries(RecordIdentifier, String) are implemented
        List<RecordIdentifier> ids = db.getMetricTimeSeriesIDs();
        for(hec.RecordIdentifier mid: ids){
            List<String> stats = db.getMetricStatistics(mid);
            MetricCollectionTimeSeries mcts = db.getMetricCollectionTimeSeries(mid, stats.get(0));
            //assertEquals(3, mcts.getIssueDates().size());
        }
        */
        dss.done();
    }

    @Test
    public void testStoreMetricTimeSeriesIDs() throws Exception{
        DssDatabase db = getNewTestDssDatabase();
        RecordIdentifier id  = new hec.RecordIdentifier("Kanektok.BCAC1","flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);

        MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, MAX, AVERAGE});
        MetricCollectionTimeSeries output = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(test);
        db.write(output);
        //db.catalog.update();

        List<RecordIdentifier> mIds = db.getMetricTimeSeriesIDs();
        assertEquals(3, mIds.size());

        /* commented out until getMetricStatistics(RecordIdentifer) and getMetricCollectionTimeSeries(RecordIdentifier, String) are implemented
        List<String> stats = db.getMetricStatistics(mIds.get(0));
        MetricCollectionTimeSeries mcts = db.getMetricCollectionTimeSeries(mIds.get(0), stats.get(0));
        assertEquals(3, mcts.getIssueDates().size());
         */


    }

    @Test
    public void testStoringMetricsInPairedData() throws Exception {
        DssDatabase db = getNewTestDssDatabase();
        RecordIdentifier id  = new hec.RecordIdentifier("Kanektok.BCAC1","flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);
        MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, MAX, AVERAGE});
        MetricCollectionTimeSeries output = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(test);
        for(MetricCollection mc : output)
            db.write(mc);
        //db.catalog.update();

        HecPairedData dss = new HecPairedData(db.getFileName());
        String[] pathsToFind = new String[] {
                "//Kanektok.BCAC1/"+ DssDatabase.metricPairedDataIdentifier+ "-flow-stats///T:20131103-1200|V:20131103-120000|/",
                "//Kanektok.BCAC1/"+ DssDatabase.metricPairedDataIdentifier+ "-flow-stats///T:20131104-1200|V:20131104-120000|/",
                "//Kanektok.BCAC1/"+ DssDatabase.metricPairedDataIdentifier+ "-flow-stats///T:20131105-1200|V:20131105-120000|/"
        };

        for (String path : pathsToFind) {
            PairedDataContainer pdc = new PairedDataContainer();
            pdc.fullName = path;
            int status = dss.read(pdc);
            assertEquals(0, status);
            assertEquals(3, pdc.yOrdinates.length);
            assertEquals(59, pdc.xOrdinates.length);
        }
        dss.done();
        List<RecordIdentifier> mIds = db.getMectricPairedDataIDs();
        assertEquals(1, mIds.size());
    }
}
