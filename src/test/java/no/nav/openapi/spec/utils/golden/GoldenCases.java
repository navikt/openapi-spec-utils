package no.nav.openapi.spec.utils.golden;

import no.nav.openapi.spec.utils.jackson.dto.ContainerA;
import no.nav.openapi.spec.utils.jackson.dto.ContainerB;
import no.nav.openapi.spec.utils.jackson.dto.ContainerC;
import no.nav.openapi.spec.utils.jackson.dto.DummyEnum;
import no.nav.openapi.spec.utils.jackson.dto.ExternalPropertyIncludeA;
import no.nav.openapi.spec.utils.jackson.dto.ExternalPropertyIncludeContainer;
import no.nav.openapi.spec.utils.jackson.dto.NotEnumCodeValue;
import no.nav.openapi.spec.utils.jackson.dto.ObjectAnnotatedEnum;
import no.nav.openapi.spec.utils.jackson.dto.OtherAbstractClass;
import no.nav.openapi.spec.utils.jackson.dto.OtherData;
import no.nav.openapi.spec.utils.jackson.dto.OtherExtensionClassA;
import no.nav.openapi.spec.utils.jackson.dto.OtherExtensionClassB;
import no.nav.openapi.spec.utils.jackson.dto.SomeAbstractClass;
import no.nav.openapi.spec.utils.jackson.dto.SomeExtensionClassA;
import no.nav.openapi.spec.utils.jackson.dto.ThirdExtensionClassA;
import no.nav.openapi.spec.utils.jackson.dto.ThirdExtensionClassB;
import no.nav.openapi.spec.utils.jackson.dto.ThirdSuperClass;
import no.nav.openapi.spec.utils.jackson.dto.ToStringObjectEnum;
import no.nav.openapi.spec.utils.jackson.dto.UnAnnotatedEnum;
import no.nav.openapi.spec.utils.jackson.dto.ValueAnnotatedEnum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * Jackson version independent definition of the cases used by GoldenOutputTest.
 * <p>
 * The golden files in src/test/resources/golden was recorded with openapi-spec-utils 2.x (Jackson 2, swagger-core 2).
 * GoldenOutputTest checks that the current version still produces the same output for the same input.
 * <p>
 * Run with -Dgolden.record=true to overwrite the golden files with the output of the current version.
 */
public final class GoldenCases {
    private GoldenCases() {}

    public static final Path GOLDEN_DIR = Path.of("src/test/resources/golden");
    public static final Path ACTUAL_DIR = Path.of("target/golden-actual");

    public static boolean isRecording() {
        return Boolean.getBoolean("golden.record");
    }

    public static Map<String, Object> serializationCases() {
        final var cases = new LinkedHashMap<String, Object>();
        for (final var v : ValueAnnotatedEnum.values()) cases.put("ValueAnnotatedEnum." + v.name(), v);
        for (final var v : UnAnnotatedEnum.values()) cases.put("UnAnnotatedEnum." + v.name(), v);
        for (final var v : ObjectAnnotatedEnum.values()) cases.put("ObjectAnnotatedEnum." + v.name(), v);
        for (final var v : ToStringObjectEnum.values()) cases.put("ToStringObjectEnum." + v.name(), v);
        for (final var v : DummyEnum.values()) cases.put("DummyEnum." + v.name(), v);
        cases.put("containerA", containerA());
        cases.put("containerB", new ContainerB(ValueAnnotatedEnum.VALUE_B, "txt"));
        cases.put("containerB.empty", new ContainerB(null, null));
        cases.put("otherData.withNull", new OtherData("txt1", 1, null));
        cases.put("containerC.full", containerCFull());
        cases.put("containerC.empty", containerCEmpty());
        cases.put("thirdExtensionClassA", new ThirdExtensionClassA());
        cases.put("otherExtensionClassA", new OtherExtensionClassA());
        cases.put("someExtensionClassA", new SomeExtensionClassA());
        cases.put("externalPropertyIncludeContainer", new ExternalPropertyIncludeContainer(DummyEnum.DUMMY_V1, new ExternalPropertyIncludeA("tst")));
        cases.put("notEnumCodeValue", new NotEnumCodeValue("txt", "cv"));
        cases.put("list.toStringObjectEnum", List.of(ToStringObjectEnum.VALUE_A, ToStringObjectEnum.VALUE_B));
        cases.put("map.toStringObjectEnumKey", new TreeMap<>(Map.of(ToStringObjectEnum.VALUE_A, "a", ToStringObjectEnum.VALUE_B, "b")));
        cases.put("map.unAnnotatedEnumKey", new TreeMap<>(Map.of(UnAnnotatedEnum.VALUE_A, "a")));
        cases.put("map.valueAnnotatedEnumKey", new TreeMap<>(Map.of(ValueAnnotatedEnum.VALUE_A, "a")));
        cases.put("optional.empty", Optional.empty());
        cases.put("optional.enum", Optional.of(ToStringObjectEnum.VALUE_B));
        return cases;
    }

