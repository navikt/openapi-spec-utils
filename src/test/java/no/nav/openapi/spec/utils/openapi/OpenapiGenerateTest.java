package no.nav.openapi.spec.utils.openapi;

import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class OpenapiGenerateTest {

    private OpenAPI resolveDummyOpenApi() throws OpenApiConfigurationException {
        final var setupHelper = new OpenApiSetupHelper(new DummyApplication(), new Info(), new Server());
        setupHelper.addResourceClass(DummyRestService.class.getCanonicalName());
        setupHelper.addResourceClass(DummyApplication.class.getCanonicalName());
        return setupHelper.resolveOpenAPI();
    }

    @Test
    public void testOpenapiGenerate() throws OpenApiConfigurationException {
        // Generer først openapi spesifikasjon basert på dummy klasser deklarert i denne test-pakke.
        final var openapi = resolveDummyOpenApi();

        // Sjekk at generert spesifikasjon er som forventa
        assertThat(openapi).isNotNull();
        var schemas = openapi.getComponents().getSchemas();
        assertThat(schemas).containsKey("DummyDto");
        final var dummyDto = schemas.get("DummyDto");
        Map<String, Schema> properties = dummyDto.getProperties();
        assertThat(properties).containsKeys("nummerProperty", "tekstProperty", "enumProperty", "durationProperty", "yearMonthProperty");
        assertThat(properties.get("nummerProperty").getType()).isEqualTo("integer");
        assertThat(properties.get("tekstProperty").getType()).isEqualTo("string");
        // Sjekk at reskriving til å ha enums som refs fungerte
        final String expectedDummyEnumName = "DummyDtoEnumProperty";
        final String expectedDummyEnumRef = "#/components/schemas/" + expectedDummyEnumName;
        assertThat(properties.get("enumProperty").get$ref()).isEqualTo(expectedDummyEnumRef);
        assertThat(schemas).containsKey(expectedDummyEnumName);
        final var dummyEnum = schemas.get(expectedDummyEnumName);
        assertThat(dummyEnum).isNotNull();
        final var dummyEnumValues = dummyEnum.getEnum();
        assertThat(dummyEnumValues).containsExactlyInAnyOrderElementsOf(Arrays.stream(DummyEnum.values()).map(v -> v.enumVerdi).toList());
        final var dummyEnumNames = dummyEnum.getExtensions().get("x-enum-varnames");
        if(dummyEnumNames instanceof List) {
            final List<String> names = (List<String>) dummyEnumNames;
            assertThat(names).containsExactlyInAnyOrderElementsOf(Arrays.stream(DummyEnum.values()).map(v -> v.name()).toList());
        } else {
            fail();
        }
        assertThat(properties.get("durationProperty").getType()).isEqualTo("string");
        assertThat(properties.get("durationProperty").getFormat()).isEqualTo("duration");

        assertThat(properties.get("yearMonthProperty").getType()).isEqualTo("string");
        assertThat(properties.get("yearMonthProperty").getFormat()).isEqualTo("year-month");
    }

    @Test
    public void testApplicationPathResolve() throws OpenApiConfigurationException {
        final var openapi = resolveDummyOpenApi();
        final Paths paths = openapi.getPaths();
        assertThat(paths.keySet()).contains(DummyApplication.API_URI + "/testing/dummy");
    }
}
