package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.OptBoolean;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "cls", include = JsonTypeInfo.As.PROPERTY, visible = true, requireTypeIdForSubtypes = OptBoolean.TRUE)
// Because
//@JsonSubTypes({@JsonSubTypes.Type(SomeExtensionClassA.class), @JsonSubTypes.Type(SomeExtensionClassB.class)})
public abstract class SomeAbstractClass {
    public String infoOnSuperClass = "someInfoOnAbstractSuperClass";
}
