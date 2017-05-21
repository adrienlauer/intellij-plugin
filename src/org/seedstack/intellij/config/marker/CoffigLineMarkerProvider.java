package org.seedstack.intellij.config.marker;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.seedstack.intellij.SeedStackIcons;
import org.seedstack.intellij.config.CoffigClasses;
import org.seedstack.intellij.config.util.CoffigPsiUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CoffigLineMarkerProvider extends RelatedItemLineMarkerProvider {
    private volatile Collection<? super RelatedItemLineMarkerInfo> lastResult;
    private volatile Map<String, PsiClass> configClasses;

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
        if (lastResult != result) {
            synchronized (this) {
                lastResult = result;
                configClasses = new HashMap<>();
                CoffigClasses
                        .from(element.getProject())
                        .all()
                        .forEach(match -> configClasses.put(match.getFullPath(), match.getConfigClass()));
            }
        }
        if (element instanceof YAMLKeyValue) {
            String path = String.join(".", CoffigPsiUtil.resolvePath(element));
            if (configClasses != null) {
                PsiClass configClass = configClasses.get(path);
                if (configClass != null) {
                    PsiIdentifier nameIdentifier = configClass.getNameIdentifier();
                    if (nameIdentifier != null) {
                        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder
                                .create(SeedStackIcons.LOGO)
                                .setTargets(nameIdentifier)
                                .setTooltipText("Navigate to configuration class");
                        result.add(builder.createLineMarkerInfo(element));
                    }
                }
            }
        }
    }
}
