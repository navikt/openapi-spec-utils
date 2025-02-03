package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Kun for bruk i OpenapiGenerateTest
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
