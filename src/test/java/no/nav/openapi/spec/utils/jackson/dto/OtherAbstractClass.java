package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "typename")
@JsonSubTypes({
        @JsonSubTypes.Type(value = OtherExtensionClassA.class, name = "a"),
        @JsonSubTypes.Type(value = OtherExtensionClassB.class, name = "b")
})
public abstract class OtherAbstractClass {
    public String infoOnSuperClass = "someInfoOnOtherAbstractClass";
}
