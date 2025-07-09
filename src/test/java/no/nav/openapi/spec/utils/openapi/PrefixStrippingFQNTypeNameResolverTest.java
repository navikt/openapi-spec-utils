package no.nav.openapi.spec.utils.openapi;

import no.nav.openapi.spec.utils.jackson.dto.ContainerA;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class PrefixStrippingFQNTypeNameResolverTest {

    @Test
    void noPrefixTest() {
        final var resolver = new PrefixStrippingFQNTypeNameResolver("");
        assertThat(resolver.getNameOfClass(DummyDto.class)).isEqualTo(DummyDto.class.getName());
        assertThat(resolver.getNameOfClass(DummyEnum.class)).isEqualTo(DummyEnum.class.getName());
        assertThat(resolver.getNameOfClass(ContainerA.class)).isEqualTo(ContainerA.class.getName());
    }

    @Test
    void neverMatchedPrefixTest() {
        final var resolver = new PrefixStrippingFQNTypeNameResolver("some.package.");
        assertThat(resolver.getNameOfClass(DummyDto.class)).isEqualTo(DummyDto.class.getName());
        assertThat(resolver.getNameOfClass(DummyEnum.class)).isEqualTo(DummyEnum.class.getName());
        assertThat(resolver.getNameOfClass(ContainerA.class)).isEqualTo(ContainerA.class.getName());
    }

    @Test
    void simplePrefixTest() {
        final var resolver = new PrefixStrippingFQNTypeNameResolver("no.nav.");
        assertThat(resolver.getNameOfClass(DummyDto.class)).isEqualTo("openapi.spec.utils.openapi.DummyDto");
        assertThat(resolver.getNameOfClass(DummyEnum.class)).isEqualTo("openapi.spec.utils.openapi.DummyEnum");
        assertThat(resolver.getNameOfClass(ContainerA.class)).isEqualTo("openapi.spec.utils.jackson.dto.ContainerA");
    }

    @Test
    void nonCollidingSameBaseNameTest() {
        final var resolver = new PrefixStrippingFQNTypeNameResolver("no.nav.openapi.spec.utils.");
        assertThat(resolver.getNameOfClass(DummyDto.class)).isEqualTo("openapi.DummyDto");
        assertThat(resolver.getNameOfClass(no.nav.openapi.spec.utils.jackson.dto.DummyEnum.class)).isEqualTo("jackson.dto.DummyEnum");
        assertThat(resolver.getNameOfClass(DummyEnum.class)).isEqualTo("openapi.DummyEnum");
        assertThat(resolver.getNameOfClass(ContainerA.class)).isEqualTo("jackson.dto.ContainerA");
    }

    @Test
    void collidingSameBaseNameTest() {
        final var resolver = new PrefixStrippingFQNTypeNameResolver(
                "no.nav.openapi.spec.utils.openapi.",
                "no.nav.openapi.spec.utils.jackson.dto.",
                "no.nav.openapi.spec.utils."
        );
        assertThat(resolver.getNameOfClass(DummyDto.class)).isEqualTo("DummyDto");
        assertThat(resolver.getNameOfClass(no.nav.openapi.spec.utils.jackson.dto.DummyEnum.class)).isEqualTo("DummyEnum");
        assertThat(resolver.getNameOfClass(DummyEnum.class)).isEqualTo("openapi.DummyEnum");
        assertThat(resolver.getNameOfClass(ContainerA.class)).isEqualTo("ContainerA");
    }
}