package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;

public interface JsonParserPreProcessor {
    public JsonParser preProcess(JsonParser p) throws IOException;
}
