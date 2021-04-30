package hec.stats;

public  class MaxAvgDuration implements Computable {
    Integer duration;

    public MaxAvgDuration(Integer duration) {
        this.duration = duration;
    }
    @Override
    public float compute(float[] values) {
        Integer denominator = this.duration;
        float maxVal = Float.MIN_VALUE;
        float avg = 0;
        float durationVolume = 0;
        for(int i = 0; i<values.length;i++){
            durationVolume += values[i];
            if(i==(this.duration-1)){
                avg =durationVolume/denominator;
                maxVal = avg;
            }else if(i>=this.duration){
                float oldval = values[i-this.duration];
                durationVolume-=oldval;
                avg =durationVolume/denominator;
                if(avg>maxVal)maxVal = avg;
            }
        }
        return maxVal;
    }
}
