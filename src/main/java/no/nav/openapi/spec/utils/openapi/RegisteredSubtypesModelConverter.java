package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.databind.type.SimpleType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.util.AnnotationsUtils;
import io.swagger.v3.oas.models.media.Schema;

import java.util.*;
import java.util.stream.Collectors;

/**
 * When the type to resolve is an abstract class without any @Schema annotation, and registeredSubtypes has one or more
 * classes that are subtypes of the type, this will automatically add a @Schema annotation with "oneOf" set to all
 * registered matching subtypes to the type to resolve. When it is then resolved further by the chain, this leads to
 * all matching subtypes being declared as possitble "oneOf" values.
 * <br>
 * Used so that we can avoid manually having to hardcode the @Schema(oneOf = ...) annotation.
 */
public class RegisteredSubtypesModelConverter implements ModelConverter {
    final Set<Class<?>> registeredSubtypes;

    public RegisteredSubtypesModelConverter(final Set<Class<?>> registeredSubtypes) {
        this.registeredSubtypes = registeredSubtypes;
    }

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if(chain.hasNext()) {
            if(type.isResolveAsRef() ) {
                if(type.getType() instanceof SimpleType simpleType) {
                    final Class<?> cls = simpleType.getRawClass();
                    // Resolve current schema annotation from context (if set there) and type.
                    // contextWins is not used in the base ModelResolver, but think it is correct to use here.
                    final var incomingSchemaAnnotation = AnnotationsUtils.mergeSchemaAnnotations(type.getCtxAnnotations(), simpleType, true);
                    if(!AnnotationUtils.hasOneOfSchema(incomingSchemaAnnotation)) {
                        // Create schema with oneOf for the current type based on registeredSubTypes
                        final var subclasses = registeredSubtypes.stream().filter(cls::isAssignableFrom).collect(Collectors.toUnmodifiableSet());
                        final var oneOfSchema = subclasses.isEmpty() ? null : AnnotationCreator.createOneOfSchemaAnnotation(subclasses);
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
