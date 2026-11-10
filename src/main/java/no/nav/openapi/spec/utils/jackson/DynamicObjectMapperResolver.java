package no.nav.openapi.spec.utils.jackson;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;
import tools.jackson.databind.json.JsonMapper;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves which JsonMapper to use based on the value of the {@link #HEADER_KEY} request header.
 * <p>
 * Jackson 3 JacksonJsonProvider looks up a ContextResolver for JsonMapper, so this resolves JsonMapper instances.
 * Use together with {@link DynamicJacksonJsonProvider} so that the resolved mapper is not cached.
 */
@Provider
@Produces({ MediaType.APPLICATION_JSON })
public class DynamicObjectMapperResolver implements ContextResolver<JsonMapper> {
    // The request header value specifying which json serializer to use has this key:
    public static String HEADER_KEY = "X-Json-Serializer-Option";
    // Recommended header value to use for the serializer compatible with generated openapi spec:
    public static String JSON_SERIALIZER_OPENAPI = "openapi-compat";

    @Context
    private HttpHeaders headers;

    private final Map<String, JsonMapper> objectMappers = new HashMap<>();
    private final JsonMapper defaultObjectMapper;


    public DynamicObjectMapperResolver(final JsonMapper defaultObjectMapper) {
        this.defaultObjectMapper = Objects.requireNonNull(defaultObjectMapper);
    }

    public void addObjectMapper(final String name, final JsonMapper mapper) {
        this.objectMappers.put(name, Objects.requireNonNull(mapper));
    }

    /**
     * JsonMapper is immutable, so the instance itself can safely be shared. Use {@link JsonMapper#rebuild()} to make
     * a modified version.
     */
    public JsonMapper getDefaultObjectMapper() {
        return this.defaultObjectMapper;
    }

    public Optional<JsonMapper> getObjectMapper(final String serializerOption) {
        return Optional.ofNullable(this.objectMappers.get(serializerOption));
    }

    private String getJsonSerializerOptionHeaderValue() {
        final var headerValues = headers.getRequestHeader(HEADER_KEY);
        if(headerValues != null) {
            final var firstValue = headerValues.getFirst();
            if(firstValue != null) {
                return firstValue;
            }
        }
        return "";
    }

    /**
     * Denne metode er her slik at brukerkode kan overskrive den for å overstyre kva mapper som blir returnert i spesialtilfeller
     * basert på både serializerOption og type.
     * <p>Behov i k9-sak. (Søknad.class har spesiell ObjectMapper)</p>
     */
    protected JsonMapper resolveMapper(final Class<?> type, final String serializerOption) {
        final JsonMapper foundMapper = this.objectMappers.get(serializerOption);
        if(foundMapper != null) {
            return foundMapper;
        }
        return this.defaultObjectMapper;
    }

    @Override
    public JsonMapper getContext(Class<?> type) {
        final String serializerOption = this.getJsonSerializerOptionHeaderValue();
        return this.resolveMapper(type, serializerOption);
    }
}
