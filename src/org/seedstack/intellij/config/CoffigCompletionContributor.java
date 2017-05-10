package org.seedstack.intellij.config;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class CoffigCompletionContributor extends CompletionContributor {
    private static final IElementType TEXT;

    static {
        try {
            Class<?> YAMLTokenTypes = Class.forName("org.jetbrains.yaml.YAMLTokenTypes");
            TEXT = (IElementType) YAMLTokenTypes.getField("TEXT").get(null);
        } catch (Exception e) {
            throw new RuntimeException("Unable to access YAML language", e);
        }
    }

    public CoffigCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement(TEXT).withLanguage(CoffigLanguage.YAMLINSTANCE),
                new CompletionProvider<CompletionParameters>() {
                    public void addCompletions(@NotNull CompletionParameters parameters, ProcessingContext context, @NotNull CompletionResultSet resultSet) {
                        resultSet.addElement(LookupElementBuilder.create("Hello"));
                    }
                }
        );
    }
}