package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.ObjectMapperFactory;
import no.nav.openapi.spec.utils.jackson.dto.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SubtypeObjectMappingTest {

    private final ObjectMapper om;

    public SubtypeObjectMappingTest() {
        final var modifier = OpenapiCompatObjectMapperModifier.withDefaultModifications();
        this.om = modifier.modify(ObjectMapperFactory.createJson());
        this.om.registerSubtypes(ThirdExtensionClassA.class, ThirdExtensionClassB.class, ExternalPropertyIncludeA.class);
    }

    @Test
    public void testDeSerializationThirdSuper() throws IOException {
        final var reader = this.om.reader();
        {
            final var a = new ThirdExtensionClassA();
            { // Do a roundtrip from subtype to serialized form and back to supertype that is instance of subtype
                final var serialized = this.om.writeValueAsString(a);
                final ThirdSuperClass deserialized = reader.readValue(serialized, ThirdSuperClass.class);
                assertThat(deserialized).isInstanceOf(ThirdExtensionClassA.class);
                assertThat(deserialized).isEqualTo(a);
            }
            {
                final var serializedFromTs = "{\"propertyA\":\"AAA\",\"superInfo\":\"superInfo\",\"@type\":\"KODE_A\"}";
                final ThirdSuperClass deserialized = reader.readValue(serializedFromTs, ThirdSuperClass.class);
                assertThat(deserialized).isInstanceOf(ThirdExtensionClassA.class);
                assertThat(deserialized).isEqualTo(a);
            }
        }
        {
            final var b = new ThirdExtensionClassB();
            { // Do a roundtrip from subtype to serialized form and back to supertype that is instance of subtype
                final var serialized = this.om.writeValueAsString(b);
                final ThirdSuperClass deserialized = reader.readValue(serialized, ThirdSuperClass.class);
                assertThat(deserialized).isInstanceOf(ThirdExtensionClassB.class);
                assertThat(deserialized).isEqualTo(b);
            }
            {
                final var serializedFromTs = "{\"propertyB\":\"BBB\",\"superInfo\":\"superInfo\",\"@type\":\"KODE_B\"}";
                final ThirdSuperClass deserialized = reader.readValue(serializedFromTs, ThirdSuperClass.class);
                assertThat(deserialized).isInstanceOf(ThirdExtensionClassB.class);
                assertThat(deserialized).isEqualTo(b);
            }
        }
    }

    @Test
    public void testDeSerializationOtherAbstractClass() throws IOException {
        final var reader = this.om.reader();
        {
            final var a = new OtherExtensionClassA();
            {
                final var serialized = this.om.writeValueAsString(a);
                final OtherAbstractClass deserialized = reader.readValue(serialized, OtherAbstractClass.class);
                assertThat(deserialized).isInstanceOf(OtherExtensionClassA.class);
                assertThat(deserialized).isEqualTo(a);
            }
            {
                final var serializedFromTs = "{\"otherExtensionA\":\"otherExtensionA\",\"infoOnSuperClass\":\"someInfoOnOtherAbstractClass\",\"typename\":\"a\"}";
                final OtherAbstractClass deserialized = reader.readValue(serializedFromTs, OtherAbstractClass.class);
                assertThat(deserialized).isInstanceOf(OtherExtensionClassA.class);
                assertThat(deserialized).isEqualTo(a);
                final var serializedWithAlternativeTypename = "{\"otherExtensionA\":\"otherExtensionA\",\"infoOnSuperClass\":\"someInfoOnOtherAbstractClass\",\"typename\":\"a2\"}";
                final OtherAbstractClass deserializedFromAlternativeTypename = reader.readValue(serializedWithAlternativeTypename, OtherAbstractClass.class);
                assertThat(deserializedFromAlternativeTypename).isInstanceOf(OtherExtensionClassA.class);
                assertThat(deserializedFromAlternativeTypename).isEqualTo(deserialized);
            }
        }
    }

    @Test
    public void testDeSerializationSomeAbstractClass() throws IOException {
        final var reader = this.om.reader();
        {
            final var a = new SomeExtensionClassA();
            {
                final var serialized = this.om.writeValueAsString(a);
                final SomeAbstractClass deserialized = reader.readValue(serialized, SomeAbstractClass.class);
                assertThat(deserialized).isInstanceOf(SomeExtensionClassA.class);
                assertThat(deserialized).isEqualTo(a);
            }
            {
                final var serializedFromTs = "{\"infoOnSuperClass\":\"someInfoOnAbstractSuperClass\",\"extensionA\":\"extensionA\",\"cls\":\"no.nav.openapi.spec.utils.jackson.dto.SomeExtensionClassA\"}";
                final SomeAbstractClass deserialized = reader.readValue(serializedFromTs, SomeAbstractClass.class);
                assertThat(deserialized).isInstanceOf(SomeExtensionClassA.class);
                assertThat(deserialized).isEqualTo(a);
            }
        }
    }

    @Test
    public void testDeSerializationExternalPropertyIncludeContainer() throws IOException {
        final var reader = this.om.reader();
        {
            final var a = new ExternalPropertyIncludeContainer(DummyEnum.DUMMY_V1, new ExternalPropertyIncludeA("tst"));
            {
                final var serialized = this.om.writeValueAsString(a);
                final ExternalPropertyIncludeContainer deserialized = reader.readValue(serialized, ExternalPropertyIncludeContainer.class);
                assertThat(deserialized).isInstanceOf(ExternalPropertyIncludeContainer.class);
                assertThat(deserialized).isEqualTo(a);
            }
            {
                final var serializedFromTs = "{\"included\":{\"tst\":\"tst\"},\"includedDiscriminator\":\"V1\"}";
                final ExternalPropertyIncludeContainer deserialized = reader.readValue(serializedFromTs, ExternalPropertyIncludeContainer.class);
                assertThat(deserialized).isEqualTo(a);
            }
        }
    }
}
