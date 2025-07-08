package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.ModelConverter;
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

import java.util.*;

public class OpenApiSetupHelper {
    private final Application application;
    private final Info info;
    private final Server server;
    private String scannerClass = "io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner";
    private final Set<String> resourcePackages = new HashSet<String>();
    private final Set<String> resourceClasses = new HashSet<String>();
    private final Set<Class<?>> registeredSubTypes = new LinkedHashSet<>();

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

    /**
     * Classes manually registered into ObjectMapper should be added here too. {@link OneOfSubtypesModelConverter} will then
     * do its thing when appropriate.
     */
    public void registerSubTypes(final Collection<Class<?>> subtypes) {
        this.registeredSubTypes.addAll(subtypes);
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

    protected OpenAPI createWithCustomizations(final OpenAPIConfiguration baseConfig) throws OpenApiConfigurationException {
        final var context = this.buildContext(baseConfig);
        final Set<ModelConverter> modelConverters = new LinkedHashSet<>(3);
        // EnumVarnamesConverter adds x-enum-varnames for property name on generated enum objects.
        modelConverters.add(new EnumVarnamesConverter(this.objectMapper()));
        // TimeTypesModelConverter converts Duration to OpenAPI string with format "duration".
        modelConverters.add(new TimeTypesModelConverter(this.objectMapper()));
        // OneOfSubtypeModelConverter automatically adds @Schema(oneOf ...) for registeredSubtypes
        modelConverters.add(new OneOfSubtypesModelConverter(this.registeredSubTypes));
        context.setModelConverters(modelConverters);
        // Convert and rename enums, add nullable on Optional returntypes:
        final var optionalResponseTypeAdjustingReader = new OptionalResponseTypeAdjustingReader(baseConfig);
        optionalResponseTypeAdjustingReader.setApplication(this.application); // <- Neccessary for @ApplicationPath to have effect
        context.setOpenApiReader(new ConvertEnumsToRefsWrappingReader(optionalResponseTypeAdjustingReader));
        context.init();
        return context.read();
    }

    public OpenAPI resolveOpenAPI() throws OpenApiConfigurationException {
        return this.createWithCustomizations(this.initBaseConfig());
    }

}
