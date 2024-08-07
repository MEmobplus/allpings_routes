package org.heigit.ors.config.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnvDeserializerTest {

    private static Stream<Arguments> provideEnvPropertiesToJsonTestCases() {
        ObjectMapper mapper = new ObjectMapper();

        return Stream.of(Arguments.of(null, null), Arguments.of(Collections.emptyList(), mapper.createObjectNode()),
                // Single key-value
                Arguments.of(List.of(Map.entry("key1", "value1"), Map.entry("key2", "value2")), mapper.createObjectNode().put("key1", "value1").put("key2", "value2")), // Nested Map
                Arguments.of(List.of(Map.entry("nested.key1", "value1"), Map.entry("nested.key2", "value2")), mapper.createObjectNode().set("nested", mapper.createObjectNode().put("key1", "value1").put("key2", "value2"))), // Overwrite key
                Arguments.of(List.of(Map.entry("nested.key1", "value1"), Map.entry("nested.key2", "value2"), Map.entry("nested.key1", "value3")), mapper.createObjectNode().set("nested", mapper.createObjectNode().put("key1", "value3").put("key2", "value2"))), Arguments.of(List.of(Map.entry("nested.key1", "value1"), Map.entry("nested.key2", "value2"), Map.entry("nested.key3", "value3")), mapper.createObjectNode().set("nested", mapper.createObjectNode().put("key1", "value1").put("key2", "value2").put("key3", "value3"))), // Multiple nested maps
                Arguments.of(List.of(Map.entry("nested1.nested2.key1", "value1"), Map.entry("nested1.nested2.key2", "value2"), Map.entry("nested1.key3", "value3")), mapper.createObjectNode().set("nested1", mapper.createObjectNode().put("key3", "value3").set("nested2", mapper.createObjectNode().put("key1", "value1").put("key2", "value2")))));
    }

    @Test
    void testDoubleRootNestedObject() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode doubleNestedRoot = mapper.createObjectNode();

        ObjectNode nested2_1 = mapper.createObjectNode();
        nested2_1.put("key1", "value1");

        ObjectNode nested2 = mapper.createObjectNode();
        nested2.set("nested2", nested2_1);

        ObjectNode nested2_2 = mapper.createObjectNode();
        nested2_2.put("key1", "value5");
        nested2_2.put("key2", "value2");
        nested2_2.put("key4", "value4");

        ObjectNode nested1 = mapper.createObjectNode();
        nested1.put("key3", "value3");
        nested1.set("nested2", nested2_2);

        doubleNestedRoot.set("nested1", nested1);
        doubleNestedRoot.set("nested2", nested2);
        List<Map.Entry<String, String>> entry = List.of(
                Map.entry("nested2.nested2.key1", "value1"),
                Map.entry("nested1.nested2.key2", "value2"),
                Map.entry("nested1.key3", "value3"),
                Map.entry("nested1.nested2.key1", "value5"),
                Map.entry("nested1.nested2.key4", "value4")
        );
        assertEquals(doubleNestedRoot, EnvDeserializer.envPropertiesToJson(entry));
    }

    @ParameterizedTest
    @MethodSource("provideEnvPropertiesToJsonTestCases")
    void testCorrectEnvPropertiesToJson(List<Map.Entry<String, String>> envVars, ObjectNode expectedJson) {
        ObjectNode result = EnvDeserializer.envPropertiesToJson(envVars);
        assertEquals(expectedJson, result);
    }

}