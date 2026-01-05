package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * An AnnotationIntrospector that when asked to find annotation related to an enum property, filters out the ones in given
 * blacklist.
 * <p>
 *     Initialize with defaultOpenapiCompat() function to get an instance that does the right thing<sup>tm</sup> for openapi,
 *     by filtering out the JsonFormat class that might cause enum to be serialized as object.
 * </p>
 */
public class FilteringEnumAnnotationIntrospector extends JacksonAnnotationIntrospector {
    private final List<Class<? extends Annotation>> blacklistedAnnotations;

    public FilteringEnumAnnotationIntrospector(final List<Class<? extends Annotation>> blacklistedAnnotations) {
        this.blacklistedAnnotations = List.copyOf(blacklistedAnnotations);
    }

    public static FilteringEnumAnnotationIntrospector defaultOpenapiCompat() {
        return new FilteringEnumAnnotationIntrospector(List.of(JsonFormat.class));
    }

    public List<Class<? extends Annotation>> getBlacklistedAnnotations() {
        return blacklistedAnnotations;
    }

    @Override
    protected <A extends Annotation> A _findAnnotation(Annotated ann, Class<A> annoClass) {
        final JavaType typ = ann.getType();
        // When annotation is on an enum (or property declaration of enum)
        if(typ != null && (typ.isEnumType() || typ.isTypeOrSubTypeOf(Enum.class))) {
            // Deactivate all blacklisted annotations.
            if(blacklistedAnnotations.contains(annoClass)) {
                return null;
            }
        }
        return super._findAnnotation(ann, annoClass);
    }
}
