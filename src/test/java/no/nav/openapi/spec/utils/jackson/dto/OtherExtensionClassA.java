package no.nav.openapi.spec.utils.jackson.dto;

import java.util.Objects;

public class OtherExtensionClassA extends OtherAbstractClass {
    public String otherExtensionA = "otherExtensionA";

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OtherExtensionClassA that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(otherExtensionA, that.otherExtensionA);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), otherExtensionA);
    }
}
