package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
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
public class JsonParserPreProcessingDeserializer<T> extends JsonDeserializer<T> {
    private final JsonDeserializer<T> wrappedDeserializer;
    private final JsonParserPreProcessor preProcessor;

    public JsonParserPreProcessingDeserializer(
            final JsonDeserializer<T> wrappedDeserializer,
            final JsonParserPreProcessor preProcessor) {
        this.wrappedDeserializer = Objects.requireNonNull(wrappedDeserializer);
        this.preProcessor = Objects.requireNonNull(preProcessor);
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        // Pre-process the JSON input
        final JsonParser preProcessedParser = this.preProcessJsonParser(p);

        // Delegate to the default deserializer
        return wrappedDeserializer.deserialize(preProcessedParser, ctxt);
    }

    private JsonParser preProcessJsonParser(final JsonParser originalParser) throws IOException {
        return this.preProcessor.preProcess(originalParser);
    }
}
