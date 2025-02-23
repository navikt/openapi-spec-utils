package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface SomeInterface {
    @JsonProperty
    public String codeValue();
}
