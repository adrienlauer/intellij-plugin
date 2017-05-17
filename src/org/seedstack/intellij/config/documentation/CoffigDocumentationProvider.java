package org.seedstack.intellij.config.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.Nullable;
import org.seedstack.intellij.config.util.CoffigPsiUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.seedstack.intellij.config.util.CoffigPsiUtil.isConfigFile;
import static org.seedstack.intellij.config.util.CoffigPsiUtil.resolvePath;

public class CoffigDocumentationProvider implements DocumentationProvider {
    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement psiElement, PsiElement originalElement) {

        return null;
    }

    @Nullable
    @Override
    public List<String> getUrlFor(PsiElement psiElement, PsiElement originalElement) {
        return null;
    }

    @Nullable
    @Override
    public String generateDoc(PsiElement psiElement, @Nullable PsiElement originalElement) {
        if (!isConfigFile(psiElement)) {
            return null;
        }
        Project project = psiElement.getProject();
        CoffigPsiUtil.resolveConfigAnnotation(project)
                .flatMap(psiClass -> CoffigPsiUtil.findConfigClass(resolvePath(psiElement), psiClass, project))
                .map(this::resolveResourceBundle)
                .map(resourceBundleOptional -> resourceBundleOptional
                        .map(resourceBundle -> Arrays.stream(ProjectRootManager.getInstance(project).getContentSourceRoots())
                                .map(virtualFile -> virtualFile.findFileByRelativePath(resourceBundle))
                                .filter(Objects::nonNull)
                                .findFirst()))
                .ifPresent(System.out::println);
        return null;
    }

    private Optional<String> resolveResourceBundle(PsiClass configClass) {
        return Optional.ofNullable(configClass.getQualifiedName()).map(qualifiedName -> String.format("%s.properties", qualifiedName.replace(".", "/")));
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object o, PsiElement psiElement) {
        return null;
    }

    @Nullable
    @Override
    public PsiElement getDocumentationElementForLink(PsiManager psiManager, String s, PsiElement psiElement) {
        return null;
    }
}
