package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY)
public abstract class ThirdSuperClass {
    public String superInfo = "superInfo";

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ThirdSuperClass that)) return false;
        return Objects.equals(superInfo, that.superInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(superInfo);
    }
}
