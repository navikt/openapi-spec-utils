package no.nav.openapi.spec.utils.jackson;

import tools.jackson.core.JsonParser;

public interface JsonParserPreProcessor {
    public JsonParser preProcess(JsonParser p);
}
