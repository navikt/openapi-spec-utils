package no.nav.openapi.spec.utils.jackson;

import com.fasterxml.jackson.databind.BeanDescription;

/**
 * Used to determine if a JsonParserPreProcessor should be applied to given beanDesc
 */
public interface BeanDescriptionMatcher {
    public boolean isMatch(final BeanDescription beanDesc);
}
