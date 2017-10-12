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
import com.sun.xml.xsom.parser.XSOMParser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

/**
 * SchemaFile is a class used to parse xsd schema files. It contains several
 * methods to validate an xml file, create xml templates or simply get element
 * names given a xpath.
 *
 * @author samarita
 */
public class SchemaFile {

    XSOMParser parser = new XSOMParser();
    String xsdFilePath;

    /**
     * SchemaFile constructor
     *
     * @param xsdFile Absolute path of xsd file as a <code>String</code>
     */
    public SchemaFile(String xsdFile) {
        try {
            this.xsdFilePath = xsdFile;
            parser.parse(xsdFile);
        } catch (Exception exp) {
            exp.printStackTrace(System.out);
        }
    }

    /**
     * Gets all element names of SchemaFile
     *
     * @return All element names as a <code>ArrayList</code>
     */
    public ArrayList<String> getElements() {
        ArrayList<String> elements = new ArrayList<String>();
        try {
            Iterator jtr = this.parser.getResult().getSchema(1).iterateElementDecls();
            while (jtr.hasNext()) {
                XSElementDecl e = (XSElementDecl) jtr.next();
                elements.add(e.getName());

            }
            return elements;
        } catch (SAXException ex) {
            ex.printStackTrace();
            return null;
        }

    }

    /**
     * Validates an xml against a SchemaFile
     *
     * @param xml Xml content as a <code>String</code>
     * @param lang Message language as a <code>String</code> ("gr","en" for now)
     * @return Validation message as a <code>String</code>
     */
    public String validate(String xml, String lang) {
        String output = "Valid xml.";
        if (lang.equals("gr")) {
            output = "Το xml είναι έγκυρο.";
        }
        String invalidXMLPart = "";
        File schemaFile = new File(this.xsdFilePath);

        StringReader reader = new StringReader(xml);

        Source xmlFile = new StreamSource(reader);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        MyErrorHandler myErrorHandler = new MyErrorHandler();
        Validator validator = null;
        try {
            Schema schema = schemaFactory.newSchema(schemaFile);
            validator = schema.newValidator();
            validator.setErrorHandler(myErrorHandler);
            validator.validate(xmlFile);

        } catch (IOException ex) {
        } catch (SAXException e) {
            e.printStackTrace();
            if (myErrorHandler.getLog().contains("Line: ")) {
                String errorMsg = myErrorHandler.getLog();
                String lineNumber = errorMsg.substring(errorMsg.lastIndexOf("Line: "), errorMsg.lastIndexOf("Column: ")).replaceAll("Line:\\s*", "").trim();

                LinkedHashMap<Integer, String> xmlLines = getXMLLineByLine(xml, Integer.parseInt(lineNumber));
                int lineWithProblemIndex = Integer.parseInt(lineNumber);
                String lineWithProblem = xmlLines.get(lineWithProblemIndex);
                Utils utils = new Utils();
                ArrayList<String> elements = utils.findReg("(?:<([^\\s]+)[^>]*/>|<([^\\s]+)[^>]*>(.*?)</\\2>)", lineWithProblem, Pattern.DOTALL);

                int lineIndex = lineWithProblemIndex;
                while (elements.size() < 1) {
                    lineIndex = lineIndex - 1;
                    if (lineIndex < lineWithProblemIndex - 30) { //Show last 30 lines...
                        elements.add("...\n" + lineWithProblem + "\n...");
                        break; //Bad bad bad, but then again who wants to wait forever?
                    }
                    lineWithProblem = xmlLines.get(lineIndex) + "\n" + lineWithProblem;

                }

                if (!elements.isEmpty()) {
                    if (lang.equals("gr")) {
                        invalidXMLPart = "\nΠροβληματικό τμήμα: " + elements.get(0);
                    } else {
                        invalidXMLPart = "\nPart with error: " + elements.get(0);

                    }
                }

            }
            if (lang.equals("gr")) {
                output = "Το xml δεν είναι έγκυρο. Πιο αναλυτικά:\n" + myErrorHandler.getLog();
            } else {
                output = "Invalid xml:\n" + myErrorHandler.getLog();

            }
            output = output + invalidXMLPart;
        }
        return output;
    }

    /**
     * Validates an xml against a SchemaFile
     *
     * @param xml Xml content as a <code>String</code>
     * @return If valid <code>true</code>, else <code>false</code>
     */
    public boolean validate(String xml) {
        boolean isValid = false;

        File schemaFile = new File(this.xsdFilePath);

        StringReader reader = new StringReader(xml);

        Source xmlFile = new StreamSource(reader);
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        MyErrorHandler myErrorHandler = new MyErrorHandler();
        Validator validator = null;
        try {
            Schema schema = schemaFactory.newSchema(schemaFile);
            validator = schema.newValidator();
            validator.setErrorHandler(myErrorHandler);
            validator.validate(xmlFile);
            isValid = true;

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();

        }
        return isValid;
    }

