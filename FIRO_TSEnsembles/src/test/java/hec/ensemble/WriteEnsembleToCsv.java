package hec.ensemble;
import java.io.FileWriter ;

public class WriteEnsembleToCsv {

    public static void main(String[] args) throws Exception{
        WriteEnsembleToCsv a = new hec.ensemble.WriteEnsembleToCsv();
        a.Write();
    }
    public void Write() throws  Exception{

    Ensemble e =TestData.getSampleEnsemble("Kanektok.SCRN2", "flow");

     float[][] values = e.getValues();
        FileWriter  f = new FileWriter ("c:/temp/a.csv");
        // write Date-time in first row
        java.time.ZonedDateTime[] zonedDateTimes = e.startDateTime();
        for (int i = 0; i < zonedDateTimes.length; i++) {
            f.write(zonedDateTimes[i].toString()+", ");
        }
        f.write("\n");
        for (int row = 0; row < values.length ; row++) {
            for (int column = 0; column < values[0].length ; column++) {
                f.write(values[row][column] + ", ");
            }
            f.write("\n");
        }

        f.close();
    }
}
