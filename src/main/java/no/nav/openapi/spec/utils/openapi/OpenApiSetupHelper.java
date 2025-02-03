package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.ObjectMapperFactory;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.core.Application;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class OpenApiSetupHelper {
    private final Application application;
    private final Info info;
    private final Server server;
    private String scannerClass = "io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner";
    private final Set<String> resourcePackages = new HashSet<String>();
    private final Set<String> resourceClasses = new HashSet<String>();

    public OpenApiSetupHelper(
            final Application application,
            final Info info,
            final Server server
    ) {
        this.application = Objects.requireNonNull(application);
        this.info = Objects.requireNonNull(info);
        this.server = Objects.requireNonNull(server);
    }

    public void setScannerClass(final String scannerClass) {
        this.scannerClass = scannerClass;
    }
    public void addResourcePackage(final String resourcePackage) {
        this.resourcePackages.add(resourcePackage);
    }
    public void addResourceClass(final String resourceClass) {
        this.resourceClasses.add(resourceClass);
    }
    public String getScannerClass() {
        return this.scannerClass;
    }
    public Set<String> getResourcePackages() {
        return Collections.unmodifiableSet(this.resourcePackages);
    }
    public Set<String> getResourceClasses() {
        return Collections.unmodifiableSet(this.resourceClasses);
    }

    protected ObjectMapper objectMapper() {
        return ObjectMapperFactory.createJson();
    }

    protected OpenAPIConfiguration initBaseConfig()  {
        final OpenAPI oas = new OpenAPI();
        oas.info(this.info);
        oas.addServersItem(this.server);
        return new SwaggerConfiguration()
                .openAPI(oas)
                .prettyPrint(true)
                .scannerClass(this.scannerClass)
                .resourceClasses(this.resourceClasses)
                .resourcePackages(this.resourcePackages);
    }

    protected OpenApiContext buildContext(final OpenAPIConfiguration baseConfig) throws OpenApiConfigurationException {
        return new JaxrsOpenApiContextBuilder<>()
                .application(this.application)
                .openApiConfiguration(baseConfig)
                .buildContext(false);
    }

    protected OpenAPI addOpenApiCustomizations(final OpenAPIConfiguration baseConfig) throws OpenApiConfigurationException {
        final var context = this.buildContext(baseConfig);
        // EnumVarnamesConverter legger til x-enum-varnames for property namn på genererte enum objekt.
        context.setModelConverters(Set.of(new EnumVarnamesConverter(this.objectMapper())));
        // Konverter og rename enums, legg til nullable på Optional returtyper:
        final var optionalResponseTypeAdjustingReader = new OptionalResponseTypeAdjustingReader(baseConfig);
        optionalResponseTypeAdjustingReader.setApplication(this.application); // <- Nødvendig for at @ApplicationPath skal få effekt
        context.setOpenApiReader(new ConvertEnumsToRefsWrappingReader(optionalResponseTypeAdjustingReader));
        context.init();
        return context.read();
    }

    public OpenAPI resolveOpenAPI() throws OpenApiConfigurationException {
        return this.addOpenApiCustomizations(this.initBaseConfig());
    }

}
