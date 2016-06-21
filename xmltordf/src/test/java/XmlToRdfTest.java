/*
Copyright 2016 ACANDO AS

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

import no.acando.xmltordf.*;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.util.FileManager;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.openrdf.model.Namespace;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class XmlToRdfTest {

    static final String HELLO = "hello";
    static final String HTTP_A = "http://a/";
    static final String HTTP_TEST = "http://test/";
    static final String HTTP_A_NAME = "http://a/name";
    static final String ID = "id";
    static final String LL = "ll";
    static final String QQ = "qq";
    static final String ELEMENT_NAME = "name";
    static final String RDF_NODE_ID = org.apache.jena.vocabulary.RDF.uri + "nodeID";

    @Rule
    public ErrorCollector collector = new ErrorCollector();

    @Test
    public void simple() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());

    }

    @Test
    public void mixedContent() throws Exception {
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void simpleWithAttrs() throws Exception {

        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).build());

    }

    @Test
    public void simpleWithAttrsUUID() throws Exception {

        boolean[] errorOccured = {false};
        collector = new ErrorCollector() {
            @Override
            public void addError(Throwable error) {
                errorOccured[0] = true;
            }
        };

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .overrideNamespace(HTTP_TEST)
            .uuidBasedIdInsteadOfBlankNodes(true)
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .overrideNamespace(HTTP_TEST)
            .uuidBasedIdInsteadOfBlankNodes(true)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .overrideNamespace(HTTP_TEST)
            .uuidBasedIdInsteadOfBlankNodes(true)
            .build());

        assertTrue("This test just tests that the results are NOT isomorphic, which they will be when you don't use blank nodes anymore.", errorOccured[0]);

    }

    @Test
    public void inLineData() throws Exception {
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).build());

        //TODO: make new test for fast that doesn't do mixed content
//        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").build());

    }

    @Test
    public void specialCharacters() throws Exception {
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).build());

    }

    //TODO: actually create test data
    @Ignore
    @Test
    public void namespaces() throws Exception {
        testAdvancedStream(Builder
            .getAdvancedBuilderStream()
            .autoAttributeNamespace(false)
            .setBaseNamespace(HTTP_TEST, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedSesame(Builder
            .getAdvancedBuilderSesame()
            .autoAttributeNamespace(false)
            .setBaseNamespace(HTTP_TEST, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedJena(Builder
            .getAdvancedBuilderJena()
            .autoAttributeNamespace(false)
            .setBaseNamespace(HTTP_TEST, Builder.AppliesTo.bothElementsAndAttributes)
            .build());
    }

    @Test
    public void namespacesAutoSuffix() throws Exception {
        testAdvancedJena(Builder
            .getAdvancedBuilderJena()
            .build());

        testAdvancedSesame(Builder
            .getAdvancedBuilderSesame()
            .build());

        testAdvancedStream(Builder
            .getAdvancedBuilderStream()
            .build());
    }

    @Test
    public void namespacesAutoSuffix2() throws Exception {
        testAdvancedStream(Builder
            .getAdvancedBuilderStream()
            .autoAddSuffixToNamespace(false)
            .build());

        testAdvancedSesame(Builder
            .getAdvancedBuilderSesame()
            .autoAddSuffixToNamespace(false)
            .build());

        testAdvancedJena(Builder
            .getAdvancedBuilderJena()
            .autoAddSuffixToNamespace(false)
            .build());

    }

    @Test
    public void namespacesAutoSuffix3() throws Exception {
        testAdvancedJena(Builder
            .getAdvancedBuilderJena()
            .build());

        testAdvancedSesame(Builder
            .getAdvancedBuilderSesame()
            .build());

        testAdvancedStream(Builder
            .getAdvancedBuilderStream()
            .build());

    }

    @Test
    public void namespacesAutoSuffixSlash() throws Exception {
        testAdvancedStream(Builder
            .getAdvancedBuilderStream()
            .autoAddSuffixToNamespace("/")
            .build());

        testAdvancedSesame(Builder
            .getAdvancedBuilderSesame()
            .autoAddSuffixToNamespace("/")
            .build());

        testAdvancedJena(Builder
            .getAdvancedBuilderJena()
            .autoAddSuffixToNamespace("/")
            .build());
    }

    @Test
    public void checkIndexes() throws Exception {

        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).addIndex(true).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).addIndex(true).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).addIndex(true).build());

    }

    @Test
    public void checkIndexesFalse() throws Exception {

        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).addIndex(false).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).addIndex(false).build());
        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).addIndex(false).build());

    }

    @Test
    public void attributeForId() throws Exception {

        final String a = "A";
        final String b = "B";

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .addUseAttributeForId(HTTP_TEST + a, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .addUseAttributeForId(HTTP_TEST + b, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .addUseAttributeForId(HTTP_TEST + a, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .addUseAttributeForId(HTTP_TEST + b, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .addUseAttributeForId(HTTP_TEST + a, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .addUseAttributeForId(HTTP_TEST + b, HTTP_TEST + ID, (var) -> HTTP_TEST + var)
            .build());

    }

    @Test
    public void attributeForIdWithNs() throws Exception {

        final String a = "http://example.com/A";
        final String b = "http://example2.com/B";

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .addUseAttributeForId(a, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .addUseAttributeForId(b, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .addUseAttributeForId(a, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .addUseAttributeForId(b, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .addUseAttributeForId(a, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .addUseAttributeForId(b, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)
            .build());
    }

    @Test
    public void attributeForId2() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .addUseAttributeForId(null, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .addUseAttributeForId(null, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)

            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .addUseAttributeForId(null, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .addUseAttributeForId(null, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .addUseAttributeForId(null, "http://example.com/" + ID, (var) -> HTTP_TEST + var)
            .addUseAttributeForId(null, "http://example2.com/" + ID, (var) -> HTTP_TEST + var)
            .build());
    }

    @Test
    public void classMapping() throws Exception {


        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(HTTP_TEST + "B", "http://hurra/B2")
            .build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(HTTP_TEST + "B", "http://hurra/B2")
            .build());
        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(HTTP_TEST + "B", "http://hurra/B2")
            .build());

        testFast(Builder.getFastBuilder()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(HTTP_TEST + "B", "http://hurra/B2")
            .build());

    }

    @Test
    public void classMappingWithFunction() throws Exception {


        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(null, (u, v) -> u + v.toLowerCase())
            .renameElement(HTTP_TEST + "name", (u, v) -> u + v.toUpperCase())
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(null, (u, v) -> u + v.toLowerCase())
            .renameElement(HTTP_TEST + "name", (u, v) -> u + v.toUpperCase())
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(null, (u, v) -> u + v.toLowerCase())
            .renameElement(HTTP_TEST + "name", (u, v) -> u + v.toUpperCase())
            .build());

        testFast(Builder.getFastBuilder()
            .overrideNamespace(HTTP_TEST)
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .renameElement(HTTP_TEST + "A", "http://hurra/A2")
            .renameElement(null, (u, v) -> u + v.toLowerCase())
            .renameElement(HTTP_TEST + "name", (u, v) -> u + v.toUpperCase())
            .build());


    }

    @Test
    public void detectLiteralProperties() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void detectLiteralPropertiesWithAttributes() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void detectLiteralPropertiesWithAttributesEdgeCaseWithRdfType() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void detectLiteralPropertiesEdgeCaseWithoutParent() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    //TODO: make test for unmixed content
    @Test
    public void detectLiteralPropertiesEdgeCaseWithoutParent2() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

//        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void detectLiteralPropertiesWithAttributesEdgeCaseWithoutParent() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void tesAautoDetectLiteralPropertiesSetToFalse() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.expand).build());

    }

    @Test
    public void withEmptyElements() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void withSpecialAmp() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST).simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace("http://test/").simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void attrValueTransform() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(null, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(null, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(null, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(null, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void attrValueTransform2() throws Exception {

        final String hurrah = HTTP_TEST + "hurra";

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void attrValueTransform3() throws Exception {

        final String hurrah = HTTP_TEST + "hurra";

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(null, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(null, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(null, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(null, hurrah, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void attrValueTransform4() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void attrValueTransform5() throws Exception {

        final String test = HTTP_TEST + "test";
        final String lala = "lala";
        final String dada = "dada";

        testAdvancedStream(Builder.getAdvancedBuilderStream().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .addTransformationForAttributeValue(null, test, (val) -> val.replaceAll(lala, dada))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .addTransformationForAttributeValue(null, test, (val) -> val.replaceAll(lala, dada))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .addTransformationForAttributeValue(null, test, (val) -> val.replaceAll(lala, dada))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

        testFast(Builder.getFastBuilder().overrideNamespace(HTTP_TEST)
            .addTransformationForAttributeValue(HTTP_TEST + ELEMENT_NAME, null, (val) -> val.replaceAll(LL, QQ))
            .addTransformationForAttributeValue(null, test, (val) -> val.replaceAll(lala, dada))
            .simpleTypePolicy(SimpleTypePolicy.compact).build());

    }

    @Test
    public void specialShallowHandling() throws Exception {

        final String ø = "ø";
        final String æ = "æ";


        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .addTransformationForAttributeValue(null, RDF_NODE_ID,
                (val) -> val.replaceAll("%C3%B8", ø).replaceAll("%C3%A6", æ))
            .addUseAttributeForId(null, RDF_NODE_ID, (val) -> "http://example.com/" + val)
            .build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .addTransformationForAttributeValue(null, RDF_NODE_ID,
                (val) -> val.replaceAll("%C3%B8", ø).replaceAll("%C3%A6", æ))
            .addUseAttributeForId(null, RDF_NODE_ID, (val) -> "http://example.com/" + val)
            .build());
        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .addTransformationForAttributeValue(null, RDF_NODE_ID,
                (val) -> val.replaceAll("%C3%B8", ø).replaceAll("%C3%A6", æ))
            .addUseAttributeForId(null, RDF_NODE_ID, (val) -> "http://example.com/" + val)
            .build());
    }

    @Test
    public void skosFromLexaurus() throws Exception {
        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .addUseAttributeForId(null, RDF_NODE_ID, (var) -> HTTP_TEST + var)
            .convertComplexElementsWithOnlyAttributesToPredicates(true).build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .addUseAttributeForId(null, RDF_NODE_ID, (var) -> HTTP_TEST + var)
            .convertComplexElementsWithOnlyAttributesToPredicates(true).build());
        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .addUseAttributeForId(null, RDF_NODE_ID, (var) -> HTTP_TEST + var)
            .convertComplexElementsWithOnlyAttributesToPredicates(true).build());
    }

    @Test
    public void convertShallowElementsToPropertiesWithAutoDetectLiteralProperties() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());
        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());
    }


    @Test
    public void insertPropertyBetween() throws Exception {

        final String hasB = "http://a/hasB";
        final String a = "http://a/A";
        final String aB = "http://a/B";

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate(hasB).between(a, aB)
            .build());
        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate(hasB).between(a, aB)
            .build());
        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate(hasB).between(a, aB)
            .build());

    }

    @Test
    public void insertPropertyBetweenWildcard() throws Exception {

        final String hasB = "http://example.org/hasB";
        final String hasC = "http://example.org/hasC";
        final String hasD = "http://example.org/hasD";

        final String A = "http://example.org/A";
        final String B = "http://example.org/B";
        final String C = "http://example.org/C";

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .insertPredicate(hasB).betweenAnyParentAndSpecificChild(B)
            .insertPredicate(hasC).betweenSpecificParentAndAnyChild(A)
            .insertPredicate(hasD).betweenAny()
            .build());


//        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
//            .convertComplexElementsWithOnlyAttributesToPredicates(true)
//            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
//            .insertPredicate(hasB).between(a, aB)
//            .build());
//        testAdvancedJena(Builder.getAdvancedBuilderJena()
//            .convertComplexElementsWithOnlyAttributesToPredicates(true)
//            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
//            .insertPredicate(hasB).between(a, aB)
//            .build());

    }

    @Test(expected = RuntimeException.class)
    public void insertPropertyBetween2() throws Exception {

        Builder.getAdvancedBuilderJena()
            .insertPredicate("http://a/hasB").between("http://a/A", "http://a/B")
            .insertPredicate("http://a/hasC").between("http://a/A", "http://a/B");

    }

    @Test
    public void invertPropertyBetween() throws Exception {

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .invertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .invertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .insertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .invertPredicate("http://a/belongsToA").between("http://a/A", "http://a/B")
            .build());

    }

    @Test
    public void longLiteral() throws ParserConfigurationException, SAXException, IOException {

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .overrideNamespace("http://example.org")
            .build());

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .overrideNamespace("http://example.org")
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .overrideNamespace("http://example.org")
            .build());

        testFast(Builder.getFastBuilder()
            .overrideNamespace("http://example.org")
            .build());
    }

    @Test
    public void specifyDatatype() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XMLSchema.INTEGER)
            .setDatatype("http://a/date", XMLSchema.DATE)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XSDDatatype.XSDinteger)
            .setDatatype("http://a/date", XSDDatatype.XSDdate)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XMLSchema.INTEGER.toString())
            .setDatatype("http://a/date", XMLSchema.DATE.toString())
            .build());

    }

    @Test
    public void specifyDatatype2() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(false)
            .convertComplexElementsWithOnlyAttributesToPredicates(false)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XMLSchema.INTEGER)
            .setDatatype("http://a/date", XMLSchema.DATE)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(false)
            .convertComplexElementsWithOnlyAttributesToPredicates(false)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XSDDatatype.XSDinteger)
            .setDatatype("http://a/date", XSDDatatype.XSDdate)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .convertComplexElementsWithOnlyAttributesAndSimpleTypeChildrenToPredicate(false)
            .convertComplexElementsWithOnlyAttributesToPredicates(false)
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setDatatype("http://a/num", XMLSchema.INTEGER.toString())
            .setDatatype("http://a/date", XMLSchema.DATE.toString())
            .build());

    }

    @Test
    public void literalOnPropertyTests() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, SimpleValueFactory.getInstance().createIRI(HTTP_TEST))
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, NodeFactory.createURI(HTTP_TEST))
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, HTTP_TEST)
            .build());

    }

    @Test
    public void literalOnPropertyShallowTests() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, SimpleValueFactory.getInstance().createIRI(HTTP_TEST))
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, NodeFactory.createURI(HTTP_TEST))
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.expand)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, HTTP_TEST)
            .convertComplexElementsWithOnlyAttributesToPredicates(true)
            .build());

    }

    @Test
    public void literalOnPropertyTestWithAutoDetectLiteral() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, SimpleValueFactory.getInstance().createIRI(HTTP_TEST))
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, NodeFactory.createURI(HTTP_TEST))
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, HTTP_TEST)
            .build());

    }

    @Test
    public void literalOnPropertyNullPointer() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, SimpleValueFactory.getInstance().createIRI(HTTP_TEST))
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, NodeFactory.createURI(HTTP_TEST))
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .mapTextInElementToUri(HTTP_A_NAME, HELLO, HTTP_TEST)
            .build());

    }

    @Test
    public void qnameInAttributeValue() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .resolveAsQnameInAttributeValue(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .resolveAsQnameInAttributeValue(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .resolveAsQnameInAttributeValue(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

    }


    @Test
    public void xsiTypeSupport() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .xsiTypeSupport(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .xsiTypeSupport(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .simpleTypePolicy(SimpleTypePolicy.compact)
            .xsiTypeSupport(true)
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .build());

    }

    @Test
    public void complexTransformStart() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .addComplexElementTransformAtStartOfElement("http://example.org/B", element -> {
                if(element.hasChild.size() == 0){
                    element.type = "http://example.org/HELLO";

                }            })
            .build());


        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .addComplexElementTransformAtStartOfElement("http://example.org/B", element -> {
                if(element.hasChild.size() == 0){
                    element.type = "http://example.org/HELLO";

                }            })
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .addComplexElementTransformAtStartOfElement("http://example.org/B", element -> {
                if(element.hasChild.size() == 0){
                    element.type = "http://example.org/HELLO";

                }            })
            .build());

    }

    @Test
    public void complexTransformEnd() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .addComplexElementTransformAtEndOfElement("http://example.org/B", element -> {
                if (element.hasChild.size() > 0) {
                    element.type = "http://example.org/HELLO";

                }
            })
            .build());


        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .addComplexElementTransformAtEndOfElement("http://example.org/B", element -> {
                if (element.hasChild.size() > 0) {
                    element.type = "http://example.org/HELLO";

                }
            })
            .build());

        testAdvancedStream(Builder.getAdvancedBuilderStream()
            .addComplexElementTransformAtEndOfElement("http://example.org/B", element -> {
                if (element.hasChild.size() > 0) {
                    element.type = "http://example.org/HELLO";

                }
            })
            .build());

    }


    @Test
    public void setQueueSize() throws Exception {

        testAdvancedSesame(Builder.getAdvancedBuilderSesame()
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setBuffer(1)
            .build());

        testAdvancedJena(Builder.getAdvancedBuilderJena()
            .setBaseNamespace(HTTP_A, Builder.AppliesTo.bothElementsAndAttributes)
            .setBuffer(1)
            .build());

    }

    private void testAdvancedJena(XmlToRdfAdvancedJena build) throws IOException, ParserConfigurationException, SAXException {
        TestFiles testFiles = getTestFiles();

        Model actualModelJena = build.convertToDataset(new FileInputStream(testFiles.xml)).getDefaultModel();

        Model expectedModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.expected.getCanonicalPath());

        if (!expectedModel.isIsomorphicWith(actualModelJena)) {
            try {
                assertEquals("Not isomorphic for object method with jena dataset.", modelToString(expectedModel), modelToString(actualModelJena));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }

    }

    private void testFast(XmlToRdfFast build) throws IOException, ParserConfigurationException, SAXException {

        TestFiles testFiles = getTestFiles();

        FileOutputStream out = new FileOutputStream(testFiles.path + "/actualFast.ttl");
        build.convertToStream(new FileInputStream(testFiles.xml), out);
        out.close();
        Model actualModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.path + "/actualFast.ttl");

        Model expectedModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.expected.getCanonicalPath());
        boolean isomorphicWith = expectedModel.isIsomorphicWith(actualModel);

        if (!isomorphicWith) {

            try {
                assertEquals("Not isomorphic for fast method.", modelToString(expectedModel), modelToString(actualModel));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }

    }

    private void testAdvancedStream(XmlToRdfAdvancedStream build) throws IOException, ParserConfigurationException, SAXException {

        TestFiles testFiles = getTestFiles();

        build.convertToStream(new FileInputStream(testFiles.xml), new FileOutputStream(testFiles.path + "/actualObject.n3"));

        Model actualModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.path + "/actualObject.n3");

        Model expectedModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.expected.getCanonicalPath());

        if (!expectedModel.isIsomorphicWith(actualModel)) {
            try {
                assertEquals("Not isomorphic for object method.", modelToString(expectedModel), modelToString(actualModel));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }

    }

    private void testAdvancedSesame(XmlToRdfAdvancedSesame build) throws IOException, ParserConfigurationException, SAXException {
        TestFiles testFiles = getTestFiles();

        Repository repository = build.convertToRepository(new FileInputStream(testFiles.xml));

        String rdf = repositoryToString(repository, RDFFormat.JSONLD);
        Model actualModelSesame = ModelFactory.createDefaultModel().read(new ByteArrayInputStream(rdf.getBytes()), "", RDFLanguages.strLangJSONLD);

        Model expectedModel = FileManager.get().readModel(ModelFactory.createDefaultModel(), testFiles.expected.getCanonicalPath());

        if (!expectedModel.isIsomorphicWith(actualModelSesame)) {
            try {
                assertEquals("Not isomorphic for object method with sesame repository.", modelToString(expectedModel), modelToString(actualModelSesame));

            } catch (AssertionError error) {
                collector.addError(error);
            }
        }
    }

    private class TestFiles {
        String path;
        File xml;
        File expected;

        public TestFiles(String path, File xml, File expected) {
            this.path = path;
            this.xml = xml;
            this.expected = expected;
        }
    }

    private TestFiles getTestFiles() throws IOException {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String methodName = stackTrace[3].getMethodName();
        String path = "testFiles/" + methodName;
        File pathDir = new File(path);
        if (!pathDir.exists()) {
            pathDir.mkdir();
        }

        File xml = new File(path + "/input.xml");
        File expected = new File(path + "/expected.ttl");

        if (!xml.exists()) {
            xml.createNewFile();
        }

        if (!expected.exists()) {
            expected.createNewFile();
        }

        return new TestFiles(path, xml, expected);

    }

    private String modelToString(Model createdModel) {

        createdModel.setNsPrefix("xmlToRdf", "http://acandonorway.github.com/XmlToRdf/ontology.ttl#");
        createdModel.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        createdModel.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");

        StringWriter stringWriter = new StringWriter();
        createdModel.write(stringWriter, "TTL");
        return stringWriter.toString();
    }

    private static String repositoryToString(Repository repository, RDFFormat format) {

        StringWriter stringWriter = new StringWriter();
        RDFWriter writer = Rio.createWriter(format, stringWriter);

        writer.startRDF();
        try (RepositoryConnection connection = repository.getConnection()) {
            RepositoryResult<Namespace> namespaces = connection.getNamespaces();

            while (namespaces.hasNext()) {
                Namespace next = namespaces.next();
                writer.handleNamespace(next.getPrefix(), next.getName());
            }

            RepositoryResult<Statement> statements = connection.getStatements(null, null, null);
            while (statements.hasNext()) {
                writer.handleStatement(statements.next());
            }
        }
        writer.endRDF();

        return stringWriter.toString();
    }
}