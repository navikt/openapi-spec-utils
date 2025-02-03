package no.nav.openapi.spec.utils.openapi;

import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.media.Schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Endrer resultatet frå gitt wrappedReader slik at alle enums frå dto typer blir flytta ut til å bli eigne toppnivå ref
 * typer, med namn lik type namn + property namn.
 * <p>Feks blir enum på FagsakDto.sakstype ein eigen type med namn FagsakDtoSakstype.</p>
 * <p>Dette gjere at generert kode får eigne typer (og const objekt) for alle enums, med
 * fornuftige og unike namn.</p>
 */
public class ConvertEnumsToRefsWrappingReader implements OpenApiReader {

    private final OpenApiReader wrappedReader;

    public ConvertEnumsToRefsWrappingReader(final OpenApiReader wrappedReader) {
        this.wrappedReader = Objects.requireNonNull(wrappedReader);
    }

    @Override
    public void setConfiguration(OpenAPIConfiguration openAPIConfiguration) {
        wrappedReader.setConfiguration(openAPIConfiguration);
    }

    @Override
    public OpenAPI read(Set<Class<?>> classes, Map<String, Object> resources) {
        final var result = this.wrappedReader.read(classes, resources);
        convertEnumsToRefs(result);
        return result;
    }

    private static String initCaps(final String inp) {
        if(inp != null && !inp.isEmpty()) {
            final String initCap = inp.substring(0,1).toUpperCase();
            return initCap + inp.substring(1);
        }
        return inp;
    }

    protected String resolveEnumName(final String parentSchemaTypeName, final String enumPropertyName) {
        return parentSchemaTypeName + initCaps(enumPropertyName);
    }

    /**
     * Utfører mutering av gitt OpenAPI instans.
     */
    protected void convertEnumsToRefs(final OpenAPI inp) {
        final var components = inp.getComponents();
        final var inpSchemas = components.getSchemas();
        final var resultSchemas = new HashMap<String, Schema>();

        for(final var inpSchemasEntry : inpSchemas.entrySet()) {
            final var schemaTypeName = inpSchemasEntry.getKey();
            final var typeSchema = inpSchemasEntry.getValue();
            final Map<String, Schema<?>> inpProperties = typeSchema.getProperties();
            if(inpProperties != null && !inpProperties.isEmpty()) {
                final Map<String, Schema<?>> resultProperties = new HashMap<String, Schema<?>>();
                for(final var propertiesEntry : inpProperties.entrySet()) {
                    final String propName = propertiesEntry.getKey();
                    final Schema<?> propSchema = propertiesEntry.getValue();
                    final boolean isEnumProperty = propSchema.getEnum() != null && !propSchema.getEnum().isEmpty();
                    final boolean isItemsOfEnumProperty = propSchema.getItems() != null && propSchema.getItems().getEnum() != null && !propSchema.getItems().getEnum().isEmpty();
                    if(isEnumProperty || isItemsOfEnumProperty) {
                        // This propSchema type (or items within) is an enum. Move it to be a top level type with a name based on
                        // schemaTypeName + propName, and replace the current inline instance with a ref to the top level
                        // type.
                        final String enumName = resolveEnumName(schemaTypeName, propName);
                        final boolean exists = inpSchemas.keySet().stream().anyMatch(key -> key.equals(enumName));
                        if (exists) {
                            throw new RuntimeException("convertEnumsToRefs: Enum name " + enumName + " already exists in schema. Please rename property to avoid naming collision");
                        }
                        if(isEnumProperty) {
                            // Add enum type to top level result schemas
                            resultSchemas.put(enumName, propSchema);
                            // Add ref to top level enum type to result properties for this property
                            resultProperties.put(propName, new Schema().$ref(enumName));
                        } else if(isItemsOfEnumProperty) {
                            // Add items enum type to top level result schemas
                            resultSchemas.put(enumName, propSchema.getItems());
                            // Add propSchema with items property referencing new top level enum to result schema
                            // This mutates the items prop of propSchema *after* the original items has been put into
                            // resultSchemas map.
                            resultProperties.put(propName, propSchema.items(new Schema().$ref(enumName)));
                        }
                    } else {
                        // No-op, copy input property to result
                        resultProperties.put(propName, propSchema);
                    }
                }
                typeSchema.setProperties(resultProperties);
            }
            resultSchemas.put(schemaTypeName, typeSchema);
        }
        // Replace schemas of input with process result
        components.setSchemas(resultSchemas);
    }
}
