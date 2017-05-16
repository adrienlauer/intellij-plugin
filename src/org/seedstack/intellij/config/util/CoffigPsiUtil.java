package org.seedstack.intellij.config.util;

import com.intellij.codeInsight.completion.JavaLookupElementBuilder;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationOwner;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifierListOwner;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.AnnotatedElementsSearch;
import com.intellij.psi.search.searches.ClassInheritorsSearch;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.yaml.psi.YAMLKeyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public final class CoffigPsiUtil {
    private static final String COFFIG_ANNOTATION_QNAME = "org.seedstack.coffig.Config";

    private CoffigPsiUtil() {
        // no instantiation allowed
    }

    public static Optional<PsiClass> resolveConfigAnnotation(Project project) {
        return Optional.ofNullable(JavaPsiFacade.getInstance(project).findClass(COFFIG_ANNOTATION_QNAME, GlobalSearchScope.allScope(project)));
    }

    public static Set<PsiClass> classesExtending(PsiClassType psiClassReferenceType, boolean includeAbstract) {
        Set<PsiClass> results = new HashSet<>();
        Optional.of(psiClassReferenceType)
                .map(PsiClassType::resolve)
                .map(ClassInheritorsSearch::search)
                .ifPresent(psiClasses -> psiClasses.forEach(psiClass -> {
                    if (!includeAbstract && !psiClass.hasModifierProperty("abstract")) {
                        results.add(psiClass);
                    }
                }));
        return results;
    }

    public static Optional<LookupElementBuilder> buildLookup(PsiClass psiClass) {
        String qualifiedName = psiClass.getQualifiedName();
        String name = psiClass.getName();
        if (qualifiedName != null && name != null) {
            return Optional.of(JavaLookupElementBuilder.forClass(psiClass, qualifiedName, true).withPresentableText(name));
        } else {
            return Optional.empty();
        }
    }

    public static Optional<LookupElementBuilder> buildLookup(PsiEnumConstant psiEnumConstant) {
        return Optional.of(LookupElementBuilder.create(psiEnumConstant).withIcon(psiEnumConstant.getIcon(Iconable.ICON_FLAG_VISIBILITY)));
    }

    public static List<String> resolvePath(PsiElement psiElement) {
        List<String> path = new ArrayList<>();
        do {
            if (psiElement instanceof YAMLKeyValue) {
                path.add(0, ((YAMLKeyValue) psiElement).getKeyText());
            }
        } while ((psiElement = psiElement.getParent()) != null);
        return path;
    }

    public static Optional<PsiField> findConfigField(PsiClass configClass, String propertyName) {
        for (PsiField psiField : configClass.getAllFields()) {
            if (propertyName.equals(psiField.getName())) {
                return Optional.of(psiField);
            }
            Optional<String> realName = findConfigAnnotation(psiField).flatMap(CoffigPsiUtil::getConfigValue);
            if (realName.isPresent() && propertyName.equals(realName.get())) {
                return Optional.of(psiField);
            }
        }
        return Optional.empty();
    }

    public static Optional<PsiClass> findConfigClass(List<String> path, PsiClass configAnnotation, Project project) {
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
                findConfigAnnotation(psiClass).flatMap(CoffigPsiUtil::getConfigValue).ifPresent(key -> configClasses.put(key, psiClass));
            }
            return true;
        });
        return configClasses;
    }

    public static Optional<PsiAnnotation> findConfigAnnotation(PsiModifierListOwner psiModifierListOwner) {
        return Optional.ofNullable(psiModifierListOwner.getModifierList())
                .map(PsiAnnotationOwner::getAnnotations)
                .map(Arrays::stream)
                .map(stream -> stream.filter(annotation -> COFFIG_ANNOTATION_QNAME.equals(annotation.getQualifiedName())))
                .flatMap(Stream::findFirst);
    }

    public static Optional<String> getConfigValue(PsiAnnotation psiAnnotation) {
        return Optional.ofNullable(psiAnnotation.getParameterList().getAttributes()[0].getValue())
                .map(PsiElement::getText)
                .map(text -> text.substring(1, text.length() - 1));
    }
}
