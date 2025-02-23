package no.nav.openapi.spec.utils.jackson.dto;

import java.util.List;

public record ContainerA(
        ValueAnnotatedEnum valueAnnotatedEnum,
        UnAnnotatedEnum unAnnotatedEnum,
        ObjectAnnotatedEnum objectAnnotatedEnum,
        OtherData otherData,
        List<ValueAnnotatedEnum> listOfValueAnnotatedEnums,
        NotEnumCodeValue notEnumCodeValue
) {
}
