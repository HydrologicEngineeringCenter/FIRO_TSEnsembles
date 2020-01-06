package hec.ensemble;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Testing {


    private static String CacheDir = "C:\\Temp\\hefs_cache";
    static String TestFile = CacheDir + "\\test.csv";
    static String[] watershedNames = {"RussianNapa", "EastSierra", "FeatherYuba"};

    @Test
    public void ReadCsv() {
        RfcCsvFile csv = new RfcCsvFile(TestFile);
        float[][] data = csv.GetEnsemble("SCRN2");

        AssertSCRN2(data);
        float[][] susc1 = csv.GetEnsemble("SUSC1");

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
            RfcCsvFile csv = new RfcCsvFile(TestFile);
            float[][] data = csv.GetEnsemble("SCRN2");

            String fn = "c:/temp/test.db";
            File f = new File(fn);
            f.delete();

            JdbcEnsembleDatabase db = new JdbcEnsembleDatabase(fn);
            EnsembleTimeSeries ets = new EnsembleTimeSeries("texas","","");
            ZonedDateTime issuedate = ZonedDateTime.of(2019, 1, 1, 12, 0,0,0, ZoneId.of("GMT"));

            ets.addEnsemble(issuedate, data, issuedate, csv.getInterval());
            db.Write(ets);

            db = new JdbcEnsembleDatabase(fn);
            EnsembleTimeSeries ws2 = db.Read("texas", issuedate);
            float[][] data2 = ws2.ensembleList.get(0).values;


            AssertSCRN2(data2);
            assertEquals(data.length, data2.length);
            assertEquals(data[0].length, data2[0].length);

        } catch (Exception e) {
            System.out.println("error");
            System.out.println(e.getMessage());
            assertFalse(true);
        }
    }

    @Test
    public void bulkTesting() throws Exception
    {
        String fn = "c:/temp/ensembleTester.db";
        File f = new File(fn);
        f.delete();

        ZonedDateTime t1 = ZonedDateTime.of(2013, 11, 3, 12, 0, 0,0,ZoneId.of("GMT"));
        //   ZonedDateTime t2 = ZonedDateTime.of(2018, 11, 3, 12, 0, 0,0,ZoneId.of("GMT"));
        ZonedDateTime t2 = t1.plusDays(50);
        double writeTime = 0;
        double readTime = 0;

        for (String name:watershedNames) {

            CsvEnsembleReader reader = new CsvEnsembleReader(CacheDir);
            EnsembleTimeSeries[] ws = reader.Read(name, t1, t2);
            writeTime += ensembleWriter(fn, ws);
            readTime  += ensembleReader(fn,t1,t2);
        }
        System.out.println("SUMMARY");
        System.out.println("Write Time: " + writeTime+" s");
        System.out.println("Read Time: " + readTime +" s");

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

        long total = 0;
        try (JdbcEnsembleDatabase db = new JdbcEnsembleDatabase(fn)) {
            for (EnsembleTimeSeries e : ets) {
                long start = System.currentTimeMillis();
                db.Write(e);
                long end = System.currentTimeMillis();
                total += end - start;
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw (e);
        }
        double rval = total / 1000.0;
        System.out.println("Write Time: " + rval);
        return rval;
    }


    /**
     * 189 seconds to read 8.17 gb file
     */
    public double ensembleReader(String fileName, ZonedDateTime t1, ZonedDateTime t2) throws Exception {
        //select id, issue_date,watershed, location_name, length(byte_value_array)  from timeseries_ensemble order by issue_date, watershed

        long start = System.currentTimeMillis();
        long total = 0;
        try (JdbcEnsembleDatabase db = new JdbcEnsembleDatabase(fileName)) {
            for (String name : watershedNames) {
                EnsembleTimeSeries ets = db.Read(name, t1);

            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        long end = System.currentTimeMillis();
        total += end - start;
        double rval = total/1000.0;
        System.out.println("Read Time: " + rval);
        return rval;

    }

}
