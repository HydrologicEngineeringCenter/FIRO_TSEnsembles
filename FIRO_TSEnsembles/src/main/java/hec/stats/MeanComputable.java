package hec.stats;

public class MeanComputable implements Computable{
    @Override
    public float compute(float[] values){
        //calculate the mean of values
        float sum = 0f;
        for (int i = 0; i < values.length; i++) {
            sum += values[i];
            }
        return sum / values.length;
    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.MEAN};
    }
}