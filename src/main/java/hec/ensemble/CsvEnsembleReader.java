package hec.ensemble;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class CsvEnsembleReader {


    String path; // path to csv files

    public CsvEnsembleReader(String path) {
        this.path = path;
    }


    /// <summary>
    /// Reads list of Forecast
    /// </summary>
    /// <param name="watershedName"></param>
    /// <param name="issueDate"></param>
    /// <returns></returns>
    RfcCsvFile Read(String watershedName, ZonedDateTime issueDate)
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
            Logger.log("Found " + f.toString() + " in cache.  Reading...");
            RfcCsvFile csv = new RfcCsvFile(p.toString());
            return csv;
        }
        else
        {
            Logger.logWarning("Warning: " + p.toString() + " not found, skipping");
            return null;
        }
    }
    public Watershed Read(String watershedName, ZonedDateTime startDate, ZonedDateTime endDate)
    {

        if (!ValidDates(startDate, endDate))
            return null;
        Watershed output = new Watershed(watershedName);

        ZonedDateTime t = startDate;

        while( t.isBefore(endDate.plusHours(1)))
        {
            RfcCsvFile csv = Read(watershedName, t);

            if (csv != null)
            {
                for (String locName : csv.LocationNames)
                {
                    ZonedDateTime t1 = csv.TimeStamps[0];
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

    boolean ValidDates(ZonedDateTime startDate, ZonedDateTime endDate)
    {
        if (startDate.getHour() != 12)
        {
            Logger.logError("start time must be 12");
            return false;
        }
        if (endDate.getHour() != 12)
        {
            Logger.logError("end time must be 12");
            return false;
        }

        if (startDate.isAfter(endDate))
        {
            Logger.logError("end date should be after start date");
            return false;
        }
        return true;
    }
}
