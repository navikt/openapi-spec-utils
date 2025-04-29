package no.nav.openapi.spec.utils.openapi.models;

import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.media.Schema;
import no.nav.openapi.spec.utils.openapi.TimeTypesModelConverter;

import java.time.Duration;
import java.util.Objects;

/**
 * DurationSchema blir brukt av {@link TimeTypesModelConverter}.
 * <p>
 * Inspirert av <a href="https://github.com/swagger-api/swagger-core/pull/4821/files">...</a>
 */
public class DurationSchema extends Schema<Duration> {

    public DurationSchema() {
        this(SpecVersion.V30);
    }

    public DurationSchema(SpecVersion specVersion) {
        super("string", "duration", specVersion);
    }

    @Override
    public DurationSchema format(String format) {
        super.setFormat(format);
        return this;
    }

    @Override
    public DurationSchema _default(Duration _default) {
        super.setDefault(_default);
        return this;
    }

    @Override
    protected Duration cast(Object value) {
        if (value != null) {
            try {
                if (value instanceof Duration) {
                    return (Duration) value;
                } else if (value instanceof String) {
                    return Duration.parse((String) value);
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public DurationSchema addEnumItem(Duration _enumItem) {
        super.addEnumItemObject(_enumItem);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DurationSchema {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
