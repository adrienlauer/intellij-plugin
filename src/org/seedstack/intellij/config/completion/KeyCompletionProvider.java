package org.seedstack.intellij.config.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiVariable;
import org.seedstack.intellij.config.util.CoffigPsiUtil;
import org.seedstack.intellij.spi.config.CompletionResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.seedstack.intellij.config.util.CoffigPsiUtil.findConfigAnnotation;
import static org.seedstack.intellij.config.util.CoffigPsiUtil.findConfigClasses;
import static org.seedstack.intellij.config.util.CoffigPsiUtil.resolveConfigAnnotation;

class KeyCompletionProvider implements CompletionResolver {
    @Override
    public Stream<LookupElementBuilder> resolve(List<String> path, PsiElement position) {
        Project project = position.getProject();
        Optional<PsiClass> configAnnotation = resolveConfigAnnotation(project);
        if (!configAnnotation.isPresent()) {
            return Stream.empty();
        }

        PsiClass containingClass = null;
        if (!path.isEmpty()) {
            for (String part : path) {
                PsiClass subConfigClass = findConfigClasses(configAnnotation.get(), project, containingClass).get(part);
                if (subConfigClass == null) {
                    return Stream.empty();
                } else {
                    containingClass = subConfigClass;
                }
            }
        }

        Stream<String> keyStream = findConfigClasses(configAnnotation.get(), project, containingClass).keySet().stream();
        if (containingClass != null) {
            keyStream = Stream.concat(keyStream, Arrays.stream(containingClass.getAllFields()).map(this::resolveFieldName));
        }

        return keyStream.map(key -> key.split("\\.")[0])
                .map(LookupElementBuilder::create);
    }

    private String resolveFieldName(PsiField psiField) {
        return findConfigAnnotation(psiField)
                .flatMap(CoffigPsiUtil::getConfigValue)
                .orElseGet(() ->
                        Optional.of(psiField)
                                .map(PsiVariable::getType)
                                .filter(psiType -> psiType instanceof PsiClassType)
                                .map(psiType -> ((PsiClassType) psiType).resolve())
                                .flatMap(CoffigPsiUtil::findConfigAnnotation)
                                .flatMap(CoffigPsiUtil::getConfigValue)
                                .orElse(psiField.getName())
                );
    }
}
