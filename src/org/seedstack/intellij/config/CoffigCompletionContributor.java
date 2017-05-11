package org.seedstack.intellij.config;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.patterns.PlatformPatterns;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLTokenTypes;

public class CoffigCompletionContributor extends CompletionContributor {
    public CoffigCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(YAMLTokenTypes.TEXT).withLanguage(YAMLLanguage.INSTANCE), new CoffigCompletionProvider());
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(YAMLTokenTypes.SCALAR_STRING).withLanguage(YAMLLanguage.INSTANCE), new CoffigCompletionProvider());
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(YAMLTokenTypes.SCALAR_DSTRING).withLanguage(YAMLLanguage.INSTANCE), new CoffigCompletionProvider());
    }
}