    /**
     * Deserialize given json to given type.
     */
    public record DeserCase(String json, Class<?> type) {}

    public static Map<String, DeserCase> deserializationCases() {
        final var cases = new LinkedHashMap<String, DeserCase>();
        final List<Class<?>> enumTypes = List.of(ValueAnnotatedEnum.class, UnAnnotatedEnum.class, ObjectAnnotatedEnum.class, ToStringObjectEnum.class, DummyEnum.class);
        final List<String> enumInputs = List.of(
                "\"A\"", "\"B\"", "\"VALUE_A\"", "\"VALUE_B\"", "\"KODE-A\"", "\"V1\"", "\"DUMMY_V2\"", "\"unknown\"", "\"\"", "null", "1",
                "{\"codeValue\":\"B\"}", "{\"codeValue\":\"KODE-B\",\"navn\":\"x\"}", "{\"kode\":\"A\"}"
        );
        for (final var type : enumTypes) {
            for (final var inp : enumInputs) {
                cases.put(type.getSimpleName() + " <- " + inp, new DeserCase(inp, type));
            }
        }
        cases.put("ContainerA <- strings", new DeserCase(
                "{\"valueAnnotatedEnum\":\"B\",\"unAnnotatedEnum\":\"A\",\"objectAnnotatedEnum\":\"A\",\"otherData\":{\"txt1\":\"t\",\"num1\":1},\"listOfValueAnnotatedEnums\":[\"A\",\"B\"],\"notEnumCodeValue\":\"vvvv\"}",
                ContainerA.class));
        cases.put("ContainerA <- objects", new DeserCase(
                "{\"valueAnnotatedEnum\":{\"codeValue\":\"B\"},\"unAnnotatedEnum\":{\"codeValue\":\"A\"},\"objectAnnotatedEnum\":{\"codeValue\":\"A\"},\"otherData\":{\"txt1\":\"t\",\"num1\":1},\"listOfValueAnnotatedEnums\":[{\"codeValue\":\"A\"}],\"notEnumCodeValue\":{\"codeValue\":\"vvvv\"}}",
                ContainerA.class));
        cases.put("ContainerA <- unknown property", new DeserCase("{\"valueAnnotatedEnum\":\"B\",\"unknownProperty\":1}", ContainerA.class));
        cases.put("ContainerB <- unknown property", new DeserCase("{\"enum1\":\"A\",\"txt1\":\"t\",\"unknownProperty\":1}", ContainerB.class));
        cases.put("OtherData <- missing primitive", new DeserCase("{\"txt1\":\"t\"}", OtherData.class));
        cases.put("OtherData <- null primitive", new DeserCase("{\"txt1\":\"t\",\"num1\":null}", OtherData.class));
        cases.put("OtherData <- trailing tokens", new DeserCase("{\"txt1\":\"t\",\"num1\":2} {}", OtherData.class));
        cases.put("ContainerC <- strings", new DeserCase(
                "{\"toStringObjectEnum\":\"KODE-A\",\"toStringObjectEnums\":[\"KODE-B\"],\"enumKeyedMap\":{\"A\":\"a\"},\"date\":\"2024-02-29\",\"dateTime\":\"2024-02-29T12:13:14\",\"duration\":\"PT1H\",\"yearMonth\":\"2024-02\",\"optionalText\":\"t\",\"flag\":true}",
                ContainerC.class));
        cases.put("ContainerC <- objects", new DeserCase(
                "{\"toStringObjectEnum\":{\"codeValue\":\"KODE-A\",\"navn\":\"Value A\"},\"toStringObjectEnums\":[{\"codeValue\":\"KODE-B\"}],\"thirdSuperClass\":{\"@type\":\"KODE_B\",\"propertyB\":\"x\"},\"others\":[{\"typename\":\"b\"}]}",
                ContainerC.class));
        cases.put("ThirdSuperClass <- KODE_A", new DeserCase("{\"@type\":\"KODE_A\",\"propertyA\":\"AAA\",\"superInfo\":\"superInfo\"}", ThirdSuperClass.class));
        cases.put("OtherAbstractClass <- a2", new DeserCase("{\"typename\":\"a2\",\"otherExtensionA\":\"x\"}", OtherAbstractClass.class));
        cases.put("SomeAbstractClass <- cls", new DeserCase("{\"cls\":\"no.nav.openapi.spec.utils.jackson.dto.SomeExtensionClassA\",\"extensionA\":\"y\"}", SomeAbstractClass.class));
        cases.put("ExternalPropertyIncludeContainer <- V1", new DeserCase("{\"included\":{\"tst\":\"tst\"},\"includedDiscriminator\":\"V1\"}", ExternalPropertyIncludeContainer.class));
        return cases;
    }

