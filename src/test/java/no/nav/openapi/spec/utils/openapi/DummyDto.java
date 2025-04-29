package no.nav.openapi.spec.utils.openapi;

import java.time.Duration;

/**
 * Kun for bruk i OpenapiGenerateTest
 */
public class DummyDto {
    public int nummerProperty = 123;
    public String tekstProperty;
    public DummyEnum enumProperty = DummyEnum.DUMMY_V1;
    public Duration durationProperty = Duration.parse("P20DT1H13S");
}
