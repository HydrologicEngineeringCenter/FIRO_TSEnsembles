package hec.ensemble.stats;

import javax.measure.IncommensurableException;
import javax.measure.Unit;
import java.util.logging.Level;
import java.util.logging.Logger;

public  class MaxAccumDuration implements Computable, Configurable {
    private static final Logger logger = Logger.getLogger(MaxAccumDuration.class.getName());
    private static final String DEFAULT_INPUT_UNITS = "cfs";
    Integer accumulatingDuration; //duration in hours
    Configuration config;
    private Unit<?> unit;

    /**
     * The max accumulated volume computes the max volume for a given duration (i.e. max 2-day volume).
     * @param duration this is expected to be in integer hours. Units will be converted to acre-ft from cfs
     */
    public MaxAccumDuration(Integer duration) {
        this.accumulatingDuration = duration;
    }

    //empty constructor added to satisfy <init> requirement when deserializing from XML with reflection
    public MaxAccumDuration(){}

    private Integer timeStepsPerDuration() {
        int timeStep = (int) config.getDuration().toHours();

        if (timeStep == 0) {
            throw new ArithmeticException("check time-series inputs");
        }
        if (timeStep > accumulatingDuration) {
            throw new ArithmeticException("Your time step, " + timeStep + ", is greater than your input duration " + accumulatingDuration + ". Duration must be greater than or equal to your time step");
        }
        if (accumulatingDuration % timeStep != 0) {
            throw new ArithmeticException("Duration does not divide evenly with time step. Change duration to divide evenly by " + timeStep);
        }
        return this.accumulatingDuration / timeStep;

        //need to determine the time step of the data and compare to the duration to know how many time-steps to include.  If duration is smaller than timestep throw in exception
    }

    @Override
    public float compute(float[] values) {
        double factor = getAccumulationConversionFactor();
        long duration = getDuration();

        int timeSPD = timeStepsPerDuration();
        float maxVal = Float.MIN_VALUE;
        float vol;
        float durationVolume = 0;
        for(int i = 0; i<values.length;i++){
            durationVolume += factor * values[i] * duration;
            if(i==(timeSPD -1)){
                vol =durationVolume;
                maxVal = vol;
            }else if(i>=timeSPD){
                float oldval = (float) (factor * values[i-timeSPD] * duration);
                durationVolume-=oldval;
                vol =durationVolume;
                if(vol>maxVal)maxVal = vol;
            }
        }
        return maxVal;
    }

    private double getAccumulationConversionFactor() {
        unit = ConvertUnits.convertStringUnits(getInputUnits());
        try {
            return ConvertUnits.getAccumulationConversionFactor(unit);
        } catch (IncommensurableException e) {
            logger.log(Level.SEVERE, "Error in max accumulated duration compute");
            throw new RuntimeException(e);
        }
    }

    private String getInputUnits() {
        if(config == null || config.getUnits().isEmpty()) {
            return DEFAULT_INPUT_UNITS;
        } else {
            return  config.getUnits();
        }
    }

    private long getDuration() {
        return config.getDuration().getSeconds();
    }

    @Override
    public String getOutputUnits() {
        return UnitsUtil.getOutputUnitString(unit);
    }

    @Override
    public void configure(Configuration c) {
        config = c;
    }

    @Override
    public String StatisticsLabel() {
        return "MAXACCUMDURATION" + "(" + accumulatingDuration + "Hour)";
    }
}
