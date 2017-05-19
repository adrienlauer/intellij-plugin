package org.seedstack.intellij.config.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationOwner;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class CoffigPsiUtil {
    private static final String COFFIG_ANNOTATION_QNAME = "org.seedstack.coffig.Config";

    private CoffigPsiUtil() {
        // no instantiation allowed
    }

    public static Optional<PsiClass> resolveConfigAnnotation(Project project) {
        return Optional.ofNullable(JavaPsiFacade.getInstance(project).findClass(COFFIG_ANNOTATION_QNAME, GlobalSearchScope.allScope(project)));
    }

    public static boolean isConfigFile(@NotNull PsiElement psiElement) {
        return Objects.equals(psiElement.getContainingFile().getLanguage().getID(), "coffig/yaml");
    }

    public static boolean isKey(PsiElement position) {
        PsiElement parentContext = position.getParent().getContext();
        PsiElement leftContext = Optional.ofNullable(position.getContext()).map(PsiElement::getPrevSibling).orElse(null);
        return parentContext instanceof YAMLMapping || parentContext instanceof YAMLDocument || leftContext != null && ((LeafPsiElement) leftContext).getElementType() == YAMLTokenTypes.INDENT;
    }

    public static boolean isValue(PsiElement position) {
        return position.getParent().getContext() instanceof YAMLKeyValue;
    }

    public static String[] resolvePath(PsiElement psiElement) {
        List<String> path = new ArrayList<>();
        do {
            if (psiElement instanceof YAMLKeyValue) {
                path.add(0, ((YAMLKeyValue) psiElement).getKeyText());
            }
        } while ((psiElement = psiElement.getParent()) != null);
        return path.toArray(new String[path.size()]);
    }

    public static Optional<PsiField> findConfigField(PsiClass configAnnotation, PsiClass configClass, String propertyName) {
        for (PsiField psiField : configClass.getAllFields()) {
            if (propertyName.equals(psiField.getName())) {
                return Optional.of(psiField);
            }
            Optional<String> realName = findConfigAnnotation(configAnnotation, psiField).flatMap(CoffigPsiUtil::getConfigValue);
            if (realName.isPresent() && propertyName.equals(realName.get())) {
                return Optional.of(psiField);
            }
        }
        return Optional.empty();
    }

    public static Optional<PsiClass> findConfigClass(PsiClass configAnnotation, Project project, String[] path) {
        PsiClass resultClass = null;
        for (String part : path) {
            Optional<PsiClass> psiClass = Optional.ofNullable(findConfigClasses(configAnnotation, project, resultClass).get(part));
            if (psiClass.isPresent()) {
                resultClass = psiClass.get();
            } else {
                return Optional.empty();
            }
        }
        return Optional.ofNullable(resultClass);
    }

    public static Map<String, PsiClass> findConfigClasses(PsiClass configAnnotation, Project project, @Nullable PsiClass containingClass) {
        Map<String, PsiClass> configClasses = new HashMap<>();
        AnnotatedElementsSearch.searchPsiClasses(configAnnotation, GlobalSearchScope.allScope(project)).forEach(psiClass -> {
            if (psiClass.getContainingClass() == containingClass) {
                findConfigAnnotation(configAnnotation, psiClass).flatMap(CoffigPsiUtil::getConfigValue).ifPresent(key -> configClasses.put(key, psiClass));
            }
            return true;
        });
        return configClasses;
    }

    public static Optional<PsiAnnotation> findConfigAnnotation(PsiClass configAnnotation, PsiModifierListOwner psiModifierListOwner) {
        return Optional.ofNullable(psiModifierListOwner.getModifierList())
                .map(PsiAnnotationOwner::getAnnotations)
                .map(Arrays::stream)
                .map(stream -> stream.filter(annotation -> Optional.ofNullable(annotation.getNameReferenceElement())
                        .map(PsiReference::resolve)
                        .map(psiElement -> psiElement == configAnnotation)
                        .orElse(false)
                ))
                .flatMap(Stream::findFirst);
    }

    public static Optional<String> getConfigValue(PsiAnnotation psiAnnotation) {
        return Optional.ofNullable(psiAnnotation.getParameterList().getAttributes()[0].getValue())
                .map(PsiElement::getText)
                .map(text -> text.substring(1, text.length() - 1));
    }
}
