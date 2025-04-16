package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.core.util.ObjectMapperFactory;
import no.nav.openapi.spec.utils.jackson.dto.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class OpenapiCompatObjectMapperModifierTest {

    @Test
    public void testThatNullSerializesToNotIncluded() throws JsonProcessingException {
        final var modifier = OpenapiCompatObjectMapperModifier.withDefaultModifications();
        final var objectMapper = modifier.modify(ObjectMapperFactory.createJson());
        final var inp = new OtherData("txt1", 1, null);
        final var jsonStr = objectMapper.writeValueAsString(inp);
        // Sjekk at null property ikkje er med i serialisert json
        assertThat(jsonStr).doesNotContain("num2");
        final var container = new ContainerA(
                ValueAnnotatedEnum.VALUE_B,
                UnAnnotatedEnum.VALUE_A,
                ObjectAnnotatedEnum.VALUE_A,
                inp,
                List.of(ValueAnnotatedEnum.VALUE_A, ValueAnnotatedEnum.VALUE_B),
                new NotEnumCodeValue("txt for vvvv", "vvvv")
        );
        final var jsonStr2 = objectMapper.writeValueAsString(container);
        // Sjekk at null property ikkje er med i serialisert json
        assertThat(jsonStr2).doesNotContain("num2");
    }
}
