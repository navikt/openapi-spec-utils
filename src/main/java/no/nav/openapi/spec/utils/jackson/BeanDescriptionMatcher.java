package no.nav.openapi.spec.utils.jackson;

import tools.jackson.databind.BeanDescription;

/**
 * Used to determine if a JsonParserPreProcessor should be applied to given beanDescRef
 */
public interface BeanDescriptionMatcher {
    public boolean isMatch(final BeanDescription.Supplier beanDescRef);
}
