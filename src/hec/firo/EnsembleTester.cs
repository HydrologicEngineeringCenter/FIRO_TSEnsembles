using H5Assist;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;

namespace Hec.TimeSeries.Ensemble
{
  public class EnsembleTester
  {
    // So I can test this reliably at work....
    const bool SPEEDRUN = false;
    const bool SkipDSS = true;
    public static string CacheDir = @"C:\Temp\hefs_cache";
    static string logFile = "Ensemble_testing.log";
    static string tag = "round3";

    // Global start/end times 
    static DateTime StartTime = new DateTime(2013, 11, 3, 12, 0, 0);
    static DateTime EndTime = new DateTime(2018, 11, 3, 12, 0, 0);


    static string NL = Environment.NewLine;
    const string Separator = " | ";
    const int FileNameColSize = 40;
    const int NumEnsColSize = 10;
    const int TimeColSize = 10;
    const int FileSzColSize = 10;

    static bool DisableTestReporting = false;

    static void Main(string[] args)
    {
      if (SPEEDRUN)
        EndTime = StartTime.AddDays(365);

      DSSIO.DSSReader.UseTrainingWheels = false;

      Log(NL + NL + "------" + DateTime.Now.ToString() + "-------" + NL + NL);
      Log("Filename".PadRight(FileNameColSize) + Separator +
          "#Ensembles".PadRight(NumEnsColSize) + Separator +
          "Seconds".PadRight(TimeColSize) + Separator +
          "File Size".PadRight(FileSzColSize) + NL);

      Warmup("RussianNapa");

      var watershedNames = new string[] { "RussianNapa", "EastSierra", "FeatherYuba" };
      Watershed[] baseWaterShedData = ReadCsvFiles(watershedNames);

      Console.WriteLine("Starting test:");

      int count = 0;
      foreach (var w in baseWaterShedData)
      {
        // CSV files missing?
        if (w.Locations.Count == 0)
        {
          LogWarning("Watershed Data not found: " + w.Name + Environment.NewLine);
          continue;
        }

        bool delete = count == 0;
        Console.WriteLine("Writing Watershed: " + w.Name);
        WriteAllFormats(w, delete);
        count++;      }

      foreach (var w in baseWaterShedData)
      {
        if (w.Locations.Count == 0)
          continue;
        
        ReadAllFormats(w.Name);
      }

      Console.WriteLine("Test complete, log-file written to " + logFile);
    }

    private static void Warmup(string watershedName)
    {
      // Let the JITTER settle down with the smallest case
      Console.WriteLine("Warmup time period, results will not be logged.");
      DisableTestReporting = true;

      // I'd like to warmup more, but it's SO FREAKING SLOW
      int daysWarmup = 3;
      CsvEnsembleReader r = new CsvEnsembleReader(CacheDir);
      Watershed w = r.Read(watershedName, StartTime, StartTime.AddDays(daysWarmup));
      WriteAllFormats(w, true);

      DisableTestReporting = false;
      Console.WriteLine("Finished Warmup.");
    }

    private static Watershed[] ReadCsvFiles(string[] watersheds)
    {
      List<Watershed> rval = new List<Watershed>(watersheds.Length);
      CsvEnsembleReader csvReader = new CsvEnsembleReader(CacheDir);
      Console.WriteLine("Reading CSV Directory...");
      var rt = Stopwatch.StartNew();
      foreach (var wsName in watersheds)
      {
        var ws = csvReader.ReadParallel(wsName, StartTime, EndTime);
        rval.Add(ws);
      }

      rt.Stop();
      Console.WriteLine("Finished reading csv's in " + Math.Round(rt.Elapsed.TotalSeconds) + " seconds.");
      return rval.ToArray();
    }

    private static void WriteAllFormats(Watershed waterShedData, bool delete)
    {
      File.AppendAllText(logFile, NL);
      string fn, dir;

      if (!SkipDSS)
      {
        // DSS 6/7
        fn = "ensemble_V7_" + tag + ".dss";
        if (delete) File.Delete(fn);
        WriteTimed(fn, tag, () => DssEnsemble.Write(fn, waterShedData));
        }
        fn = "ensemble_V7_profiles_" + tag + ".dss";
        if (delete) File.Delete(fn);
        WriteTimed(fn, tag, () => DssEnsemble.WriteToTimeSeriesProfiles(fn, waterShedData));
      

      bool compress = true;
      // SQLITE
      fn = "ensemble_sqlite_" + tag + ".db";
      if (delete) File.Delete(fn);
      WriteTimed(fn, tag, () =>
      {
        SqLiteEnsemble.Write(fn, waterShedData, compress, false);
      });

      fn = "ensemble_pisces_" + tag + ".pdb";
      if (delete) File.Delete(fn);
      WriteTimed(fn, tag, () =>
      {
        SqLiteEnsemble.WriteWithDataTable(fn, waterShedData, compress, true);
      });


      // Serial HDF5
      fn = "ensemble_serial_1RowPerChunk.h5";
      if (delete) File.Delete(fn);
      WriteTimed(fn, tag, () =>
      {
        using (var h5w = new H5Writer(fn))
          HDF5Ensemble.Write(h5w, waterShedData);
      });

      // Parallel HDF5
      foreach (int c in new[] { 1, 10, -1 })
      {
        fn = "ensemble_parallel_" + c.ToString() + "RowsPerChunk.h5";
        if (delete) File.Delete(fn);
        WriteTimed(fn, tag, () =>
        {
          using (var h5w = new H5Writer(fn))
            HDF5Ensemble.WriteParallel(h5w, waterShedData, c);
        });
      }
    }

