package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.swagger.v3.core.util.ObjectMapperFactory;
import no.nav.openapi.spec.utils.jackson.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonParserPreProcessingDeserializerTest {

    private static BeanDeserializerModifier makePreProcessingModifier() {
        final var preProcessorAndMatcher = new ObjectToPropertyPreProcessor(SomeInterface.class, "codeValue");
        return new JsonParserPreProcessingDeserializerModifier(preProcessorAndMatcher);
    }

    public static List<ObjectMapper> objectMappers() {
        final ObjectMapper om = ObjectMapperFactory.createJson();
        final var deserializerModifier = makePreProcessingModifier();
        final SimpleModule codeValueSerializedAsObject = new SimpleModule();
        codeValueSerializedAsObject.addSerializer(new SomeInterfaceSpecialSerializer(true));
        codeValueSerializedAsObject.setDeserializerModifier(deserializerModifier);
        final SimpleModule codeValueSerializedAsString = new SimpleModule();
        codeValueSerializedAsString.addSerializer(new SomeInterfaceSpecialSerializer(false));
        codeValueSerializedAsString.setDeserializerModifier(deserializerModifier);
        return List.of(
                om,
                om.copy().registerModule(codeValueSerializedAsObject),
                om.copy().registerModule(codeValueSerializedAsString)
        );
    }

    private ContainerA containerA() {
        return new ContainerA(
                ValueAnnotatedEnum.VALUE_B,
                UnAnnotatedEnum.VALUE_A,
                ObjectAnnotatedEnum.VALUE_A,
                new OtherData("aaa", 111, null),
                List.of(ValueAnnotatedEnum.VALUE_A, ValueAnnotatedEnum.VALUE_B),
                new NotEnumCodeValue("txt for vvvv", "vvvv")
        );
    }

    @ParameterizedTest
    @MethodSource("objectMappers")
    public void testRoundTripAllObjectMappers(final ObjectMapper om) throws JsonProcessingException {
        final ContainerA inp = this.containerA();
        final String jsonStr = om.writeValueAsString(inp);
        final ContainerA out = om.readValue(jsonStr, ContainerA.class);
        assertThat(out).isEqualTo(inp);
    }

    // Test that containerA serialized with objectmapper always doing objects for SomeInterface can be deserializer with
    // objectmapper serializing as strings (that has deserializer preprocessing for it)
    @Test
    public void testMixingMapperDeserialization() throws JsonProcessingException {
        final var inp = this.containerA();
        final var objectMappers = objectMappers();
        final var alwaysToObjectMapper = objectMappers.get(1);
        final var alwaysToStringMapper = objectMappers.get(2);
        final String jsonStr = alwaysToObjectMapper.writeValueAsString(inp);
        final ContainerA out = alwaysToStringMapper.readValue(jsonStr, ContainerA.class);
        assertThat(out).isEqualTo(inp);
    }

    // Test the fallback for when given propertyName is not found in ObjectToPropertyPreProcessor.
    // Will then fall back to using unmodified JsonParser, which might work.
    @Test
    public void testFallbackToPassthrough() throws JsonProcessingException {
        final ObjectMapper om = ObjectMapperFactory.createJson();
        final SimpleModule mod = new SimpleModule();
        // Add deserializermodifier with incorrect propertyName. Will always fail
        mod.setDeserializerModifier(new JsonParserPreProcessingDeserializerModifier(new ObjectToPropertyPreProcessor(SomeInterface.class, "wrongPropName")));
        om.registerModule(mod);
        final var inp = ObjectAnnotatedEnum.VALUE_A;
        final String jsonStr = om.writeValueAsString(inp);
        final var out = om.readValue(jsonStr, ObjectAnnotatedEnum.class);
        assertThat(out).isEqualTo(inp);
    }

    // Test that changing the definition of a type having JsonFormat(shape = OBJECT) to using JsonValue works, i.e.
    // data serialized as object can be deserialized with type annotated as JsonValue without doing anything other than
    // using the preprocessor.
    @Test
    public void testChangingFromObjectToValue() throws JsonProcessingException {
        final var inp = ObjectAnnotatedEnum.VALUE_B;
        final ObjectMapper om = ObjectMapperFactory.createJson();
        final SimpleModule mod = new SimpleModule();
        mod.setDeserializerModifier(new JsonParserPreProcessingDeserializerModifier(new ObjectToPropertyPreProcessor(SomeInterface.class, "codeValue")));
        om.registerModule(mod);
        final String jsonStr = om.writeValueAsString(inp);
        final var out = om.readValue(jsonStr, ValueAnnotatedEnum.class);
        assertThat(out.codeValue()).isEqualTo(inp.codeValue());
        assertThat(out).isEqualTo(ValueAnnotatedEnum.VALUE_B);
    }
}
