package no.nav.openapi.spec.utils.jackson.dto;

/**
 * Record with extra non-canonical constructors based on a domain type. These should not affect property order,
 * add extra properties, or pull the domain type into the openapi spec.
 */
public record IntrospectionRecord(String tekst, int nummer, String annen) {

    public IntrospectionRecord(final IntrospectionDomene domene) {
        this(domene.domeneTekst(), domene.domeneNummer(), null);
    }

    public IntrospectionRecord(final IntrospectionDomene domene, final String ekstraVerdi) {
        this(domene.domeneTekst(), domene.domeneNummer(), ekstraVerdi);
    }
}
