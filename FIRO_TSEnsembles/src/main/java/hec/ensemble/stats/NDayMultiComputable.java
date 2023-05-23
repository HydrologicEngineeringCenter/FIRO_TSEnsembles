package hec.ensemble.stats;

public class NDayMultiComputable implements Computable, StatisticsReportable, Configurable {
    private MultiComputable stepOneCompute;
    private int accumulatingDays;
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

    @Override
    public float compute(float[] values) {
        values = stepOneCompute.multiCompute(values);

        int timestep = (int) config.getDuration().toHours();
        int timestepDay = 24 / timestep;
        return values[timestepDay * accumulatingDays];
    }

    @Override
    public String getOutputUnits() {
        return stepOneCompute.getOutputUnits();
    }


    @Override
    public void configure(Configuration c) {
        config = c; //figure out how many values make up a day and multiple by number of days
        try {
            Configurable configurable = (Configurable) stepOneCompute;
            configurable.configure(this.config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Statistics[] Statistics()
    {
        Statistics[] statsarray = new Statistics[stepOneCompute.Statistics().length+1];
        statsarray[0] = Statistics.CUMULATIVE;
        int count = 1;
        for(Statistics s: stepOneCompute.Statistics()){
            statsarray[count] = s;
            count++;
        }
        return new Statistics[0];
    }

    @Override
    public String StatisticsLabel() {
        return stepOneCompute.StatisticsLabel()+"("+ accumulatingDays +"DAY)";
    }


}

