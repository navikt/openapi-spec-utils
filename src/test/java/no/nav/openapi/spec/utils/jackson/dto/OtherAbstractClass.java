package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "typename")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OtherExtensionClassA.class, names = {"a", "a2"}),
        @JsonSubTypes.Type(value = OtherExtensionClassB.class, name = "b")
})
public abstract class OtherAbstractClass {
    public String infoOnSuperClass = "someInfoOnOtherAbstractClass";

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OtherAbstractClass that)) return false;
        return Objects.equals(infoOnSuperClass, that.infoOnSuperClass);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(infoOnSuperClass);
    }
}
