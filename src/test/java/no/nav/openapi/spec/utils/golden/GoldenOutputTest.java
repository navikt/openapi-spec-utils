package no.nav.openapi.spec.utils.golden;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import no.nav.openapi.spec.utils.jackson.JsonParserPreProcessingDeserializerModifier;
import no.nav.openapi.spec.utils.jackson.ObjectToPropertyPreProcessor;
import no.nav.openapi.spec.utils.jackson.OpenapiCompatObjectMapperModifier;
import no.nav.openapi.spec.utils.jackson.dto.ExternalPropertyIncludeA;
import no.nav.openapi.spec.utils.jackson.dto.SomeExtensionClassA;
import no.nav.openapi.spec.utils.jackson.dto.SomeExtensionClassB;
import no.nav.openapi.spec.utils.jackson.dto.SomeInterface;
import no.nav.openapi.spec.utils.jackson.dto.ThirdExtensionClassA;
import no.nav.openapi.spec.utils.jackson.dto.ThirdExtensionClassB;
import no.nav.openapi.spec.utils.openapi.DummyApplication;
import no.nav.openapi.spec.utils.openapi.DummyRestService;
import no.nav.openapi.spec.utils.openapi.IntrospectionRestService;
import no.nav.openapi.spec.utils.openapi.OpenApiResource;
import no.nav.openapi.spec.utils.openapi.OpenApiSetupHelper;
import no.nav.openapi.spec.utils.openapi.PrefixStrippingFQNTypeNameResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashMap;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that serialization, deserialization and openapi generation produces the same output as when the golden files
 * were recorded. See {@link GoldenCases}.
 * <p>
 * Only the creation of the ObjectMappers in this class is Jackson version specific.
 */
public class GoldenOutputTest {

    /**
     * Base mapper, configured like the base mappers in k9-sak and fptilbake.
     */
    private static JsonMapper baseMapper() {
        return JsonMapper.builderWithJackson2Defaults()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .registerSubtypes(ThirdExtensionClassA.class, ThirdExtensionClassB.class, ExternalPropertyIncludeA.class)
                .build();
    }

    private static JsonMapper withPreProcessing(final JsonMapper om) {
        final var module = new SimpleModule("preprocessing");
        module.setDeserializerModifier(new JsonParserPreProcessingDeserializerModifier(new ObjectToPropertyPreProcessor(SomeInterface.class, "codeValue")));
        return om.rebuild().addModule(module).build();
    }

    private static ObjectMapper mapper(final String variant) {
        return switch (variant) {
            case "plain" -> baseMapper();
            case "openapi-compat" -> OpenapiCompatObjectMapperModifier.withDefaultModifications().modify(baseMapper());
            case "preprocess" -> withPreProcessing(baseMapper());
            case "openapi-compat-on-preprocess" -> OpenapiCompatObjectMapperModifier.withDefaultModifications().modify(withPreProcessing(baseMapper()));
            default -> throw new IllegalArgumentException(variant);
        };
    }

    @ParameterizedTest
    @ValueSource(strings = {"plain", "openapi-compat", "preprocess", "openapi-compat-on-preprocess"})
    public void serialization(final String variant) throws Exception {
        final var om = mapper(variant);
        final var results = new LinkedHashMap<String, String>();
        GoldenCases.serializationCases().forEach((name, value) -> {
            try {
                results.put(name, om.writeValueAsString(value));
            } catch (Exception e) {
                results.put(name, "ERROR");
            }
        });
        final String actual = GoldenCases.toLines(results);
        final String fileName = "serialize-" + variant + ".txt";
        assertThat(actual).isEqualTo(GoldenCases.recordOrReadGolden(fileName, actual));
    }

    @ParameterizedTest
    @ValueSource(strings = {"plain", "openapi-compat", "preprocess", "openapi-compat-on-preprocess"})
    public void deserialization(final String variant) throws Exception {
        final var om = mapper(variant);
        final var results = new LinkedHashMap<String, String>();
        GoldenCases.deserializationCases().forEach((name, c) -> {
            try {
                results.put(name, GoldenCases.describe(om.readValue(c.json(), c.type())));
            } catch (Exception e) {
                results.put(name, "ERROR");
            }
        });
        final String actual = GoldenCases.toLines(results);
        final String fileName = "deserialize-" + variant + ".txt";
        assertThat(actual).isEqualTo(GoldenCases.recordOrReadGolden(fileName, actual));
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void openapi(final boolean stripTypeNamePrefixes) throws Exception {
        final var setupHelper = new OpenApiSetupHelper(new DummyApplication(), new Info().title("golden").version("1"), new Server().url("/golden"));
        setupHelper.addResourcePackage("no.nav.openapi.spec.utils");
        setupHelper.addResourceClass(DummyRestService.class.getCanonicalName());
        setupHelper.registerSubTypes(Set.of(
                SomeExtensionClassB.class, SomeExtensionClassA.class,
                ThirdExtensionClassA.class, ThirdExtensionClassB.class,
                ExternalPropertyIncludeA.class
        ));
        if (stripTypeNamePrefixes) {
            setupHelper.setTypeNameResolver(new PrefixStrippingFQNTypeNameResolver("no.nav.openapi.spec.utils."));
        }
        final var resource = new OpenApiResource(setupHelper.resolveOpenAPI());
        final String suffix = stripTypeNamePrefixes ? "stripped" : "fqn";
        final String json = resource.openApiAsString(false);
        assertThat(json).isEqualTo(GoldenCases.recordOrReadGolden("openapi-" + suffix + ".json", json));
        final String yaml = resource.openApiAsString(true);
        assertThat(yaml).isEqualTo(GoldenCases.recordOrReadGolden("openapi-" + suffix + ".yaml", yaml));
    }

    /**
     * Requires compilation with -parameters, so that constructor parameter names are available to the bean introspection.
     */
    @Test
    public void openapiIntrospection() throws Exception {
        final var setupHelper = new OpenApiSetupHelper(new DummyApplication(), new Info().title("golden").version("1"), new Server().url("/golden"));
        setupHelper.addResourcePackage("no.nav.openapi.spec.utils");
        setupHelper.addResourceClass(IntrospectionRestService.class.getCanonicalName());
        setupHelper.setTypeNameResolver(new PrefixStrippingFQNTypeNameResolver("no.nav.openapi.spec.utils."));
        final String json = new OpenApiResource(setupHelper.resolveOpenAPI()).openApiAsString(false);
        assertThat(json).isEqualTo(GoldenCases.recordOrReadGolden("openapi-introspection.json", json));
    }
}
