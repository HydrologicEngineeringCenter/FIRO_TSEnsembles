package hec.firo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CsvEnsembleReader {


    String path; // path to csv files

    public CsvEnsembleReader(String path) {
        this.path = path;
    }

    static boolean DebugMode = false;

    static void Log(String msg) {
        if (DebugMode)
            System.out.println(msg);
    }

    static void LogWarning(String msg) {
        System.out.println("Warning: " + msg);
    }
    /// <summary>
    /// Reads list of Forecast
    /// </summary>
    /// <param name="watershedName"></param>
    /// <param name="issueDate"></param>
    /// <returns></returns>
    RfcCsvFile Read(String watershedName, LocalDateTime issueDate)
    {
        //https://www.cnrfc.noaa.gov/csv/2019092312_RussianNapa_hefs_csv_hourly.zip


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHH");
        String fileName = formatter.format(issueDate) + "_";
        fileName += watershedName;
        fileName += "_hefs_csv_hourly";

        //String csvFileName =
          Path p = Paths.get(path, fileName + ".csv");
          File f = p.toFile();
        if ( f.exists())
        {
            Log("Found " + f.toString() + " in cache.  Reading...");
            RfcCsvFile csv = new RfcCsvFile(p.toString());
            return csv;
        }
        else
        {
            LogWarning("Warning: " + p.toString() + " not found, skipping");
            return null;
        }
    }
    public Watershed Read(String watershedName, LocalDateTime startDate, LocalDateTime endDate)
    {

        if (!ValidDates(startDate, endDate))
            return null;
        Watershed output = new Watershed(watershedName);

        LocalDateTime t = startDate;

        while( t.isBefore(endDate.plusHours(1)))
        {
            RfcCsvFile csv = Read(watershedName, t);

            if (csv != null)
            {
                for (String locName : csv.LocationNames)
                {
                    LocalDateTime t1 = csv.TimeStamps[0];
                    Forecast f = output.AddForecast(locName, t, csv.GetEnsemble(locName),t1,csv.getInterval());
                    //f.TimeStamps = csv.TimeStamps;
                }
            }
            t = t.plusDays(1);
        }
        return output;
    }

//    public Watershed ReadParallel(String watershedName, DateTime startDate, DateTime endDate)
//    {
//        if (!ValidDates(startDate, endDate))
//            return null;
//
//        var output = new Watershed(watershedName);
//
//        // Each forecast is one day
//        int numTotal = (int)Math.Round((endDate - startDate).TotalDays) + 1;
//
//        Parallel.For(0, numTotal, i =>
//                {
//                        DateTime day = startDate.AddDays(i);
//
//        var csv = Read(watershedName, day);
//
//        if (csv != null)
//        {
//            lock (output)
//            {
//                foreach (String locName in csv.LocationNames)
//                {
//                    Forecast f = output.AddForecast(locName, day, csv.GetEnsemble(locName),csv.TimeStamps);
//                    f.TimeStamps = csv.TimeStamps;
//                }
//            }
//        }
//
//      });
//
//        return output;
//    }

    boolean ValidDates(LocalDateTime startDate, LocalDateTime endDate)
    {
        if (startDate.getHour() != 12)
        {
            System.out.println("start time must be 12");
            return false;
        }
        if (endDate.getHour() != 12)
        {
            System.out.println("end time must be 12");
            return false;
        }

        if (startDate.isAfter(endDate))
        {
            System.out.println("end date should be after start date");
            return false;
        }
        return true;
    }
}
