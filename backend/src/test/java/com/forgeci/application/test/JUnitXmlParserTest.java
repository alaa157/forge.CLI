package com.forgeci.application.test;

import static org.junit.jupiter.api.Assertions.*;

import com.forgeci.domain.test.TestStatus;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class JUnitXmlParserTest {

    @Test
    void parsesStatusesDurationAndSuite() {
        String xml =
                """
                <testsuites>
                  <testsuite name="UserSuite">
                    <testcase classname="com.example.UserTest" name="createsUser" time="0.125"/>
                    <testcase classname="com.example.UserTest" name="rejects" time="0.2">
                      <failure message="boom">stack</failure>
                    </testcase>
                    <testcase classname="com.example.UserTest" name="skips"><skipped/></testcase>
                  </testsuite>
                </testsuites>
                """;
        var rows = new JUnitXmlParser()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        assertEquals(3, rows.size());
        assertEquals("UserSuite", rows.get(0).suite());
        assertEquals(125, rows.get(0).durationMs());
        assertEquals(TestStatus.FAILED, rows.get(1).status());
        assertEquals(TestStatus.SKIPPED, rows.get(2).status());
    }

    @Test
    void rejectsExternalEntities() {
        String xml =
                "<!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                        + "<testsuite><testcase classname=\"x\" name=\"&xxe;\"/></testsuite>";
        assertThrows(
                IllegalArgumentException.class,
                () -> new JUnitXmlParser()
                        .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))));
    }
}
