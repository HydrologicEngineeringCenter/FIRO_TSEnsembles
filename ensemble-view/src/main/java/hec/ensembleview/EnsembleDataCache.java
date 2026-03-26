package hec.ensembleview;

import hec.EnsembleDatabase;
import hec.RecordIdentifier;
import hec.ensemble.Ensemble;
import hec.ensemble.EnsembleTimeSeries;

import java.time.ZonedDateTime;

/**
 * Caches EnsembleTimeSeries data fetched from the database.
 * The cache is keyed by RecordIdentifier — a new fetch only happens
 * when the location changes or the cache is explicitly invalidated.
 */
class EnsembleDataCache {
    private EnsembleTimeSeries cachedEnsembleTimeSeries;
    private RecordIdentifier cachedRid;
    private EnsembleTimeSeries cumulativeEnsembleTimeSeries;

    /**
     * Returns the EnsembleTimeSeries for the given RecordIdentifier,
     * fetching from the database only on a cache miss.
     */
    EnsembleTimeSeries getEnsembleTimeSeries(RecordIdentifier rid, EnsembleDatabase db) {
        if (cachedEnsembleTimeSeries != null && rid != null && rid.equals(cachedRid)) {
            return cachedEnsembleTimeSeries;
        }
        cachedEnsembleTimeSeries = db.getEnsembleTimeSeries(rid);
        cachedRid = rid;
        return cachedEnsembleTimeSeries;
    }

    Ensemble getEnsemble(RecordIdentifier rid, ZonedDateTime zdt, EnsembleDatabase db) {
        return getEnsembleTimeSeries(rid, db).getEnsemble(zdt);
    }

    EnsembleTimeSeries getCumulativeEnsembleTimeSeries() {
        return cumulativeEnsembleTimeSeries;
    }

    void setCumulativeEnsembleTimeSeries(EnsembleTimeSeries ets) {
        this.cumulativeEnsembleTimeSeries = ets;
    }

    /**
     * Clears all cached data, forcing a re-fetch on next access.
     */
    void invalidate() {
        cachedEnsembleTimeSeries = null;
        cachedRid = null;
        cumulativeEnsembleTimeSeries = null;
    }
}