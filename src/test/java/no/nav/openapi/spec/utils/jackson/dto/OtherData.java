package no.nav.openapi.spec.utils.jackson.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "description of other data, tests stackoveflow issue that was does not reappear")
public record OtherData(
        String txt1,
        int num1,
        Integer num2
) {
}
