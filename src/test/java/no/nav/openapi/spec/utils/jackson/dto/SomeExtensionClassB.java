package no.nav.openapi.spec.utils.jackson.dto;

import java.util.Objects;

public class SomeExtensionClassB extends SomeAbstractClass {
    public String extensionB = "extensionB";

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SomeExtensionClassB that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(extensionB, that.extensionB);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), extensionB);
    }
}
