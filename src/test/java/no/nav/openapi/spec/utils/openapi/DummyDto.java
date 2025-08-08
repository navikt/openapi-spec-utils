package no.nav.openapi.spec.utils.openapi;

import no.nav.openapi.spec.utils.jackson.dto.*;

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
//  RegisteredSubtypesModelConverter will autopmatically create @Schema annotation as declared below, since SomeExtensionClassX
//  is added to OpenApiSetupHelper registeredSubTypes. This is therefore commented out here just to show what would otherwise
//  be needed:
//  @Schema(oneOf = {SomeExtensionClassA.class, SomeExtensionClassB.class})
    public SomeAbstractClass abstractClass = new SomeExtensionClassA();
    // Test of inheritance annotated with JsonSubTypes for all subtypes.
    public OtherAbstractClass otherAbstractClass = new OtherExtensionClassA();
}
