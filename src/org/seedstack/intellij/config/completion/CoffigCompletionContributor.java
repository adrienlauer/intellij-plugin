package org.seedstack.intellij.config.completion;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.YAMLLanguage;
import org.jetbrains.yaml.YAMLTokenTypes;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static org.seedstack.intellij.config.util.CoffigPsiUtil.resolvePath;

public class CoffigCompletionContributor extends CompletionContributor {
    private static final DispatchingProvider DISPATCHING_COMPLETION_PROVIDER = new DispatchingProvider();

    public CoffigCompletionContributor() {
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(YAMLTokenTypes.TEXT).withLanguage(YAMLLanguage.INSTANCE), DISPATCHING_COMPLETION_PROVIDER);
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(YAMLTokenTypes.SCALAR_STRING).withLanguage(YAMLLanguage.INSTANCE), DISPATCHING_COMPLETION_PROVIDER);
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(YAMLTokenTypes.SCALAR_DSTRING).withLanguage(YAMLLanguage.INSTANCE), DISPATCHING_COMPLETION_PROVIDER);
        extend(CompletionType.BASIC, PlatformPatterns.psiElement(YAMLTokenTypes.SCALAR_KEY).withLanguage(YAMLLanguage.INSTANCE), DISPATCHING_COMPLETION_PROVIDER);
    }

    private static class DispatchingProvider extends CompletionProvider<CompletionParameters> {
        private static final String MACRO_START = "${";
        private static final String MACRO_END = "}";
        private static final KeyCompletionProvider KEY_COMPLETION_PROVIDER = new KeyCompletionProvider();
        private static final ValueCompletionProvider VALUE_COMPLETION_PROVIDER = new ValueCompletionProvider();

        @Override
        protected void addCompletions(@NotNull CompletionParameters completionParameters, ProcessingContext processingContext, @NotNull CompletionResultSet completionResultSet) {
            Stream<LookupElementBuilder> stream = null;
            if (isConfigFile(completionParameters)) {
                PsiElement position = completionParameters.getPosition();
                PsiElement parentContext = position.getParent().getContext();
                PsiElement leftContext = Optional.ofNullable(position.getContext()).map(PsiElement::getPrevSibling).orElse(null);
                List<String> path = resolvePath(position);

                if (parentContext instanceof YAMLMapping || leftContext != null && ((LeafPsiElement) leftContext).getElementType() == YAMLTokenTypes.INDENT) {
                    stream = KEY_COMPLETION_PROVIDER.resolve(path, position);
                } else if (parentContext instanceof YAMLKeyValue) {
                    String prefix = completionResultSet.getPrefixMatcher().getPrefix();
                    if (isInsideMacro(prefix)) {
                        String reference = prefix.substring(prefix.lastIndexOf(MACRO_START) + MACRO_START.length());
                        String[] referencePath = reference.isEmpty() ? new String[0] : reference.split("\\.");
                        stream = KEY_COMPLETION_PROVIDER.resolve(Arrays.asList(referencePath), position);
                    } else {
                        stream = VALUE_COMPLETION_PROVIDER.resolve(path, position);
                    }
                }
            }

            if (stream != null) {
                stream.forEach(element -> {
                    System.out.println(element);
                    completionResultSet.addElement(element);
                });
            }
        }

        private boolean isConfigFile(@NotNull CompletionParameters completionParameters) {
            return Objects.equals(completionParameters.getOriginalFile().getLanguage().getID(), "coffig/yaml");
        }

        private boolean isInsideMacro(String prefix) {
            return prefix.lastIndexOf(MACRO_START) > prefix.lastIndexOf(MACRO_END);
        }
    }
}
