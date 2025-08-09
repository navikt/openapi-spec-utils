package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.type.SimpleType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.models.media.Schema;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * When the type to resolve has @JsonSubTypes annotation, add a @Schema(oneOf = ...) annotation with the subtypes declared
 * in the @JsonSubTypes to the ctxAnnotations on it.
 * <p>
 * The base ModelResolver called last in the chain will then add desired oneOf elements for all the declare subtypes, and
 * we avoid the default behaviour of @JsonSubTypes, which is to also add the allOf schema element on the subtypes.
 * <p>
 * <b>NB:</b> For this to work as intended, the ObjectMapper used by ModelResolver must be set up with the
 * #{@link NoJsonSubTypesAnnotationIntrospector}. Otherwise the base ModelResolver will still add allOf element on subtypes.
 */
public class JsonSubTypesModelConverter implements ModelConverter {

    public JsonSubTypesModelConverter() {
    }

    private Optional<JsonSubTypes> getSubTypesAnnotation(final Annotation[] annotations) {
        if(annotations != null) {
            return Arrays.stream(annotations).flatMap(annotation -> {
                if (annotation instanceof JsonSubTypes jsonSubTypes) {
                    return Stream.of(jsonSubTypes);
                } else {
                    return null;
                }
            }).findFirst();
        }
        return Optional.empty();
    }

    private static Stream<Annotation> annotationsWithoutJsonSubTypes(final Annotation[] annotations) {
        return Arrays.stream(annotations).filter(annotation -> !(annotation instanceof JsonSubTypes));
    }

    private static Annotation[] firstNonEmptyAnnotations(final Annotation[] a, final Annotation[] b) {
        if(a != null && a.length > 0) {
            return a;
        }
        return b;
    }

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if(chain.hasNext()) {
            if(type.getType() instanceof SimpleType simpleType) {
                final Class<?> cls = simpleType.getRawClass();
                final var incomingSchemaAnnotation = AnnotationUtils.resolveIncomingSchemaAnnotation(type.getCtxAnnotations(), simpleType);
                if(!AnnotationUtils.hasOneOfSchema(incomingSchemaAnnotation)) {
                    // If class has @JsonSubTypes annotation, create @Schema(oneOf = ...) based on it and add it to type.ctxAnnotations
                    final var jsonSubtypesAnnotation = cls.getDeclaredAnnotation(JsonSubTypes.class);
                    if (jsonSubtypesAnnotation != null) {
                        final Set<Class<?>> subClasses = Arrays.stream(jsonSubtypesAnnotation.value()).map(jst -> jst.value()).collect(Collectors.toUnmodifiableSet());
                        final var oneOfSchema = subClasses.isEmpty() ? null : AnnotationCreator.createOneOfSchemaAnnotation(subClasses);
                        final var outgoingSchema = AnnotationUtils.mergeOneOfInto(incomingSchemaAnnotation, oneOfSchema);
                        final var newCtxAnnotations = AnnotationUtils.replaceSchemaOrArraySchemaAnnotation(type.getCtxAnnotations(), outgoingSchema);
                        return chain.next().resolve(type.ctxAnnotations(newCtxAnnotations), context, chain);
                    }
                }
            }
            return chain.next().resolve(type, context, chain);
        } else {
            return null;
        }
    }
}
