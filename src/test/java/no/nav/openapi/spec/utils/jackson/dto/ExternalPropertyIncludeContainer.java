package no.nav.openapi.spec.utils.jackson.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

public record ExternalPropertyIncludeContainer(
        // Used as discriminator for the included property
        DummyEnum includedDiscriminator,
        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "includedDiscriminator"
        )
        ExternalPropertyIncludeInterface included
) {
}
