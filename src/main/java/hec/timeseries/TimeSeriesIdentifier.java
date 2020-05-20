package hec.timeseries;

import java.time.Duration;

import hec.Identifier;
/**
 * Contains the information required to identify a Time Series.
 * 
 * @author Michael Neilson &lt;michael.a.neilson@usace.army.mil&gt;
 */
public class TimeSeriesIdentifier implements Identifier{
    public String name;
    public String units;
    public final String datatype = "Time Series";
    public Duration interval;
    public Duration duration;

    public TimeSeriesIdentifier(String name, Duration interval, Duration duration, String units) {        
        this.name = name;
        this.duration = duration;
        this.interval = interval;
        this.units = units;
    }

    
    public static Identifier fromCatalogEntry(String name, String meta_info){
        String parts[] = meta_info.split("\\.");
        Duration interval = Duration.parse(parts[0]);
        Duration duration = Duration.parse(parts[1]);
        String units = parts[2];
        return new TimeSeriesIdentifier(name, interval, duration, units);        
    }

    public String name(){
        return this.name;
    }

    @Override
    public String toString()
    {
        return new StringBuilder(datatype)
                    .append("|").append(name)
                    .append("|").append(interval)
                    .append(".").append(duration)
                    .append(".").append(units).toString();
    }

	@Override
	public String catalogName() {
		return new StringBuilder(datatype)
                    .append("|").append(name)
                    .append("|").append(interval)
                    .append(".").append(duration)
                    .append(".").append(units).toString();
	}

    @Override
    public boolean equals(Object other ){
        if( !(other instanceof TimeSeriesIdentifier)) return false;
        return this.catalogName().equals(
                ((TimeSeriesIdentifier)other).catalogName()
            );
    }

    public Duration interval(){
        return interval;
    }

    public Duration duration(){
        return duration;
    }

    public String units(){
        return units;
    }
    
    @Override
    public String datatype(){
        return datatype;
    }
    
    
}
