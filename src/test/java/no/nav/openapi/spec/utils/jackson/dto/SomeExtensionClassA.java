package no.nav.openapi.spec.utils.jackson.dto;

import java.util.Objects;

public class SomeExtensionClassA extends SomeAbstractClass {
    public String extensionA = "extensionA";

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SomeExtensionClassA that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(extensionA, that.extensionA);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), extensionA);
    }
}
