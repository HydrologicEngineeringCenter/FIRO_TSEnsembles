package hec.stats;

import org.jdom.Element;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;

public final class Serializer {
    static <T> Element toXML(T computableThing) throws Exception {
        //Get this class's fields and create an element with this class's name.
        Field[] fields = computableThing.getClass().getDeclaredFields();
        Element ele = new Element(computableThing.getClass().getName());
        for (Field f : fields) {
            try {
                //Get information on the field
                Type type = f.getType();
                String stringType = type.getTypeName();
                String fieldName = f.getName();
                //Make sure we can access it even if it's private
                int modifiers = f.getModifiers();
                if (Modifier.isProtected(modifiers)) {
                    System.out.println("protected");
                } else if (Modifier.isPrivate(modifiers)) {
                    f.setAccessible(true);
                    System.out.println("private");
                }
                //Get the object, and skip serializing if the value is null
                String attribute = null;
                Object objectFieldValue = f.get(computableThing);
                if (objectFieldValue == null) {
                    continue;
                }
                //Check what type the field holds,and serialize it appropriately.
                switch (stringType) {
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
                        float[] floatArray = (float[]) objectFieldValue;
                        attribute = Arrays.toString(floatArray);
                        break;
                    case "Statistics[]":
                        Statistics[] stats = (Statistics[]) objectFieldValue;
                        String statsString = Arrays.toString(stats);
                        ele.setAttribute(f.getName(), statsString);
                        break;
                    case "hec.stats.Computable":
                        Computable computable = (Computable) objectFieldValue;
                        Element computableEle = computable.toXML();
                        ele.addContent(computableEle);
                    default:
                        throw new Exception("We didn't catch " + f.getName() + " of Type " + stringType);
                }
                if (attribute != null) {
                    ele.setAttribute(fieldName, attribute);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                throw ex;
            }
        }
        return ele;
    }
    public static <T> T fromXML(Element ele) throws ClassNotFoundException, InvocationTargetException, InstantiationException, NoSuchMethodException, IllegalAccessException {
        T computable;
        Class<?> c;
        String computableName = ele.getName();
        try {
            c = Class.forName(computableName);
            computable = (T) c.getConstructor().newInstance();
            Field[] fields = c.getDeclaredFields();
            for (Field field : fields) {
                Type type = field.getType();
                String stringType = type.getTypeName();
                String fieldName = field.getName();
                int modifiers = field.getModifiers();
                if (Modifier.isProtected(modifiers)) {
                    System.out.println("protected");
                } else if (Modifier.isPrivate(modifiers)) {
                    field.setAccessible(true);
                    System.out.println("private");
                }
                switch (stringType) {
                    case "java.lang.Double":
                        field.set(computable, Double.parseDouble(ele.getAttribute(fieldName).getValue()));
                        break;
                    case "java.lang.Integer":
                        field.set(computable, Integer.parseInt(ele.getAttribute(fieldName).getValue()));
                        break;
                    case "java.lang.float":
                        field.set(computable, Float.parseFloat(ele.getAttributeValue(fieldName)));
                    case "float[]":
                        String floatArray = ele.getAttributeValue(fieldName);
                        String[] splitFloatArray = floatArray.split(",");
                        int numberOfValues = splitFloatArray.length;
                        float[] floats = new float[numberOfValues];
                        for (int i = 0; i < numberOfValues; i++) {
                            float value = Float.parseFloat(splitFloatArray[i]);
                            floats[i] = value;
                        }
                        field.set(computable, floats);
                        break;
                    case "hec.stats.Configuration":
                        //unsupported
                        break;
                    case "Statistics[]":
                        String statisticsString = ele.getAttributeValue(fieldName);
                        String[] statisticsStringSplit = statisticsString.split(",");
                        int numValues = statisticsStringSplit.length;
                        Statistics[] statsArray = new Statistics[numValues];
                        for (int i = 0; i < numValues; i++) {
                            Statistics stat = Statistics.valueOf(statisticsStringSplit[i]);
                            statsArray[i] = stat;
                        }
                        field.set(computable, statsArray);
                        break;
                    case "hec.stats.Computable":
                        Element computableElement = ele.getChild(fieldName);
                        Computable computer = Computable.fromXML(computableElement);
                        field.set(computable, computer);
                        break;
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
