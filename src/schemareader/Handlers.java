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
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import schemareader.Visitor.SimpleTypeRestriction;

/**
 * Handlers class
 * @author samarita
 */
class Handlers {

    String fullPath;
    ArrayList<Element> elements = new ArrayList<Element>();
    ArrayList<String> elementNamesAndComplexTypes = new ArrayList<String>();

    Handlers(String fullPath) {
        this.fullPath = fullPath;
    }

    void handleParticles(XSComplexType complexType, String path) {
        XSParticle particle = complexType.getContentType().asParticle();
        if (particle != null) {
            handleParticle(particle, path);
        }
    }

    private void handleParticle(XSParticle particle, String path) {
        XSTerm term = particle.getTerm();
        if (term.isModelGroup()) {
            handleGroup(term.asModelGroup(), path, particle.getMinOccurs(), particle.getMaxOccurs());
        } else if (term.isModelGroupDecl()) {
            handleGroupDecl(term.asModelGroupDecl(), path, particle.getMinOccurs(), particle.getMaxOccurs());
        } else if (term.isElementDecl()) {
            Element elem = null;

            path = path + "/" + term.asElementDecl().getName();
            if (path.startsWith(this.fullPath)) {
                elem = new Element(term.asElementDecl(), path, particle.getMinOccurs(), particle.getMaxOccurs());
            }

            if (term.asElementDecl().getType().isComplexType()) {
                XSType baseType = term.asElementDecl().getType().getBaseType();

                if (path.startsWith(this.fullPath)) {
                    elem.setAttributes(handleAttributes(term.asElementDecl().getType().asComplexType()));
                    //Get restrictions of base type!
                    if (baseType.isSimpleType()) {
                        elem.setRestrictions(handleSimpleType(term.asElementDecl().getType().getBaseType().asSimpleType()));
                    }
                    elements.add(elem);
                }

                if (elem != null) {
                    if (elementNamesAndComplexTypes.contains(term.asElementDecl().getName() + "___" + elem.getType()) && path.contains("/" + term.asElementDecl().getName() + "/")) {
                        System.out.println("RECURSION!");
                        return;
                    } else {
                        elementNamesAndComplexTypes.add(term.asElementDecl().getName() + "___" + elem.getType());
                    }
                }

                if (path.endsWith("/" + term.asElementDecl().getName() + "/" + term.asElementDecl().getName())) {
                    System.out.println("Recursion detected! Path is:" + path);
                    System.out.println("Breaking loop to avoid memory leak!");

                } else {
                    handleParticles(term.asElementDecl().getType().asComplexType(), path);
                }
            } else {
                if (path.startsWith(this.fullPath)) {
                    elem.setRestrictions(handleSimpleType(term.asElementDecl().getType().asSimpleType()));

                    elements.add(elem);
                }
            }

        }
    }

    private void handleGroup(XSModelGroup modelGroup, String path, BigInteger minOccurs, BigInteger maxOccurs) {

        String modelGroupType = "";
        if (path.startsWith(this.fullPath)) {
            if (modelGroup.getCompositor() == modelGroup.getCompositor().SEQUENCE) {
                modelGroupType = "Sequence";

            } else if (modelGroup.getCompositor() == modelGroup.getCompositor().CHOICE) {
                modelGroupType = "Choice";

            } else { //UNDEFINED!!!
            }
            elements.add(new Element(modelGroup.getChildren().length + "_" + modelGroupType + "_" + minOccurs + "_" + maxOccurs));
        }

        for (XSParticle particle : modelGroup.getChildren()) {
            handleParticle(particle, path);
        }

    }

    private void handleGroupDecl(XSModelGroupDecl modelGroupDecl, String path, BigInteger minOccurs, BigInteger maxOccurs) {
        handleGroup(modelGroupDecl.getModelGroup(), path, minOccurs, maxOccurs);
    }

    HashMap<String, String> handleSimpleType(XSSimpleType simpleType) {
        SimpleTypeRestriction restriction = new SimpleTypeRestriction();
        HashMap<String, String> restrs = initRestrictions(simpleType, restriction);
        return restrs;
    }

