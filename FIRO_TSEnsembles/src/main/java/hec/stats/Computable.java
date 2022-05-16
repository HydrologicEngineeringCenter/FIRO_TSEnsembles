package hec.stats;


import org.jdom.Element;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public interface Computable extends StatisticsReportable{
    public float compute(float[] values);

    //to xml, from xml here.
    //reflect in
    //multi computable and single computable dont implement computable. Similar code will be needed to handle those objects.

    default public org.jdom.Element toXML(Element parent){
        Field[] flds = this.getClass().getDeclaredFields();
        Element ele = new Element(this.getClass().getName());
        for(Field f: flds){
            try {
                switch(f.getType().getName()){
                    case "double":
                        ele.setAttribute(f.getName(),Double.toString(f.getDouble(this)));
                        break;
                    case "int":
                        ele.setAttribute(f.getName(),Integer.toString(f.getInt(this)));
                        break;
                    case "float":
                        ele.setAttribute(f.getName(),Float.toString(f.getFloat(this)));
                        break;
                    case "float[]":
                        //UNSUPPORTED
                        break;
                    case "Configuration":
                        //UNSUPPORTED
                        break;
                    case "Statistics[]":
                        //UNSUPPORTED
                    default:
                        break;
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                System.out.println("Failed Cast" );
            }
        }
        return ele;
    }

    public static Computable fromXML(Element ele){
        Computable computable = null;
        Class<?> c;

        String computableName = ele.getName();
        try {
            c = Class.forName(computableName);
            computable=(Computable) c.getConstructor().newInstance();
            Field[] flds = c.getDeclaredFields();
            for (Field f : flds){
                switch(f.getType().getName()){
                    case "double":
                        f.set(computable,Double.parseDouble(ele.getAttribute(f.getName()).getValue()));
                        break;
                    case "int":
                        f.set(computable,Integer.parseInt(ele.getAttribute(f.getName()).getValue()));
                        break;
                    case "float":
                        f.set(computable,Float.parseFloat(ele.getAttributeValue(f.getName())));
                    case "float[]":
                        //UNSUPPORTED
                        break;
                    case "Configuration":
                        //UNSUPPORTED
                        break;
                    case "Statistics[]":
                        //UNSUPPORTED
                    default:
                        break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }

        switch (computableName){
            case "MaxAccumDuration":
                //Unsupported
                break;
            case "MaxAvgDuration":
                //Unsupported
                break;
            case "MaxComputable":
                //Unsupported
                break;
            case "MeanComputable":
                //Unsupported
                break;
            case "MedianComputable":
                //Unsuported
                break;
            case "MinComputable":
                //Unsupported
                break;
            case "PercentilesComputable":
                //Unsupported
                break;
            default:
                break;
        }
        return computable;
    }
}