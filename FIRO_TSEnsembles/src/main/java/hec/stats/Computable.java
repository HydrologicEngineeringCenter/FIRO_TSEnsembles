package hec.stats;


import hec.metrics.MetricsConfiguration;
import org.jdom.Element;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;

public interface Computable extends StatisticsReportable{
    public float compute(float[] values);

    //to xml, from xml here.
    //reflect in
    //multi computable and single computable don't implement computable. Similar code will be needed to handle those objects.

    default public org.jdom.Element toXML() throws Exception{
        Field[] fields = this.getClass().getDeclaredFields();
        Element ele = new Element(this.getClass().getName());
        for(Field f: fields){
            try {
                Type type = f.getType();
                String stringType = type.getTypeName();
                String fieldName = f.getName();
                Object objectFieldValue = f.get(this);
                String attribute = null;

                int modifiers = f.getModifiers();
                if(Modifier.isProtected(modifiers)) {
                    System.out.println("protected");
                }
                else if(Modifier.isPrivate(modifiers)) {
                    f.setAccessible(true);
                    System.out.println("private");
                }
                switch(stringType){
                    case "java.lang.Double":
                        double doubleVal = (double) objectFieldValue;
                        attribute = Double.toString(doubleVal);
                        break;
                    case "java.lang.Integer":
                        int intValue = (int) objectFieldValue;
                        attribute = Integer.toString(intValue);
                        break;
                    case "java.lang.float":
                        float floatValue = (float) objectFieldValue;
                        attribute = Float.toString(floatValue);
                        break;
                    case "float[]":
                        float[] floatArray = (float[])objectFieldValue;
                        attribute = Arrays.toString(floatArray);
                        break;
                    case "hec.stats.Configuration":
                        Object objectOfConfig = f.get(this);
                        Configuration config = (Configuration) objectOfConfig;
                        Element configEle = config.toXML();
                        ele.addContent(configEle);
                        break;
                    case "Statistics[]":
                        Object objectOfStatisticsArray = f.get(this);
                        Statistics[] stats = (Statistics[]) objectOfStatisticsArray;
                        ele.setAttribute(f.getName(), Arrays.toString(stats));
                        break;
                    default:
                        throw new Exception("We didn't catch " + f.getName() + " of Type " + stringType);
                }
                if(attribute != null){
                    ele.setAttribute(fieldName,attribute);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                System.out.println("Failed Cast" );
            }
        }
        return ele;
    }

     static Computable fromXML(Element ele){
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
        return computable;
    }
}