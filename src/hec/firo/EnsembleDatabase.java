package hec.firo;


import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

/**
 *  Read/Write Ensembles to a ODBC database
 */
public class EnsembleDatabase {

    static String DateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    private static String TableName = "timeseries_blob";

    String FileName;
    Connection _connection;
    public EnsembleDatabase(String fileName) throws Exception
    {
      FileName= fileName;
      Properties prop = new Properties();
      //prop.setProperty("shared_cache", "false");
        //  Synchronous=Off;Pooling=True;Journal Mode=Off";  // dangerous but faster.
        //
      _connection =  DriverManager.getConnection("jdbc:sqlite:"+FileName,prop);
      _connection.setAutoCommit(false);
        CreateTable();
    }

    PreparedStatement insertCMD;

    public void Write(Watershed watershed)
    {
        try {
            String sql = "INSERT INTO timeseries_blob ([id], [issue_date], [watershed], [location_name], "+
                    " [timeseries_start_date], [member_length], [member_count], [compressed], [byte_value_array]) VALUES "+
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?)";

            insertCMD = _connection.prepareStatement(sql);

            boolean compress = true;
            int index = GetMaxID();
            for(Location loc : watershed.Locations)
            {
                for(Forecast f : loc.Forecasts)
                {
                    index++;
                    float[][] data = f.Ensemble;
                    byte[] bytes = EnsembleCompression.Compress(data);
                     InsertEnsemble(index, f.IssueDate, watershed.Name, loc.Name, f.TimeStamps[0],
                            data[0].length, data.length, compress, bytes);
                }
            }
            _connection.commit();
        }catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally {

        }

    }

    public static Watershed Read(String watershedName, LocalDateTime startTime, LocalDateTime endTime)
    {
        Watershed rval = new Watershed(watershedName);

        String sql = "select * from " + TableName +
                " WHERE issue_date >= '" + FormatDate(startTime) + "' "
                + " AND issue_date <= '" + FormatDate(endTime) + "' "
                + " AND watershed = '" + watershedName + "' ";
        sql += " order by watershed,issue_date,location_name";



//        LocalDateTime prevIssueDate = Convert.ToDateTime(table.Rows[0]["issue_date"]);
//        DateTime currentDate = Convert.ToDateTime(table.Rows[0]["issue_date"]);
//        foreach (DataRow row in table.Rows)
//        {
//            currentDate = Convert.ToDateTime(row["issue_date"]);
//
//            var times = GetTimes(row);
//            GetValues(row,ref values);
//
//            rval.AddForecast(row["location_name"].ToString(),
//                    currentDate,
//                    values,
//                    times);
//
//        }
        return rval;
    }


    static DateTimeFormatter _formatter = DateTimeFormatter.ofPattern(DateTimeFormat);
    private static String FormatDate(LocalDateTime t)
    {
     return t.format(_formatter);
    }

    private void InsertEnsemble(int id, LocalDateTime issue_date, String watershed, String location_name,
                               LocalDateTime timeseries_start_date,int member_length, int member_count,boolean compressed,
                               byte[] byte_value_array) throws Exception
    {


        insertCMD.setInt(1, id);
        insertCMD.setString(2,FormatDate(issue_date));
        insertCMD.setString(3,watershed);
        insertCMD.setString(4,location_name);
        insertCMD.setString(5, FormatDate(timeseries_start_date));
        insertCMD.setInt(6, member_length);
        insertCMD.setInt(7, member_count);
        insertCMD.setBoolean(8, compressed  );
        insertCMD.setBytes(9, byte_value_array);
        insertCMD.execute();
    }


    private int GetMaxID()
    {
        String sql = "SELECT max(id) FROM "+TableName;
        int rval = -1;
        try {
            Statement stmt  = _connection.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                rval = rs.getInt("id");
                return rval;
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return rval;
    }

    private void  CreateTable()throws Exception
    {
        String sql = "CREATE TABLE IF NOT EXISTS " + TableName
                + " ( id integer not null primary key,"
                + "    issue_date datetime, "
                + "   watershed NVARCHAR(100) ,"
                + "   location_name NVARCHAR(100) ,"
                + "   timeseries_start_date datetime ,"
                + "   member_length integer    ,"
                + "   member_count integer    ,"
                + "   compressed integer    ,"
                + "  byte_value_array BLOB NULL )";
        PreparedStatement cmd = _connection.prepareStatement(sql);
        cmd.execute();
    }



}
