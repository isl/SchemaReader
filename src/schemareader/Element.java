/*
 * Copyright 2012-2015 Institute of Computer Science,
 * Foundation for Research and Technology - Hellas
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved
 * by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations
 * under the Licence.
 *
 * Contact:  POBox 1385, Heraklio Crete, GR-700 13 GREECE
 * Tel:+30-2810-391632
 * Fax: +30-2810-391638
 * E-mail: isl@ics.forth.gr
 * http://www.ics.forth.gr/isl
 *
 * Authors : Georgios Samaritakis, Konstantina Konsolaki.
 *
 * This file is part of the SchemaReader project.
 */
package schemareader;

import com.sun.xml.xsom.XSElementDecl;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * Element is a convenience class used for XML elements so that you do not have
 * to learn XSOM API.
 *
 * @author samarita
 */
public class Element {

    XSElementDecl elem;
    int minOccurs, maxOccurs;
    HashMap attributes = new HashMap();
    HashMap restrictions = new HashMap();
    String fullPath, name;
    String type;
    String subtree;
    boolean isComplex = false;
    boolean isSimple = false;
    boolean isDummy = false;
    String info;
    int depth;

    /**
     * Default Element constructor
     *
     * @param element Element as a <code>XSElementDecl</code>
     * @param path Path as a <code>String</code>
     * @param minOccurs Minimum occurences as a <code>BigInteger</code>
     * @param maxOccurs Maximum occurences as a <code>BigInteger</code>
     */
    public Element(XSElementDecl element, String path, BigInteger minOccurs, BigInteger maxOccurs) {

        this.name = path.substring(path.lastIndexOf("/") + 1, path.length());
        String type = element.getType().getName();

        if (type == null) {
            this.type = element.getType().getBaseType().getName();
        } else {

            this.type = type;

        }
        this.fullPath = path;
        this.minOccurs = minOccurs.intValue();
        this.maxOccurs = maxOccurs.intValue();

    }

    /**
     * Gets name
     *
     * @return Name as a <code>String</code>
     */
    public String getName() {
        return name;
    }

    /**
     * Gets type
     *
     * @return Type as a <code>String</code>
     */
    public String getType() {
        return type;
    }

    /**
     * Gets depth
     *
     * @return Depth as a <code>int</code>
     */
    public int getDepth() {
        return StringUtils.countMatches(this.fullPath, "/") + 1;
    }

    /**
     * Dummy constructor
     *
     * @param info
     */
    public Element(String info) {
        this.isDummy = true;
        this.info = info;

    }

    /**
     * Sets info
     *
     * @param info Info to set as a <code>String</code>
     */
    public void setInfo(String info) {
        this.info = info;

    }

    /**
     * Gets info
     *
     * @return Info as a <code>String</code>
     */
    public String getInfo() {
        return this.info;

    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        output = output.append("<").append(this.name);

        if (this.attributes.isEmpty()) {
            output = output.append(">");
        } else {
            Set<String> attrs = this.attributes.keySet();
            for (String attr : attrs) {
                String attrFeaturesAsString = (String) this.attributes.get(attr);
                String[] attrFeatures = attrFeaturesAsString.split(", ");
                String attrType = attrFeatures[0];
                String attrUse = attrFeatures[1];
                String attrFixed = attrFeatures[2];
                String attrDefault = attrFeatures[3];
                String attrValue = "";
                if (attrUse.contains("Required")) {
                    if (!attrFixed.equals("null")) {
                        attrValue = attrFixed;
                    } else if (!attrDefault.equals("null")) {
                        attrValue = attrDefault;
                    }
                    output = output.append(" ").append(attr).append("='").append(attrValue).append("'");
                }
            }
            output = output.append(">");
        }
        output = output.append("</").append(this.name).append(">");

        return output.toString();
    }

    /**
     * Gets attributes
     *
     * @return Attribute names as a <code>HashMap</code>
     */
    public HashMap getAttributes() {
        return attributes;
    }

    /**
     * Gets restrictions (xsd enumerations, facets, patterns etc.)
     *
     * @return Restrictions as a <code>HashMap</code>
     */
    public HashMap getRestrictions() {
        return restrictions;
    }

    void setAttributes(HashMap attributes) {
        this.attributes = attributes;
    }

    void setRestrictions(HashMap restrictions) {
        this.restrictions = restrictions;
    }

    /**
     * Gets full path
     *
     * @return Path as a <code>String</code>
     */
    public String getFullPath() {
        return fullPath;
    }

    /**
     * Gets subtree with Element as root
     *
     * @return Subtree as a <code>String</code>
     */
    public String getSubtree() {
        return subtree;
    }

    /**
     * Checks if Element is complex
     *
     * @return If complex <code>true</code>, else <code>false</code>
     */
    public boolean isComplexType() {
        return this.isComplex;
    }

    /**
     * Checks if Element is simple
     *
     * @return If simple <code>true</code>, else <code>false</code>
     */
    public boolean isSimpleType() {
        return this.isSimple;
    }

    /**
     * Gets minimum occurences
     *
     * @return Minimum occurences as an <code>int</code>
     */
    public int getMinOccurs() {
        return this.minOccurs;
    }

    /**
     * Checks if Element is optional
     *
     * @return If optional <code>true</code>, else <code>false</code>
     */
    public boolean isOptional() {
        int elemMinOccurs = this.minOccurs;
        String parentInfo = this.info;
        if (parentInfo != null) {
            int parentMinOccurs = Integer.parseInt(parentInfo.split("_")[1]);
            if (parentMinOccurs == 0) {
                return true;
            }
        }

        if (elemMinOccurs == 0) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Gets maximum occurences
     * @return Minimum occurences as an <code>int</code>
     */
    public int getMaxOccurs() {
        return this.maxOccurs;

    }
}
