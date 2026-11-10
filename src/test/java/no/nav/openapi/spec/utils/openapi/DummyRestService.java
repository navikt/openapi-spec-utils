package no.nav.openapi.spec.utils.openapi;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import no.nav.openapi.spec.utils.jackson.dto.AbstractWithSchema;
import no.nav.openapi.spec.utils.jackson.dto.ActualWithSchema;
import no.nav.openapi.spec.utils.jackson.dto.ContainerA;
import no.nav.openapi.spec.utils.jackson.dto.ContainerB;
import no.nav.openapi.spec.utils.jackson.dto.ContainerC;
import no.nav.openapi.spec.utils.jackson.dto.ToStringObjectEnum;

import java.util.Optional;

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
    @Path("/abstract")
    @Operation(description = "reproduction of resolve loop issue")
    public AbstractWithSchema other() {
        return new ActualWithSchema();
    }

    @GET
    @Path("/optional")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "returns optional dummy dto")
    public Optional<DummyDto> optionalDummy() {
        return Optional.empty();
    }

    @POST
    @Path("/containerA")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "takes ContainerA, returns ContainerB")
    public ContainerB containerA(final ContainerA containerA) {
        return new ContainerB();
    }

    @GET
    @Path("/containerC/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(description = "returns ContainerC")
    public Optional<ContainerC> containerC(@PathParam("id") final Long id, @QueryParam("type") final ToStringObjectEnum type) {
        return Optional.empty();
    }
}
