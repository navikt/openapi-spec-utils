package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import no.nav.openapi.spec.utils.jackson.dto.SomeInterface;

import java.io.IOException;

public class SomeInterfaceSpecialSerializer extends StdSerializer<SomeInterface> {
    /**
     * If true, all implementations of SomeInterface are serialized as json object with one codeValue property,
     * regardless of its json annotations.
     * If false, all implementations are serialized as a json string of codeValue property regardless of its annotations.
     */
    private boolean asObject;

    public SomeInterfaceSpecialSerializer(final boolean asObject) {
        super(SomeInterface.class);
        this.asObject = asObject;
    }

    @Override
    public void serialize(SomeInterface some, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        if(asObject) {
            jgen.writeStartObject();
            jgen.writeStringField("codeValue", some.codeValue());
            jgen.writeEndObject();
        } else {
            jgen.writeString(some.codeValue());
        }
    }

}