    /**
     * Describes a deserialized result in a deterministic way, so that it can be compared across versions.
     */
    public static String describe(final Object result) {
        if (result == null) {
            return "null";
        }
        final String str;
        if (result instanceof ThirdSuperClass t) {
            str = "superInfo=" + t.superInfo + ", " + (t instanceof ThirdExtensionClassA a ? a.propertyA : ((ThirdExtensionClassB) t).propertyB);
        } else if (result instanceof OtherAbstractClass o) {
            str = "infoOnSuperClass=" + o.infoOnSuperClass + ", " + (o instanceof OtherExtensionClassA a ? a.otherExtensionA : ((OtherExtensionClassB) o).otherExtensionB);
        } else if (result instanceof SomeExtensionClassA s) {
            str = "infoOnSuperClass=" + s.infoOnSuperClass + ", " + s.extensionA;
        } else if (result instanceof ContainerC c) {
            final var others = new ArrayList<String>();
            if (c.others() != null) {
                c.others().forEach(o -> others.add(describe(o)));
            }
            str = new ContainerC(c.toStringObjectEnum(), c.toStringObjectEnums(), c.enumKeyedMap(), c.date(), c.dateTime(), c.duration(), c.yearMonth(), c.optionalText(), c.nullableNumber(), c.flag(), null, null)
                    + " thirdSuperClass=" + describe(c.thirdSuperClass()) + " others=" + (c.others() == null ? "null" : others);
        } else {
            str = String.valueOf(result);
        }
        final String enumName = result instanceof Enum<?> e ? "/" + e.name() : "";
        return result.getClass().getSimpleName() + enumName + ": " + str;
    }

    public static ContainerA containerA() {
        return new ContainerA(
                ValueAnnotatedEnum.VALUE_B,
                UnAnnotatedEnum.VALUE_A,
                ObjectAnnotatedEnum.VALUE_A,
                new OtherData("txt1", 1, null),
                List.of(ValueAnnotatedEnum.VALUE_A, ValueAnnotatedEnum.VALUE_B),
                new NotEnumCodeValue("txt for vvvv", "vvvv")
        );
    }

    public static ContainerC containerCFull() {
        return new ContainerC(
                ToStringObjectEnum.VALUE_A,
                List.of(ToStringObjectEnum.VALUE_B, ToStringObjectEnum.VALUE_A),
                new TreeMap<>(Map.of(ValueAnnotatedEnum.VALUE_A, "a", ValueAnnotatedEnum.VALUE_B, "b")),
                LocalDate.of(2024, 2, 29),
                LocalDateTime.of(2024, 2, 29, 12, 13, 14),
                Duration.parse("P20DT1H13S"),
                YearMonth.of(2024, 2),
                Optional.of("txt"),
                42,
                true,
                new ThirdExtensionClassB(),
                List.of(new OtherExtensionClassA(), new OtherExtensionClassB())
        );
    }

    public static ContainerC containerCEmpty() {
        return new ContainerC(null, List.of(), Map.of(), null, null, null, null, Optional.empty(), null, false, null, null);
    }

    /**
     * Formats given case results as "name => result" lines.
     */
    public static String toLines(final Map<String, String> results) {
        final var sb = new StringBuilder();
        results.forEach((k, v) -> sb.append(k).append(" => ").append(v).append('\n'));
        return sb.toString();
    }

    /**
     * In recording mode, writes actual to golden file. Otherwise, returns golden file content. Actual is always written
     * to target/golden-actual, so that differences can be inspected with a diff tool.
     */
    public static String recordOrReadGolden(final String fileName, final String actual) throws IOException {
        Files.createDirectories(ACTUAL_DIR);
        Files.writeString(ACTUAL_DIR.resolve(fileName), actual);
        if (isRecording()) {
            Files.createDirectories(GOLDEN_DIR);
            Files.writeString(GOLDEN_DIR.resolve(fileName), actual);
            return actual;
        }
        return Files.readString(GOLDEN_DIR.resolve(fileName));
    }
}
