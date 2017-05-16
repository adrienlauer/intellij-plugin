package org.seedstack.intellij.config.completion.value;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiWildcardType;
import org.seedstack.intellij.config.util.CoffigPsiUtil;
import org.seedstack.intellij.spi.config.ValueCompletionResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ClassCompletionResolver implements ValueCompletionResolver {
    private static final String JAVA_LANG_CLASS = Class.class.getName();

    @Override
    public boolean canHandle(PsiClass rawType) {
        return JAVA_LANG_CLASS.equals(rawType.getQualifiedName());
    }

    @Override
    public Stream<LookupElementBuilder> resolveCompletions(List<String> path, PsiClass rawType, PsiType[] parameterTypes) {
        Stream<PsiClass> psiClassStream = null;
        if (parameterTypes.length == 1 && parameterTypes[0] instanceof PsiWildcardType) {
            PsiWildcardType psiWildcardType = ((PsiWildcardType) parameterTypes[0]);
            if (psiWildcardType.isBounded()) {
                if (psiWildcardType.isExtends()) {
                    psiClassStream = CoffigPsiUtil.classesExtending((PsiClassType) psiWildcardType.getExtendsBound(), false).stream();
                } else if (psiWildcardType.isSuper()) {
                    psiClassStream = Arrays.stream(psiWildcardType.getSuperBound().getSuperTypes())
                            .map(psiType -> (PsiClassType) psiType)
                            .map(PsiClassType::resolve);
                }
            }
        }
        if (psiClassStream != null) {
            return psiClassStream.map(CoffigPsiUtil::buildLookup).filter(Optional::isPresent).map(Optional::get);
        } else {
            return Stream.empty();
        }
    }
}
