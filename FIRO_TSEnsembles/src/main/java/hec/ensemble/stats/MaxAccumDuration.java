package hec.ensemble.stats;

public  class MaxAccumDuration implements Computable, Configurable {
    Integer _duration; //duration in hours
    Configuration _c;

    /**
     * The max accumulated volume computes the max volume for a given duration (i.e. max 2-day volume).
     * @param duration this is expected to be in integer hours
     */
    public MaxAccumDuration(Integer duration) {
        this._duration = duration;
    }

    //empty constructor added to satisfy <init> requirement when deserializing from XML with reflection
    public MaxAccumDuration(){}

    public MaxAccumDuration(Integer duration, Configuration c) {
        this._duration = duration;
        _c = c;
    }

    private Integer timeStepsPerDuration() {
            Integer timeStep = (int) _c.getDuration().toHours();
            if(timeStep == null) {
                throw new ArithmeticException("check time-series inputs");
            }
            if(timeStep > _duration) {
                throw new ArithmeticException("Your time step, " + timeStep + ", is greater than your input duration " + _duration + ". Duration must be greater than or equal to your time step");
            }
            if(_duration % timeStep != 0) {
                throw new ArithmeticException("Duration does not divide evenly with time step. Change duration to divide evenly by " + timeStep);
            }
            return this._duration/ timeStep;

         //need to determine the time step of the data and compare to the duration to know how many time-steps to include.  If duration is smaller than timestep throw in exception
    }

    @Override
    public float compute(float[] values) {

        Integer timeSPD = timeStepsPerDuration();
        float maxVal = Float.MIN_VALUE;
        float vol = 0;
        float durationVolume = 0;
        for(int i = 0; i<values.length;i++){
            durationVolume += values[i];
            if(i==(timeSPD -1)){
                vol =durationVolume;
                maxVal = vol;
            }else if(i>=timeSPD){
                float oldval = values[i-timeSPD];
                durationVolume-=oldval;
                vol =durationVolume;
                if(vol>maxVal)maxVal = vol;
            }
        }
        return maxVal;
    }
    @Override
    public void configure(Configuration c) {
        _c = c;

    }
    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.MAXACCUMDURATION};//but what duration?
    }
}
