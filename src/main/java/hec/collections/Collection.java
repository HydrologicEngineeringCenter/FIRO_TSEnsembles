package hec.collections;

import hec.collections.storage.*;
/**
 * 
 * @author Michael Neilson &lt;michael.a.neilson@usace.army.mil&gt;
 * @since 20200507
 */
public abstract class Collection {
    CollectionStorage storageStrategy;

    public Collection(CollectionStorage strategy ){
        storageStrategy = strategy;
    }    

    /**     
     * @return CollectionIdentifier object that can uniquely identify this Collection in the catalog
     */
    abstract public CollectionIdentifier identifier();    

    /**
     * @return Unique Identifier for the type of collection
     */
    abstract public String subtype();

    /** 
     * @return how many members are in this collection
     */
    abstract public int numberMembers();

    /**
     * @return The strategy used for storage
     * 
     */
    public CollectionStorage storageStrategy(){
        return storageStrategy;
    }
}