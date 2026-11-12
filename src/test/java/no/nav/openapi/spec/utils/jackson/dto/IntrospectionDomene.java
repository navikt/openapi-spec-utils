package no.nav.openapi.spec.utils.jackson.dto;

/**
 * Domain type only used as parameter in non-canonical record constructors. Should not appear in openapi spec.
 */
public class IntrospectionDomene {
    private final String domeneTekst;
    private final int domeneNummer;

    public IntrospectionDomene(final String domeneTekst, final int domeneNummer) {
        this.domeneTekst = domeneTekst;
        this.domeneNummer = domeneNummer;
    }

    public String domeneTekst() {
        return domeneTekst;
    }

    public int domeneNummer() {
        return domeneNummer;
    }
}
