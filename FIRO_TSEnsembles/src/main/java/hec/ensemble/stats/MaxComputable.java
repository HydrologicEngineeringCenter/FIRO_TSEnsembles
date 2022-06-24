package hec.ensemble.stats;

public class MaxComputable implements Computable{
    @Override
    public float compute(float[] values){
        //calculate the max of values
        int size= values.length;
        java.util.Arrays.sort(values);
        return values[size-1];
    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.MAX};
    }
}