package no.nav.openapi.spec.utils.jackson.dto;

import java.util.Objects;

public class OtherExtensionClassB extends OtherAbstractClass {
    public String otherExtensionB = "otherExtensionB";

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OtherExtensionClassB that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(otherExtensionB, that.otherExtensionB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), otherExtensionB);
    }
}
