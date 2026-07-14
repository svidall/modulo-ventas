package com.ventas.modulo.config;

import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.annotationIntrospector(new JacksonAnnotationIntrospector() {
            @Override
            public List<PropertyName> findPropertyAliases(Annotated m) {
                List<PropertyName> aliases = super.findPropertyAliases(m);
                if (aliases == null) {
                    aliases = new ArrayList<>();
                }
                String propName = getPropertyNameOfMember(m);
                if (propName != null && !propName.isEmpty()) {
                    String snake = toSnakeCase(propName);
                    String camel = toCamelCase(propName);
                    
                    // Add snake_case alias
                    if (!propName.equals(snake)) {
                        PropertyName p = PropertyName.construct(snake);
                        if (!aliases.contains(p)) {
                            aliases.add(p);
                        }
                    }
                    
                    // Add camelCase alias
                    if (!propName.equals(camel)) {
                        PropertyName p = PropertyName.construct(camel);
                        if (!aliases.contains(p)) {
                            aliases.add(p);
                        }
                    }
                }
                return aliases.isEmpty() ? null : aliases;
            }
        });
    }

    private String getPropertyNameOfMember(Annotated m) {
        if (m instanceof AnnotatedField) {
            return m.getName();
        } else if (m instanceof AnnotatedMethod) {
            String name = m.getName();
            if (name.startsWith("get") && name.length() > 3) {
                return decapitalize(name.substring(3));
            } else if (name.startsWith("set") && name.length() > 3) {
                return decapitalize(name.substring(3));
            } else if (name.startsWith("is") && name.length() > 2) {
                return decapitalize(name.substring(2));
            }
        } else if (m instanceof AnnotatedParameter) {
            return m.getName();
        }
        return null;
    }

    private String decapitalize(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        char[] chars = string.toCharArray();
        chars[0] = Character.toLowerCase(chars[0]);
        return new String(chars);
    }

    private String toSnakeCase(String input) {
        if (input == null) return null;
        return input.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }

    private String toCamelCase(String input) {
        if (input == null) return null;
        if (!input.contains("_")) return input;
        StringBuilder sb = new StringBuilder();
        boolean nextUpper = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper) {
                    sb.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    sb.append(Character.toLowerCase(c));
                }
            }
        }
        return sb.toString();
    }
}
