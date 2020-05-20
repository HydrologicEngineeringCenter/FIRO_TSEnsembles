package hec.collections;

import hec.Identifier;

/**
 * Identifies a simple name for the collection as 
 * well as a a way to identify what time series
 * Are part of this collection
 * @since 20200507
 * @author Michael Neilson &lt;michael.a.neilson@usace.army.mil&gt;
 */
public class CollectionIdentifier implements Identifier {

    public static String DSS_FORMAT = ".*C\\:[0-9a-zA-Z]+$"; // basic format used by HEC-DSS to identify a collection.

    private String name;
    private String expression;
    private final String datatype = "Collection";

    public CollectionIdentifier( String name, String expression ){
        this.name = name;
        this.expression = expression;
    }

    @Override
    public String catalogName() {
        StringBuilder sb = new StringBuilder();
        sb.append(datatype).append("|")
          .append(name).append("|")
          .append(expression);
        return sb.toString();
    }

    public String expression(){
        return expression;
    }

    @Override
    public String datatype() {
        
        return datatype;
    }


}
