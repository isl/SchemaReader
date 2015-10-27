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

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import java.util.Vector;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.parser.XSOMParser;
import java.util.Collection;
import java.util.Iterator;

/**
 * Visitor class
 * @author samarita
 */
public class Visitor {

    static String nodeXpath = "";
    static boolean nodeFound = false;

    

    /**
     *
     */
    public static class SimpleTypeRestriction {

        /**
         *
         */
        public String[] enumeration = null;

        /**
         *
         */
        public String maxValue = null;

        /**
         *
         */
        public String minValue = null;

        /**
         *
         */
        public String length = null;

        /**
         *
         */
        public String maxLength = null;

        /**
         *
         */
        public String minLength = null;

        /**
         *
         */
        public String[] pattern = null;

        /**
         *
         */
        public String totalDigits = null;

        public String toString() {
            String enumValues = "";
            if (enumeration != null) {
                for (String val : enumeration) {
                    enumValues += val + ", ";
                }
                enumValues = enumValues.substring(0, enumValues.lastIndexOf(','));
            }

            String patternValues = "";
            if (pattern != null) {
                for (String val : pattern) {
                    patternValues += "(" + val + ")|";
                }
                patternValues = patternValues.substring(0, patternValues.lastIndexOf('|'));
            }
            String retval = "";
            retval += maxValue == null ? "" : "[MaxValue  = " + maxValue + "]\t";
            retval += minValue == null ? "" : "[MinValue  = " + minValue + "]\t";
            retval += maxLength == null ? "" : "[MaxLength = " + maxLength + "]\t";
            retval += minLength == null ? "" : "[MinLength = " + minLength + "]\t";
            retval += pattern == null ? "" : "[Pattern(s) = " + patternValues + "]\t";
            retval += totalDigits == null ? "" : "[TotalDigits = " + totalDigits + "]\t";
            retval += length == null ? "" : "[Length = " + length + "]\t";
            retval += enumeration == null ? "" : "[Values = " + enumValues + "]\t";

            return retval;
        }
    }

    private static void initRestrictions(XSSimpleType xsSimpleType, SimpleTypeRestriction simpleTypeRestriction) {
        XSRestrictionSimpleType restriction = xsSimpleType.asRestriction();
        if (restriction != null) {
            Vector<String> enumeration = new Vector<String>();
            Vector<String> pattern = new Vector<String>();

            for (XSFacet facet : restriction.getDeclaredFacets()) {
                if (facet.getName().equals(XSFacet.FACET_ENUMERATION)) {
                    enumeration.add(facet.getValue().value);
                }
                if (facet.getName().equals(XSFacet.FACET_MAXINCLUSIVE)) {
                    simpleTypeRestriction.maxValue = facet.getValue().value;
                }
                if (facet.getName().equals(XSFacet.FACET_MININCLUSIVE)) {
                    simpleTypeRestriction.minValue = facet.getValue().value;
                }
                if (facet.getName().equals(XSFacet.FACET_MAXEXCLUSIVE)) {
                    simpleTypeRestriction.maxValue = String.valueOf(Integer.parseInt(facet.getValue().value) - 1);
                }
                if (facet.getName().equals(XSFacet.FACET_MINEXCLUSIVE)) {
                    simpleTypeRestriction.minValue = String.valueOf(Integer.parseInt(facet.getValue().value) + 1);
                }
                if (facet.getName().equals(XSFacet.FACET_LENGTH)) {
                    simpleTypeRestriction.length = facet.getValue().value;
                }
                if (facet.getName().equals(XSFacet.FACET_MAXLENGTH)) {
                    simpleTypeRestriction.maxLength = facet.getValue().value;
                }
                if (facet.getName().equals(XSFacet.FACET_MINLENGTH)) {
                    simpleTypeRestriction.minLength = facet.getValue().value;
                }
                if (facet.getName().equals(XSFacet.FACET_PATTERN)) {
                    pattern.add(facet.getValue().value);
                }
                if (facet.getName().equals(XSFacet.FACET_TOTALDIGITS)) {
                    simpleTypeRestriction.totalDigits = facet.getValue().value;
                }
            }
            if (enumeration.size() > 0) {
                simpleTypeRestriction.enumeration = enumeration.toArray(new String[]{});
            }
            if (pattern.size() > 0) {
                simpleTypeRestriction.pattern = pattern.toArray(new String[]{});
            }
        }
    }