    private static void ReadAllFormats(string watershedName)
    {
      //TODO - compare validateWatershed data with computed

      File.AppendAllText(logFile, NL);
      File.AppendAllText(logFile, "---------- Reading  Ensembles ----------" + NL);
      string fn;

      // TimeSeriesOfEnsembleLocations wshedData=null;
      DateTime startTime = DateTime.MinValue;
      DateTime endTime = DateTime.MaxValue;

      if (!SkipDSS)
      {
        // DSS
        fn = "ensemble_V7_" + tag + ".dss";
        ReadTimed(fn, () =>
        {
          return DssEnsemble.Read(watershedName, startTime, endTime, fn);
        });
      }
        fn = "ensemble_V7_profiles_" + tag + ".dss";
        ReadTimed(fn, () =>
       {
         return DssEnsemble.ReadTimeSeriesProfiles(watershedName, startTime, endTime, fn);
       });
      

      // SQLITE
      fn = "ensemble_sqlite_" + tag + ".db";
      ReadTimed(fn, () =>
      {
        return SqLiteEnsemble.Read(watershedName, startTime, endTime, fn);
      });

      // Pisces
      fn = "ensemble_pisces_" + tag + ".pdb";
      ReadTimed(fn, () =>
      {
        return SqLiteEnsemble.Read(watershedName, startTime, endTime, fn);
      });

      // Serial HDF5
      fn = "ensemble_serial_1RowPerChunk.h5";
      ReadTimed(fn, () =>
      {
        using (var hr = new H5Reader(fn))
          return HDF5Ensemble.Read(hr, watershedName);
      });


      // Parallel HDF5
      foreach (int c in new[] { 1, 10, -1 })
      {
        fn = "ensemble_parallel_" + c.ToString() + "RowsPerChunk.h5";
        ReadTimed(fn, () =>
         {
           using (var hr = new H5Reader(fn))
             return HDF5Ensemble.Read(hr, watershedName);
         });
      }

    }


    // Writer helpers
    private static void WriteTimed(string filename, string tag, Action CreateFile)
    {
      try
      {
        Console.WriteLine("Saving to " + filename);

        // Record the amount of time from start->end, including flushing to disk.
        var sw = Stopwatch.StartNew();

        CreateFile();

        sw.Stop();
        LogWriteResult(filename, tag, sw.Elapsed);
      }
      catch (Exception ex)
      {
        LogWarning(ex.Message);
      }
    }
    private static void WriteDirectoryTimed(string dirName, string tag, Action CreateFile)
    {
      try
      {
        // Clear it
        if (Directory.Exists(dirName))
          Directory.Delete(dirName, true);

        Directory.CreateDirectory(dirName);

        // Record the amount of time from start->end, including flushing to disk.
        var sw = Stopwatch.StartNew();

        CreateFile();

        sw.Stop();
        LogWriteResult(dirName, tag, sw.Elapsed);
      }
      catch (Exception ex)
      {
        LogWarning(ex.Message);
      }
    }

    private static void ReadTimed(string filename, Func<Watershed> f)
    {
      try
      {
        // Record the amount of time from start->end, including flushing to disk.
        var sw = Stopwatch.StartNew();
        var ensemblesFromDisk = f();
        sw.Stop();
        LogReadResult(filename, 0, sw.Elapsed);
        //Compare(filename, csvWaterShedData, ensemblesFromDisk);
      }
      catch (Exception ex)
      {
        LogWarning(ex.Message);
      }
    }