    private LinkedHashMap<Integer, String> getXMLLineByLine(String str, int lineNumber) {

        LineNumberReader lineNumberReader = null;
        LinkedHashMap<Integer, String> lineNumbersAndContent = new LinkedHashMap<Integer, String>();

        try {

            //Construct the LineNumberReader object
            lineNumberReader = new LineNumberReader(new StringReader(str));

            String line = null;

            while ((line = lineNumberReader.readLine()) != null) {

                if (lineNumberReader.getLineNumber() <= lineNumber) {
                    lineNumbersAndContent.put(lineNumberReader.getLineNumber(), line);
                }
            }

        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
        } finally {
            //Close the BufferedWriter
            try {
                if (lineNumberReader != null) {
                    lineNumberReader.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        return lineNumbersAndContent;
    }

    /**
     * Validates an xml against a SchemaFile
     *
     * @param xmlFile Xml content as a <code>File</code>
     * @param lang Message language as a <code>String</code> ("gr","en" for now)
     * @return Validation message as a <code>String</code>
     */
    public String validate(File xmlFile, String lang) {
        String xmlFilePath = xmlFile.getPath();
        String output = "File " + xmlFilePath + " is valid.";
        if (lang.equals("gr")) {
            output = "Το αρχείο " + xmlFilePath + " είναι έγκυρο.";
        }

        File schemaFile = new File(this.xsdFilePath);

        Source xmlFileSource = new StreamSource(xmlFile);

        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        MyErrorHandler myErrorHandler = new MyErrorHandler();
        try {
            Schema schema = schemaFactory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.setErrorHandler(myErrorHandler);
            validator.validate(xmlFileSource);

        } catch (IOException ex) {
        } catch (SAXException e) {
            System.out.println("--");

            output = output = "File " + xmlFilePath + " is not valid:\n" + myErrorHandler.getLog();
            if (lang.equals("gr")) {
                output = "Το αρχείο " + xmlFilePath + " δεν είναι έγκυρο. Πιο αναλυτικά:\n" + myErrorHandler.getLog();
            }

//            e.printStackTrace();
        }
//        System.out.println(myErrorHandler.getLog());
        return output;
    }

    /**
     * Creates LabelsAndLinksFile to use in eXist (strictly FeXML method!)
     *
     * @param type Entity type as a <code>String</code>
     * @param xpath Xpath as a <code>String</code>
     * @param lang Language as a <code>String</code>
     * @return LabelsAndLinksFile content as a <code>String</code>
     */
    public String createLabelsAndLinksFile(String type, String xpath, String lang) {
        StringBuilder output = new StringBuilder("<nodes>");
        ArrayList<Element> elements = new ArrayList<Element>();
        if (xpath.contains("/")) {
            elements = getElements(xpath.substring(0, xpath.lastIndexOf("/")));
        } else {

            try {
                elements = getElements(type);
            } catch (NullPointerException ex) { //Old type entities (Authentic)
                elements = getElements("Οντότητα");
            }

        }

        for (Element el : elements) {

            if (!el.fullPath.contains("/admin")) {

                StringBuilder temp = new StringBuilder("<node type=\"" + type + "\">\r\n<xpath>" + el.fullPath + "</xpath>\r\n");
                String vocabulary = "";

                if (lang.equals("gr")) {
                    temp = temp.append("<gr>").append(el.name).append("</gr>\r\n<en></en>\r\n");

                } else if (lang.equals("en")) {
                    temp = temp.append("<gr></gr>\r\n<en>").append(el.name).append("</en>\r\n");

                }
                if (el.attributes.containsKey("ics_vocabulary") || el.attributes.containsKey("sps_vocabulary")) {
                    vocabulary = el.name + ".xml";
                    temp = temp.append("<vocabulary>").append(vocabulary).append("</vocabulary>\r\n");
                }
                if (el.attributes.containsKey("ics_type") || el.attributes.containsKey("sps_type")) {
                    temp = temp.append("<valueFrom type=\"\"></valueFrom>\r\n");
                }
                if (el.attributes.containsKey("sps_facet")) {
                    temp = temp.append("<facet username=\"ExternalReader\" themasUrl=\"http://139.91.183.97:8080/THEMAS/\" thesaurusName=\"TESTDATA\" facetId=\"\"/>\r\n");
                }
                output.append(temp).append("</node>");
            }
        }

        return output.append("</nodes>").toString();
    }

    /**
     * Creates xml subtrees according to a SchemaFile
     *
     * @param xpath Subtree root as a <code>String</code>
     * @param mode Creation mode as a <code>String</code> ("minimum","medium" or
     * "maximum" for now)
     * @return
     */
    public String createXMLSubtree(String xpath, String mode) {
        ArrayList<Element> elements = new ArrayList<Element>();
        if (xpath.contains("/")) {
            elements = getElements(xpath.substring(0, xpath.lastIndexOf("/")));
        } else {
            elements = getElements(xpath);
        }
        StringBuilder subtreeStart = new StringBuilder();
        StringBuilder subtreeEnd = new StringBuilder();
        ArrayList<String> endTags = new ArrayList<String>();

        int depth = 0;
        String endTag = "";
        String elStart = "";
        String elEnd = "";
        boolean isChoice = false;
        boolean firstInChoice = false;
        boolean isRoot = true;
        String optionalPath = "";
        for (Element el : elements) {

            if (el.getFullPath().equals(xpath) || el.getFullPath().startsWith(xpath + "/")) {

                if (!optionalPath.equals("") && el.getFullPath().startsWith(optionalPath)) {
                } else {
                    if (!mode.equals("maximum")) { //added medium mode = maximum except opotional elements at admin part
                        if (mode.equals("minimum")) {
                            if (!isRoot && el.isOptional()) {
                                optionalPath = el.getFullPath();
                                continue;
                            }
                        } else {

                            if (!isRoot && el.isOptional() && !el.name.equals("admin") && el.getFullPath().contains("admin")) {
                                optionalPath = el.getFullPath();
                                continue;
                            }
                        }
                    }

                    int currentDepth = el.getFullPath().split("/").length;
                    String[] splitElem = el.toString().split("><");
                    elStart = splitElem[0] + ">";
                    elEnd = "<" + splitElem[1];
                    if (!mode.equals("medium")) {

                        if (el.info != null) {

                            if (el.info.startsWith("Choice")) {
                                if (isChoice == false) {
                                    firstInChoice = true;
                                } else {
                                    firstInChoice = false;
                                }
                                isChoice = true;
                            } else {
                                isChoice = false;
                                firstInChoice = false;

                            }
                        }
                    }
                    if (isChoice == false || (isChoice == true && firstInChoice == true)) {
                        if (depth > 0 && currentDepth > depth) {
                            subtreeStart = subtreeStart.append(elStart);
                            endTags.add(0, endTag);
                            endTag = elEnd;
                        } else if (currentDepth < depth) {
                            int depthDiff = depth - currentDepth;
                            subtreeStart.append(endTag);

                            for (int i = 0; i < depthDiff; i++) {
                                if (!endTags.isEmpty()) {
                                    subtreeStart.append(endTags.remove(0));
                                }
                            }
                            subtreeStart.append(elStart);
                        } else {

                            subtreeStart = subtreeStart.append(endTag).append(elStart);
                        }
                        endTag = elEnd;
                    }
                    depth = currentDepth;
                }
                isRoot = false;
            }

        }
        StringBuilder restEndTags = new StringBuilder();
        for (String end : endTags) {
            restEndTags = restEndTags.append(end);
        }
        return subtreeStart.toString() + endTag + restEndTags + subtreeEnd.toString();
    }

    /**
     * Gets element names of a SchemaFile subtree
     *
     * @param fullPath Subtree root as a <code>String</code>
     * @return Subtree element names as a <code>ArrayList</code>
     */
    public ArrayList<Element> getElements(String fullPath) {

        try {
            boolean getFullTree = false;
            Handlers handlers = new Handlers(fullPath);
            String root = fullPath;
            if (fullPath.contains("/")) {
                root = fullPath.substring(0, fullPath.indexOf("/"));
            } else {
                getFullTree = true;
            }

            XSElementDecl element = null;
            if (this.parser.getResult().getSchemas().size() > 1) {
                element = this.parser.getResult().getSchema(1).getElementDecl(root);
            } else {
                element = this.parser.getResult().getSchema(0).getElementDecl(root);
            }
            Element rootElem = new Element(element, root, BigInteger.ONE, BigInteger.ONE);
            if (getFullTree) {
                handlers.elements.add(rootElem);
            }

            if (element.getType().isComplexType()) {
                handlers.handleAttributes(element.getType().asComplexType());
                handlers.handleParticles(element.getType().asComplexType(), root);
            } else {

                handlers.handleSimpleType(element.getType().asSimpleType());
            }

            ArrayList<String> list = new ArrayList<String>();
            ArrayList<Element> pureElements = new ArrayList<Element>();
            for (Element el : handlers.elements) {
                if (el != null) {
                    if (el.isDummy) {
                        list.add(el.info);
                    } else {
                        if (!list.isEmpty()) {
                            String listString = list.remove(list.size() - 1);

                            String type = listString.substring(listString.indexOf("_") + 1);
                            String index = listString.substring(0, listString.indexOf("_"));

                            int howManyLeft = Integer.parseInt(index);
                            howManyLeft = howManyLeft - 1;
                            if (howManyLeft > 0) {
                                list.add(howManyLeft + "_" + type);
                            }
                            el.setInfo(type);
                        }
                        pureElements.add(el);
                    }
                }
            }
            //  pureElements.remove(0);
            return pureElements;
        } catch (SAXException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
