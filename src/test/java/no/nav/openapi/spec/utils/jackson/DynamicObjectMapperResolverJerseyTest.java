package no.nav.openapi.spec.utils.jackson;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import no.nav.openapi.spec.utils.http.DynamicObjectMapperResolverVaryFilter;
import no.nav.openapi.spec.utils.jackson.dto.ObjectAnnotatedEnum;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that DynamicJacksonJsonProvider together with DynamicObjectMapperResolver selects JsonMapper per request
 * based on the {@link DynamicObjectMapperResolver#HEADER_KEY} header, running in Jersey.
 */
public class DynamicObjectMapperResolverJerseyTest {

    public record Dto(ObjectAnnotatedEnum e, String n) {}

    @Path("/test")
    public static class TestResource {
        @GET
        @Produces(MediaType.APPLICATION_JSON)
        public Dto get() {
            return new Dto(ObjectAnnotatedEnum.VALUE_A, null);
        }

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.TEXT_PLAIN)
        public String post(final ObjectAnnotatedEnum e) {
            return e.name();
        }
    }

    private final JerseyTest jersey = new JerseyTest() {
        @Override
        protected Application configure() {
            final var defaultMapper = JsonMapper.builderWithJackson2Defaults().build();
            final var resolver = new DynamicObjectMapperResolver(defaultMapper);
            resolver.addObjectMapper(DynamicObjectMapperResolver.JSON_SERIALIZER_OPENAPI, OpenapiCompatObjectMapperModifier.withDefaultModifications().modify(defaultMapper));
            return new ResourceConfig()
                    .register(TestResource.class)
                    .register(new DynamicJacksonJsonProvider())
                    .register(resolver)
                    .register(DynamicObjectMapperResolverVaryFilter.class);
        }
    };

    @BeforeEach
    void setUp() throws Exception {
        jersey.setUp();
    }

    @AfterEach
    void tearDown() throws Exception {
        jersey.tearDown();
    }

    private Response get(final String serializerOption) {
        final var req = jersey.target("test").request(MediaType.APPLICATION_JSON);
        return serializerOption != null ? req.header(DynamicObjectMapperResolver.HEADER_KEY, serializerOption).get() : req.get();
    }

    private String post(final String serializerOption, final String json) {
        final var req = jersey.target("test").request(MediaType.TEXT_PLAIN);
        if (serializerOption != null) {
            req.header(DynamicObjectMapperResolver.HEADER_KEY, serializerOption);
        }
        try (final var resp = req.post(Entity.entity(json, MediaType.APPLICATION_JSON))) {
            return resp.getStatus() + ":" + resp.readEntity(String.class);
        }
    }

    @Test
    public void serializesWithMapperSelectedByHeader() {
        // Alternate between options to verify that the resolved mapper is not cached.
        for (int i = 0; i < 2; i++) {
            try (final var resp = get(null)) {
                assertThat(resp.readEntity(String.class)).isEqualTo("{\"e\":{\"codeValue\":\"A\"},\"n\":null}");
                assertThat(resp.getHeaderString("Vary")).isEqualTo(DynamicObjectMapperResolver.HEADER_KEY);
            }
            try (final var resp = get(DynamicObjectMapperResolver.JSON_SERIALIZER_OPENAPI)) {
                assertThat(resp.readEntity(String.class)).isEqualTo("{\"e\":\"VALUE_A\"}");
            }
            try (final var resp = get("unknown-option")) {
                assertThat(resp.readEntity(String.class)).isEqualTo("{\"e\":{\"codeValue\":\"A\"},\"n\":null}");
            }
        }
    }

    @Test
    public void deserializesWithMapperSelectedByHeader() {
        for (int i = 0; i < 2; i++) {
            assertThat(post(null, "{\"codeValue\":\"B\"}")).isEqualTo("200:VALUE_B");
            assertThat(post(DynamicObjectMapperResolver.JSON_SERIALIZER_OPENAPI, "\"VALUE_B\"")).isEqualTo("200:VALUE_B");
        }
    }
}
