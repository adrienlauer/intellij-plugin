package org.seedstack.intellij.coffig;

import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;

public class CoffigLanguage extends Language {
    public static final CoffigLanguage INSTANCE = new CoffigLanguage();

    private CoffigLanguage() {
        super(YAMLLanguage.INSTANCE, "COFFIG", "application/coffig");
    }

    @NotNull
    public String getDisplayName() {
        return "Coffig";
    }
}
