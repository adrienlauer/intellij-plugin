package org.seedstack.intellij.spi.config;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;

import java.util.List;
import java.util.stream.Stream;

public interface ValueCompletionResolver {
    boolean canHandle(PsiClass rawType);

    Stream<LookupElementBuilder> resolveCompletions(List<String> path, PsiClass rawType, PsiType[] parameterTypes);
}
