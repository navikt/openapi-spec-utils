package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Provider
@Produces({ MediaType.APPLICATION_JSON })
public class DynamicObjectMapperResolver  implements ContextResolver<ObjectMapper> {
    // The request header value specifying which json serializer to use has this key:
    public static String HEADER_KEY = "X-Json-Serializer-Option";
    // Recommended header value to use for the serializer compatible with generated openapi spec:
    public static String JSON_SERIALIZER_OPENAPI = "openapi-compat";

    @Context
    private HttpHeaders headers;

    private final Map<String, ObjectMapper> objectMappers = new HashMap<>();
    private final ObjectMapper defaultObjectMapper;


    public DynamicObjectMapperResolver(final ObjectMapper defaultObjectMapper) {
        this.defaultObjectMapper = Objects.requireNonNull(defaultObjectMapper);
    }

    public void addObjectMapper(final String name, final ObjectMapper mapper) {
        this.objectMappers.put(name, mapper);
    }

    public ObjectMapper getDefaultObjectMapperCopy() {
        return this.defaultObjectMapper.copy();
    }
    public Optional<ObjectMapper> getObjectMapperCopy(final String serializerOption) {
        final var maybeObjectMapper = Optional.ofNullable(this.objectMappers.get(serializerOption));
        return maybeObjectMapper.map(ObjectMapper::copy);
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
    protected ObjectMapper resolveMapper(final Class<?> type, final String serializerOption) {
        final ObjectMapper foundMapper = this.objectMappers.get(serializerOption);
        if(foundMapper != null) {
            return foundMapper;
        }
        return this.defaultObjectMapper;
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
        final String serializerOption = this.getJsonSerializerOptionHeaderValue();
        return this.resolveMapper(type, serializerOption);
    }
}
