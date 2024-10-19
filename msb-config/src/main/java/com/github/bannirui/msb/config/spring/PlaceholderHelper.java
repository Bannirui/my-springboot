package com.github.bannirui.msb.config.spring;

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import java.util.Set;
import java.util.Stack;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.util.StringUtils;

public class PlaceholderHelper {
    private static final String PLACEHOLDER_PREFIX = "${";
    private static final String PLACEHOLDER_SUFFIX = "}";
    private static final String VALUE_SEPARATOR = ":";
    private static final String SIMPLE_PLACEHOLDER_PREFIX = "{";
    private static final String EXPRESSION_PREFIX = "#{";
    private static final String EXPRESSION_SUFFIX = "}";

    public PlaceholderHelper() {
    }

    public Object resolvePropertyValue(ConfigurableBeanFactory beanFactory, String beanName, String placeholder) {
        String strVal = beanFactory.resolveEmbeddedValue(placeholder);
        BeanDefinition bd = beanFactory.containsBean(beanName) ? beanFactory.getMergedBeanDefinition(beanName) : null;
        return this.evaluateBeanDefinitionString(beanFactory, strVal, bd);
    }

    private Object evaluateBeanDefinitionString(ConfigurableBeanFactory beanFactory, String value, BeanDefinition beanDefinition) {
        if (beanFactory.getBeanExpressionResolver() == null) {
            return value;
        } else {
            Scope scope = beanDefinition != null ? beanFactory.getRegisteredScope(beanDefinition.getScope()) : null;
            return beanFactory.getBeanExpressionResolver().evaluate(value, new BeanExpressionContext(beanFactory, scope));
        }
    }

    public Set<String> extractPlaceholderKeys(String propertyString) {
        Set<String> placeholderKeys = Sets.newHashSet();
        if (!Strings.isNullOrEmpty(propertyString) &&
            (this.isNormalizedPlaceholder(propertyString) || this.isExpressionWithPlaceholder(propertyString))) {
            Stack<String> stack = new Stack();
            stack.push(propertyString);

            while (!stack.isEmpty()) {
                String strVal = (String) stack.pop();
                int startIndex = strVal.indexOf("${");
                if (startIndex == -1) {
                    placeholderKeys.add(strVal);
                } else {
                    int endIndex = this.findPlaceholderEndIndex(strVal, startIndex);
                    if (endIndex != -1) {
                        String placeholderCandidate = strVal.substring(startIndex + "${".length(), endIndex);
                        if (placeholderCandidate.startsWith("${")) {
                            stack.push(placeholderCandidate);
                        } else {
                            int separatorIndex = placeholderCandidate.indexOf(":");
                            if (separatorIndex == -1) {
                                stack.push(placeholderCandidate);
                            } else {
                                stack.push(placeholderCandidate.substring(0, separatorIndex));
                                String defaultValuePart = this.normalizeToPlaceholder(placeholderCandidate.substring(separatorIndex + ":".length()));
                                if (!Strings.isNullOrEmpty(defaultValuePart)) {
                                    stack.push(defaultValuePart);
                                }
                            }
                        }

                        if (endIndex + "}".length() < strVal.length() - 1) {
                            String remainingPart = this.normalizeToPlaceholder(strVal.substring(endIndex + "}".length()));
                            if (!Strings.isNullOrEmpty(remainingPart)) {
                                stack.push(remainingPart);
                            }
                        }
                    }
                }
            }

            return placeholderKeys;
        } else {
            return placeholderKeys;
        }
    }

    private boolean isNormalizedPlaceholder(String propertyString) {
        return propertyString.startsWith("${") && propertyString.contains("}");
    }

    private boolean isExpressionWithPlaceholder(String propertyString) {
        return propertyString.startsWith("#{") && propertyString.contains("}") && propertyString.contains("${") && propertyString.contains("}");
    }

    private String normalizeToPlaceholder(String strVal) {
        int startIndex = strVal.indexOf("${");
        if (startIndex == -1) {
            return null;
        } else {
            int endIndex = strVal.lastIndexOf("}");
            return endIndex == -1 ? null : strVal.substring(startIndex, endIndex + "}".length());
        }
    }

    private int findPlaceholderEndIndex(CharSequence buf, int startIndex) {
        int index = startIndex + "${".length();
        int withinNestedPlaceholder = 0;

        while (index < buf.length()) {
            if (StringUtils.substringMatch(buf, index, "}")) {
                if (withinNestedPlaceholder <= 0) {
                    return index;
                }

                --withinNestedPlaceholder;
                index += "}".length();
            } else if (StringUtils.substringMatch(buf, index, "{")) {
                ++withinNestedPlaceholder;
                index += "{".length();
            } else {
                ++index;
            }
        }

        return -1;
    }
}
