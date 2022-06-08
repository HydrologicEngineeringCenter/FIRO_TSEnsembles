package hec.stats;


import hec.metrics.MetricsConfiguration;
import org.jdom.Element;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;

public interface Computable extends StatisticsReportable{
    float compute(float[] values);

    default org.jdom.Element toXML() throws Exception{
        Field[] fields = this.getClass().getDeclaredFields();
        Element ele = new Element(this.getClass().getName());
        for(Field f: fields){
            try {
                Type type = f.getType();
                String stringType = type.getTypeName();
                String fieldName = f.getName();
                int modifiers = f.getModifiers();
                if(Modifier.isProtected(modifiers)) {
                    System.out.println("protected");
                }
                else if(Modifier.isPrivate(modifiers)) {
                    f.setAccessible(true);
                    System.out.println("private");
                }
                String attribute = null;
                Object objectFieldValue = f.get(this);
                if(objectFieldValue == null){
                    continue;
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
                        Configuration config = (Configuration) objectFieldValue;
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
                throw ex;
            }
        }
        return ele;
    }

     static Computable fromXML(Element ele) throws ClassNotFoundException, InvocationTargetException, InstantiationException, NoSuchMethodException, IllegalAccessException {
              Computable computable;
                    Class<?> c;
                    String computableName = ele.getName();
                    try {
                        c = Class.forName(computableName);
                        computable=(Computable) c.getConstructor().newInstance();
                        Field[] fields = c.getDeclaredFields();
                        for (Field field : fields){
                            Type type = field.getType();
                            String stringType = type.getTypeName();
                            String fieldName = field.getName();
                            int modifiers = field.getModifiers();
                            if(Modifier.isProtected(modifiers)) {
                                System.out.println("protected");
                            }
                            else if(Modifier.isPrivate(modifiers)) {
                                field.setAccessible(true);
                                System.out.println("private");
                            }
                            switch(stringType){
                                case "java.lang.Double":
                        field.set(computable,Double.parseDouble(ele.getAttribute(fieldName).getValue()));
                        break;
                    case "java.lang.Integer":
                        field.set(computable,Integer.parseInt(ele.getAttribute(fieldName).getValue()));
                        break;
                    case "java.lang.float":
                        field.set(computable,Float.parseFloat(ele.getAttributeValue(fieldName)));
                    case "float[]":
                        String floatArray = ele.getAttributeValue(fieldName);
                        String[] splitFloatArray = floatArray.split(",");
                        int numberOfValues = splitFloatArray.length;
                        float[] floats = new float[numberOfValues];
                        for(int i=0; i<numberOfValues;i++) {
                            float value = Float.parseFloat(splitFloatArray[i]);
                            floats[i] = value;
                        }
                        field.set(computable,floats);
                        break;
                    case "hec.stats.Configuration":
                        //unsupported
                        break;
                    case "Statistics[]":
                        String statisticsString = ele.getAttributeValue(fieldName);
                        String[] statisticsStringSplit = statisticsString.split(",");
                        int numValues = statisticsStringSplit.length;
                        Statistics[] statsArray = new Statistics[numValues];
                        for(int i=0; i<numValues;i++){
                            Statistics stat = Statistics.valueOf(statisticsStringSplit[i]);
                            statsArray[i]= stat;
                        }
                        field.set(computable,statsArray);
                    default:
                        break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            throw e;
        }
        return computable;
    }
}