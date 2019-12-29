package hec.firo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.sun.xml.internal.ws.api.message.ExceptionHasMessage;
import org.junit.Test;

import java.io.File;
import java.time.LocalDateTime;


public class Testing {

    static String CacheDir = "C:\\Temp\\hefs_cache";
    @Test
    public void ReadCsv()
    {
     RfcCsvFile csv = new RfcCsvFile(CacheDir+"\\test.csv");
        float[][] data = csv.GetEnsemble("SCRN2");

        assertEquals(-1.0f, data[0][ 0],0.0001);
        assertEquals(-2.1f, data[0][1],0.0001);
        assertEquals(-3.1f, data[0][2],0.0001);
        assertEquals(-59.0f, data[58][ 0],0.0001);
        assertEquals(-59.1f, data[58][1],0.0001);
        assertEquals(-59.2f, data[58][2],0.0001);

    }

    public void WriteEnsemble(){
        try {
            RfcCsvFile csv = new RfcCsvFile(CacheDir + "\\test.csv");
            float[][] data = csv.GetEnsemble("SCRN2");
            String fn= "c:/temp/test.db";
            File f = new File(fn);
            f.delete();

            EnsembleDatabase db = new EnsembleDatabase(fn);
            Watershed ws = new Watershed("texas");
            LocalDateTime issuedate= LocalDateTime.of(2019,1,1,12,0);
            LocalDateTime[] timestamps = {issuedate};
            ws.AddForecast("home",issuedate,data,timestamps);
            db.Write(ws);

        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }


}
