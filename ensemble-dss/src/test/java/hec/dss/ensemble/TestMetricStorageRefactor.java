package hec.dss.ensemble;

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
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static hec.ensemble.stats.Statistics.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMetricStorageRefactor {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(TestMetricStorageRefactor.class.getName());

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
     * @param dssFilename   name of DSS database to create
     * @param numberOfDates number of forecasts to include
     */
    private void createDssFileFromCsv(String dssFilename, int numberOfDates) throws Exception {

        String cacheDir = getTestDataDirectory();
        ZonedDateTime issueDate1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, java.time.ZoneId.of("GMT"));
        ZonedDateTime issueDate2 = issueDate1.plusDays(numberOfDates - 1);

        hec.ensemble.CsvEnsembleReader csvReader = new hec.ensemble.CsvEnsembleReader(cacheDir);
        EnsembleTimeSeries[] ets = csvReader.Read("Kanektok", issueDate1, issueDate2);

        LOGGER.info(System.getenv("java.library.path"));

        DssDatabase db = new DssDatabase(dssFilename);

        db.write(ets);
        db.close();
    }
    private static String getNewDssFileName() throws Exception {
        java.io.File f = java.io.File.createTempFile("tmp-", ".dss");
        if (f.exists())
            f.delete();
        return f.getAbsolutePath();
    }
    private DssDatabase getNewTestDssDatabase() throws Exception {
        String dssFilename = getNewDssFileName();
        createDssFileFromCsv(dssFilename, 3);
        DssDatabase db = new DssDatabase(dssFilename);
        return db;
    }

    public void testWriteComputableAcrossTraces() throws Exception {
        String WATFPart = "TESTING:FRA:FIRO_WFP-PRADOMETRICS/";

        DssDatabase db = getNewTestDssDatabase();
        RecordIdentifier id = new RecordIdentifier("Kanektok.BCAC1", "flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);
        Computable test = new MaxComputable();
        db.setOverriddenFPart(WATFPart);
        MetricCollectionTimeSeries output = ets.iterateAcrossEnsembleTracesWithSingleComputable(test);
        db.write(output);

        HecTimeSeries dss = new HecTimeSeries(db.getFileName());
        String[] pathsToFind = new String[]{
                "//Kanektok.BCAC1/" + DssDatabase.metricTimeseriesIdentifier + "-flow-MAX/    01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|" + WATFPart,
                "//Kanektok.BCAC1/" + DssDatabase.metricTimeseriesIdentifier + "-flow-MAX/    01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|" + WATFPart,
                "//Kanektok.BCAC1/" + DssDatabase.metricTimeseriesIdentifier + "-flow-MAX/    01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|" + WATFPart,
        };
        for (String path : pathsToFind) {
            TimeSeriesContainer tsc = new TimeSeriesContainer();
            tsc.fullName = path;
            int status = dss.read(tsc, true);
            assertEquals(0, status);
            assertEquals(337, tsc.values.length);
            assertEquals(337, tsc.times.length);
        }
    }

    public void testWriteComputableAcrossTime() throws Exception {
        String WATFPart = "TESTING:FRA:FIRO_WFP-PRADOMETRICS";
        DssDatabase db = getNewTestDssDatabase();
        RecordIdentifier id = new RecordIdentifier("Kanektok.BCAC1", "flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);
        Computable test = new MaxComputable();
        MetricCollectionTimeSeries output = ets.iterateAcrossTimestepsOfEnsemblesWithSingleComputable(test);
        db.write(output);

        HecTimeSeries dss = new HecTimeSeries(db.getFileName());
        String[] pathsToFind = new String[]{
                "//Kanektok.BCAC1/" + DssDatabase.metricTimeseriesIdentifier + "-flow-Cumulative(4day)-Percentile(0.95)/    01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|" + WATFPart,
                "//Kanektok.BCAC1/" + DssDatabase.metricTimeseriesIdentifier + "-flow-Cumulative(4day)-Percentile(0.95)/    01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|" + WATFPart,
                "//Kanektok.BCAC1/" + DssDatabase.metricTimeseriesIdentifier + "-flow-Cumulative(4day)-Percentile(0.95)/    01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|" + WATFPart,
        };

        for (String path : pathsToFind) {
            TimeSeriesContainer tsc = new TimeSeriesContainer();
            tsc.fullName = path;
            int status = dss.read(tsc, true);
            assertEquals(0, status);
            assertEquals(337, tsc.values.length);
            assertEquals(337, tsc.times.length);
        }
    }

    public void testWriteComputableSingleValueSummary() throws Exception {
        String WATFPart = "TESTING:FRA:FIRO_WFP-PRADOMETRICS";
        DssDatabase db = getNewTestDssDatabase();
        RecordIdentifier id = new RecordIdentifier("Kanektok.BCAC1", "flow");
        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);

        MultiComputable cumulativeComputable = new CumulativeComputable();
        Computable cumulative = new NDayMultiComputable(cumulativeComputable, 3);
        Computable percentileCompute = new PercentilesComputable(0.95f);
        SingleComputable twoStep = new TwoStepComputable(cumulative, percentileCompute, false);

        MetricCollectionTimeSeries output = ets.computeSingleValueSummary(twoStep);
        db.write(output);

        HecPairedData dss = new HecPairedData(db.getFileName());
        String[] pathsToFind = new String[]{
                "//Kanektok.BCAC1/" + DssDatabase.metricPairedDataIdentifier + "-flow-MAX/    01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|" + WATFPart,
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

