package hec.stats;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.math.*;

public  class MovingAvg implements MultiComputable, Configurable {
    Integer _duration; //duration in hours
    String _start;
    Configuration _c;

    /**
     * Instantiates a moving computable object
     *
     * @param duration this is expected to be in integer hours
     */

    /* duration is the data set times*/
    public MovingAvg(Integer duration, String start) {
        this._duration = duration;
        this._start=start;
    }

    private Integer timeStepsPerDuration() {
        Integer timeStep = (int) _c.getDuration().toHours();
        if (timeStep == null) {
            throw new ArithmeticException("check time-series inputs");
        }
        if (timeStep > _duration) {
            throw new ArithmeticException("Your time step, " + timeStep + ", is greater than your input duration " + _duration + ". Duration must be greater than or equal to your time step");
        }
        if (_duration % timeStep != 0) {
            throw new ArithmeticException("Duration does not divide evenly with time step. Change duration to divide evenly by " + timeStep);
        }
        return this._duration / timeStep;

        //need to determine the time step of the data and compare to the duration to know how many time-steps to include.  If duration is smaller than timestep throw in exception
    }

    private Integer graphing() {
    Integer denominator = timeStepsPerDuration();

    int ret=0;

    if (this._start==null){
        throw new ArithmeticException("input moving average calculation start");

    }
    if (this._start=="Beginning") {
        ret= 0;
    }
    if (this._start=="Middle") {
        ret=(int) Math.ceil((float)denominator/2)-1;
    }
    if(this._start == "End") {
        ret = denominator-1;
    }
    return ret;
    }

    @Override
    public float[] multiCompute(float[] values) {
        Integer denominator = timeStepsPerDuration();

        float[] ret = new float[values.length];
        float durationVolume = 0;

        for (int i = 0; i < (values.length); i++) {
            durationVolume += values[i];
            if (i == (denominator-1)) {
                ret[i-(denominator-1) + graphing()] =  durationVolume / denominator;
            } else if (i >= (denominator-1)) {
                float oldval = values[i - denominator];
                durationVolume -= oldval;
                ret[i-(denominator-1) + graphing()] = durationVolume / denominator;
            }
        }

        return ret;
    }

    @Override
    public Statistics[] Statistics() {
        return new Statistics[]{Statistics.MOVINGAVG};//but what duration?
    }

    @Override
    public void configure(Configuration c) {
        _c = c;

    }
}