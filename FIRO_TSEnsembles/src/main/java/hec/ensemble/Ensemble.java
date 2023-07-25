package hec.ensemble;

import hec.ensemble.stats.Computable;
import hec.ensemble.stats.Configurable;
import hec.ensemble.stats.MultiComputable;
import hec.ensemble.stats.SingleComputable;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * an Ensemble is an array of time-series data
 *
 * each time-series in the Ensemble has the same timestamps
 *
 */
public class Ensemble
  {
    private final EnsembleConfiguration _configuration;
    protected EnsembleTimeSeries parent = null;
    private float[][] values;


    public Ensemble(ZonedDateTime issueDate, float[][] values, ZonedDateTime startDate, Duration interval, String units)
    {
      this.values = values;
      this._configuration = new EnsembleConfiguration(issueDate,startDate,interval,units);
    }

    public int getTimeCount(){
      return values[0].length;
  }

    /**
     * Computes an array of ZonedDateTime based on the
     * interval and startDateTime of the ensemble.
     * @return returns array of ZonedDateTime
     */
    public ZonedDateTime[] startDateTime() {

      ZonedDateTime[] rval = new ZonedDateTime[getTimeCount()];
      ZonedDateTime t = _configuration.getStartDate();
      int size= getTimeCount();
      for (int i = 0; i <size ; i++) {
        rval[i] =t;
        t = t.plus(_configuration.getDuration());
      }
      return rval;
    }



    public ZonedDateTime getIssueDate() {
      return _configuration.getIssueDate();
    }

    public Duration getInterval()
    {
      return _configuration.getDuration();
    }

    public ZonedDateTime getStartDateTime() {
      return _configuration.getStartDate();
    }

    public String getUnits() { return _configuration.getUnits();}

    /**
     * ensemble data
     * row represents ensemble members
     * columns are time steps
     * @return two-dimensional size float[][] (all rows are the same size)
     */
    public float[][] getValues() {
      return values;
    }

    /**
     * iterate over the traces, for all of their timesteps. The result summarizes the ensembles traces into a summary value for each trace
     * @param cmd a computable statistic
     * @return A summary of float for each trace.
     */
    public float[] iterateForTracesAcrossTime(Computable cmd){
      if (cmd instanceof Configurable){
        ((Configurable)cmd).configure(_configuration);
      }
      int size= values.length;
      float[] rval = new float[size];
      for (int i = 0; i <size ; i++) {
          rval[i] = cmd.compute(values[i]);
      }

      return rval;
    }

    /**
     * iterate over the timesteps for all traces. The result sumarizes the ensemble into a time series of a statistic that represents all traces.
     * @param cmd a computable statistic
     * @return a timeseries summary of float (representing all traces with a statistic for each timestep)
     */
    public float[] iterateForTimeAcrossTraces(Computable cmd){
      if (cmd instanceof Configurable){
        ((Configurable)cmd).configure(_configuration);
      }
      int size= values[0].length;//number of timesteps
      float[] rval = new float[size];
      int traces = values.length;//number of traces
      float[] tracevals = new float[traces];
      for (int i = 0; i <size ; i++) {//this could be more efficient as a streaming compute process.. one less loop.
        for(int j = 0; j <traces; j++){
          tracevals[j] = values[j][i];//load all trace values for this timestep into an array
        }
        rval[i] = cmd.compute(tracevals);//compute statistic for this timestep and store.
      }
      return rval;//a time series of a statistic.
    }
    /**
     * iterate over the traces, for all of their timesteps. The result summarizes the ensembles traces into an array of summary values for each trace
     * @param cmd a multicomputable statistic
     * @return A summary of float[] for each trace representing all of the statistics computed for that trace.
     */
    public float[][] multiComputeForTracesAcrossTime(MultiComputable cmd){
      if (cmd instanceof Configurable){
        ((Configurable)cmd).configure(_configuration);
      }
      int size= values.length;
      int size2 = cmd.getStatCount();
      float[] rval;
      float[][] val = new float[size2][size];
      for (int i = 0; i <size ; i++) {
        rval = cmd.multiCompute(values[i]);
        for (int j = 0; j<size2;j++){
          val[j][i] = rval[j];
        }

      }
      return val;
    }
    /**
     * iterate over the timesteps for all traces. The result sumarizes the ensemble into a time series of a collection of statistics that represents all traces.
     * @param cmd a multicomputable statistic
     * @return a timeseries  represented as [timesteps][statistics]float summary of []float (representing all traces with a collection of statistics for each timestep)
     */
    public float[][] multiComputeForTimeAcrossTraces(MultiComputable cmd){
      if (cmd instanceof Configurable){
        ((Configurable)cmd).configure(_configuration);
      }
      int size= values[0].length;//number of timesteps
      int size2 = cmd.getStatCount();
      float[][] val = new float[size2][size];
      int traces = values.length;//number of traces
      float[] rval;
      float[] tracevals = new float[traces];
      for (int i = 0; i <size ; i++) {
        for(int j = 0; j <traces; j++){
          tracevals[j] = values[j][i];//load all trace values for this timestep into an array
        }
        rval = cmd.multiCompute(tracevals);//compute a collection of statistics for this timestep and store.
        for (int k = 0; k<size2;k++){
          val[k][i] = rval[k];
        }
      }
      return val;//a time series of a collection of statistics.
    }
    public float singleComputeForEnsemble(SingleComputable cmd){
      if (cmd instanceof Configurable){
        ((Configurable)cmd).configure(_configuration);
      }
      return cmd.compute(values);
    }

    /**
     * iterate over each trace and its timestep.  The result is an array of time series as initial ensemble forecasts transformed based on the statistic, such as the cumulative discharge
     * @param cmd a multicomputable statistic
     * @return a time series represented as []float
     */
    public float[][] multiComputeForEachTraces(MultiComputable cmd){
      if (cmd instanceof Configurable){
        ((Configurable)cmd).configure(_configuration);
      }
      int traces= values.length;  //number of traces
      int time = values[0].length;  //number of time steps
      float[] rval;
      float[][] val = new float[traces][time];
      for (int i = 0; i <traces ; i++) {
        rval = cmd.multiCompute(values[i]);
        for (int j = 0; j<time;j++){
          val[i][j] = rval[j];
        }

      }
      return val;
    }
  }

