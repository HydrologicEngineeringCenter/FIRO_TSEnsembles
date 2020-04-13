package hec.paireddata;

import hec.Identifier;

public class PairedDataIdentifier implements Identifier {
    final String the_datatype = "Paired Data";
    String name;
    String from;
    String to;


    PairedDataIdentifier(String name, String from_unit,String to_unit){
        this.name = name;
        this.from = from_unit;
        this.to = to_unit;        
    }

    @Override
    public String catalogName() {
        // TODO Auto-generated method stub
        StringBuilder sb = new StringBuilder(the_datatype)
                        .append("|")
                     .append(name)
                     .append("|")
                     .append(from)
                     .append("/")
                     .append(to);
        return sb.toString();
    }

    @Override
    public String datatype() {
        // TODO Auto-generated method stub
        return this.the_datatype;
    }

}
