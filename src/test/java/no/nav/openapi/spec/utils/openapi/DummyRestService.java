package no.nav.openapi.spec.utils.openapi;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import no.nav.openapi.spec.utils.jackson.dto.OtherData;

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

    @POST
    @Path("/other")
    @Operation(description = "reproduction of resolve loop issue")
    public OtherData other() {
        return new OtherData("", 1, 2);
    }
}
