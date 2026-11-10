openapi spec utils
==================

## Formål
Dette prosjekt er til for å:

- Dokumentere korleis ein frå eksisterande k9-sak, k9-klage og liknande java serverkode kan generere ei openapi json spesifikasjonsfil som fungerer bra som utgangspunkt for å i neste omgang genere ein typescript klient med https://github.com/navikt/openapi-ts-clientmaker
- Dokumentere endringer i oppsett for serialisering/deserialisering av response/request data i nevnte kodebaser som er nødvendig for at request/response format skal stemme med generert openapi spesifikasjon.
- Publisere kodebibliotek med hjelpefunksjonalitet som kan brukast av ovennevnte kodebaser for å generere openapi spesifikasjon og kompatibelt oppsett for serialisering/deserialisering.

## Henvendelser
Dette prosjektet er vedlikeholdt av [navikt/k9saksbehandling](CODEOWNERS)

Kontakt [#sykdom-i-familien](https://nav-it.slack.com/archives/CNGKVQVJ9) ved spørsmål.

## Versjoner

| openapi-spec-utils | Jackson | swagger-core | jakarta.ws.rs |
|--------------------|---------|--------------|---------------|
| 3.x | 3.x (`tools.jackson`) | 3.x (`swagger-rest`) | 3.x / 4.x (testa med Jersey 4.0.3) |
| 2.x | 2.x (`com.fasterxml.jackson`) | 2.2.x (`swagger-jaxrs2-jakarta`) | 3.x / 4.x |

### Endringer i 3.0 (breaking)
- Krever Jackson 3 og swagger-core 3 (`io.swagger.core.v3:swagger-rest`). Jackson annotasjonar (`com.fasterxml.jackson.annotation`) er uendra.
- `DynamicObjectMapperResolver` er no ein `ContextResolver<JsonMapper>` (Jackson 3 sin `JacksonJsonProvider` slår opp `JsonMapper`). Konstruktør og `addObjectMapper` tek `JsonMapper`. `getDefaultObjectMapperCopy()`/`getObjectMapperCopy(..)` er bytta ut med `getDefaultObjectMapper()`/`getObjectMapper(..)` sidan `JsonMapper` er immutable.
- `DynamicJacksonJsonProvider` utvider `tools.jackson.jakarta.rs.json.JacksonJsonProvider`.
- `OpenapiCompatObjectMapperModifier.modify(..)` returnerer ny mapper i staden for å endre innsendt mapper. Den kan også brukast på ein `MapperBuilder`. Konstruktøren tek `ValueDeserializerModifier` og `Set<EnumFeature>` i staden for `BeanDeserializerModifier` og `Set<SerializationFeature>`.
- `BeanDescriptionMatcher`/`PreProcessorMatcher` får `BeanDescription.Supplier` i staden for `BeanDescription`. `JsonParserPreProcessingDeserializerModifier` utvider `ValueDeserializerModifier`.
- `OpenApiResource.openApiAsString` kastar `JacksonException` (unchecked) i staden for `JsonProcessingException`.
- Utdatert `OpenapiCompatAnnotationIntrospector` er fjerna. Bruk `OpenapiCompatObjectMapperModifier`.
- ObjectMapper som `OpenApiSetupHelper` bruker til å lage spesifikasjon har samme bean introspeksjon som Jackson 2 (`DETECT_PARAMETER_NAMES` av, m.m., som i `configureForJackson2()`). Uten dette ville konstruktørparameternamn bli med som ekstra `writeOnly` properties når koden er kompilert med `-parameters`. Overstyr `OpenApiSetupHelper.objectMapper()` viss server sin mapper har anna oppsett.
- Jackson 3 har ikkje lenger "legacy" namngiving av properties. Ein getter som `getENavn()` blir no `ENavn` (Jackson 2: `enavn`), både i serialisering og i generert spesifikasjon.

For å få same serialisering som med Jackson 2 bør base mapper lagast med `JsonMapper.builderWithJackson2Defaults()`. Med Jackson 3 sitt standardoppsett og `-parameters` kan f.eks. enums med fleire `@JsonCreator` metodar feile med "Conflicting property-based creators". Golden testar (`GoldenOutputTest`) sjekkar at serialisering, deserialisering og generert openapi spesifikasjon er identisk med resultatet frå versjon 2 (unntak: `openapi-introspection.json`, der enavn blir ENavn).

## Bakgrunn
Hovedproblemet som dette prosjekt skal hjelpe til med å fikse er at nevnte kodebaser (k9-sak, k9-klage, etc) inneheld "komplekse" enum definisjoner, som har vore definert til å bli serialisert som json objekt (med @JsonFormat annotasjoner). Dette er ikkje kompatibelt med nokon openapi spesifikasjon, som berre tillater enkle string/tal som verdi på enums. I tillegg er det slik at dei fleste av disse enums implementerer eit `Kodeverdi` interface med ein property med navn `kode`, som returnerer ein enkel unik string verdi. Det er denne string verdien som er ønskelig å få sendt inn/ut som verdi på disse enums.

Dagens endepunkt returnerer altså enum verdier som json objekt, slik standard Jackson serialisering tilseier når dei er annotert med `@JsonFormat(shape = Shape.OBJECT)`, og dagens frontend kode forventer å få responser på dette format. Samtidig vil klient generert ut frå korrekt openapi spesifikasjon måtte ha ein anna serialisering for å stemme med spesifikasjonen. Ein treng altså å støtte ulike serialiseringsformat på samme tid. For å få dette til må standard _ObjectMapper_ oppsett for web endepunkt i nevnte kodebaser endrast, slik at serialisering dynamisk kan styrast med ein header på innkommande request. Når klient generert ut frå openapi spesifikasjon brukast, må den få tilbake ein respons der standard Jackson serialisering av enums annotert med `@JsonFormat` er overstyrt til å heller returnere string verdi som spesifikasjonen tilseier.

## Løysing
Her forklarast kva som har vorte gjort for å få generert ein bra spesifikasjon, og dynamisk styrt serialisering/deserialisering av respons/requests, slik at server kan fungere for både gammal og ny kode samtidig.

_NB: Den enklaste løysinga viss ein ikkje måtte støtte gammal kode samtidig ville vere å fjerne `@JsonFormat` annotasjon og legge til `@JsonValue` på alle enum definisjoner._

### Lage "korrekt" spesifikasjon 
#### Implementer toString() for enum verdi
For at openapi spesifikasjon for enums som har ein anna ønska openapi "serialiseringsverdi" enn resultatet av `name()` metoden skal bli korrekt, må ein implementere ein `toString()` metode som returnerer ønska verdi. (Viss ein ikkje kan legge på `@JsonValue` annotasjon). Eksempel:
```java
@JsonFormat(shape = Shape.OBJECT)
public enum EnKodeverdi implements Kodeverdi {
    NAVN1("Verdi1"),
    NAVN2("Verdi2");
    
    
    public String kode;
    
    private EnKodeverdi(String kode) {
        this.kode = kode;
    }
    
    // Uten denne toString ville openapi spesifikasjon for enum ha verdiane "NAVN1" og "NAVN2". Med denne definert blir
    // verdiane "Verdi1" og "Verdi2".
    @Override
    public String toString() {
        return this.kode;
    }
}
```
Dette må ein altså gjere på alle enums som er definert med  `@JsonFormat(shape = Shape.OBJECT)`, og som har ein anna ønska openapi verdi enn name() metoden.

Merk at dette ikkje endrer standard serialisering av enum, den vil framleis serialisere som json objekt, som ikkje stemmer med openapi spesifikasjon.

#### Forbedra enum spesifikasjoner
For at kodegenerering i neste omgang (https://github.com/navikt/openapi-ts-clientmaker) skal fungere best mulig, er det nokre fleire ting som bør gjerast:
1. Flytt enums ut til å vere separate refs, med namn bygd opp av klassenamn + propertynamn. Dette er nødvendig for å få gode typenamn på enums, og sikrer at ein unngår namnekollisjoner når ein har fleire properties (på ulike dto klasser) som har samme namn, men held på ulike enum typer.
2. Generer x-enum-varnames tillegg på alle enums, slik at property namn på generert javascript objekt blir det samme som på java enum.

Punkt 1 løysast ved å sette `ModelResolver.enumsAsref` true i OpenApiSetupHelper.
Punkt 2 løysast ved å legge til ein instans av `EnumVarnamesConverter` som ModelConverter før generering av OpenAPI objektet.

#### nullable respons for Optional
Vi ønsker at ein metode som returnerer Optional<T> skal ha nullable i generert OpenAPI spesifikasjon. Dette løysast ved å bruke `OptionalResponseTypeAdjustingReader` klassen.

#### fully qualified type names
Vi ønsker at typenamn i generert openapi spesifikasjon inkluderer pakkenamn, slik at vi unngår kollisjoner sjølv om det er fleire klasser med samme namn (frå ulike pakker) i spesifikasjonen.

#### automatiske subtyper for abstrakte klasser
OpenApiSetupHelper har støtte for å automatisk legge til "@Schema(oneOf = ...)" annotasjoner for abstrakte klasser, når subtyper er registrert med metoden `registerSubTypes`. På denne måten får vi generert gode openapi typer for abstrakte klasser uten å måtte manuelt kode alle konkrete subtyper inn i @Schema(oneOf) annotasjon.

**Bruk `OpenApiSetupHelper` for å få generert ein `OpenAPI` spesifikasjon med disse tilpasninger på plass.**

### Implementer dynamisk openapi kompatibel serialisering.
For at klient skal kunne styre kva serialisering/deserialisering server skal utføre dynamisk, legger vi til støtte for ein eigendefinert request header: `X-Json-Serializer-Option`. `openapi-compat` brukast som verdi for å signalisere at openapi kompatibel request/response format skal brukast.

- Legg til ein `ObjectMapperResolver` basert på `DynamicObjectMapperResolver` slik det er gjort i k9-sak, eller bruk `DynamicObjectMapperResolver` direkte. Denne ser på header for aktiv request, og returnerer ein av dei ulike definerte `JsonMapper` variantane.
- Legg til `DynamicJacksonJsonProvider` for å deaktivere standard caching av resultat frå `ObjectMapperResolver`. Nødvendig sidan `DynamicObjectMapperResolver` resultat varierer basert på request.
- Bruk `OpenapiCompatObjectMapperModifier` frå dette prosjekt til å bygge openapi kompatibel serialisering/deserialisering uten å finne opp kruttet på nytt.
- Bruk `DynamicObjectMapperResolverVaryFilter` frå dette prosjektet til å legge til Vary header på alle responser for å unngå potensielle cache feil som følge av dynamisk serialisering.


### Bruk eigen OpenApiResource
For at openapi spesifikasjon som visast frå swagger server skal stemme med alle tilpasninger gjort må ein bruke `OpenApiResource` frå dette prosjektet istadenfor standard klasse med samme namn.


### Eksempel
Sjå **ApplicationConfig.java** i k9-sak og k9-klage kodebaser for eksempel på korleis disse stega kan gjerast.

Sjå **OpenapiGenerate.java** og **web/pom.xml**  i k9-sak/k9-klage for eksempel på oppsett for generering av openapi.json spesifikasjon i build pipeline (uten å starte server)
