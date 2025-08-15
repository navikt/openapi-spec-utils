package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Objects;

@JsonTypeName(value = ThirdExtensionClassA.KODE_A)
public class ThirdExtensionClassA extends ThirdSuperClass {
    public static final String KODE_A = "KODE_A";
    public String propertyA = "AAA";


    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ThirdExtensionClassA that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(propertyA, that.propertyA);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), propertyA);
    }
}
