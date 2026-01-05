package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.util.Set;

/**
 * Hjelper til med å bygge om gitt ObjectMapper til å ha tilpasninger som stemmer med generert openapi spesifikasjon
 */
public class OpenapiCompatObjectMapperModifier {
    private final JsonInclude.Include serializationInclusion;
    private final JacksonAnnotationIntrospector annotationIntrospector;
    private final Set<SerializationFeature> serializationFeatures;
    private final BeanDeserializerModifier deserializerModifier;

    public OpenapiCompatObjectMapperModifier(
            JacksonAnnotationIntrospector annotationIntrospector,
            BeanDeserializerModifier deserializerModifier,
            Set<SerializationFeature> serializationFeatures,
            JsonInclude.Include serializationInclusion
    ) {
        this.annotationIntrospector = annotationIntrospector;
        this.deserializerModifier = deserializerModifier;
        this.serializationFeatures = serializationFeatures;
        this.serializationInclusion = serializationInclusion;
    }

    public static OpenapiCompatObjectMapperModifier withDefaultModifications() {
        return new OpenapiCompatObjectMapperModifier(
                FilteringEnumAnnotationIntrospector.defaultOpenapiCompat(), // <- Deaktiverer JsonFormat annotasjon
                new OpenapiCompatEnumBeanDeserializerModifier(), // <- EnumDeserializer skal først sjå etter @JsonValue, deretter toString()
                Set.of(SerializationFeature.WRITE_ENUMS_USING_TO_STRING), // <- Bruk toString() viss @JsonValue ikkje er spesifisert
                JsonInclude.Include.NON_ABSENT // <- Ikkje serializer null properties (eller empty Optional), blir då undefined som stemmer med generert kode.
        );
    }

    public SimpleModule getOpenapiCompatDeserializerModule() {
        final var module = new SimpleModule("OpenapiCompatDeserializerModule");
        module.setDeserializerModifier(this.deserializerModifier);
        return module;
    }

    /**
     * Modifiserer gitt objectMapper med konfigurerte tilpasninger. NB: Muterer innsendt objectMapper. Send inn kopi
     * viss du vil behalde original versjon også.
     * @return innsendt objectMapper modifisert med konfigurerte tilpasninger.
     */
    public ObjectMapper modify(final ObjectMapper objectMapper) {
        if(this.serializationInclusion != null) {
            objectMapper.setSerializationInclusion(this.serializationInclusion);
        }
        if(this.annotationIntrospector != null) {
            objectMapper.setAnnotationIntrospector(this.annotationIntrospector);
        }
        if(this.serializationFeatures != null) {
            for(final var feat : this.serializationFeatures) {
                objectMapper.enable(feat);
            }
        }
        if(this.deserializerModifier != null) {
            objectMapper.registerModule(this.getOpenapiCompatDeserializerModule());
        }
        return objectMapper;
    }
}
