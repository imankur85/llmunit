package io.llmunit.util;

import io.llmunit.context.TestParameters;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Renders `{{placeholder}}` templates with resolved test parameters and extracts the
 * placeholder names for parameter resolution.
 */
public final class PromptTemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_.-]+)\\s*}}");

    private PromptTemplateRenderer() {
    }

    public static Set<String> extractPlaceholders(String template) {
        Set<String> placeholders = new LinkedHashSet<>();
        if (template == null) {
            return placeholders;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        while (matcher.find()) {
            placeholders.add(matcher.group(1));
        }
        return placeholders;
    }

    public static String render(String template, TestParameters parameters) {
        if (template == null) {
            return "";
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String name = matcher.group(1);
            String value = parameters == null ? "" : parameters.value(name);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}