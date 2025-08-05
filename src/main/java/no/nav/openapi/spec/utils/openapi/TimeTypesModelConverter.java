package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.databind.type.SimpleType;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;
import no.nav.openapi.spec.utils.openapi.models.DurationSchema;
import no.nav.openapi.spec.utils.openapi.models.YearMonthSchema;

import java.time.Duration;
import java.time.YearMonth;
import java.util.Iterator;

/**
 * Overstyrer standard openapi spesifikasjonsgenerering for java.time.* verdier.
 * <p>
 * swagger openapi generator har ikkje spesiell støtte for java.time.* typer. Openapi spesifikasjon for disse blir derfor
 * i utgangspunktet generert som object med alle interne properties spesifisert. Dette stemmer ikkje med faktisk
 * (de)serialisering i jackson når ObjectMapper er konfigurert til å bruke JavaTimeModule. Då blir disse (de)serialisert
 * som string.
 * <p>
 * Meir spesifikt blir java.time.Duration verdier, når SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS er disabled i
 * ObjectMapper, (de)serialisert frå/til ISO-8601 string.
 * <p>
 * Denne ModelConverter overstyrer derfor openapi spesifikasjonsgenerering av nokre java.time.* typer med å spesifisere
 * returnere Schema instanser for disse, som igjen spesifiserer type og format som stemmer med det jackson ObjectMapper
 * er satt opp til.
 * <p>
 * Inspirasjon for denne er henta frå <a href="https://github.com/swagger-api/swagger-core/issues/2784#issuecomment-388325057">...</a>
 * <p>
 * Fleire spesialiserte Schema klasser, for andre java.time.* typer kan legges til her seinare, ved behov.
 */
public class TimeTypesModelConverter implements ModelConverter {
    public TimeTypesModelConverter() {
    }

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if(type.isSchemaProperty()) {
            if(type.getType() != null && type.getType() instanceof SimpleType simpleType) {
                final Class<?> cls = simpleType.getRawClass();
                if(Duration.class.isAssignableFrom(cls)) {
                    return new DurationSchema();
                }
                if(YearMonth.class.isAssignableFrom(cls)) {
                    return new YearMonthSchema();
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
