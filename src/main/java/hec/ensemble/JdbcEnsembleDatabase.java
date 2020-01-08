package hec.ensemble;

import java.sql.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *  Read/Write Ensembles to a JDBC database
 */
public class JdbcEnsembleDatabase implements AutoCloseable {


    private static String TableName = "timeseries_ensemble";

    private String FileName;
    Connection _connection;

    public JdbcEnsembleDatabase(String database) throws Exception
    {
      FileName= database;
      Properties prop = new Properties();
      //prop.setProperty("shared_cache", "false");
        //  Synchronous=Off;Pooling=True;Journal Mode=Off";  // dangerous but faster.
        //
      _connection =  DriverManager.getConnection("jdbc:sqlite:"+FileName,prop);
      _connection.setAutoCommit(false);
        CreateTable();
    }

    public void close()
    {
        try {
            _connection.commit();
            _connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    PreparedStatement insertCMD;

    public void Write(EnsembleTimeSeries ets) throws Exception {
        Write(new EnsembleTimeSeries[]{ets});
    }
        public void Write(EnsembleTimeSeries[] ets) throws Exception
    {
        try {
            String sql = "INSERT INTO "+TableName+" ([id], [issue_date], [watershed], [location_name], "+
                    " [timeseries_start_date], [member_length], [member_count], [compressed], [byte_value_array]) VALUES "+
                    "(?, ?, ?, ?, ?, ?, ?, ?, ?)";

            insertCMD = _connection.prepareStatement(sql);

            boolean compress = true;
            int index = GetNextID();
            for(EnsembleTimeSeries loc : ets)
            {
                for(Ensemble e : loc.items)
                {
                    index++;
                    float[][] data = e.values;
                    byte[] bytes = EnsembleCompression.Pack(data,compress);
                     InsertEnsemble(index, e.IssueDate, loc.getWatershedName(),
                             loc.getLocationName(), e.startDateTime,
                            data[0].length, data.length, compress, bytes);
                }
            }
            _connection.commit();
        }catch(Exception e)
        {
            Logger.logError("writing ensembles "+e.getMessage());
        }
        finally {
           // if( _connection!= null)
             //   _connection.close();
        }

    }
    public Ensemble Read(String  locationName, ZonedDateTime issueDate) {

        EnsembleTimeSeries ts = ReadTS(locationName,issueDate,issueDate);
        if( ts.size() >0)
            return ts.items.get(0);
        return  null;
    }

    public EnsembleTimeSeries ReadTS(String  locationName, ZonedDateTime issueDateStart, ZonedDateTime issueDateEnd)
    {
        EnsembleTimeSeries rval = new EnsembleTimeSeries(locationName,"","");

        String sql = "select * from " + TableName +
                " WHERE issue_date  >= '" + DateUtility.FormatDate(issueDateStart) + "' "
                +" AND issue_date <= '"+DateUtility.FormatDate(issueDateEnd)+" '"
                + " AND location_name = ? ";
        sql += " order by issue_date";

        try {
            PreparedStatement statement  = _connection.prepareStatement(sql);
            statement.setString(1,locationName);
            ResultSet rs    = statement.executeQuery();
            // loop through the result set
            while (rs.next()) {
                int id = rs.getInt(1);
                String d = rs.getString(2);
                ZonedDateTime issue_date = DateUtility.parseDateTime(d);

                //watershedName = rs.getString(3,);
                //String locName = rs.getString(4);
                d = rs.getString(5);
                ZonedDateTime start_date =  DateUtility.parseDateTime(d);
                int member_length = rs.getInt(6 );
                int member_count = rs.getInt(7);
                boolean compressed = rs.getBoolean(8);
                byte[] byte_value_array = rs.getBytes(9);
                float[][] values = EnsembleCompression.UnPack(byte_value_array,member_count,member_length,compressed);
                int secondsPerHour = 3600;// TO DO .. FIX ME hardcoded (need to add increment to schema)
                Ensemble e = new Ensemble(issue_date,values,start_date,Duration.ofSeconds(secondsPerHour));
                rval.addEnsemble(e);
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage());
        }
        return rval;
    }



    private void InsertEnsemble(int id, ZonedDateTime issue_date, String watershed, String location_name,
                               ZonedDateTime timeseries_start_date,int member_length, int member_count,boolean compressed,
                               byte[] byte_value_array) throws Exception
    {


        insertCMD.setInt(1, id);
        insertCMD.setString(2,DateUtility.FormatDate(issue_date));
        insertCMD.setString(3,watershed);
        insertCMD.setString(4,location_name);
        insertCMD.setString(5, DateUtility.FormatDate(timeseries_start_date));
        insertCMD.setInt(6, member_length);
        insertCMD.setInt(7, member_count);
        insertCMD.setBoolean(8, compressed  );
        insertCMD.setBytes(9, byte_value_array);
        insertCMD.execute();
    }


    private int GetNextID()
    {
        String sql = "SELECT max(id) max FROM "+TableName;
        int rval = 0;
        try {
            Statement stmt  = _connection.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                Object o = rs.getObject("max");
                if( o == null)
                    return 0;
                rval = (int)o;
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage());
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


    public List<String> getLocations() {

        List<String> rval = new ArrayList<>();
        String sql = "select distinct location_name from " + TableName +
              " order by location_name";
        try {
            Statement stmt  = _connection.createStatement();
            ResultSet rs    = stmt.executeQuery(sql);
            // loop through the result set
            while (rs.next()) {
                rval.add(rs.getString(1));
            }
        } catch (Exception e) {
            Logger.logError(e.getMessage());
        }
        return rval;
    }
}
