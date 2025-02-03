package no.nav.openapi.spec.utils.openapi;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.Schema;

import java.util.Arrays;
import java.util.Iterator;

/**
 * For alle enum verdier som blir definert i schema, legg til x-enum-varnames liste med namn på enum verdiane.
 * <p>
 * Dette for at generert (typescript) klientkode i neste omgang skal generere const objekter og ha property namn
 * med samme verdi som java kode har definert for enum verdiane.
 */
public class EnumVarnamesConverter implements ModelConverter {
    private final ObjectMapper objectMapper;

    public EnumVarnamesConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Schema<?> resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if(chain.hasNext()) {
            // First, resolve the schema for current type
            final var resolvedSchema = chain.next().resolve(type, context, chain);
            // If resolved schema type has enum values, resolve and add x-enum-varnames
            if(resolvedSchema.getEnum() != null && !resolvedSchema.getEnum().isEmpty()) {
                // Find enum name values, and add them to "x-enum-varnames" extension.
                // This makes the generated enum consts have the same property names as the java enums has declared.
                final var enumValues = resolvedSchema.getEnum();
                final JavaType javaType = objectMapper.constructType(type.getType());
                if (javaType != null && javaType.isEnumType()) {
                    final Class<Enum> enumClass = (Class<Enum>) javaType.getRawClass();
                    final String[] names = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray(String[]::new);
                    if(names.length != enumValues.size()) {
                        throw new RuntimeException("resolved enum values count ("+enumValues.size()+") not equal constructed enum class constants count ("+names.length+")");
                    }
                    resolvedSchema.addExtension("x-enum-varnames", names);
                }
            }
            return resolvedSchema;
        } else {
            return null;
        }
    }
}
