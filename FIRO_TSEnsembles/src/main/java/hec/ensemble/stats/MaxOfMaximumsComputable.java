package hec.ensemble.stats;

public class MaxOfMaximumsComputable implements SingleComputable {
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
    public String StatisticsLabel() {
        return null;
    }
}
