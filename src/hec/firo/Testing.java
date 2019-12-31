package hec.firo;

import org.junit.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.time.LocalDateTime;

import static org.junit.Assert.*;


public class Testing {

    static String CacheDir = "C:\\Temp\\hefs_cache";
    @Test
    public void ReadCsv()
    {
     RfcCsvFile csv = new RfcCsvFile(CacheDir+"\\test.csv");
        float[][] data = csv.GetEnsemble("SCRN2");

        AssertSCRN2(data);

    }

    private void AssertSCRN2(float[][] data) {
        assertEquals(-1.0f, data[0][ 0],0.0001);
        assertEquals(-2.1f, data[0][1],0.0001);
        assertEquals(-3.1f, data[0][2],0.0001);
        assertEquals(-59.0f, data[58][ 0],0.0001);
        assertEquals(-59.1f, data[58][1],0.0001);
        assertEquals(-59.2f, data[58][2],0.0001);
    }

    @Test
    public void readWriteEnsemble(){
        try {
            RfcCsvFile csv = new RfcCsvFile(CacheDir + "\\test.csv");
            float[][] data = csv.GetEnsemble("SCRN2");
            String fn= "c:/temp/test.db";
            File f = new File(fn);
            f.delete();

            EnsembleDatabase db = new EnsembleDatabase(fn);
            Watershed ws = new Watershed("texas");
            LocalDateTime issuedate= LocalDateTime.of(2019,1,1,12,0);

            ws.AddForecast("home",issuedate,data,issuedate,csv.getInterval());
            db.Write(ws);

            db = new EnsembleDatabase(fn);
            Watershed ws2 = db.Read("texas",issuedate,issuedate);
            float[][] data2 = ws2.Locations.get(0).Forecasts.get(0).Ensemble;
            AssertSCRN2(data2);


        }catch (Exception e)
        {
            System.out.println("error");
            System.out.println(e.getMessage());
            assertFalse(true);
        }
    }

    @Test
    public void Load40Days()
    {
        String cacheDir = "c:/temp/hefs_cache";
        int numDays = 40;
        String fn = "c:/temp/java-ensemble-test"+numDays+".db";

        File f = new File(fn);
        f.delete();
        // TO DO.. need Ensemble Reader
        CsvEnsembleReader reader = new CsvEnsembleReader(cacheDir);

        String[] watershedNames = { "RussianNapa", "EastSierra", "FeatherYuba" };

        try {
            EnsembleDatabase db = new EnsembleDatabase(fn);
            for (String name : watershedNames) {

                LocalDateTime t1 = LocalDateTime.of(2013, 11, 3, 12, 0, 0);
                Watershed ws = reader.Read(name, t1, t1.plusDays(numDays));

                db.Write( ws);
            }
        }
        catch (Exception ex)
        {
        System.out.println("Error: "+ex.getMessage());

        }
    }

    @Test
    public void SqLiteTest() {
        //-Djava.io.tmpdir=c:/temp
        Connection c =null;
        String cs = "jdbc:sqlite:c:/temp/sqlite_test.db";
        try{
        c = DriverManager.getConnection(cs);
        System.out.println("Hi");
        }
        catch (Exception e)
        {
            System.out.println("Error: "+e.getMessage());
        }


    }



}
