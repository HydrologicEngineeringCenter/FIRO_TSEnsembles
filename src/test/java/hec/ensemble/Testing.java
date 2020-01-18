package hec.ensemble;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class Testing {


    private static String CacheDir = "C:\\Temp\\hefs_cache";
    static String[] watershedNames = {"RussianNapa", "EastSierra", "FeatherYuba"};

    private String getTestCsvFileName()
    {
        String path = new File(getClass().getResource(
                "/hefs_cache/2013110312_Kanektok_hefs_csv_hourly.csv").getFile()).toString();
        return path;
    }
    private String getCacheDir()
    {
        File f = new File(getTestCsvFileName());
        String rval =  f.getParent();
        return rval;
    }

    @Test
    public void ReadCsv() {

        RfcCsvFile csv = new RfcCsvFile(getTestCsvFileName());
        float[][] data = csv.getEnsemble("SCRN2");

        AssertSCRN2(data);
        float[][] susc1 = csv.getEnsemble("SUSC1");

        assertEquals(1.0f, susc1[0][0], 0.0001);
        assertEquals(2.0f, susc1[0][1], 0.0001);
        assertEquals(3.0f, susc1[0][2], 0.0001);


    }

    private void AssertSCRN2(float[][] data) {
        assertEquals(-1.0f, data[0][0], 0.0001);
        assertEquals(-2.1f, data[0][1], 0.0001);
        assertEquals(-3.1f, data[0][2], 0.0001);
        assertEquals(-59.0f, data[58][0], 0.0001);
        assertEquals(-59.1f, data[58][1], 0.0001);
        assertEquals(-59.2f, data[58][2], 0.0001);
    }

    /**
     * Creates a EnsembleTimeSeriesDatabase
     * with multiple locations and issue dates
     *
     * @param filename name of database to create
     *
     */
    private EnsembleTimeSeriesDatabase createTestDatabase(String filename, int numberOfDates) throws Exception {


        ZonedDateTime issueDate1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime issueDate2 = issueDate1.plusDays(numberOfDates);

        CsvEnsembleReader csvReader = new CsvEnsembleReader(getCacheDir());
        EnsembleTimeSeries[] ets = csvReader.Read("Kanektok", issueDate1, issueDate2);

        EnsembleTimeSeriesDatabase db = new JdbcEnsembleTimeSeriesDatabase(filename, true);
        db.write(ets);
        return db;
    }
    /**
     * Reads CSV, saves one ensemble to a database, then reads ensemble back in.
     */
    @Test
    public void importCsvToDatabase() {
        try {

            String fn = "test.db";
            File f = new File(fn);
            f.delete();

            EnsembleTimeSeriesDatabase db  = createTestDatabase(fn,1);
            // --- READ
            TimeSeriesIdentifier tsid = new TimeSeriesIdentifier("Kanektok.SCRN2","flow");
            EnsembleTimeSeries ets =  db.getEnsembleTimeSeries(tsid);
            List<ZonedDateTime> issueDates = ets.getIssueDates();
            Ensemble e = db.getEnsemble(tsid, issueDates.get(0));
            float[][] data2 = e.values;
            AssertSCRN2(data2);

        } catch (Exception e) {
            Logger.logError(e);
            fail();
        }
    }

    /**
     * write Time: 468.171 s
     *
     * @throws Exception
     */
    //@Test
    public void bulkTesting() throws Exception {
        String fn = "c:/temp/ensembleTester.db";
        File f = new File(fn);
        f.delete();

        ZonedDateTime t1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime t2 = ZonedDateTime.of(2018, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        // ZonedDateTime t2 = t1.plusDays(30);
        double writeTime = 0;
        double readTime = 0;

        for (String name : watershedNames) {

            CsvEnsembleReader reader = new CsvEnsembleReader(CacheDir);
            EnsembleTimeSeries[] ets = reader.Read(name, t1, t2);
            writeTime += ensembleWriter(fn, ets);
        }
        readTime = ensembleReader(fn, t1, t2);
        System.out.println("SUMMARY");
        System.out.println("write Time: " + writeTime + " s");
        System.out.println("Read Time: " + readTime + " s");

    }

    /**
     * Read Time: 205.362 s
     * @throws Exception
     */
    //@Test
    public void readAll() throws Exception {
        String fn = "c:/temp/ensembleTester_copy.db";

        ZonedDateTime t1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        ZonedDateTime t2 = ZonedDateTime.of(2018, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
        double readTime = 0;

        readTime = ensembleReader(fn, t1, t2);

        System.out.println("Read Time: " + readTime + " s");

    }

    /**
     * ensembleWriter may be used to test
     * writing large amounts of ensemble data
     * <p>
     * Results:
     * initial: 420 seconds to write a file 8.17 gb in size
     */

    private double ensembleWriter(String fn, EnsembleTimeSeries[] ets)
     throws Exception{
        //select id, issue_date,watershed, location_name, length(byte_value_array)  from timeseries_ensemble order by issue_date, watershed
        long start = System.currentTimeMillis();
        try (JdbcEnsembleTimeSeriesDatabase db = new JdbcEnsembleTimeSeriesDatabase(fn,true)) {
            for (EnsembleTimeSeries e : ets) {
                db.write(e);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw (e);
        }
        double rval = (System.currentTimeMillis()-start) / 1000.0;
        System.out.println("write Time: " + rval);
        return rval;
    }


    /**
     * 189 seconds to read 8.17 gb file
     */
    public double ensembleReader(String fileName, ZonedDateTime t1, ZonedDateTime t2) throws Exception {
        //select id, issue_date,watershed, location_name, length(byte_value_array)  from timeseries_ensemble order by issue_date, watershed

        long start = System.currentTimeMillis();
        int count = 0;
        try (JdbcEnsembleTimeSeriesDatabase db = new JdbcEnsembleTimeSeriesDatabase(fileName,false)) {
            TimeSeriesIdentifier[] locations = db.getTimeSeriesIDs();
            for (TimeSeriesIdentifier tsid : locations) {
                EnsembleTimeSeries ets = db.getEnsembleTimeSeriesWithData(tsid, t1,t2);
                if( ets.getCount() ==0 )
                    System.out.println("Warning no ensembles found at location '"+tsid+"'");
                count += ets.getCount();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        double rval =(System.currentTimeMillis() - start)/1000.0;
        System.out.println("Read Time: " + rval);
        System.out.println("count = " + count);

        return rval;

    }

    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss",
            Locale.getDefault()).withZone(ZoneId.of("GMT"));
    @Test
    public void dateTesting(){

        String dt = "2013-11-03 12:00:00";
        ZonedDateTime zdt = ZonedDateTime.parse(dt,fmt);
        assertEquals(3,zdt.getDayOfMonth());
        assertEquals(11,zdt.getMonthValue());
        assertEquals(2013,zdt.getYear());
        assertEquals(12,zdt.getHour());
        assertEquals(0,zdt.getMinute());
    }


    @Test
    public void simulateResSim() throws Exception
    {
        String fn = "ResSim.db";
        File f = new File(fn);
        f.delete();

        // get an ensembleTimeSeries from the database
        // in initialization code somewhere.
        // database layer (base/interface ) = jdbc/sqlite instance
        createTestDatabase(fn,2);
        EnsembleTimeSeriesDatabase  db =new JdbcEnsembleTimeSeriesDatabase(fn,false);
        // InMemoryEnsembleTimeSeriesDatabase

        TimeSeriesIdentifier tsid = new TimeSeriesIdentifier("Kanektok.FARC1F","flow");

        EnsembleTimeSeries ets = db.getEnsembleTimeSeries(tsid);
        if( ets == null)
            throw new Exception("could not find "+tsid.toString());
        Object R = null; // Represents result of ResSim script processing an ensemble.
        // -- end initialization

        Ensemble e = null;
        // t represents ResSim timestep (RunTimeStep)
        ZoneId pst = ZoneId.of("America/Los_Angeles");
        ZonedDateTime t = ZonedDateTime.of(2019,12,25,0,0,0,0,pst);
        ZonedDateTime timeOfPreviousEnsemble = t;
        int numSteps  = 168; // hourly time steps
        int tolerance = 24; // require new ensemble at least every 24 hours


        for (int i = 0; i <numSteps ; i++) {

            // assuming issue_times might not be exactly regular.
            if( i == 0 || (t.isAfter(timeOfPreviousEnsemble) && ets.issueDateExists(t,tolerance) )) {
                e = ets.getEnsemble(t, tolerance);// gets nearest ensemble at or before time t
                R =  ProcessEnsemble(e);// process the new ensemble
                timeOfPreviousEnsemble = t;
            }

            // Do smart stuff with R

            t = t.plusHours(1);
        }
    }

    private Object ProcessEnsemble(Ensemble e) {
        return new Object();
    }
}