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

    /**
     * This is here to let implementers of new identifiers that they will
     * need a function that does this
     * @param name catalog name entry
     * @param meta_info catalog meta info block
     * @return Identifier object built from the name,meta_info catalog pair
     */
    public static Identifier fromCatalogEntry( String name, String meta_info){
        throw new RuntimeException("This method should be called only on subclasses");
    };
}