    HashMap<String, String> handleAttributes(XSComplexType complexType) {
        HashMap<String, String> attrs = new HashMap<String, String>();

        Collection<? extends XSAttributeUse> c = complexType.getAttributeUses();
        Iterator<? extends XSAttributeUse> i = c.iterator();
        while (i.hasNext()) {
            // i.next is attributeUse
            XSAttributeUse attUse = i.next();

            XSAttributeDecl attributeDecl = attUse.getDecl();
            XSSimpleType xsAttributeType = attributeDecl.getType();
            String use = "";
            if (attUse.isRequired()) {
                use = "Required";
            } else {
                use = "Optional";
            }
            attrs.put(attributeDecl.getName(), xsAttributeType.getName() + ", " + use + ", " + attributeDecl.getFixedValue() + ", " + attributeDecl.getDefaultValue());
        }
        return attrs;

    }

    private HashMap<String, String> initRestrictions(XSSimpleType xsSimpleType, SimpleTypeRestriction simpleTypeRestriction) {
        HashMap<String, String> restrictions = new HashMap<String, String>();

        XSRestrictionSimpleType restriction = xsSimpleType.asRestriction();
        if (restriction != null) {

            ArrayList<String> enumeration = new ArrayList<String>();
            ArrayList<String> pattern = new ArrayList<String>();

            for (XSFacet facet : restriction.getDeclaredFacets()) {
                if (facet.getName().equals(XSFacet.FACET_ENUMERATION)) {
                    enumeration.add(facet.getValue().value);
                }
                if (facet.getName().equals(XSFacet.FACET_MAXINCLUSIVE)) {
                    restrictions.put("maxValue", facet.getValue().value);
                }
                if (facet.getName().equals(XSFacet.FACET_MININCLUSIVE)) {
                    restrictions.put("minValue", facet.getValue().value);
                }
                if (facet.getName().equals(XSFacet.FACET_MAXEXCLUSIVE)) {
                    restrictions.put("maxValue", String.valueOf(Integer.parseInt(facet.getValue().value) - 1));
                }
                if (facet.getName().equals(XSFacet.FACET_MINEXCLUSIVE)) {
                    restrictions.put("minValue", String.valueOf(Integer.parseInt(facet.getValue().value) + 1));

                }
                if (facet.getName().equals(XSFacet.FACET_LENGTH)) {
                    restrictions.put("length", facet.getValue().value);
                }
                if (facet.getName().equals(XSFacet.FACET_MAXLENGTH)) {
                    restrictions.put("maxLength", facet.getValue().value);
                }
                if (facet.getName().equals(XSFacet.FACET_MINLENGTH)) {
                    restrictions.put("minLength", facet.getValue().value);
                }
                if (facet.getName().equals(XSFacet.FACET_PATTERN)) {
                    pattern.add(facet.getValue().value);
                }
                if (facet.getName().equals(XSFacet.FACET_TOTALDIGITS)) {
                    restrictions.put("totalDigits", facet.getValue().value);
                    simpleTypeRestriction.totalDigits = facet.getValue().value;
                }
            }
            if (enumeration.size() > 0) {
                StringBuilder enums = new StringBuilder();
                for (int i = 0; i < enumeration.size(); i++) {
                    String delimiter = "###___###";
                    if (i == enumeration.size() - 1) {
                        delimiter = "";
                    }
                    enums = enums.append(enumeration.get(i)).append(delimiter);
                }

                restrictions.put("enumerations", enums.toString());

            }
            if (pattern.size() > 0) {
                StringBuilder patterns = new StringBuilder();
                for (int i = 0; i < pattern.size(); i++) {
                    String delimiter = "###___###";
                    if (i == pattern.size() - 1) {
                        delimiter = "";
                    }
                    patterns = patterns.append(pattern.get(i)).append(delimiter);
                }
                restrictions.put("patterns", patterns.toString());

            }
        }
        return restrictions;
    }
}
