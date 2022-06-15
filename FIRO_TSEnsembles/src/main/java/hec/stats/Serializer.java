package hec.stats;

import org.jdom.Element;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

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
                    case "hec.stats.Statistics[]":
                        Statistics[] stats = (Statistics[]) objectFieldValue;
                        attribute = Arrays.toString(stats);
                        break;
                    case "hec.stats.Computable":
                        Computable computable = (Computable) objectFieldValue;
                        Element computableEle = Serializer.toXML(computable); //recursive call
                        computableEle.setAttribute("fieldName", fieldName);
                        ele.addContent(computableEle);
                        break;
                    case "boolean":
                        boolean bool = (boolean)objectFieldValue;
                        attribute = Boolean.toString(bool);
                        break;
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

                String attributeValue = ele.getAttributeValue(fieldName);
                if(attributeValue == null){
                    continue;
                }
                switch (stringType) {
                    case "java.lang.Double":
                        field.set(computable, Double.parseDouble(attributeValue));
                        break;
                    case "java.lang.Integer":
                        field.set(computable, Integer.parseInt(attributeValue));
                        break;
                    case "java.lang.float":
                        field.set(computable, Float.parseFloat(attributeValue));
                    case "float[]":
                        String floatArrayNoBrackets = attributeValue.substring(1, attributeValue.length()-1);
                        String[] splitFloatArray = floatArrayNoBrackets.split(",");
                        int numberOfValues = splitFloatArray.length;
                        float[] floats = new float[numberOfValues];
                        for (int i = 0; i < numberOfValues; i++) {
                            float value = Float.parseFloat(splitFloatArray[i]);
                            floats[i] = value;
                        }
                        field.set(computable, floats);
                        break;
                    case "Statistics[]":
                        String statisticsStringNoBrackets = attributeValue.substring(1, attributeValue.length()-1);
                        String[] statisticsStringSplit = statisticsStringNoBrackets.split(",");
                        int numValues = statisticsStringSplit.length;
                        Statistics[] statsArray = new Statistics[numValues];
                        for (int i = 0; i < numValues; i++) {
                            Statistics stat = Statistics.valueOf(statisticsStringSplit[i]);
                            statsArray[i] = stat;
                        }
                        field.set(computable, statsArray);
                        break;
                    case "hec.stats.Computable":
                        List<Object> childs =  ele.getChildren();
                        for( Object child: childs){
                            Element childElement = (Element)child;
                            String elementFieldName = childElement.getAttribute("fieldName").toString();
                            if(elementFieldName.equals(fieldName)){
                                Computable computer = Serializer.fromXML(childElement);
                                field.set(computable, computer);
                            }
                        }
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
