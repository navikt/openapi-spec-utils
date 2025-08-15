package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName(ThirdExtensionClassB.KODE_B)
public class ThirdExtensionClassB extends ThirdSuperClass {
    public static final String KODE_B = "KODE_B";
    public String propertyB = "BBB";

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ThirdExtensionClassB that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(propertyB, that.propertyB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertyB);
    }
}
