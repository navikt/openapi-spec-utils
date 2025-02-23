package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

// This will by default serialize to/from enum name string
public enum UnAnnotatedEnum implements SomeInterface {
    VALUE_A("A"),
    VALUE_B("B");

    private String codeValue;

    private UnAnnotatedEnum(final String codeValue) {
        this.codeValue = codeValue;
    }

    public String codeValue() {
        return codeValue;
    }

    // Adds JsonCreator to support deserialization from both name and codeValue.
    @JsonCreator
    public static UnAnnotatedEnum fromString(final String str) {
        // Resolve enum from name or codeValue, so that deserialization works in both cases.
        try {
            return UnAnnotatedEnum.valueOf(str);
        } catch (IllegalArgumentException e) {
            // Expected exception when we need to try deserializing from codeValue instead
            for(final var v: values()) {
                if(v.codeValue.equalsIgnoreCase(str)) {
                    return v;
                }
            }
            // No match from codeValue either
            throw new IllegalArgumentException("could not resolve UnAnnotatedEnum from string \"" + str + "\" by name or codeValue.", e);
        }
    }
}
