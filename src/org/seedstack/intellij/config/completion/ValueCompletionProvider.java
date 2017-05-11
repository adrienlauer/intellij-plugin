package org.seedstack.intellij.config.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

class ValueCompletionProvider extends AbstractCompletionProvider {
    @Override
    protected void doAddCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        findConfigClasses("");
        completionResultSet.addElement(LookupElementBuilder.create(String.join(".", resolvePath(completionParameters.getPosition()))));
    }
}
