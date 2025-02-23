package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.swagger.v3.core.util.ObjectMapperFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Objects;

/**
 * Denne kan erstatte bruk av standard OpenApiResource for å serve openapi definisjon som yaml/json frå swagger grensesnittet.
 * <p>
 * Nødvendig for å få levert den faktisk genererte openapi spesifikasjon med tilpasninger.
 * </p>
 * <p>
 * Standard OpenApiResource endepunkt tok ikkje med ModelConverters/Reader som vart satt som ferdige instanser på bygd
 * OpenApiContext. Tok kun med dei som var satt med klassenamn slik at dei vart instansiert av Resource klassen.
 * </p>
 */
@Path("/openapi.{type:json|yaml}")
public class OpenApiResource {
    private OpenAPI resolvedOpenAPI;
    private final ObjectMapper jsonOutputMapper;
    private final ObjectMapper yamlOutputMapper;

    public OpenApiResource(
            final OpenAPI resolvedOpenAPI,
            final ObjectMapper jsonOutputMapper,
            final ObjectMapper yamlOutputMapper) {
        this.resolvedOpenAPI = resolvedOpenAPI;
        this.jsonOutputMapper = withDeterministicOutput(Objects.requireNonNull(jsonOutputMapper));
        this.yamlOutputMapper = withDeterministicOutput(Objects.requireNonNull(yamlOutputMapper));
    }

    protected ObjectMapper withDeterministicOutput(final ObjectMapper om) {
        return om.copy()
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true)
                .configure(MapperFeature.SORT_CREATOR_PROPERTIES_FIRST, true)
                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
    }

    public OpenApiResource(final OpenAPI resolvedOpenAPI) {
        this(resolvedOpenAPI, ObjectMapperFactory.createJson(), ObjectMapperFactory.createYaml());
    }

    public void setResolvedOpenAPI(final OpenAPI resolvedOpenAPI) {
        this.resolvedOpenAPI = Objects.requireNonNull(resolvedOpenAPI);
    }

    public boolean wantsYaml(final String type) {
        if(type.equalsIgnoreCase("yaml")) {
            return true;
        } else if(type.equalsIgnoreCase("json")) {
            return false;
        } else {
            throw new IllegalArgumentException("type must be json or yaml. Was: " + type);
        }
    }

    /**
     * @param wantsYaml true: Return openapi spec as yaml. false: Return as json
     * @return the contained OpenAPI object serialized as json or yaml string.
     */
    public String openApiAsString(final boolean wantsYaml) throws JsonProcessingException {
        final ObjectMapper om = wantsYaml ? this.yamlOutputMapper : this.jsonOutputMapper;
        return om.writerWithDefaultPrettyPrinter().writeValueAsString(this.resolvedOpenAPI);
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    @Operation(hidden = true)
    public Response getOpenApi(@PathParam("type") final String type) throws JsonProcessingException {
        final boolean wantsYaml = type.equalsIgnoreCase("yaml");
        final String contentType = wantsYaml ? "application/yaml" : MediaType.APPLICATION_JSON;
        final String content = this.openApiAsString(wantsYaml);
        return Response.status(Response.Status.OK)
                .entity(content)
                .type(contentType)
                .build();
    }
}
