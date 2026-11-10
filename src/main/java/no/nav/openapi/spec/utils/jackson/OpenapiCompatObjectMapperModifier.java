package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.cfg.MapperBuilder;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.introspect.JacksonAnnotationIntrospector;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.util.Set;

/**
 * Hjelper til med å bygge om gitt ObjectMapper til å ha tilpasninger som stemmer med generert openapi spesifikasjon
 */
public class OpenapiCompatObjectMapperModifier {
    private final JsonInclude.Include serializationInclusion;
    private final JacksonAnnotationIntrospector annotationIntrospector;
    private final Set<EnumFeature> enabledEnumFeatures;
    private final ValueDeserializerModifier deserializerModifier;

    public OpenapiCompatObjectMapperModifier(
            JacksonAnnotationIntrospector annotationIntrospector,
            ValueDeserializerModifier deserializerModifier,
            Set<EnumFeature> enabledEnumFeatures,
            JsonInclude.Include serializationInclusion
    ) {
        this.annotationIntrospector = annotationIntrospector;
        this.deserializerModifier = deserializerModifier;
        this.enabledEnumFeatures = enabledEnumFeatures;
        this.serializationInclusion = serializationInclusion;
    }

    public static OpenapiCompatObjectMapperModifier withDefaultModifications() {
        return new OpenapiCompatObjectMapperModifier(
                FilteringEnumAnnotationIntrospector.defaultOpenapiCompat(), // <- Deaktiverer JsonFormat annotasjon
                new OpenapiCompatEnumBeanDeserializerModifier(), // <- EnumDeserializer skal først sjå etter @JsonValue, deretter toString()
                Set.of(EnumFeature.WRITE_ENUMS_USING_TO_STRING), // <- Bruk toString() viss @JsonValue ikkje er spesifisert
                JsonInclude.Include.NON_ABSENT // <- Ikkje serializer null properties (eller empty Optional), blir då undefined som stemmer med generert kode.
        );
    }

    public SimpleModule getOpenapiCompatDeserializerModule() {
        final var module = new SimpleModule("OpenapiCompatDeserializerModule");
        module.setDeserializerModifier(this.deserializerModifier);
        return module;
    }

    /**
     * Legg konfigurerte tilpasninger til gitt builder.
     * @return innsendt builder, modifisert med konfigurerte tilpasninger.
     */
    public <M extends ObjectMapper, B extends MapperBuilder<M, B>> B modify(final B builder) {
        this.applyTo(builder);
        return builder;
    }

    private void applyTo(final MapperBuilder<?, ?> builder) {
        if(this.serializationInclusion != null) {
            // Same as ObjectMapper.setSerializationInclusion did in Jackson 2.
            builder.changeDefaultPropertyInclusion(ignored -> JsonInclude.Value.construct(this.serializationInclusion, this.serializationInclusion));
        }
        if(this.annotationIntrospector != null) {
            builder.annotationIntrospector(this.annotationIntrospector);
        }
        if(this.enabledEnumFeatures != null) {
            for(final var feat : this.enabledEnumFeatures) {
                builder.enable(feat);
            }
        }
        if(this.deserializerModifier != null) {
            builder.addModule(this.getOpenapiCompatDeserializerModule());
        }
    }

    /**
     * Lager ny JsonMapper basert på gitt jsonMapper, med konfigurerte tilpasninger. Innsendt jsonMapper blir ikkje endra.
     * @return ny JsonMapper med konfigurerte tilpasninger.
     */
    public JsonMapper modify(final JsonMapper jsonMapper) {
        return this.modify(jsonMapper.rebuild()).build();
    }

    /**
     * Lager ny ObjectMapper basert på gitt objectMapper, med konfigurerte tilpasninger. Innsendt objectMapper blir ikkje endra.
     * @return ny ObjectMapper (av same type som innsendt) med konfigurerte tilpasninger.
     */
    public ObjectMapper modify(final ObjectMapper objectMapper) {
        final MapperBuilder<?, ?> builder = objectMapper.rebuild();
        this.applyTo(builder);
        return builder.build();
    }
}
