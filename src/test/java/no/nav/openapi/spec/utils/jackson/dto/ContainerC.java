package no.nav.openapi.spec.utils.jackson.dto;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Container with a broader set of property types, used in golden output tests.
 */
public record ContainerC(
        ToStringObjectEnum toStringObjectEnum,
        List<ToStringObjectEnum> toStringObjectEnums,
        Map<ValueAnnotatedEnum, String> enumKeyedMap,
        LocalDate date,
        LocalDateTime dateTime,
        Duration duration,
        YearMonth yearMonth,
        Optional<String> optionalText,
        Integer nullableNumber,
        boolean flag,
        ThirdSuperClass thirdSuperClass,
        List<OtherAbstractClass> others
) {
}
