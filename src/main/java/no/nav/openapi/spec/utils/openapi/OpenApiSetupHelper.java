package no.nav.openapi.spec.utils.openapi;

import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.jackson.TypeNameResolver;
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

    private String makeRandomId() {
        return UUID.randomUUID().toString();
    }

    protected OpenApiContext buildContext(final OpenAPIConfiguration baseConfig) throws OpenApiConfigurationException {
        return new JaxrsOpenApiContextBuilder<>()
                .ctxId(makeRandomId()) // Use new random ctxId each time to avoid caching
                .application(this.application)
                .openApiConfiguration(baseConfig)
                .buildContext(false);
    }

    protected OpenAPI createWithCustomizations(final OpenAPIConfiguration baseConfig) throws OpenApiConfigurationException {
        final var context = this.buildContext(baseConfig);
        final Set<ModelConverter> modelConverters = new LinkedHashSet<>(4);
        // EnumVarnamesConverter adds x-enum-varnames for property name on generated enum objects.
        modelConverters.add(new EnumVarnamesConverter());
        // TimeTypesModelConverter converts Duration to OpenAPI string with format "duration".
        modelConverters.add(new TimeTypesModelConverter());
        // OneOfSubtypeModelConverter automatically adds @Schema(oneOf ...) for registeredSubtypes
        if(!this.registeredSubTypes.isEmpty()) {
            modelConverters.add(new OneOfSubtypesModelConverter(this.registeredSubTypes));
        }
        // If context has been built before, we need to  reset ModelConverters for changes in it (or TypeNameResolver.std) to take effect
        ModelConverters.reset();
        ModelResolver.enumsAsRef = true;
        TypeNameResolver.std.setUseFqn(true);
        context.setModelConverters(modelConverters);
        // Convert and rename enums, add nullable on Optional returntypes:
        final var optionalResponseTypeAdjustingReader = new OptionalResponseTypeAdjustingReader(baseConfig);
        optionalResponseTypeAdjustingReader.setApplication(this.application); // <- Neccessary for @ApplicationPath to have effect
        context.setOpenApiReader(optionalResponseTypeAdjustingReader);
        context.init();
        return context.read();
    }

    public OpenAPI resolveOpenAPI() throws OpenApiConfigurationException {
        return this.createWithCustomizations(this.initBaseConfig());
    }

}
