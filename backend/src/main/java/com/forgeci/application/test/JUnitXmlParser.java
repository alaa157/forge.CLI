package com.forgeci.application.test;

import com.forgeci.domain.test.TestStatus;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class JUnitXmlParser {

    public List<JUnitTestCase> parse(InputStream input) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            f.setNamespaceAware(false);

            Document d = f.newDocumentBuilder().parse(input);
            List<JUnitTestCase> out = new ArrayList<>();
            NodeList cases = d.getElementsByTagName("testcase");
            for (int i = 0; i < cases.getLength(); i++) {
                Element e = (Element) cases.item(i);
                String className = e.getAttribute("classname");
                String name = e.getAttribute("name");
                String suite = suiteName(e, className);
                long ms = parseDuration(e.getAttribute("time"));

                TestStatus status = TestStatus.PASSED;
                String failure = null;
                String stdout = text(e, "system-out");
                String stderr = text(e, "system-err");
                if (e.getElementsByTagName("failure").getLength() > 0) {
                    status = TestStatus.FAILED;
                    failure = text(e, "failure");
                } else if (e.getElementsByTagName("error").getLength() > 0) {
                    status = TestStatus.ERROR;
                    failure = text(e, "error");
                } else if (e.getElementsByTagName("skipped").getLength() > 0) {
                    status = TestStatus.SKIPPED;
                }
                out.add(new JUnitTestCase(suite, className, name, ms, status, failure, stdout, stderr));
            }
            return List.copyOf(out);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid or unsafe JUnit XML", e);
        }
    }

    private String suiteName(Element testCase, String className) {
        Node parent = testCase.getParentNode();
        if (parent instanceof Element pe && "testsuite".equals(pe.getTagName())) {
            String name = pe.getAttribute("name");
            if (name != null && !name.isBlank()) {
                return name;
            }
        }
        return className == null ? "" : className;
    }

    private long parseDuration(String s) {
        try {
            return Math.max(0, Math.round(Double.parseDouble(s) * 1000));
        } catch (Exception e) {
            return 0;
        }
    }

    private String text(Element e, String tag) {
        NodeList n = e.getElementsByTagName(tag);
        return n.getLength() == 0 ? null : n.item(0).getTextContent();
    }
}
