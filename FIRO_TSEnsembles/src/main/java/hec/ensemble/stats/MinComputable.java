package hec.ensemble.stats;

public class MinComputable implements Computable{
    @Override
    public float compute(float[] values){
        //calculate the min of values
        /*float returnvalue = 99999999999.00f;
        int size= values.length;
        float[] rval = new float[size];
        for (int i = 0; i <size ; i++) {
            if(returnvalue>values[i]){
                returnvalue = values[i];
            }
        }
        return returnvalue;*/
        java.util.Arrays.sort(values);
        return values[0];
    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.MIN};
    }

    @Override
    public String StatisticsLabel() {
        return "MIN";
    }
}