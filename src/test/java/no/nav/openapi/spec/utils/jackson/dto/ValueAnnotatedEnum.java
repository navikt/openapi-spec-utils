package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ValueAnnotatedEnum implements SomeInterface {
    VALUE_A("A"),
    VALUE_B("B");

    private String codeValue;

    private ValueAnnotatedEnum(final String codeValue) {
        this.codeValue = codeValue;
    }

    @JsonValue
    public String codeValue() {
        return codeValue;
    }
}
