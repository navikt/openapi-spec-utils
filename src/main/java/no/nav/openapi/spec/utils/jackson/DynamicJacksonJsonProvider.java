package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.jakarta.rs.cfg.JakartaRSFeature;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

/**
 * Overrides default JacksonJsonProvider to disable the caching of resolved ObjectMapper so that we can resolve to
 * different ObjectMappers based on incoming http request header by using DynamicObjectMapperResolver or similar.
 */
@Provider
@Consumes({MediaType.APPLICATION_JSON, "text/json", MediaType.WILDCARD})
@Produces({MediaType.APPLICATION_JSON, "text/json", MediaType.WILDCARD})
public class DynamicJacksonJsonProvider extends JacksonJsonProvider {
    public DynamicJacksonJsonProvider() {
        super();
        // Disable caching and enable dynamic lookup to get the ContextResolver lookup based on incoming header to work.
        this.disable(JakartaRSFeature.CACHE_ENDPOINT_WRITERS);
        this.disable(JakartaRSFeature.CACHE_ENDPOINT_READERS);
        this.enable(JakartaRSFeature.DYNAMIC_OBJECT_MAPPER_LOOKUP);
    }
}

