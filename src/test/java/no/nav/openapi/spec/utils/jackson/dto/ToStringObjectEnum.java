package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Same pattern as Kodeverdi enums in k9-sak: Serialized as object by default, toString() returns the kode, which is
 * what should be used in the openapi compatible format.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ToStringObjectEnum implements SomeInterface {
    VALUE_A("KODE-A", "Value A"),
    VALUE_B("KODE-B", "Value B");

    @JsonProperty("codeValue")
    private final String codeValue;

    @JsonProperty("navn")
    private final String navn;

    ToStringObjectEnum(final String codeValue, final String navn) {
        this.codeValue = codeValue;
        this.navn = navn;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ToStringObjectEnum fraKode(final Object node) {
        final String kode;
        if (node instanceof String s) {
            kode = s;
        } else if (node instanceof java.util.Map<?, ?> m) {
            kode = String.valueOf(m.get("codeValue"));
        } else {
            throw new IllegalArgumentException("Ugyldig input: " + node);
        }
        for (final var v : values()) {
            if (v.codeValue.equals(kode)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Ukjent kode: " + kode);
    }

    @Override
    public String codeValue() {
        return codeValue;
    }

    public String getNavn() {
        return navn;
    }

    @Override
    public String toString() {
        return codeValue;
    }
}
