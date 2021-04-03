package hec.stats;

public class MinComputable implements Computable{
    @Override
    public float compute(float[] values){
        //calculate the min of values
        float returnvalue = 99999999999.00f;
        int size= values.length;
        float[] rval = new float[size];
        for (int i = 0; i <size ; i++) {
            if(returnval>values[i]){
                returnval = values[i];
            }
        }
        return returnvalue;
    }
}