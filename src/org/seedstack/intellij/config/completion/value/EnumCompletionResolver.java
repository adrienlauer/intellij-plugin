package org.seedstack.intellij.config.completion.value;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiEnumConstant;
import com.intellij.psi.PsiType;
import org.seedstack.intellij.config.util.CoffigPsiUtil;
import org.seedstack.intellij.spi.config.ValueCompletionResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class EnumCompletionResolver implements ValueCompletionResolver {
    @Override
    public boolean canHandle(PsiClass rawType) {
        return rawType.isEnum();
    }

    @Override
    public Stream<LookupElementBuilder> resolveCompletions(List<String> path, PsiClass rawType, PsiType[] parameterTypes) {
        return Arrays.stream(rawType.getChildren())
                .filter(child -> child instanceof PsiEnumConstant)
                .map(child -> CoffigPsiUtil.buildLookup((PsiEnumConstant) child))
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
