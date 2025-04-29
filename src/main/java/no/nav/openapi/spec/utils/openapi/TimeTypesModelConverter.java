package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import no.nav.openapi.spec.utils.openapi.models.DurationSchema;

import java.time.Duration;
import java.util.Iterator;

/**
 * Overstyrer standard openapi spesifikasjonsgenerering for Duration verdier.
 * <p>
 * Duration verdier blir i utgangspunktet definert som object i openapi spesifikasjonen, med alle interne properties
 * spesifisert. Dette stemmer ikkje med faktisk serialisering/deserialisering, som med SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS
 * disabled i ObjectMapper vil (de)serialisere Duration frå/til ISO-8601 string.
 * <p>
 * Denne ModelConverter overstyrer openapi spesifikasjonsgenerering av Duration til å bli string type med format "duration".
 * Henta frå <a href="https://github.com/swagger-api/swagger-core/issues/2784#issuecomment-388325057">...</a>
 * <p>
 * Den er navngitt TimeTypesModelConverter sidan det kanskje vil vere aktuelt å legge til kode for fleire typer som har
 * med tid å gjere seinare, ved behov.
 */
public class TimeTypesModelConverter implements ModelConverter {
    private final ObjectMapper objectMapper;

    public TimeTypesModelConverter(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if(type.isSchemaProperty()) {
            final JavaType javaType = this.objectMapper.constructType(type.getType());
            if(javaType != null) {
                final Class<?> cls = javaType.getRawClass();
                if(Duration.class.isAssignableFrom(cls)) {
                    return new DurationSchema();
                }
            }
        }
        if(chain.hasNext()) {
            return chain.next().resolve(type, context, chain);
        } else {
            return null;
        }
    }

}
