package io.llmunit.context;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class TestParameters {

    private final Map<String, Object> values = new LinkedHashMap<>();

    public void put(String name, Object value) {
        values.put(name, value);
    }

    public Object get(String name) {
        return values.get(name);
    }

    public Optional<String> stringValue(String name) {
        return Optional.ofNullable(get(name)).map(Object::toString);
    }

    public String value(String name) {
        return stringValue(name).orElse("");
    }

    public Map<String, Object> asMap() {
        return Map.copyOf(values);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }
}