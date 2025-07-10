package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

import java.lang.annotation.Annotation;

public class NoJsonSubTypesAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Override
    protected <A extends Annotation> A _findAnnotation(Annotated ann, Class<A> annoClass) {
        if(JsonSubTypes.class.equals(annoClass)) {
            return  null;
        }
        return super._findAnnotation(ann, annoClass);
    }
}
