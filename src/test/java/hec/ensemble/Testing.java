package hec.ensemble;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class Testing {


    private static String CacheDir = "C:\\Temp\\hefs_cache";
    static String TestFile = CacheDir + "\\2013110312_test_hefs_csv_hourly.csv";
    static String[] watershedNames = {"RussianNapa", "EastSierra", "FeatherYuba"};

    @Test
    public void ReadCsv() {
        RfcCsvFile csv = new RfcCsvFile(TestFile);
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
     * Reads CSV, saves one ensemble to a database, then reads ensemble back in.
     */
    @Test
    public void readWriteEnsemble() {
        try {

            String fn = "c:/temp/test.db";
            File f = new File(fn);
            f.delete();

            CsvEnsembleReader csvReader = new CsvEnsembleReader(CacheDir);
            ZonedDateTime issueDate = ZonedDateTime.of(2013, 11, 3, 12, 0, 0, 0, ZoneId.of("GMT"));
            RfcCsvFile csv = csvReader.Read("test", issueDate);
            //RfcCsvFile csv = new RfcCsvFile(TestFile);
            float[][] data = csv.getEnsemble("SCRN2");


            JdbcEnsembleDatabase db = new JdbcEnsembleDatabase(fn);
            EnsembleTimeSeries ets = new EnsembleTimeSeries("texas", "", "");

            ets.addEnsemble(csv.getIssueDate(), data, csv.TimeStamps[0], csv.getInterval());
            db.Write(ets);

            // --- READ
            db = new JdbcEnsembleDatabase(fn);
            Ensemble e = db.Read("texas", csv.getIssueDate());
            float[][] data2 = e.values;


            AssertSCRN2(data2);
            assertEquals(data.length, data2.length);
            assertEquals(data[0].length, data2[0].length);

        } catch (Exception e) {
            System.out.println("error");
            System.out.println(e.getMessage());
            fail();
        }
    }

    /**
     * Write Time: 468.171 s
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
        System.out.println("Write Time: " + writeTime + " s");
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
        try (JdbcEnsembleDatabase db = new JdbcEnsembleDatabase(fn)) {
            for (EnsembleTimeSeries e : ets) {
                db.Write(e);
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw (e);
        }
        double rval = (System.currentTimeMillis()-start) / 1000.0;
        System.out.println("Write Time: " + rval);
        return rval;
    }


    /**
     * 189 seconds to read 8.17 gb file
     */
    public double ensembleReader(String fileName, ZonedDateTime t1, ZonedDateTime t2) throws Exception {
        //select id, issue_date,watershed, location_name, length(byte_value_array)  from timeseries_ensemble order by issue_date, watershed

        long start = System.currentTimeMillis();
        int count = 0;
        try (JdbcEnsembleDatabase db = new JdbcEnsembleDatabase(fileName)) {
            List<String> locations = db.getLocations();
            for (String name : locations) {
                EnsembleTimeSeries ets = db.ReadTS(name, t1,t2);
                if( ets.size() ==0 )
                    System.out.println("Warning no ensembles found at location '"+name+"'");
                count += ets.size();
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


}