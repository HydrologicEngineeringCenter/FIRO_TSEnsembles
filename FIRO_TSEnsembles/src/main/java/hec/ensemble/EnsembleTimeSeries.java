package hec.ensemble;

import hec.RecordIdentifier;
import hec.metrics.MetricCollection;
import hec.metrics.MetricCollectionTimeSeries;
import hec.metrics.MetricTypes;
import hec.stats.Computable;
import hec.stats.MultiComputable;
import hec.stats.SingleComputable;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 *  EnsembleTimeSeries is a collection of Ensembles over time
 *
 */
public class EnsembleTimeSeries implements  Iterable<Ensemble> 
  {

    private RecordIdentifier timeseriesID;
    private String units;
    private String dataType;
    private String version;
    //https://docs.oracle.com/javase/7/docs/api/java/util/TreeMap.html
    private TreeMap<ZonedDateTime,Ensemble> items;

    public int getCount()
    {
      return items.size();
    }

    public EnsembleTimeSeries(RecordIdentifier timeseriesID, String units, String dataType, String version)
    {
      init(timeseriesID,units,dataType,version);
    }

    private void init(RecordIdentifier timeseriesID, String units, String dataType, String version) {
      this.timeseriesID = timeseriesID;
      this.units = units;
      this.dataType = dataType;
      this.version = version;
      items = new TreeMap<>();
    }

    /**
     * addEnsemble adds a new Ensemble to this in-memory collection
     * @param issueDate issueDate of Ensemble
     * @param ensemble  data values
     * @param startDate startingDate for this ensemble.
     * @param interval time-step (Duration) between values
     * @param units  units of this ensemble
     */
    public void addEnsemble(ZonedDateTime issueDate, float[][] ensemble, ZonedDateTime startDate, Duration interval, String units)
    {
      Ensemble e = new Ensemble(issueDate,ensemble,startDate, interval, units);
      addEnsemble(e);
    }
    /**
     * addEnsemble adds a new Ensemble to this in-memory collection
     * @param ensemble ensemble to add
     */
    public void addEnsemble(Ensemble ensemble) {
      ensemble.parent = this;
      items.put(ensemble.getIssueDate(),ensemble);
    }

    /**
     * getIssueDates computes a list of ZonedDateTimes in this collection
     * @return returns an array of ZonedDateTime
     */
    public List<ZonedDateTime> getIssueDates() {
      // convert from map to a List<>
      List<ZonedDateTime> rval = new ArrayList<>(items.size());
      rval.addAll(items.keySet());
      return rval;
    }

    /**
     * Returns an specified ensemble from this im-memory collection
     * @param issueDate date used to lookup ensemble to reutrn.
     * @return an Ensemble
     */
    public Ensemble getEnsemble(ZonedDateTime issueDate) {
      return items.get(issueDate);
    }

    public String getUnits() {
      return units;
    }

    public String getDataType() {
      return dataType;
    }

    public String getVersion() {
      return version;
    }

    public RecordIdentifier getTimeSeriesIdentifier() {
      return timeseriesID;
    }

    @Override
    public Iterator<Ensemble> iterator() {
      return new EnsembleTimeSeriesIterator(this);
    }
    public MetricCollectionTimeSeries computeSingleValueSummary(SingleComputable compute){
      MetricCollectionTimeSeries mcts = new MetricCollectionTimeSeries(this.timeseriesID, this.units, MetricTypes.SINGLE_VALUE);
      for (Iterator<Ensemble> it = iterator(); it.hasNext(); ) {
        Ensemble e = it.next();
        float result = e.singleComputeForEnsemble(compute);
        EnsembleConfiguration ec = new EnsembleConfiguration(e.getIssueDate(),e.getStartDateTime(),e.getInterval(),e.getUnits());
        MetricCollection mc = new MetricCollection(ec, compute.Statistics(), new float[][] {{result}});
        mcts.addMetricCollection(mc);
      }
      return mcts;
    }
    public MetricCollectionTimeSeries iterateAcrossTimestepsOfEnsemblesWithSingleComputable(Computable compute){
      MetricCollectionTimeSeries mcts = new MetricCollectionTimeSeries(this.timeseriesID, this.units, MetricTypes.TIMESERIES_OF_ARRAY);
      for (Iterator<Ensemble> it = iterator(); it.hasNext(); ) {
        Ensemble e = it.next();
        float[] farray = e.iterateForTimeAcrossTraces(compute);
        EnsembleConfiguration ec = new EnsembleConfiguration(e.getIssueDate(),e.getStartDateTime(),e.getInterval(),e.getUnits());
        MetricCollection mc = new MetricCollection(ec, compute.Statistics(), new float[][] {farray} );
        mcts.addMetricCollection(mc);
      }
      return mcts;
    }
    public MetricCollectionTimeSeries iterateAcrossEnsembleTracesWithSingleComputable(Computable compute){
      MetricCollectionTimeSeries mcts = new MetricCollectionTimeSeries(this.timeseriesID, this.units, MetricTypes.ARRAY_OF_ARRAY);
      for (Iterator<Ensemble> it = iterator(); it.hasNext(); ) {
        Ensemble e = it.next();
        float[] farray = e.iterateForTracesAcrossTime(compute);
        EnsembleConfiguration ec = new EnsembleConfiguration(e.getIssueDate(),e.getStartDateTime(),e.getInterval(),e.getUnits());
        MetricCollection mc = new MetricCollection(ec, compute.Statistics(), new float[][] {farray} );
        mcts.addMetricCollection(mc);
      }
      return mcts;
    }
    public MetricCollectionTimeSeries iterateAcrossTimestepsOfEnsemblesWithMultiComputable(MultiComputable compute){
      MetricCollectionTimeSeries mcts = new MetricCollectionTimeSeries(this.timeseriesID, this.units, MetricTypes.TIMESERIES_OF_ARRAY);
      for (Iterator<Ensemble> it = iterator(); it.hasNext(); ) {
        Ensemble e = it.next();
        float[][] farray = e.multiComputeForTimeAcrossTraces(compute);
        EnsembleConfiguration ec = new EnsembleConfiguration(e.getIssueDate(),e.getStartDateTime(),e.getInterval(),e.getUnits());
        MetricCollection mc = new MetricCollection(ec, compute.Statistics(), farray );
        mcts.addMetricCollection(mc);
      }
      return mcts;
    }
    public MetricCollectionTimeSeries iterateAcrossTracesOfEnsemblesWithMultiComputable(MultiComputable compute){
      MetricCollectionTimeSeries mcts = new MetricCollectionTimeSeries(this.timeseriesID, this.units, MetricTypes.ARRAY_OF_ARRAY);
      for (Iterator<Ensemble> it = iterator(); it.hasNext(); ) {
        Ensemble e = it.next();
        float[][] farray = e.multiComputeForTracesAcrossTime(compute);
        EnsembleConfiguration ec = new EnsembleConfiguration(e.getIssueDate(),e.getStartDateTime(),e.getInterval(),e.getUnits());
        MetricCollection mc = new MetricCollection(ec, compute.Statistics(), farray );
        mcts.addMetricCollection(mc);
      }
      return mcts;
    }
    public MetricCollectionTimeSeries iterateTracesOfEnsemblesWithMultiComputable(MultiComputable compute){
      MetricCollectionTimeSeries mcts = new MetricCollectionTimeSeries(this.timeseriesID, this.units, MetricTypes.TIMESERIES_OF_ARRAY);
      for (Iterator<Ensemble> it = iterator(); it.hasNext(); ) {
        Ensemble e = it.next();
        float[][] farray = e.multiComputeForEachTraces(compute);
        EnsembleConfiguration ec = new EnsembleConfiguration(e.getIssueDate(),e.getStartDateTime(),e.getInterval(),e.getUnits());
        MetricCollection mc = new MetricCollection(ec, compute.Statistics(), farray );
        mcts.addMetricCollection(mc);
      }
      return mcts;
    }

  }
