package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ObjectAnnotatedEnum implements SomeInterface {
    VALUE_A("A"),
    VALUE_B("B");

    private String codeValue;

    private ObjectAnnotatedEnum(final String codeValue) {
        this.codeValue = codeValue;
    }

    @JsonCreator
    public static ObjectAnnotatedEnum fromCodeValue(final String codeValue) {
        for(final var v: values()) {
            if(v.codeValue.equalsIgnoreCase(codeValue)) {
                return v;
            }
        }
        throw new RuntimeException(codeValue + " not a valid ObjectAnnotatedEnum value");
    }

    @JsonCreator
    public static ObjectAnnotatedEnum fromObjectProp(@JsonProperty("codeValue") final String codeValue) {
        return fromCodeValue(codeValue);
    }

    public String codeValue() {
        return codeValue;
    }
}
