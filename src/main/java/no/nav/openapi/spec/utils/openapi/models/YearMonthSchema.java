package no.nav.openapi.spec.utils.openapi.models;

import io.swagger.v3.oas.models.SpecVersion;
import io.swagger.v3.oas.models.media.Schema;
import no.nav.openapi.spec.utils.openapi.TimeTypesModelConverter;

import java.time.YearMonth;

/**
 * YearMonthSchema blir brukt av {@link TimeTypesModelConverter} for å sette korrekt openapi format for YearMonth typen.
 * <p>
 * Samme opplegg som {@link DurationSchema}
 */
public class YearMonthSchema extends Schema<YearMonth> {
    public YearMonthSchema() {
        this(SpecVersion.V30);
    }

    public YearMonthSchema(SpecVersion specVersion) {
        super("string", "year-month", specVersion);
    }

    @Override
    public YearMonthSchema format(String format) {
        super.setFormat(format);
        return this;
    }

    @Override
    public YearMonthSchema _default(YearMonth _default) {
        super.setDefault(_default);
        return this;
    }

    @Override
    protected YearMonth cast(Object value) {
        if (value != null) {
            if (value instanceof YearMonth) {
                return (YearMonth) value;
            } else if (value instanceof String) {
                return YearMonth.parse((String) value);
            } else {
                throw new ClassCastException("Cannot cast " + value.getClass() + " to YearMonth");
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder();
        sb.append("class YearMonthSchema {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
