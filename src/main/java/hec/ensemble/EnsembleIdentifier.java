package hec.ensemble;
import hec.Identifier;

public class EnsembleIdentifier implements Identifier {
    public String location;
    public String parameter;
    public final String datatype = "ensemble";

    public EnsembleIdentifier(String location, String parameter) {
        this.location = location;
        this.parameter = parameter;
    }

    @Override
    public String toString()
    {
        return location+"/"+parameter;
    }

    @Override
    public String catalogName() {
        // TODO Auto-generated method stub
        return new StringBuilder(datatype)
                    .append("|").append(location)
                    .append("/").append(parameter).toString();
    }

    public String datatype(){
        return this.datatype();
    }
    
}
