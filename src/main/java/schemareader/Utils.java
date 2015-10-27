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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Method containing useful methods
 * @author samarita
 */
public class Utils {

    /**
     * Finds matches given a regular expression
     * @param regexp Regular expression as a <code>String</code>
     * @param text Content to search as a <code>String</code>
     * @param flags Pattern flags as a <code>int</code>
     * @return Matches found as a <code>ArrayList</code>
     */
    public ArrayList<String> findReg(String regexp, String text, int flags) {

        ArrayList<String> results = new ArrayList();
        Pattern pattern;
        try {
            pattern = Pattern.compile(regexp, flags);
            Matcher matcher = pattern.matcher(text);
            boolean found = false;


            while ((found = matcher.find())) {
                results.add(matcher.group());
            }
        } catch (PatternSyntaxException ex) {
            System.out.println(ex.getDescription());
            System.out.println(ex.getMessage());
        }

        return results;

    }
}
