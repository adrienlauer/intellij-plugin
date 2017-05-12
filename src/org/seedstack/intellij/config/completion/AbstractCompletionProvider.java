package org.seedstack.intellij.config.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationOwner;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.seedstack.intellij.config.CoffigLanguage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

abstract class AbstractCompletionProvider extends CompletionProvider<CompletionParameters> {
    private static final String COFFIG_ANNOTATION_QNAME = "org.seedstack.coffig.Config";
    private PsiClass configAnnotation;
    private Project project;
    private JavaPsiFacade javaPsiFacade;

    @Override
    protected final void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        if (isConfigFile(completionParameters)) {
            Project project = completionParameters.getOriginalFile().getProject();
            JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
            Optional<PsiClass> optionalConfigAnnotation = Optional.ofNullable(javaPsiFacade.findClass(COFFIG_ANNOTATION_QNAME, GlobalSearchScope.allScope(project)));
            if (optionalConfigAnnotation.isPresent()) {
                this.project = project;
                this.javaPsiFacade = javaPsiFacade;
                this.configAnnotation = optionalConfigAnnotation.get();
                doAddCompletions(completionParameters, processingContext, completionResultSet);
            }
        }
    }

    protected boolean isConfigFile(@NotNull CompletionParameters completionParameters) {
        return completionParameters.getOriginalFile().getLanguage() == CoffigLanguage.INSTANCE;
    }

    protected Optional<PsiField> findConfigField(PsiClass configClass, String propertyName) {
        for (PsiField psiField : configClass.getAllFields()) {
            if (propertyName.equals(psiField.getName())) {
                return Optional.of(psiField);
            }
            Optional<String> realName = findConfigAnnotation(psiField).flatMap(this::getConfigValue);
            if (realName.isPresent() && propertyName.equals(realName.get())) {
                return Optional.of(psiField);
            }
        }
        return Optional.empty();
    }

    protected Optional<PsiClass> findConfigClass(List<String> path) {
        PsiClass result = null;
        for (String part : path) {
            Optional<PsiClass> psiClass = Optional.ofNullable(findConfigClasses(result).get(part));
            if (psiClass.isPresent()) {
                result = psiClass.get();
            } else {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(result);
    }

    protected Map<String, PsiClass> findConfigClasses(@Nullable PsiClass containingClass) {
        Map<String, PsiClass> configClasses = new HashMap<>();
        AnnotatedElementsSearch.searchPsiClasses(configAnnotation, GlobalSearchScope.allScope(project)).forEach(psiClass -> {
            if (psiClass.getContainingClass() == containingClass) {
                findConfigAnnotation(psiClass).flatMap(this::getConfigValue).ifPresent(key -> configClasses.put(key, psiClass));
            }
            return true;
        });
        return configClasses;
    }

    protected Optional<PsiAnnotation> findConfigAnnotation(PsiModifierListOwner psiModifierListOwner) {
        return Optional.ofNullable(psiModifierListOwner.getModifierList())
                .map(PsiAnnotationOwner::getAnnotations)
                .map(Arrays::stream)
                .map(stream -> stream.filter(annotation -> COFFIG_ANNOTATION_QNAME.equals(annotation.getQualifiedName())))
                .flatMap(Stream::findFirst);
    }

    protected Optional<String> getConfigValue(PsiAnnotation psiAnnotation) {
        return Optional.of(psiAnnotation)
                .map(PsiAnnotation::getParameterList)
                .map(PsiElement::getFirstChild)
                .map(PsiElement::getFirstChild)
                .map(PsiElement::getText)
                .map(str -> str.startsWith("\"") && str.endsWith("\"") ? str.substring(1, str.length() - 1) : null);
    }

    protected List<String> resolvePath(PsiElement psiElement) {
        List<String> path = new ArrayList<>();
        do {
            if (psiElement instanceof YAMLKeyValue) {
                path.add(0, ((YAMLKeyValue) psiElement).getKeyText());
            }
        } while ((psiElement = psiElement.getParent()) != null);
        return path;
    }

    protected abstract void doAddCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet);
}
