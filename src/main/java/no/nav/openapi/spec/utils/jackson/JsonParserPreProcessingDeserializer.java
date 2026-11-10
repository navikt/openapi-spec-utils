package no.nav.openapi.spec.utils.jackson;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.util.Objects;

/**
 * Wraps given wrappedDeserializer and applies given JsonParserPreProcessor before the wrapped deserializer is called
 * to deserialize.
 * <p>
 * This is used to support deserialization of types that was serialized to a different format than what the current
 * default deserialization supports.
 * <p>
 * preProcessor can change the incoming json to a form/value that the current deserializer can handle.
 */
public class JsonParserPreProcessingDeserializer<T> extends ValueDeserializer<T> {
    private final ValueDeserializer<T> wrappedDeserializer;
    private final JsonParserPreProcessor preProcessor;

    public JsonParserPreProcessingDeserializer(
            final ValueDeserializer<T> wrappedDeserializer,
            final JsonParserPreProcessor preProcessor) {
        this.wrappedDeserializer = Objects.requireNonNull(wrappedDeserializer);
        this.preProcessor = Objects.requireNonNull(preProcessor);
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        // Pre-process the JSON input
        final JsonParser preProcessedParser = this.preProcessor.preProcess(p);

        // Delegate to the default deserializer
        return wrappedDeserializer.deserialize(preProcessedParser, ctxt);
    }
}
