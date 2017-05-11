package org.seedstack.intellij.config;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.ArrayList;
import java.util.List;

public class CoffigCompletionProvider extends CompletionProvider<CompletionParameters> {
    @Override
    protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        completionResultSet.addElement(LookupElementBuilder.create(String.join(".", resolvePath(completionParameters.getPosition()))));

    }

    private List<String> resolvePath(PsiElement psiElement) {
        List<String> path = new ArrayList<>();
        do {
            if (psiElement instanceof YAMLKeyValue) {
                path.add(0, ((YAMLKeyValue) psiElement).getKeyText());
            }
        } while ((psiElement = psiElement.getParent()) != null);
        return path;
    }
}
