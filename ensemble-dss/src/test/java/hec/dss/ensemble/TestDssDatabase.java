package hec.dss.ensemble;
import java.time.ZonedDateTime;
import java.util.List;

import hec.RecordIdentifier;
import hec.ensemble.Ensemble;

import hec.ensemble.EnsembleTimeSeries;
import hec.heclib.dss.HecPairedData;
import hec.heclib.dss.HecTimeSeries;
import hec.io.PairedDataContainer;
import hec.io.TimeSeriesContainer;
import hec.metrics.MetricCollection;
import hec.metrics.MetricCollectionTimeSeries;
import hec.stats.MultiComputable;
import hec.stats.MultiStatComputable;
import hec.stats.Statistics;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.jupiter.api.Test;

import static hec.stats.Statistics.*;
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
    public void CsvToDssEnsemble() throws Exception{

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

        Ensemble e = db.getEnsemble(id,times.get(1));
        assertEquals(59, e.getValues().length);
        assertEquals(337, e.getValues()[0].length);
    }

    @Test
    public void CsvToDssEnsembleTimeSeries() throws Exception{

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

        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);
        assertEquals(3, ets.getCount());
        List<ZonedDateTime> zdts = ets.getIssueDates();
        for (ZonedDateTime zdt : zdts) {
            assertEquals(59, ets.getEnsemble(zdt).getValues().length);
            assertEquals(337, ets.getEnsemble(zdt).getValues()[0].length);
        }
    }

    @Test
    public void CsvToDssEnsembleTimeSeriesStoringMetricCollection() throws Exception{

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

        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);
        assertEquals(3, ets.getCount());
        List<ZonedDateTime> zdts = ets.getIssueDates();
        for (ZonedDateTime zdt : zdts) {
            assertEquals(59, ets.getEnsemble(zdt).getValues().length);
            assertEquals(337, ets.getEnsemble(zdt).getValues()[0].length);
        }

        MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, MAX, MEAN});
        MetricCollectionTimeSeries output = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(test);
        db.write(output);

        HecTimeSeries dss = new HecTimeSeries(dssFilename);
        String[] pathsToFind = new String[] {
            "//Kanektok.BCAC1/flow-MAX/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|/",
            "//Kanektok.BCAC1/flow-MEAN/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|/",
            "//Kanektok.BCAC1/flow-MIN/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|/",
            "//Kanektok.BCAC1/flow-MAX/01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|/",
            "//Kanektok.BCAC1/flow-MEAN/01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|/",
            "//Kanektok.BCAC1/flow-MIN/01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|/",
            "//Kanektok.BCAC1/flow-MAX/01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|/",
            "//Kanektok.BCAC1/flow-MEAN/01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|/",
            "//Kanektok.BCAC1/flow-MIN/01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|/"
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
    public void CsvToDssEnsembleTimeSeriesGettingStoredMetricTimeSeriesIDs() throws Exception{

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

        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);
        assertEquals(3, ets.getCount());
        List<ZonedDateTime> zdts = ets.getIssueDates();
        for (ZonedDateTime zdt : zdts) {
            assertEquals(59, ets.getEnsemble(zdt).getValues().length);
            assertEquals(337, ets.getEnsemble(zdt).getValues()[0].length);
        }

        MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, MAX, MEAN});
        MetricCollectionTimeSeries output = ets.iterateAcrossTimestepsOfEnsemblesWithMultiComputable(test);
        db.write(output);

        HecTimeSeries dss = new HecTimeSeries(dssFilename);
        String[] pathsToFind = new String[] {
                "//Kanektok.BCAC1/flow-MIN/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|/",
                "//Kanektok.BCAC1/flow-MAX/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|/",
                "//Kanektok.BCAC1/flow-MEAN/01Nov2013/1Hour/T:20131103-1200|V:20131103-120000|/",
                "//Kanektok.BCAC1/flow-MIN/01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|/",
                "//Kanektok.BCAC1/flow-MAX/01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|/",
                "//Kanektok.BCAC1/flow-MEAN/01Nov2013/1Hour/T:20131104-1200|V:20131104-120000|/",
                "//Kanektok.BCAC1/flow-MIN/01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|/",
                "//Kanektok.BCAC1/flow-MAX/01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|/",
                "//Kanektok.BCAC1/flow-MEAN/01Nov2013/1Hour/T:20131105-1200|V:20131105-120000|/"
        };

        for (String path : pathsToFind) {
            TimeSeriesContainer tsc = new TimeSeriesContainer();
            tsc.fullName = path;
            int status = dss.read(tsc, true);
            assertEquals(0, status);
            assertEquals(337, tsc.values.length);
            assertEquals(337, tsc.times.length);
        }

        db.catalog.update();

        List<RecordIdentifier> mIds = db.getMetricTimeSeriesIDs();
        assertEquals(3, mIds.size());

        MetricCollectionTimeSeries mcts = db.getMetricCollectionTimeSeries(mIds.get(0));
        assertEquals(3, mcts.getIssueDates().size());

        dss.done();

    }

    @Ignore
    public void StoringPercentilesInTimeSeries() throws Exception {

    }

    @Test
    public void StoringMetricsInPairedData() throws Exception {
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

        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(id);
        assertEquals(3, ets.getCount());
        List<ZonedDateTime> zdts = ets.getIssueDates();
        for (ZonedDateTime zdt : zdts) {
            assertEquals(59, ets.getEnsemble(zdt).getValues().length);
            assertEquals(337, ets.getEnsemble(zdt).getValues()[0].length);
        }

        MultiComputable test = new MultiStatComputable(new Statistics[] {MIN, MAX, MEAN});
        MetricCollectionTimeSeries output = ets.iterateAcrossTracesOfEnsemblesWithMultiComputable(test);
        for (MetricCollection mc : output)
            db.write(mc);

        HecPairedData dss = new HecPairedData(dssFilename);
        String[] pathsToFind = new String[] {
                "//Kanektok.BCAC1/flow-stats///T:20131103-1200|V:20131103-120000|/",
                "//Kanektok.BCAC1/flow-stats///T:20131104-1200|V:20131104-120000|/",
                "//Kanektok.BCAC1/flow-stats///T:20131105-1200|V:20131105-120000|/"
        };

        for (String path : pathsToFind) {
            PairedDataContainer pdc = new PairedDataContainer();
            pdc.fullName = path;
            int status = dss.read(pdc);
            assertEquals(0, status);
            assertEquals(3, pdc.yOrdinates.length);
            assertEquals(59, pdc.xOrdinates.length);
        }

        db.catalog.update();

        List<RecordIdentifier> mIds = db.getMectricPairedDataIDs();
        assertEquals(1, mIds.size());

        dss.done();


    }



}