    private static void Compare(string filename, Watershed baseWaterShedData, Watershed watershed)
    {
      Console.WriteLine();
      Console.Write("checking for any differences..");
      if (!baseWaterShedData.Equals(watershed))
      {
        Console.WriteLine(filename);
        Console.WriteLine("WARNING: watershed read form disk was different!");
        LogWarning("Difference found ");
      }
      else
        Console.WriteLine(" ok.");

      //// compare to reference.
      //var locations = watershed.Forecasts[0].Locations;
      //var refLocations = baseWaterShedData.Forecasts[0].Locations;
      //for (int i = 0; i < locations.Count; i++)
      //{
      //  // .Equals was overriden in the default implementation, 
      //  // but we can't guarantee that for any other implementation....
      //  if (!locations[i].Members.Equals(refLocations[i].Members))
      //    LogWarning("Difference found at location " + locations[i].LocationName);
      //}
    }
    private static void DuplicateCheck(Watershed baseWaterShedData)
    {
      //var hs = new Dictionary<string, int>();
      //foreach (var wshed in baseWaterShedData.Forecasts)
      //{
      //  var wsName = wshed.WatershedName;
      //  foreach (Ensemble ie in wshed.Locations)
      //  {
      //    // This is being treated like a unique entity...
      //    string ensemblePath = ie.LocationName + "|" + ie.IssueDate.Year.ToString() + "_" + ie.IssueDate.DayOfYear.ToString();
      //    if (hs.ContainsKey(ensemblePath))
      //    {
      //      Console.WriteLine("Duplicate found.");
      //      int ct = hs[ensemblePath];
      //      hs[ensemblePath] = ct + 1;
      //    }
      //    else
      //    {
      //      hs.Add(ensemblePath, 1);
      //    }

      //  }
      //}
    }


    // Logging helpers
    static void Log(string msg)
    {
      // So we can dump to a file, console-write, etc.

      // Note this isn't a file-lock, it's an object lock. Should work for our purposes 
      // if multiple threads are trying to write at once.
      lock (logFile)
        File.AppendAllText(logFile, msg);
    }
    static void LogWarning(string msg)
    {
      lock (logFile)
        File.AppendAllText(logFile, "WARNING: " + msg);
    }
    static void LogWriteResult(string path, string tag, TimeSpan ts)
    {
      if (DisableTestReporting)
        return;

      long size;

      if (File.Exists(path))
      {
        FileInfo fi = new FileInfo(path);
        size = fi.Length;
      }
      else if (Directory.Exists(path))
      {
        size = GetDirectorySize(path);
        path = path.Split('\\').Last();
      }
      else
      {
        LogWarning("Path '" + path + "' does not exist on the file system.");
        return;
      }

      double mb = size / 1024.0 / 1024.0;
      Log(path.PadRight(FileNameColSize) + Separator +
          tag.ToString().PadRight(NumEnsColSize) + Separator +
          ts.TotalSeconds.ToString("F2").PadRight(TimeColSize) + Separator +
          BytesToString(size).PadRight(FileSzColSize) + NL);
    }
    static void LogReadResult(string path, int numEnsemblesToWrite, TimeSpan ts)
    {
      if (DisableTestReporting)
        return;

      if (!File.Exists(path))
      {
        LogWarning("File " + path + " was not found!");
        return;
      }

      Log(path.PadRight(FileNameColSize) + Separator +
          numEnsemblesToWrite.ToString().PadRight(NumEnsColSize) + Separator +
          ts.TotalSeconds.ToString("F2").PadRight(TimeColSize) + Separator +
          "(Reading)".PadRight(FileSzColSize) + NL);
    }

    // Other helpers
    static long GetDirectorySize(string p)
    {
      // 1. Get array of all file names.
      string[] a = Directory.GetFiles(p, "*.*");

      // 2. Calculate total bytes of all files in a loop.
      long b = 0;
      foreach (string name in a)
      {
        // 3. Use FileInfo to get length of each file.
        FileInfo info = new FileInfo(name);
        b += info.Length;
      }

      // 4. Return total size
      return b;
    }

    //static DateTime GetEndTime(int index)
    //{
    //  if (index == 1)
    //    return new DateTime(2013, 11, 1, 12, 0, 0);
    //  if (index == 10)
    //    return new DateTime(2013, 11, 11, 12, 0, 0);
    //  if (index == 100)
    //    return new DateTime(2014, 2, 8, 12, 0, 0);
    //  if (index == 1000)
    //    return new DateTime(2016, 7, 29, 12, 0, 0);

    //  return new DateTime(2017, 11, 1, 12, 0, 0);
    //}

    /// <summary>
    /// https://stackoverflow.com/questions/281640/how-do-i-get-a-human-readable-file-size-in-bytes-abbreviation-using-net
    /// </summary>
    /// <param name="byteCount"></param>
    /// <returns></returns>
    static string BytesToString(long byteCount)
    {
      string[] suf = { "B", "KB", "MB", "GB", "TB", "PB", "EB" }; //Longs run out around EB
      if (byteCount == 0)
        return "0" + suf[0];

      long bytes = Math.Abs(byteCount);
      int place = Convert.ToInt32(Math.Floor(Math.Log(bytes, 1024)));
      double num = Math.Round(bytes / Math.Pow(1024, place), 2);
      return (Math.Sign(byteCount) * num).ToString() + suf[place];
    }

  }
}
