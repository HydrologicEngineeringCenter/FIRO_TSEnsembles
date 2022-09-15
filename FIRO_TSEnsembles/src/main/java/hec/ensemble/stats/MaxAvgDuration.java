package hec.ensemble.stats;

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

    //empty constructor added to satisfy <init>() function deserializing from XML with reflection
    public MaxAvgDuration(){}

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

        Integer denominator = timeStepsPerDuration();
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
    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.MAXAVERAGEDURATION};//but what duration?
    }

    @Override
    public String StatisticsLabel() {
        return "MAXAVERAGEDURATION" + "("+ _duration + "Hours)";
    }
}
