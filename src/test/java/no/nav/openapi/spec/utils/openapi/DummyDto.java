package no.nav.openapi.spec.utils.openapi;

import java.time.Duration;
import java.time.YearMonth;

/**
 * Kun for bruk i OpenapiGenerateTest
 */
public class DummyDto {
    public int nummerProperty = 123;
    public String tekstProperty;
    public DummyEnum enumProperty = DummyEnum.DUMMY_V1;
    public Duration durationProperty = Duration.parse("P20DT1H13S");
    public YearMonth yearMonthProperty = YearMonth.parse("2023-10");
}
