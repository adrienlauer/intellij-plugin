package org.seedstack.intellij.config.completion;

import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.seedstack.intellij.config.CoffigLanguage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class AbstractCompletionProvider extends CompletionProvider<CompletionParameters> {
    private Project project;
    private JavaPsiFacade javaPsiFacade;

    protected boolean isConfigFile(@NotNull CompletionParameters completionParameters) {
        return completionParameters.getOriginalFile().getLanguage() == CoffigLanguage.INSTANCE;
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

    protected Map<String, PsiClass> findConfigClasses(String prefix) {
        Map<String, PsiClass> configClasses = new HashMap<>();
        PsiClass configAnnotation = javaPsiFacade.findClass("org.seedstack.coffig.Config", GlobalSearchScope.allScope(project));
        if (configAnnotation != null) {
            Query<PsiClass> configurationClasses = AnnotatedElementsSearch.searchPsiClasses(configAnnotation, GlobalSearchScope.allScope(project));
            for (PsiClass psiClass : configurationClasses.findAll()) {
                if (psiClass.getContainingClass() == null) {
                    PsiModifierList modifierList = psiClass.getModifierList();
                    if (modifierList != null) {
                        PsiAnnotation[] annotations = modifierList.getAnnotations();
                    }
                }
            }
        }
        return configClasses;
    }

    @Override
    protected final void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
        // Check if Coffig file
        if (!isConfigFile(completionParameters)) {
            return;
        }

        // Get useful values
        project = completionParameters.getOriginalFile().getProject();
        javaPsiFacade = JavaPsiFacade.getInstance(project);

        // Process and add completions
        doAddCompletions(completionParameters, processingContext, completionResultSet);
    }

    protected abstract void doAddCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet);
}
