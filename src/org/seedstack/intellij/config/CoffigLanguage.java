package org.seedstack.intellij.config;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;

public class CoffigLanguage extends Language {
    public static final Language YAMLINSTANCE;

    static {
        try {
            Class<?> YAMLLanguageClass = Class.forName("org.jetbrains.yaml.YAMLLanguage");
            YAMLINSTANCE = (Language) YAMLLanguageClass.getField("INSTANCE").get(null);
        } catch (Exception e) {
            throw new RuntimeException("Unable to access YAML language", e);
        }
    }

    private CoffigLanguage() {
        super(YAMLINSTANCE, "coffig/yaml", "application/yaml", "application/x-yaml");
    }

    @NotNull
    public String getDisplayName() {
        return "Coffig YAML";
    }
}
