package org.seedstack.intellij.config.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.impl.source.PsiClassReferenceType;
import org.seedstack.intellij.spi.config.CompletionResolver;
import org.seedstack.intellij.spi.config.ValueCompletionResolver;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

import static org.seedstack.intellij.config.util.CoffigPsiUtil.findConfigClass;
import static org.seedstack.intellij.config.util.CoffigPsiUtil.findConfigField;
import static org.seedstack.intellij.config.util.CoffigPsiUtil.resolveConfigAnnotation;

class ValueCompletionProvider implements CompletionResolver {
    private Set<ValueCompletionResolver> completionResolvers = new HashSet<>();

    ValueCompletionProvider() {
        for (ValueCompletionResolver completionResolver : ServiceLoader.load(ValueCompletionResolver.class, ValueCompletionProvider.class.getClassLoader())) {
            completionResolvers.add(completionResolver);
        }
    }

    @Override
    public Stream<LookupElementBuilder> resolve(List<String> path, PsiElement position) {
        Project project = position.getProject();
        Optional<PsiClass> configAnnotation = resolveConfigAnnotation(project);
        if (configAnnotation.isPresent()) {
            if (path.size() > 1) {
                String propertyName = path.get(path.size() - 1);
                List<String> pathToClass = path.subList(0, path.size() - 1);
                return findConfigClass(configAnnotation.get(), project, pathToClass)
                        .flatMap(configClass -> findConfigField(configAnnotation.get(), configClass, propertyName))
                        .map(PsiVariable::getType)
                        .map(psiType -> buildStream(path, position, psiType))
                        .orElse(Stream.empty());
            } else if (path.size() == 1) {
                // TODO handle single values: maybe with findConfigClasses returning a stream that can be filtered (in that case with a filter searching for @SingleValue annotation)
            }
        }
        return Stream.empty();
    }

    private Stream<LookupElementBuilder> buildStream(List<String> path, PsiElement position, PsiType psiType) {
        Optional<PsiClass> rawType;
        PsiType[] parameterTypes;
        if (psiType instanceof PsiClassReferenceType) {
            rawType = Optional.ofNullable(((PsiClassReferenceType) psiType).resolve());
            parameterTypes = ((PsiClassReferenceType) psiType).getParameters();
        } else if (psiType instanceof PsiPrimitiveType) {
            rawType = Optional.ofNullable(position.getContext())
                    .map(((PsiPrimitiveType) psiType)::getBoxedType)
                    .map(PsiClassType::resolve);
            parameterTypes = new PsiType[0];
        } else {
            rawType = Optional.empty();
            parameterTypes = new PsiType[0];
        }
        if (rawType.isPresent()) {
            for (ValueCompletionResolver completionResolver : completionResolvers) {
                if (completionResolver.canHandle(rawType.get())) {
                    return completionResolver.resolveCompletions(path, rawType.get(), parameterTypes);
                }
            }
        }
        return Stream.empty();
    }
}
