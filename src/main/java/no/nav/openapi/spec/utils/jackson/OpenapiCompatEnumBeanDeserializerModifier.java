package no.nav.openapi.spec.utils.jackson;

import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.DeserializationConfig;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.ValueDeserializerModifier;
import tools.jackson.databind.deser.jdk.EnumDeserializer;
import tools.jackson.databind.introspect.AnnotatedMember;
import tools.jackson.databind.util.ClassUtil;
import tools.jackson.databind.util.EnumResolver;

/**
 * Modifiserer deserialisering for enums til å først bruke @JsonValue basert deserialisering, deretter toString()
 * viss ingen @JsonValue annotasjon finnast på enum som skal deserialiserast.
 * <p>
 * For at denne skal fungere som tenkt må den brukast i ObjectMapper der FilteringEnumAnnotationIntrospector er aktivert,
 * slik at evt @JsonFormat er ignorert ved serialisering og deserialisering.
 * <p>
 * ObjectMapper skal og ha EnumFeature.WRITE_ENUMS_USING_TO_STRING aktivert.
 */
public class OpenapiCompatEnumBeanDeserializerModifier extends ValueDeserializerModifier {

    // Basert på implementasjon frå jackson.databind.deser.BasicDeserializerFactory.
    protected EnumResolver constructPrimaryEnumResolver(final DeserializationConfig config, final BeanDescription beanDesc) {
        final AnnotatedMember jvAcc = beanDesc.findJsonValueAccessor();
        if (jvAcc != null) {
            if (config.canOverrideAccessModifiers()) {
                ClassUtil.checkAndFixAccess(jvAcc.getMember(),
                    config.isEnabled(MapperFeature.OVERRIDE_PUBLIC_ACCESS_MODIFIERS));
            }
            return EnumResolver.constructUsingMethod(config, beanDesc.getClassInfo(), jvAcc);
        }
        return EnumResolver.constructUsingToString(config, beanDesc.getClassInfo());
    }

    @Override
    public ValueDeserializer<?> modifyEnumDeserializer(DeserializationConfig config, JavaType type, BeanDescription.Supplier beanDescRef, ValueDeserializer<?> deserializer) {
        return new EnumDeserializer(
            constructPrimaryEnumResolver(config, beanDescRef.get()),
            config.isEnabled(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS),
            null,
            EnumResolver.constructUsingToString(config, beanDescRef.getClassInfo())
        );
    }
}
