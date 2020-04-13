package hec;

/**
 * Allow the different objects to be identified within the system.
 * 
 * @author Michael Neilson &lt;michael.a.neilson@usace.army.mil&gt;
 */
public interface Identifier {
    /**
     * 
     * @return String that is unique over all time series
     */
    public abstract String catalogName();
    
    public abstract boolean equals(Object other);   

    /**
     * 
     * @return String identify the type of data to the database system
     */
    public String datatype(); 
}