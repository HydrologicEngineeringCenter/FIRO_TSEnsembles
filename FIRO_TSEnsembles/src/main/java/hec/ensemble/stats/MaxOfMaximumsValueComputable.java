package hec.ensemble.stats;

public class MaxOfMaximumsValueComputable implements SingleValueComputable {
    @Override
    public float compute(float[][] values) {
        float maxval = -Float.MAX_VALUE;
        for (float[] member : values){
            for(float val : member){
                if (maxval<val){
                    maxval = val;
                }
            }
        }
        return maxval;
    }

    @Override
    public String getOutputUnits() {
        return null;
    }

    @Override
    public String StatisticsLabel() {
        return null;
    }
}
