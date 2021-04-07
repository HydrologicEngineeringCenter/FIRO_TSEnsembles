package hec.stats;

public class MeanComputable implements Computable{
    @Override
    public float compute(float[] values){
        //calculate the mean of values
        float sum = 0f;
        float[] rval = new float[values.length];
        for (int i = 0; i <= values.length; i++) {
            sum += i;
            }
        return sum / values.length;
    }
}