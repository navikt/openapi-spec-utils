package no.nav.openapi.spec.utils.jackson;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;

import java.util.ArrayList;
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
public class JsonParserPreProcessingDeserializerModifier extends ValueDeserializerModifier {
    private final List<PreProcessorMatcher> processorMatchers;

    public JsonParserPreProcessingDeserializerModifier(final List<PreProcessorMatcher> processorMatchers) {
        Objects.requireNonNull(processorMatchers).forEach(Objects::requireNonNull);
        this.processorMatchers = new ArrayList<>(processorMatchers);
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
            public boolean isMatch(BeanDescription.Supplier beanDescRef) {
                return matcher.isMatch(beanDescRef);
            }

            @Override
            public JsonParser preProcess(JsonParser p) {
                return processor.preProcess(p);
            }

        };
        this.addProcessorMatcher(m);
    }

    /**
     * If a PreProcessorMatcher matches beanDescRef, return the attached JsonParserPreProcessor.
     * Only the first match found is returned and used. It is the callers responsibility to not add multiple matching
     * preprocessors, or add them in order so that the correct one is returned here for a given BeanDescription.
     *
     * @return Found JsonParserPreProcessor to modify deserializer with, or None if no match is found.
     */
    private Optional<PreProcessorMatcher> resolvePreProcessor(final BeanDescription.Supplier beanDescRef) {
        for(final var matcher: this.processorMatchers) {
            if(matcher.isMatch(beanDescRef)) {
                return Optional.of(matcher);
            }
        }
        return Optional.empty();
    }

    private ValueDeserializer<?> wrapIfMatched(final ValueDeserializer<?> defaultDeserializer, final BeanDescription.Supplier beanDescRef) {
        final var maybePreProcessor = this.resolvePreProcessor(beanDescRef);
        if(maybePreProcessor.isPresent()) {
            return new JsonParserPreProcessingDeserializer<>(
                    defaultDeserializer,
                    maybePreProcessor.get()
            );
        }
        return defaultDeserializer;
    }

    @Override
    public ValueDeserializer<?> modifyDeserializer(final DeserializationConfig config,
                                                   final BeanDescription.Supplier beanDescRef,
                                                   final ValueDeserializer<?> deserializer) {
        final var defaultDeserializer = super.modifyDeserializer(config, beanDescRef, deserializer);
        return this.wrapIfMatched(defaultDeserializer, beanDescRef);
    }

    @Override
    public ValueDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type, BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
        final var defaultDeserializer = super.modifyEnumDeserializer(config, type, beanDescRef, deserializer);
        return this.wrapIfMatched(defaultDeserializer, beanDescRef);
    }
}
