package no.nav.openapi.spec.utils.openapi;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

/**
 * Kun for bruk i OpenapiGenerateTest
 */
@Path("/testing")
public class DummyRestService {
    public DummyRestService() {}

    @GET
    @Path("/dummy")
    @Operation(description = "returns dummy dto")
    public DummyDto dummy() {
        return new DummyDto();
    }
}
