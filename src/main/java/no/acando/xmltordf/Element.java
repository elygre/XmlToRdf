package no.acando.xmltordf;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by havardottestad on 02/06/16.
 */
public class Element {
    public String type;
    public String uri;
    public Element parent;
    public StringBuilder hasValue;
    public List<Element> hasChild = new ArrayList<>(10);
    public List<Property> properties = new ArrayList<>(3);
     long index = 0;
     boolean shallow;
     boolean autoDetectedAsLiteralProperty;


    public List<Object> mixedContent = new ArrayList<>();
    public StringBuilder tempMixedContentString = new StringBuilder("");


    public void appendValue(char[] ch, int start, int length) {
        if (hasValue == null) {
            hasValue = new StringBuilder(new String(ch, start, length));
        } else {
            hasValue.append(ch, start, length);
        }
        tempMixedContentString.append(ch, start, length);
        hasValueString = null;
    }


    public String getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public Element getParent() {
        return parent;
    }

    String hasValueString;
    boolean hasValueStringEmpty = false;

    public String getHasValue() {

        if (hasValue == null) {
            return null;
        }
        if (hasValueString == null) {
            hasValueString = hasValue.toString().trim();
            hasValueStringEmpty = hasValueString.isEmpty();
        }

        if (hasValueStringEmpty) {
            return null;
        }
        return hasValueString;
    }

    public List<Element> getHasChild() {
        return hasChild;
    }

    public List<Property> getProperties() {
        return properties;
    }

    public long getIndex() {
        return index;
    }

    public boolean isShallow() {
        return shallow;
    }

    public boolean isAutoDetectedAsLiteralProperty() {
        return autoDetectedAsLiteralProperty;
    }


    public void addMixedContent(Element element) {
        mixedContent.add(tempMixedContentString.toString());
        tempMixedContentString = new StringBuilder("");
        mixedContent.add(element);
    }


}
