package hec.paireddata;

import java.sql.Connection;
import java.util.ArrayList;

import hec.TimeSeriesDatabase;

public class PairedData {
    private String sql_table_name;
    private TimeSeriesDatabase database;

    public PairedData(TimeSeriesDatabase db, String table_name){
        this.database = db;

        this.sql_table_name = db.sqlTableFor(table_name);
    }

    double rate(ArrayList<Double> independant_variables){
        return rate(independant_variables,1);
    }

    double rate(ArrayList<Double> independant_variables, int dependant_index){

        return Double.NEGATIVE_INFINITY;
    }

}