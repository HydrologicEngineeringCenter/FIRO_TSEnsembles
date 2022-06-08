package hec.stats;
import org.jdom.Attribute;
import org.jdom.Element;

import java.lang.reflect.*;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

public interface Configuration{
    public Duration getDuration();
    public ZonedDateTime getIssueDate();
    public ZonedDateTime getStartDate();
    public String getUnits();

    default org.jdom.Element toXML(){
        Field[] fields = this.getClass().getDeclaredFields();
        Element ele = new Element(this.getClass().getName());
        for(Field f: fields){
            try {
                Type type = f.getType();
                String stringType = type.getTypeName();
                String fieldName = f.getName();
                Object objectFieldValue = f.get(this);
                String attribute = null;

                switch(stringType){
                    case "java.time.Duration":
                        Duration durationVal = (Duration) objectFieldValue;
                        attribute = durationVal.toString();
                        break;
                    case "java.time.ZonedDateTime":
                        ZonedDateTime zonedDateTimeVal = (ZonedDateTime) objectFieldValue;
                        attribute = zonedDateTimeVal.toString();
                        break;
                    case "java.lang.String":
                        attribute = (String) objectFieldValue;
                        break;
                }
                ele.setAttribute(fieldName,attribute);

            } catch (IllegalArgumentException | IllegalAccessException ex) {
                System.out.println("Failed Cast" );
            }
        }
        return ele;
    }
}