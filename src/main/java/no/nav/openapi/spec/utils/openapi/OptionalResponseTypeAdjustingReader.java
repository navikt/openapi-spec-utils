package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import io.swagger.v3.jaxrs2.Reader;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Denne klassen justerer generert openapi spesifikasjon for endepunkt som returnerer Optional&lt;T&gt; type, altså eit
 * resultat som kan vere null eller ein gitt type.
 * <p>
 * Modifiserer då generert openapi spesifikasjon for normalrespons frå endepunktet slik at respons definisjon blir satt
 * som "nullable": true. For at dette skal fungere må også den opprinnelege respons type $ref flyttast inn i ein "allOf"
 * array.
*  <p>
 * Modifikasjon skjer altså berre for responser av type "default" eller "200" pr no. Så ein kan med annotasjoner
 * ha andre respons koder som returnerer konkrete andre typer, feks ved feil.
 */
public class OptionalResponseTypeAdjustingReader extends Reader {
    // Reader instans må ha openApiConfiguration satt for at metainformasjon satt der skal bli med i resultat.
    public OptionalResponseTypeAdjustingReader(final OpenAPIConfiguration openAPIConfiguration) {
        this.setConfiguration(Objects.requireNonNull(openAPIConfiguration));
    }

    private static boolean isNormalResponseKey(final String key) {
        return key.equalsIgnoreCase("default") || key.equalsIgnoreCase("200");
    }

    @Override
    protected Operation parseMethod(Class<?> cls, Method method, List<Parameter> globalParameters, Produces methodProduces, Produces classProduces, Consumes methodConsumes, Consumes classConsumes, List<SecurityRequirement> classSecurityRequirements, Optional<ExternalDocumentation> classExternalDocs, Set<String> classTags, List<Server> classServers, boolean isSubresource, RequestBody parentRequestBody, ApiResponses parentResponses, JsonView jsonViewAnnotation, ApiResponse[] classResponses, AnnotatedMethod annotatedMethod) {
        final Operation operation = super.parseMethod(cls, method, globalParameters, methodProduces, classProduces, methodConsumes, classConsumes, classSecurityRequirements, classExternalDocs, classTags, classServers, isSubresource, parentRequestBody, parentResponses, jsonViewAnnotation, classResponses, annotatedMethod);
        if(method.getReturnType().getName().equals(Optional.class.getCanonicalName())) {
            for(Map.Entry<String, io.swagger.v3.oas.models.responses.ApiResponse> responseEntry : operation.getResponses().entrySet()) {
                if(isNormalResponseKey(responseEntry.getKey())) {
                    final var contents = responseEntry.getValue().getContent();
                    if(contents != null) {
                        for (Map.Entry<String, MediaType> mediaTypeEntry : contents.entrySet()){
                            if (mediaTypeEntry.getKey().contains("application/json")) {
                                final var schema = mediaTypeEntry.getValue().getSchema();
                                if (schema != null) {
                                    final var ref = schema.get$ref();
                                    if (ref != null) {
                                        final Schema allOfSchema = new Schema();
                                        allOfSchema.set$ref(ref);
                                        schema.set$ref(null);
                                        schema.setNullable(true);
                                        schema.addAllOfItem(allOfSchema);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return operation;
    }
}

