package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.jakarta.rs.cfg.JakartaRSFeature;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import jakarta.enterprise.inject.Specializes;
import jakarta.annotation.Priority;

/**
 * Overrides default JacksonJsonProvider to disable the caching of resolved ObjectMapper so that we can resolve to
 * different ObjectMappers based on incoming http request header by using DynamicObjectMapperResolver or similar.
 */
@Specializes
@Priority(100)
public class DynamicJacksonJsonProvider extends JacksonJsonProvider {
    public DynamicJacksonJsonProvider() {
        super();
        // Disable caching and enable dynamic lookup to get the ContextResolver lookup based on incoming header to work.
        this.disable(JakartaRSFeature.CACHE_ENDPOINT_WRITERS);
        this.disable(JakartaRSFeature.CACHE_ENDPOINT_READERS);
        this.enable(JakartaRSFeature.DYNAMIC_OBJECT_MAPPER_LOOKUP);
    }
}

