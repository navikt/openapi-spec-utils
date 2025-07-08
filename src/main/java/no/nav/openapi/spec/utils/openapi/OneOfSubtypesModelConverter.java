package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.databind.type.SimpleType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.StringToClassMapItem;
import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.media.DependentRequired;
import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * When the type to resolve is an abstract class without any @Schema annotation, and registeredSubtypes has one or more
 * classes that are subtypes of the type, this will automatically add a @Schema annotation with "oneOf" set to all
 * registered matching subtypes to the type to resolve. When it is then resolved further by the chain, this leads to
 * all matching subtypes being declared as possitble "oneOf" values.
 * <br>
 * Used so that we can avoid manually having to hardcode the @Schema(oneOf = ...) annotation.
 */
public class OneOfSubtypesModelConverter implements ModelConverter {

    final Set<Class<?>> registeredSubtypes;

    public OneOfSubtypesModelConverter(final Set<Class<?>> registeredSubtypes) {
        this.registeredSubtypes = registeredSubtypes;
    }

    /**
     * Programmatically creates an instance of swagger @Schema annotation with given oneOf value
     */
    private static io.swagger.v3.oas.annotations.media.Schema createOneOfSchemaAnnotation(Set<Class<?>> oneOf) {
        io.swagger.v3.oas.annotations.media.Schema schema = new io.swagger.v3.oas.annotations.media.Schema() {
            @Override
            public Class<?> implementation() {
                return Void.class;
            }

            @Override
            public Class<?> not() {
                return Void.class;
            }

            @Override
            public Class<?>[] oneOf() {
                final var ret = new Class<?>[oneOf.size()];
                return oneOf.toArray(ret);
            }

            @Override
            public Class<?>[] anyOf() {
                return new Class[0];
            }

            @Override
            public Class<?>[] allOf() {
                return new Class[0];
            }

            @Override
            public String name() {
                return "";
            }

            @Override
            public String title() {
                return "";
            }

            @Override
            public double multipleOf() {
                return 0;
            }

            @Override
            public String maximum() {
                return "";
            }

            @Override
            public boolean exclusiveMaximum() {
                return false;
            }

            @Override
            public String minimum() {
                return "";
            }

            @Override
            public boolean exclusiveMinimum() {
                return false;
            }

            @Override
            public int maxLength() {
                return Integer.MAX_VALUE;
            }

            @Override
            public int minLength() {
                return 0;
            }

            @Override
            public String pattern() {
                return "";
            }

            @Override
            public int maxProperties() {
                return 0;
            }

            @Override
            public int minProperties() {
                return 0;
            }

            @Override
            public String[] requiredProperties() {
                return new String[0];
            }

            @Override
            public boolean required() {
                return false;
            }

            @Override
            public RequiredMode requiredMode() {
                return RequiredMode.AUTO;
            }

            @Override
            public String description() {
                return "";
            }

            @Override
            public String format() {
                return "";
            }

            @Override
            public String ref() {
                return "";
            }

            @Override
            public boolean nullable() {
                return false;
            }

            @Override
            public boolean readOnly() {
                return false;
            }

            @Override
            public boolean writeOnly() {
                return false;
            }

            @Override
            public AccessMode accessMode() {
                return AccessMode.AUTO;
            }

            @Override
            public String example() {
                return "";
            }

            @Override
            public ExternalDocumentation externalDocs() {
                return new ExternalDocumentation() {
                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return ExternalDocumentation.class;
                    }

                    @Override
                    public String description() {
                        return "";
                    }

                    @Override
                    public String url() {
                        return "";
                    }

                    @Override
                    public Extension[] extensions() {
                        return new Extension[0];
                    }
                };
            }

            @Override
            public boolean deprecated() {
                return false;
            }

            @Override
            public String type() {
                return "";
            }

            @Override
            public String[] allowableValues() {
                return new String[0];
            }

            @Override
            public String defaultValue() {
                return "";
            }

            @Override
            public String discriminatorProperty() {
                return "";
            }

            @Override
            public DiscriminatorMapping[] discriminatorMapping() {
                return new DiscriminatorMapping[0];
            }

            @Override
            public boolean hidden() {
                return false;
            }

            @Override
            public boolean enumAsRef() {
                return false;
            }

            @Override
            public Class<?>[] subTypes() {
                return new Class[0];
            }

            @Override
            public Extension[] extensions() {
                return new Extension[0];
            }

            @Override
            public Class<?>[] prefixItems() {
                return new Class[0];
            }

            @Override
            public String[] types() {
                return new String[0];
            }

            @Override
            public int exclusiveMaximumValue() {
                return 0;
            }

            @Override
            public int exclusiveMinimumValue() {
                return 0;
            }

            @Override
            public Class<?> contains() {
                return Void.class;
            }

            @Override
            public String $id() {
                return "";
            }

            @Override
            public String $schema() {
                return "";
            }

            @Override
            public String $anchor() {
                return "";
            }

            @Override
            public String $vocabulary() {
                return "";
            }

            @Override
            public String $dynamicAnchor() {
                return "";
            }

            @Override
            public String $dynamicRef() {
                return "";
            }

            @Override
            public String contentEncoding() {
                return "";
            }

            @Override
            public String contentMediaType() {
                return "";
            }

            @Override
            public Class<?> contentSchema() {
                return Void.class;
            }

            @Override
            public Class<?> propertyNames() {
                return Void.class;
            }

            @Override
            public int maxContains() {
                return Integer.MAX_VALUE;
            }

            @Override
            public int minContains() {
                return 0;
            }

            @Override
            public Class<?> additionalItems() {
                return Void.class;
            }

            @Override
            public Class<?> unevaluatedItems() {
                return Void.class;
            }

            @Override
            public Class<?> _if() {
                return Void.class;
            }

            @Override
            public Class<?> _else() {
                return Void.class;
            }

            @Override
            public Class<?> then() {
                return Void.class;
            }

            @Override
            public String $comment() {
                return "";
            }

            @Override
            public Class<?>[] exampleClasses() {
                return new Class[0];
            }

            @Override
            public AdditionalPropertiesValue additionalProperties() {
                return AdditionalPropertiesValue.USE_ADDITIONAL_PROPERTIES_ANNOTATION;
            }

            @Override
            public DependentRequired[] dependentRequiredMap() {
                return new DependentRequired[0];
            }

            @Override
            public StringToClassMapItem[] dependentSchemas() {
                return new StringToClassMapItem[0];
            }

            @Override
            public StringToClassMapItem[] patternProperties() {
                return new StringToClassMapItem[0];
            }

            @Override
            public StringToClassMapItem[] properties() {
                return new StringToClassMapItem[0];
            }

            @Override
            public Class<?> unevaluatedProperties() {
                return Void.class;
            }

            @Override
            public Class<?> additionalPropertiesSchema() {
                return Void.class;
            }

            @Override
            public String[] examples() {
                return new String[0];
            }

            @Override
            public String _const() {
                return "";
            }

            @Override
            public SchemaResolution schemaResolution() {
                return SchemaResolution.AUTO;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return io.swagger.v3.oas.annotations.media.Schema.class;
            }

        };
        return schema;
    }

    private boolean hasNoSchemaAnnotation(final Annotation[] annotations) {
        return annotations == null ||
            Arrays.stream(annotations).noneMatch(annotation -> annotation instanceof io.swagger.v3.oas.annotations.media.Schema);
    }

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if(chain.hasNext()) {
            if(type.isResolveAsRef() && hasNoSchemaAnnotation(type.getCtxAnnotations())) {
                if(type.getType() instanceof SimpleType simpleType) {
                    if(simpleType.isAbstract() && !simpleType.isFinal()) {
                        final Class<?> cls = simpleType.getRawClass();
                        final var subclasses = registeredSubtypes.stream().filter(cls::isAssignableFrom).collect(Collectors.toUnmodifiableSet());
                        if(!subclasses.isEmpty()) {
                            final var schemaAnnotation = createOneOfSchemaAnnotation(subclasses);
                            // Create copy of existing annotations on type, add created schemaAnnotation to it
                            final var typeAnnotations = Arrays.copyOf(type.getCtxAnnotations(), type.getCtxAnnotations().length + 1);
                            typeAnnotations[typeAnnotations.length - 1] = schemaAnnotation;
                            return chain.next().resolve(type.ctxAnnotations(typeAnnotations), context, chain);
                        }
                    }
                }
            }
            return chain.next().resolve(type, context, chain);
        } else {
            return null;
        }
    }
}
