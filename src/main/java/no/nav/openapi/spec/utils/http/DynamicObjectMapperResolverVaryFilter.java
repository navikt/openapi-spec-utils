package no.nav.openapi.spec.utils.http;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import no.nav.openapi.spec.utils.jackson.DynamicObjectMapperResolver;

import java.io.IOException;

/**
 * When using DynamicObjectMapperResolver, the response content becomes dependent on the X-Json-Serializer-Option header.
 * It must therefore be set in the response Vary header to ensure that caches do not serve the wrong content to clients if
 * the client changes the X-Json-Serializer-Option header in between requests to the same URL, and the request content is
 * cached.
 * <p>
 *     This filter should therefore be added to the JAX-RS application whenever DynamicObjectMapperResolver, or a subclass
 *     of it is used.
 * </p>
 */
public class DynamicObjectMapperResolverVaryFilter implements ContainerResponseFilter {
    public DynamicObjectMapperResolverVaryFilter() {}

    @Override
    public void filter(ContainerRequestContext req, ContainerResponseContext res) throws IOException {
        res.getHeaders().add("Vary", DynamicObjectMapperResolver.HEADER_KEY);
    }
}
