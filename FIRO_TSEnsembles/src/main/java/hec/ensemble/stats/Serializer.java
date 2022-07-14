package hec.ensemble.stats;

import org.jdom.Element;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public final class Serializer {
    private static final String DOUBLE =  "java.lang.Double";
    private static final String INTEGER =  "int";
    private static final String FLOAT =  "java.lang.float";
    private static final String FLOATARRAY =  "float[]";
    private static final String STATISTICSARRAY =  "hec.ensemble.stats.Statistics[]";
    private static final String COMPUTABLE =  "hec.ensemble.stats.Computable";
    private static final String MULTICOMPUTABLE =  "hec.ensemble.stats.MultiComputable";
    private static final String SINGLECOMPUTABLE =  "hec.ensemble.stats.SingleComputable";
    private static final String BOOLEAN =  "boolean";
    private static final String FIELDNAMEATTRIBUTE =  "fieldName";

    //Supported Computables
    private static final String CUMULATIVECOMPUTABLE =  "hec.ensemble.stats.CumulativeComputable";
    private static final String NDAYMULTICOMPUTABLE =  "hec.ensemble.stats.NDayMultiComputable";
    private static final String MULTISTATCOMPUTABLE =  "hec.ensemble.stats.MultiStatComputable";
    private static final String TWOSTEPCOMPUTABLE =  "hec.ensemble.stats.TwoStepComputable";
    private static final String MAXACCUMDURATION =  "hec.ensemble.stats.MaxAccumDuration";
    private static final String MAXAVGDURATION =  "hec.ensemble.stats.MaxAvgDuration";
    private static final String MAXCOMPUTABLE =  "hec.ensemble.stats.MaxComputable";
    private static final String MAXOFMAXIMUMSCOMPUTABLE =  "hec.ensemble.stats.MaxOfMaximumsComputable";
    private static final String MEANCOMPUTABLE =  "hec.ensemble.stats.MeanComputable";
    private static final String MEDIANCOMPUTABLE =  "hec.ensemble.stats.MedianComputable";
    private static final String MINCOMPUTABLE =  "hec.ensemble.stats.MinComputable";
    private static final String PERCENTILESCOMPUTABLE =  "hec.ensemble.stats.PercentilesComputable";
    private static final String TOTAL =  "hec.ensemble.stats.Total";

    private static Object fromClassname(String classname){
        switch (classname){
            case CUMULATIVECOMPUTABLE:
                return new CumulativeComputable();
            case NDAYMULTICOMPUTABLE:
                return new NDayMultiComputable();
            case MULTISTATCOMPUTABLE:
                return new MultiStatComputable();
            case TWOSTEPCOMPUTABLE:
                return new TwoStepComputable();
            case MAXACCUMDURATION:
                return new MaxAccumDuration();
            case MAXAVGDURATION:
                return new MaxAvgDuration();
            case MAXCOMPUTABLE:
                return new MaxComputable();
            case MAXOFMAXIMUMSCOMPUTABLE:
                return new MaxOfMaximumsComputable();
            case MEANCOMPUTABLE:
                return new MeanComputable();
            case MEDIANCOMPUTABLE:
                return new MedianComputable();
            case MINCOMPUTABLE:
                return new MinComputable();
            case PERCENTILESCOMPUTABLE:
                return new PercentilesComputable();
            case TOTAL:
                return new Total();
            default:
                System.out.println("The Computable you're using has not been added to the list of available.");
                return null;
        }
    }

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
                    case DOUBLE:
                        double doubleVal = (double) objectFieldValue;
                        attribute = Double.toString(doubleVal);
                        break;
                    case INTEGER:
                        int intValue = (int) objectFieldValue;
                        attribute = Integer.toString(intValue);
                        break;
                    case FLOAT:
                        float floatValue = (float) objectFieldValue;
                        attribute = Float.toString(floatValue);
                        break;
                    case FLOATARRAY:
                        float[] floatArray = (float[]) objectFieldValue;
                        attribute = Arrays.toString(floatArray);
                        break;
                    case STATISTICSARRAY:
                        Statistics[] stats = (Statistics[]) objectFieldValue;
                        attribute = Arrays.toString(stats);
                        break;
                    case COMPUTABLE:
                        Computable computable = (Computable) objectFieldValue;
                        Element computableEle = Serializer.toXML(computable); //recursive call
                        computableEle.setAttribute(FIELDNAMEATTRIBUTE, fieldName);
                        ele.addContent(computableEle);
                        break;
                    case MULTICOMPUTABLE:
                        MultiComputable mcomputable = (MultiComputable) objectFieldValue;
                        Element mcomputableEle = Serializer.toXML(mcomputable); //recursive call
                        mcomputableEle.setAttribute(FIELDNAMEATTRIBUTE, fieldName);
                        ele.addContent(mcomputableEle);
                        break;
                    case SINGLECOMPUTABLE:
                        SingleComputable scomputable = (SingleComputable) objectFieldValue;
                        Element scomputableEle = Serializer.toXML(scomputable); //recursive call
                        scomputableEle.setAttribute(FIELDNAMEATTRIBUTE, fieldName);
                        ele.addContent(scomputableEle);
                        break;
                    case BOOLEAN:
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
           computable = (T)fromClassname(computableName);
            Field[] fields = computable.getClass().getDeclaredFields();
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
                    case DOUBLE:
                        if(attributeValue == null){ continue;}
                        field.set(computable, Double.parseDouble(attributeValue));
                        break;
                    case BOOLEAN:
                        if(attributeValue == null){ continue;}
                        field.set(computable, Boolean.parseBoolean(attributeValue));
                        break;
                    case INTEGER:
                        if(attributeValue == null){ continue;}
                        field.set(computable, Integer.parseInt(attributeValue));
                        break;
                    case FLOAT:
                        if(attributeValue == null){continue;}
                        field.set(computable, Float.parseFloat(attributeValue));
                    case FLOATARRAY:
                        if(attributeValue == null){continue;}
                        String[] floatStringSplit = prepareXMLArray(attributeValue);
                        float[] floats = new float[floatStringSplit.length];
                        for (int i = 0; i < floatStringSplit.length; i++) {
                            float value = Float.parseFloat(floatStringSplit[i]);
                            floats[i] = value;
                        }
                        field.set(computable, floats);
                        break;
                    case STATISTICSARRAY:
                        if(attributeValue == null){continue;}
                        String[] statisticsStringSplit = prepareXMLArray(attributeValue);
                        Statistics[] statsArray = new Statistics[statisticsStringSplit.length];
                        for (int i = 0; i < statisticsStringSplit.length; i++) {
                            Statistics stat = Statistics.valueOf(statisticsStringSplit[i]);
                            statsArray[i] = stat;
                        }
                        field.set(computable, statsArray);
                        break;
                    case COMPUTABLE:
                        List<Object> childs =  ele.getChildren();
                        for( Object child: childs){
                            Element childElement = (Element)child;
                            String elementFieldName = childElement.getAttributeValue(FIELDNAMEATTRIBUTE);
                            if(elementFieldName.equals(fieldName)){
                                Computable computer = Serializer.fromXML(childElement);
                                field.set(computable, computer);
                            }
                        }
                        break;
                    case MULTICOMPUTABLE:
                        List<Object> mchilds =  ele.getChildren();
                        for( Object child: mchilds){
                            Element childElement = (Element)child;
                            String elementFieldName = childElement.getAttributeValue(FIELDNAMEATTRIBUTE);
                            if(elementFieldName.equals(fieldName)){
                                MultiComputable computer = Serializer.fromXML(childElement);
                                field.set(computable, computer);
                            }
                        }
                        break;
                    case SINGLECOMPUTABLE:
                        List<Object> schilds =  ele.getChildren();
                        for( Object child: schilds){
                            Element childElement = (Element)child;
                            String elementFieldName = childElement.getAttributeValue(FIELDNAMEATTRIBUTE);
                            if(elementFieldName.equals(fieldName)){
                                SingleComputable computer = Serializer.fromXML(childElement);
                                field.set(computable, computer);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (IllegalAccessException e) {
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