package no.nav.openapi.spec.utils.openapi;

import io.swagger.v3.oas.models.OpenAPI;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class FileOutputter {
    public static void writeJsonFile(final OpenAPI openAPI, final String outputPath) throws IOException {
        Objects.requireNonNull(openAPI);
        Objects.requireNonNull(outputPath);
        if(outputPath.endsWith(".json")) {
            // Ok, write the generated openapi spec to given file path
            final Path path = Paths.get(outputPath);
            final OpenApiResource openApiResource = new OpenApiResource(openAPI);
            final String json = openApiResource.openApiAsString(false);
            Files.writeString(path, json);
        } else {
            throw new IllegalArgumentException("Invalid OpenAPI FileOutputter outputPath argument (" + outputPath + ")");
        }
    }
}
