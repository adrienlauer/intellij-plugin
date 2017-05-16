package org.seedstack.intellij.spi.config;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;

import java.util.List;
import java.util.stream.Stream;

public interface CompletionResolver {
    Stream<LookupElementBuilder> resolve(List<String> path, PsiElement position);
}
