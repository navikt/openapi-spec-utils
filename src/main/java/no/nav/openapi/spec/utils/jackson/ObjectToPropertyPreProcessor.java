package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanDescription;

import java.io.IOException;
import java.util.Objects;

/**
 * ObjectToPropertyPreProcessor is used to modify a deserializer so that an incoming json object is changed to be deserialized
 * as one of its properties instead of the whole object.
 * <p>
 * If for example the deserializer gets an object like this:
 * <pre>
 *     {"kode": "VALUE", "kodeverk": "VALUE_TYPE"}
 * </pre>
 * This preprocessor can change that so that the input to the deserializer using the parser becomes:
 * <pre>
 *     "VALUE"
 * </pre>
 * By initializing it with propertyName = "kode".
 * <p>
 * If the input JsonParser is not at the start of an object, the given propertyName is not found in the object, or has
 * no value, the input is passed back either as the original input JsonParser or a new JsonParser containing the current
 * object. This should then be a no-op. That might lead to a failure to deserialize later.
 * <p>
 * Since the propertyName is hardcoded as a string when initializing this class, implementors should add a unit test that
 * fails if at some later point the propertyName of matchCalss is changed, so that one can detect and fix the hardcoded
 * propertyName in, before deserialization starts failing. Note also that in this case one must consider compatibility
 * with old objects serialized with the old propertyName.
 */
public class ObjectToPropertyPreProcessor implements PreProcessorMatcher {
    private final Class<?> matchClass;
    /**
     * key of object property to extract and use for deserialization
     */
    private final String propertyName;

    public ObjectToPropertyPreProcessor(final Class<?> matchClass, final String propertyName) {
        this.matchClass = Objects.requireNonNull(matchClass);
        this.propertyName = Objects.requireNonNull(propertyName);
    }

    @Override
    public boolean isMatch(BeanDescription beanDesc) {
        return beanDesc.getType().isTypeOrSubTypeOf(this.matchClass);
    }

    @Override
    public JsonParser preProcess(JsonParser p) throws IOException {
        // If parser is at start of an object.
        if(p.currentToken() == JsonToken.START_OBJECT) {
            final var obj = p.readValueAsTree();
            if(obj.isObject()) {
                final var kode = obj.get(this.propertyName);
                if(kode != null) {
                    final JsonParser subParser = kode.traverse(p.getCodec());
                    subParser.nextToken(); // Bring the new parser forward to start of kode value
                    return subParser;
                }
            }
            // Not an object, or property not found. Continue as if no pre-processing has happened.
            // Don't think this will ever occur unless some implementation mistake has been done.
            // If this happens, later deserialization will probably fail.
            final var passthroughSubParser = obj.traverse(p.getCodec());
            passthroughSubParser.nextToken(); // Bring the new parser forward to start of kode value
            return passthroughSubParser;
        }
        // Not an object. Continue as if no pre-processing happened.
        return p;
    }
}
