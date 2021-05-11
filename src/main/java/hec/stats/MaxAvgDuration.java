package hec.stats;

public  class MaxAvgDuration implements Computable, Configurable {
    Integer _duration; //duration in hours
    Configuration _c;

    /**
     * Instantiates a max average computable object
     * @param duration this is expected to be in integer hours
     */
    public MaxAvgDuration(Integer duration) {
        this._duration = duration;
    }

    private Integer timeStepsPerDuration() {
        _c.getDuration().toHours();
        return 1;  //need to determine the time step of the data and compare to the duration to know how many time-steps to include.  If duration is smaller than timestep throw in exception
    }

    @Override
    public float compute(float[] values) {
        Integer denominator = this._duration;
        float maxVal = Float.MIN_VALUE;
        float avg = 0;
        float durationVolume = 0;
        for(int i = 0; i<values.length;i++){
            durationVolume += values[i];
            if(i==(this._duration -1)){
                avg =durationVolume/denominator;
                maxVal = avg;
            }else if(i>=this._duration){
                float oldval = values[i-this._duration];
                durationVolume-=oldval;
                avg =durationVolume/denominator;
                if(avg>maxVal)maxVal = avg;
            }
        }
        return maxVal;
    }

    @Override
    public void configure(Configuration c) {
        _c = c;

    }
}
