package hec.stats;

import org.jdom.Element;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public final class Serializer {
    public static <T> Element toXML(T computableThing) {
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
                }
                if (attribute != null) {
                    ele.setAttribute(fieldName, attribute);
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                System.out.println("toXML failed");
            }
        }
        return ele;
    }
    public static <T> T fromXML(Element ele)  {
        T computable = null;
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
                    System.out.println("protected, so we did nothing: " +fieldName);
                } else if (Modifier.isPrivate(modifiers)) {
                    field.setAccessible(true);
                    System.out.println("private field set accessible: " + fieldName );
                }

                String attributeValue = ele.getAttributeValue(fieldName);

                switch (stringType) {
                    case "java.lang.Double":
                        if(attributeValue == null){
                            continue;
                        }
                        field.set(computable, Double.parseDouble(attributeValue));
                        break;
                    case "java.lang.Integer":
                        if(attributeValue == null){
                            continue;
                        }
                        field.set(computable, Integer.parseInt(attributeValue));
                        break;
                    case "java.lang.float":
                        if(attributeValue == null){
                            continue;
                        }
                        field.set(computable, Float.parseFloat(attributeValue));
                    case "float[]":
                        if(attributeValue == null){
                            continue;
                        }
                        String[] floatStringSplit = prepareXMLArray(attributeValue);
                        float[] floats = new float[floatStringSplit.length];
                        for (int i = 0; i < floatStringSplit.length; i++) {
                            float value = Float.parseFloat(floatStringSplit[i]);
                            floats[i] = value;
                        }
                        field.set(computable, floats);
                        break;
                    case "hec.stats.Statistics[]":
                        if(attributeValue == null){
                            continue;
                        }
                        String[] statisticsStringSplit = prepareXMLArray(attributeValue);
                        Statistics[] statsArray = new Statistics[statisticsStringSplit.length];
                        for (int i = 0; i < statisticsStringSplit.length; i++) {
                            Statistics stat = Statistics.valueOf(statisticsStringSplit[i]);
                            statsArray[i] = stat;
                        }
                        field.set(computable, statsArray);
                        break;
                    case "hec.stats.Computable":
                        List<Object> childs =  ele.getChildren();
                        for( Object child: childs){
                            Element childElement = (Element)child;
                            String elementFieldName = childElement.getAttributeValue("fieldName");
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
            System.out.println("fromXML Failed");
        }
        return computable;

    }

    private static String[] prepareXMLArray(String attributeValue){
        String stringNoBrackets = attributeValue.substring(1, attributeValue.length()-1);
        String[] StringSplit = stringNoBrackets.split(",");
        int numValues = StringSplit.length;
        for(int i = 0; i< numValues; i++){
            StringSplit[i] = StringSplit[i].trim();
        }
        return StringSplit;
    }

}