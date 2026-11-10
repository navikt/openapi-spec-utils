package no.nav.openapi.spec.utils.openapi;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import no.nav.openapi.spec.utils.jackson.dto.IntrospectionDto;

@Path("/introspection")
public class IntrospectionRestService {

    @POST
    public IntrospectionDto introspection(final IntrospectionDto dto) {
        return dto;
    }
}
