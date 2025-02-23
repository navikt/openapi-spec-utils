package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonCreator;

public record NotEnumCodeValue(String txt, String codeValue) implements SomeInterface {
    @JsonCreator
    public static NotEnumCodeValue fromString(final String codeValue) {
        // Just make up some txt value
        return new NotEnumCodeValue("txt for "+codeValue, codeValue);
    }
}
