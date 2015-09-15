Copyright 2012-2015 Institute of Computer Science,
Foundation for Research and Technology - Hellas

Licensed under the EUPL, Version 1.1 or - as soon they will be approved
by the European Commission - subsequent versions of the EUPL (the "Licence");
You may not use this work except in compliance with the Licence.
You may obtain a copy of the Licence at:

http://ec.europa.eu/idabc/eupl

Unless required by applicable law or agreed to in writing, software distributed
under the Licence is distributed on an "AS IS" basis,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the Licence for the specific language governing permissions and limitations
under the Licence.

Contact:  POBox 1385, Heraklio Crete, GR-700 13 GREECE
Tel:+30-2810-391632
Fax: +30-2810-391638
E-mail: isl@ics.forth.gr
http://www.ics.forth.gr/isl

Authors : Georgios Samaritakis, Konstantina Konsolaki.

This file is part of the SchemaReader project.

SchemaReader
============

SchemaReader is a simple Java API to parse xsd schema files. It uses [java.net XSOM](https://xsom.java.net/ "java.net XSOM") API, but tries to keep things
as simple as possible. It supports most xsd structures and styles, but not all of them, so feel free to expand it.

## Build - Run
Folders src and lib contain all the files needed to build and create a jar file.

## Usage
The SchemaReader dependecies and licenses used are described in file SchemaReader-Dependencies-LicensesUsed.txt 

Basic usage is as simple as it gets:
```java
SchemaFile schema = new SchemaFile('xsd file path');
//Gets all element names
schema.getElements();
//Validates xml
schema.validate('xml content to validate','validation message language');
//Creates xml template
schema.createXMLSubtree('root element xpath', "mode (minimum, medium or maximum");
```

See javadoc for more details.



