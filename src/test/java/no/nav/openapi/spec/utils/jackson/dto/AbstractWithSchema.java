package no.nav.openapi.spec.utils.jackson.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "this abstract class has a schema annotation")
public abstract class AbstractWithSchema {
    public String superWithSchema = "ok";
}
