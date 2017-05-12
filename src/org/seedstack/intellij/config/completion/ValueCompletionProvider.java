package org.seedstack.intellij.config.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

class ValueCompletionProvider extends AbstractCompletionProvider {
    @Override
    protected void doAddCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        List<String> path = resolvePath(completionParameters.getPosition());
        List<String> pathToClass = path.subList(0, path.size() - 1);
        String propertyName = path.get(path.size() - 1);

        Optional<PsiType> optionalPsiType = findConfigClass(pathToClass)
                .flatMap(configClass -> findConfigField(configClass, propertyName))
                .map(PsiVariable::getType);

        if (optionalPsiType.isPresent()) {
            PsiType psiType = optionalPsiType.get();
            System.out.println(psiType.toString());
        }
    }
}