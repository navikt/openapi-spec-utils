package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.lang.annotation.Annotation;
import java.util.ArrayList;

/**
 * Deaktiverer annotasjoner utanom @JsonValue og @JsonEnumDefaultValue, slik at enums annotert med @JsonFormat(shape = Shape.OBJECT)
 * likevel ikkje blir serialisert som objekt. Dette for at serialisert resultat kan stemme med openapi spesifikasjon.
 */
public class OpenapiCompatAnnotationIntrospector extends JacksonAnnotationIntrospector {
    public static final ArrayList<Class<? extends Annotation>> whitelistedAnnotations;

    static {
        whitelistedAnnotations = new ArrayList<>();
        whitelistedAnnotations.add(JsonValue.class);
        whitelistedAnnotations.add(JsonEnumDefaultValue.class);
    }

    @Override
    protected <A extends Annotation> A _findAnnotation(Annotated ann, Class<A> annoClass) {
        final JavaType typ = ann.getType();
        // Når annotasjon er på ein enum
        if(typ != null && (typ.isEnumType() || typ.isTypeOrSubTypeOf(Enum.class))) {
            // Deaktiver alle annotasjoner whitelisted. Dei er dei einaste openapi generator bryr seg om.
            if(!whitelistedAnnotations.contains(annoClass)) {
                return null;
            }
        }
        return super._findAnnotation(ann, annoClass);
    }
}
