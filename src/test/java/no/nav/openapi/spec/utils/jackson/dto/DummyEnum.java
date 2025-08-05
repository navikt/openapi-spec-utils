package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Copy of enum in openapi package, so that we can get a name collision in test for PrefixStrippingFQNTypeNameResolver.
 */
public enum DummyEnum {
    DUMMY_V1("V1"),
    DUMMY_V2("V2"),
    ;

    @JsonValue
    public final String enumVerdi;

    DummyEnum(final String enumVerdi) {
        this.enumVerdi = enumVerdi;
    }
}
