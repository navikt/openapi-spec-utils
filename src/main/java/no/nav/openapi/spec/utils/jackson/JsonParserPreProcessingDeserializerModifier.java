package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This is used to intercept deserialization of certain types (matched by added processorMatcher), and preprocess the
 * incoming json via added JsonParserPreProcessor before the default deserializer is called with it.
 * <p>
 * Can be used to support deserializing various formats for a (super)type without having to add support for it to the
 * actual type class.
 */
public class JsonParserPreProcessingDeserializerModifier extends BeanDeserializerModifier {
    private final List<PreProcessorMatcher> processorMatchers;

    public JsonParserPreProcessingDeserializerModifier(final List<PreProcessorMatcher> processorMatchers) {
        this.processorMatchers = Objects.requireNonNull(processorMatchers);
        processorMatchers.forEach(Objects::requireNonNull);
    }

    public JsonParserPreProcessingDeserializerModifier() {
        this(List.of());
    }

    public JsonParserPreProcessingDeserializerModifier(final PreProcessorMatcher processorMatcher) {
        this(List.of(processorMatcher));
    }

    public void addProcessorMatcher(final PreProcessorMatcher processorMatcher) {
        this.processorMatchers.add(Objects.requireNonNull(processorMatcher));
    }

    public void addProcessorMatcher(final BeanDescriptionMatcher matcher, final JsonParserPreProcessor processor) {
        Objects.requireNonNull(matcher);
        Objects.requireNonNull(processor);
        final var m = new PreProcessorMatcher() {
            @Override
            public boolean isMatch(BeanDescription beanDesc) {
                return matcher.isMatch(beanDesc);
            }

            @Override
            public JsonParser preProcess(JsonParser p) throws IOException {
                return processor.preProcess(p);
            }

        };
        this.addProcessorMatcher(m);
    }

    /**
     * If a PreProcessorMatcher matches beanDesc, return the attached JsonParserPreProcessor.
     * Only the first match found is returned and used. It is the callers responsibility to not add multiple matching
     * preprocessors, or add them in order so that the correct one is returned here for a given BeanDescription.
     *
     * @return Found JsonParserPreProcessor to modify deserializer with, or None if no match is found.
     */
    private Optional<PreProcessorMatcher> resolvePreProcessor(final BeanDescription beanDesc) {
        for(final var matcher: this.processorMatchers) {
            if(matcher.isMatch(beanDesc)) {
                return Optional.of(matcher);
            }
        }
        return Optional.empty();
    }

    private JsonDeserializer<?> wrapIfMatched(final JsonDeserializer<?> defaultSerializer, final BeanDescription beanDesc) {
        final var maybePreProcessor = this.resolvePreProcessor(beanDesc);
        if(maybePreProcessor.isPresent()) {
            return new JsonParserPreProcessingDeserializer<>(
                    defaultSerializer,
                    maybePreProcessor.get()
            );
        }
        return defaultSerializer;
    }

    @Override
    public JsonDeserializer<?> modifyDeserializer(final DeserializationConfig config,
                                                  final BeanDescription beanDesc,
                                                  final JsonDeserializer<?> deserializer) {
        final var defaultSerializer = super.modifyDeserializer(config, beanDesc, deserializer);
        return this.wrapIfMatched(defaultSerializer, beanDesc);
    }

    @Override
    public JsonDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
        final var defaultSerializer = super.modifyEnumDeserializer(config, type, beanDesc, deserializer);
        return this.wrapIfMatched(defaultSerializer, beanDesc);
    }
}
