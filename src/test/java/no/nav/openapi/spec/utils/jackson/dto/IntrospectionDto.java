package no.nav.openapi.spec.utils.jackson.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Class where Jackson 3 default bean introspection differs from Jackson 2 (when compiled with -parameters):
 * <ul>
 *     <li>Constructor parameter names are detected as properties (MapperFeature.DETECT_PARAMETER_NAMES)</li>
 *     <li>Collection getter without setter used as setter (MapperFeature.USE_GETTERS_AS_SETTERS)</li>
 *     <li>Final field used as mutator (MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)</li>
 *     <li>Property name of field with single lowercase char before uppercase (MapperFeature.FIX_FIELD_NAME_UPPER_CASE_PREFIX)</li>
 * </ul>
 */
public class IntrospectionDto {
    private String aarsak;
    private final List<String> liste = new ArrayList<>();
    private final String fast;
    private String eNavn;

    IntrospectionDto() {
        this.fast = null;
    }

    public IntrospectionDto(final String annetNavn, final List<String> andreListe, final String fastVerdi, final String eNavnVerdi) {
        this.aarsak = annetNavn;
        this.liste.addAll(andreListe);
        this.fast = fastVerdi;
        this.eNavn = eNavnVerdi;
    }

    public String getAarsak() {
        return aarsak;
    }

    public List<String> getListe() {
        return liste;
    }

    public String getFast() {
        return fast;
    }

    public String getENavn() {
        return eNavn;
    }

    @Override
    public String toString() {
        return "IntrospectionDto{aarsak=" + aarsak + ", liste=" + liste + ", fast=" + fast + ", eNavn=" + eNavn + "}";
    }
}
