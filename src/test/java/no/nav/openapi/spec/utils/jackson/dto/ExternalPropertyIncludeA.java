package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(DummyEnum.V1)
public record ExternalPropertyIncludeA(String tst) implements ExternalPropertyIncludeInterface {
}
