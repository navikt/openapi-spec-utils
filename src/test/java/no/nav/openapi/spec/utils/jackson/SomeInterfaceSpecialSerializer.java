package no.nav.openapi.spec.utils.jackson;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;
import no.nav.openapi.spec.utils.jackson.dto.SomeInterface;

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
    public void serialize(SomeInterface some, JsonGenerator jgen, SerializationContext ctxt) {
        if(asObject) {
            jgen.writeStartObject();
            jgen.writeStringProperty("codeValue", some.codeValue());
            jgen.writeEndObject();
        } else {
            jgen.writeString(some.codeValue());
        }
    }

}
