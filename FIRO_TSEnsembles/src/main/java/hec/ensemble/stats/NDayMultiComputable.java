package hec.ensemble.stats;

public class NDayMultiComputable implements Computable, MultiComputable, StatisticsReportable, Configurable {
    private MultiComputable stepOneCompute;
    private int accumulatingDays;
    private float[] days;
    Configuration config;

    /**
     * The n day multi computable computes a multiComputable stat (cumulative) and gets the cumulative value for the specified day.
     * Intended to be used for iterating for traces across time and does not account for daylight savings
     */
    public NDayMultiComputable(){ } // necessary for reflection deserializing serializing
    public NDayMultiComputable(MultiComputable stepOne, int numberDays) {
        stepOneCompute = stepOne;
        accumulatingDays = numberDays;
    }
    public NDayMultiComputable(MultiComputable stepOne, float[] numberDays) {
        stepOneCompute = stepOne;
        days = numberDays;
    }

    @Override
    public float compute(float[] values) {
        values = stepOneCompute.multiCompute(values);

        int timeStep = (int) config.getDuration().toHours();
        int timeStepDay = 24 / timeStep;
        return values[timeStepDay * accumulatingDays];
    }

    @Override
    public float[] multiCompute(float[] values) {
        int size = days.length;
        float[] results = new float[size];
        int i = 0;
        values = stepOneCompute.multiCompute(values);

        for(float day : days) {
            int timeStep = (int) config.getDuration().toHours();
            int timeStepDay = 24 / timeStep;
            results[i] = values[timeStepDay * (int) day];
            i++;
        }
        return results;
    }

    @Override
    public String getOutputUnits() {
        return stepOneCompute.getOutputUnits();
    }


    @Override
    public void configure(Configuration c) {
        config = c; //figure out how many values make up a day and multiply by number of days
        try {
            Configurable configurable = (Configurable) stepOneCompute;
            configurable.configure(this.config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String StatisticsLabel() {
        StringBuilder label = new StringBuilder();
        for (int i = 0; i < days.length; i ++){
            if(i == days.length-1){
                label.append(stepOneCompute.StatisticsLabel() + "(").append(days[i]).append(")");
            }
            else{
                label.append(stepOneCompute.StatisticsLabel() + "(").append(days[i]).append(")|");
            }
        }
        return label.toString();
    }


}

