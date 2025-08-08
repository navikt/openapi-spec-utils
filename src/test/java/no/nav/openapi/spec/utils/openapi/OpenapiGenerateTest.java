package no.nav.openapi.spec.utils.openapi;

import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.openapi.spec.utils.jackson.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class OpenapiGenerateTest {
    private static final String STRIP_TYPE_NAME_PREFIX = "no.nav.openapi.spec.utils.";

    private OpenAPI resolveDummyOpenApi(final boolean stripTypeNamePrefixes) throws OpenApiConfigurationException {
        final var setupHelper = new OpenApiSetupHelper(new DummyApplication(), new Info(), new Server());
        setupHelper.addResourcePackage("no.nav.openapi.spec.utils"); // Prevents scanner from including classes outside this package
        setupHelper.addResourceClass(DummyRestService.class.getCanonicalName());
        setupHelper.registerSubTypes(Set.of(SomeExtensionClassB.class, SomeExtensionClassA.class)); // ActualWithSchema.class deliberately not registered here.
        if(stripTypeNamePrefixes) {
            setupHelper.setTypeNameResolver(new PrefixStrippingFQNTypeNameResolver(STRIP_TYPE_NAME_PREFIX));
        }
        return setupHelper.resolveOpenAPI();
    }

    private String componentRef(final String typeName) {
        return "#/components/schemas/" + typeName;
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testOpenapiGenerate(final boolean stripTypeNamePrefixes) throws OpenApiConfigurationException {
        // Generer først openapi spesifikasjon basert på dummy klasser deklarert i denne test-pakke.
        final var openapi = resolveDummyOpenApi(stripTypeNamePrefixes);

        // Helper function that creates expected names dynamically based on useFqn argument
        final Function<Class<?>, String> makeName = cls -> stripTypeNamePrefixes ? cls.getName().substring(STRIP_TYPE_NAME_PREFIX.length()) : cls.getName();

        // Sjekk at generert spesifikasjon er som forventa
        assertThat(openapi).isNotNull();
        var schemas = openapi.getComponents().getSchemas();
        assertThat(schemas).containsKey(makeName.apply(DummyDto.class));
        final var dummyDto = schemas.get(makeName.apply(DummyDto.class));
        Map<String, Schema> properties = dummyDto.getProperties();
        assertThat(properties).containsKeys("nummerProperty", "tekstProperty", "enumProperty", "durationProperty", "yearMonthProperty");
        assertThat(properties.get("nummerProperty").getType()).isEqualTo("integer");
        assertThat(properties.get("tekstProperty").getType()).isEqualTo("string");
        // Sjekk at reskriving til å ha enums som refs fungerte
        final String expectedDummyEnumName = makeName.apply(DummyEnum.class);
        final String expectedDummyEnumRef = componentRef(expectedDummyEnumName);
        assertThat(properties.get("enumProperty").get$ref()).isEqualTo(expectedDummyEnumRef);
        assertThat(schemas).containsKey(expectedDummyEnumName);
        final var dummyEnum = schemas.get(expectedDummyEnumName);
        assertThat(dummyEnum).isNotNull();
        final var dummyEnumValues = dummyEnum.getEnum();
        assertThat(dummyEnumValues).containsExactlyInAnyOrderElementsOf(Arrays.stream(DummyEnum.values()).map(v -> v.enumVerdi).toList());
        final var dummyEnumNames = dummyEnum.getExtensions().get("x-enum-varnames");
        if(dummyEnumNames instanceof String[] names) {
            assertThat(names).containsExactlyElementsOf(Arrays.stream(DummyEnum.values()).map(v -> v.name()).toList());
        } else {
            fail();
        }
        assertThat(properties.get("durationProperty").getType()).isEqualTo("string");
        assertThat(properties.get("durationProperty").getFormat()).isEqualTo("duration");

        assertThat(properties.get("yearMonthProperty").getType()).isEqualTo("string");
        assertThat(properties.get("yearMonthProperty").getFormat()).isEqualTo("year-month");

        // Check that automatic resolving of subtypes has worked:
        assertThat(schemas).containsKeys(makeName.apply(SomeExtensionClassA.class), makeName.apply(SomeExtensionClassB.class));
        // Check that abstractClass property has is set to oneOf as desired
        assertThat(properties.get("abstractClass").get$ref()).isEqualTo(componentRef(makeName.apply(SomeAbstractClass.class)));
        {
            final var abstractClass = schemas.get(makeName.apply(SomeAbstractClass.class));
            final List<Schema> oneOf = abstractClass.getOneOf();
            final var refs = oneOf.stream().map(s -> s.get$ref()).toList();
            assertThat(refs).containsExactly(componentRef(makeName.apply(SomeExtensionClassA.class)), componentRef(makeName.apply(SomeExtensionClassB.class)));
        }
        {
            final var someExtensionClassA = schemas.get(makeName.apply(SomeExtensionClassA.class));
            assertThat(someExtensionClassA.getAllOf()).isNull();
        }
        // Check that otherAbstractClass has oneOf set as desired
        assertThat(properties.get("otherAbstractClass").get$ref()).isEqualTo(componentRef(makeName.apply(OtherAbstractClass.class)));
        {
            final var otherAbstractClass = schemas.get(makeName.apply(OtherAbstractClass.class));
            final List<Schema> oneOf = otherAbstractClass.getOneOf();
            final var refs = oneOf.stream().map(s -> s.get$ref()).toList();
            assertThat(refs).containsExactly(componentRef(makeName.apply(OtherExtensionClassA.class)), componentRef(makeName.apply(OtherExtensionClassB.class)));
        }
        {
            final var otherExtensionClassA = schemas.get(makeName.apply(OtherExtensionClassA.class));
            assertThat(otherExtensionClassA.getAllOf()).isNull();
        }
    }

    @Test
    public void testFileOutputter() throws IOException, OpenApiConfigurationException {
        final var resolvedDummyOpenApi = resolveDummyOpenApi(true);
        final var filePath = "/tmp/openapi-spec-utils-testfile.json";
        FileOutputter.writeJsonFile(resolvedDummyOpenApi, filePath);
        final var fileContent = Files.readString(Path.of(filePath));
        assertThat(fileContent.length()).isGreaterThan(400);
        assertThat(fileContent.length()).isLessThan(4000000);
        assertThat(fileContent).contains("nummerProperty");
    }

    @Test
    public void testApplicationPathResolve() throws OpenApiConfigurationException {
        final var openapi = resolveDummyOpenApi(false);
        final Paths paths = openapi.getPaths();
        assertThat(paths.keySet()).contains(DummyApplication.API_URI + "/testing/dummy");
    }
}
