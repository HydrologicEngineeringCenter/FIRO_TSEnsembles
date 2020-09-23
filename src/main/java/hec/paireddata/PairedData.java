package hec.paireddata;

import java.util.ArrayList;
import java.util.function.BiConsumer;

import hec.Identifier;
import hec.TimeSeriesDatabase;

public abstract class PairedData {    
    //private PairDataSeriesStorage storageStrategy;

    public PairedData(/*TimeSeriesStorage strategy*/){
        //storageStrategy = strategy;
    }
    

    public abstract PairedDataIdentifier identifier();
    public abstract double rate(ArrayList<Double> independant_variables) ;    

    public abstract double rate(ArrayList<Double> independant_variables, int dependant_index);

    public abstract String getName();

    public abstract void getAllValues( BiConsumer<  ArrayList<Double>, ArrayList<Double> > yield);

    /**
     * @return The strategy used for storage
     * 
     */
    /*public PairedDataStorage storageStrategy(){
        return storageStrategy;
    }*/

}