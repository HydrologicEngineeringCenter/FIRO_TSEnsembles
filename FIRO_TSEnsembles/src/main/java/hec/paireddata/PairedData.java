package hec.paireddata;

import java.util.ArrayList;
import java.util.function.BiConsumer;

import hec.TimeSeriesDatabase;

public class PairedData {
    private String table_name = null;
    private ArrayList<Double> indeps = new ArrayList<>();
    private ArrayList<Double> deps = new ArrayList<>();
    public PairedData(String table_name) {
        this.table_name = table_name;        
    }

    public void addRow(double indep, double dep) {
        indeps.add(indep);
        deps.add(dep);
    }

    double rate(ArrayList<Double> independant_variables) {
        return rate(independant_variables, 1);
    }

    double rate(ArrayList<Double> independant_variables, int dependant_index){
        for( int i = 0; i < indeps.size(); i++ )
        {
            if( Math.abs(independant_variables.get(0) - indeps.get(i)) < .001){
                return deps.get(i);
            }
        }
        return Double.NEGATIVE_INFINITY;
    }

    public String getName(){
        return this.table_name;
    }

    public void getAllValues( BiConsumer<  ArrayList<Double>, ArrayList<Double> > yield){
        ArrayList<Double> _indep = new ArrayList<>(1); _indep.add(0.0);
        ArrayList<Double> _dep = new ArrayList<>(1); _dep.add(0.0);
        for( int i = 0; i < indeps.size(); i++ ){
            _indep.set(0,indeps.get(i));
            _dep.set(0,deps.get(i));
            yield.accept(_indep,_dep);
            
        }

    }

}