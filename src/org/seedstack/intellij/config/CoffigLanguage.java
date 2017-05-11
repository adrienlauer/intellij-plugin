package org.seedstack.intellij.config;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

public class CoffigLanguage extends Language {
    public static final CoffigLanguage INSTANCE = new CoffigLanguage();

    private CoffigLanguage() {
        super(YAMLLanguage.INSTANCE, "coffig/yaml", "application/yaml");
    }

    @NotNull
    public String getDisplayName() {
        return "Coffig YAML";
    }
}