    private static void printParticle(XSParticle particle, String occurs, String absPath, String indent) {
        if (nodeFound) {
            occurs = "  MinOccurs = " + particle.getMinOccurs() + ", MaxOccurs = " + particle.getMaxOccurs();
        }
        XSTerm term = particle.getTerm();
        if (term.isModelGroup()) {
            printGroup(term.asModelGroup(), occurs, absPath, indent);
        } else if (term.isModelGroupDecl()) {
            printGroupDecl(term.asModelGroupDecl(), occurs, absPath, indent);
        } else if (term.isElementDecl()) {
            printElement(term.asElementDecl(), occurs, absPath, indent);
        } else {

        }
    }

    private static void printGroup(XSModelGroup modelGroup, String occurs, String absPath, String indent) {
        if (nodeFound) {
            System.out.println(indent + "[Start of " + modelGroup.getCompositor() + occurs + "]");
        }
        for (XSParticle particle : modelGroup.getChildren()) {

            printParticle(particle, occurs, absPath, indent + "\t");
        }
        if (nodeFound) {
            System.out.println(indent + "[End of " + modelGroup.getCompositor() + "]");
        }

    }

    private static void printGroupDecl(XSModelGroupDecl modelGroupDecl, String occurs, String absPath, String indent) {
        if (nodeFound) {
            System.out.println(indent + "[Group " + modelGroupDecl.getName() + occurs + "]");
        }
        printGroup(modelGroupDecl.getModelGroup(), occurs, absPath, indent);
    }

    private static void printAttributes(XSComplexType complexType,  String indent) {
        Collection<? extends XSAttributeUse> c = complexType.getAttributeUses();
        Iterator<? extends XSAttributeUse> i = c.iterator();
        while (i.hasNext()) {
            // i.next is attributeUse
            XSAttributeUse attUse = i.next();

            XSAttributeDecl attributeDecl = attUse.getDecl();
            String use = "";
            if (attUse.isRequired()) {
                use = "Required";
            } else {
                use = "Optional";
            }

            if (nodeFound) {
                System.out.print(indent + "[Attribute " + attributeDecl.getName() + "   Use " + use + "]\n");
            }
        }

    }

    private static void printComplexType(XSComplexType complexType, String occurs, String absPath, String indent) {
        if (nodeFound) {
        System.out.println();
        }
        printAttributes(complexType,  indent);
        XSParticle particle = complexType.getContentType().asParticle();
        if (particle != null) {
            printParticle(particle, occurs, absPath, indent);
        }

    }

    private static void printSimpleType(XSSimpleType simpleType, String occurs, String absPath, String indent) {
        SimpleTypeRestriction restriction = new SimpleTypeRestriction();
        initRestrictions(simpleType, restriction);
        System.out.println(restriction.toString());
    }

    private static void printElement(XSElementDecl element, String occurs, String absPath, String indent) {
        absPath += "/" + element.getName();
        if (absPath.contains(nodeXpath)) {
            nodeFound = true;
        } else {
            nodeFound = false;
        }
        if (nodeFound) {
            System.out.print(indent + "[Element " + absPath + "   " + occurs + "] of type [" + element.getType().getBaseType().getName() + "]");
        }
        if (element.getType().isComplexType()) {
            printComplexType(element.getType().asComplexType(), occurs, absPath, indent);
        } else {
            printSimpleType(element.getType().asSimpleType(), occurs, absPath, indent);
        }
    }

    /**
     *
     * @param xsdFile
     * @param rootElement
     * @param pathToNode
     */
    public static void xsomNavigate(String xsdFile, String rootElement, String pathToNode) {
        String occurs = "";
        String absPath = "";
        String indent = "";
        nodeXpath = pathToNode;
        try {
            XSOMParser parser = new XSOMParser();
            parser.parse(xsdFile);
            printElement(parser.getResult().getSchema(1).getElementDecl(rootElement), occurs, absPath, indent);
        } catch (Exception exp) {
            exp.printStackTrace(System.out);
        }
    }

    private static void parseAttribute(XSAttributeUse attUse) {
        XSAttributeDecl attributeDecl = attUse.getDecl();
        XSSimpleType xsAttributeType = attributeDecl.getType();
        if (attUse.isRequired()) {
            System.out.println("Use: Required");
        } else {
            System.out.println("Use: Optional");
        }
        System.out.println("Fixed: " + attributeDecl.getFixedValue());
        System.out.println("Default: " + attributeDecl.getDefaultValue());
    }
}
