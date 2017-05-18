package org.seedstack.intellij.config.documentation;

import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiPackage;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.seedstack.intellij.config.util.CoffigPsiUtil.findConfigClass;
import static org.seedstack.intellij.config.util.CoffigPsiUtil.findConfigClasses;
import static org.seedstack.intellij.config.util.CoffigPsiUtil.isConfigFile;
import static org.seedstack.intellij.config.util.CoffigPsiUtil.resolveConfigAnnotation;
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
        return resolveConfigInfo(psiElement).map(this::buildDescription).orElse(null);
    }

    private String buildDescription(ConfigInfo configInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body>");
        sb.append("<h1>").append(configInfo.getPath()).append("</h1>");
        if (configInfo.getLongDescription() != null) {
            sb.append(configInfo.getLongDescription());
        } else {
            sb.append(configInfo.getDescription());
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private Optional<ConfigInfo> resolveConfigInfo(PsiElement psiElement) {
        if (isConfigFile(psiElement)) {
            Project project = psiElement.getProject();
            List<String> path = resolvePath(psiElement);
            if (!path.isEmpty()) {
                String propertyName = path.get(path.size() - 1);
                List<String> pathToClass;
                if (path.size() > 1) {
                    pathToClass = path.subList(0, path.size() - 1);
                } else {
                    pathToClass = new ArrayList<>();
                }

                return resolveConfigAnnotation(project)
                        .flatMap(configAnnotation -> findConfigClass(configAnnotation, project, pathToClass))
                        .flatMap(configClass -> findResourceBundle(project, configClass))
                        .flatMap(propertiesFile -> extractConfigInfo(propertiesFile, String.join(".", path), propertyName));
            }
        }
        return Optional.empty();
    }

    private Optional<ConfigInfo> extractConfigInfo(PropertiesFile propertiesFile, String path, String propertyName) {
        Optional<String> description = Optional.ofNullable(propertiesFile.findPropertyByKey(propertyName)).map(IProperty::getValue);
        if (description.isPresent()) {
            ConfigInfo configInfo = new ConfigInfo(path, description.get());
            Optional.ofNullable(propertiesFile.findPropertyByKey(propertyName + ".long")).map(IProperty::getValue).ifPresent(configInfo::setLongDescription);
            return Optional.of(configInfo);
        }
        return Optional.empty();
    }

    private Optional<PropertiesFile> findResourceBundle(Project project, PsiClass configClass) {
        String qualifiedName = configClass.getQualifiedName();
        if (qualifiedName != null) {
            int lastDotIndex = qualifiedName.lastIndexOf(".");
            String packageName = qualifiedName.substring(0, lastDotIndex);
            String className = qualifiedName.substring(lastDotIndex + 1);
            PsiPackage psiPackage = JavaPsiFacade.getInstance(project).findPackage(packageName);
            if (psiPackage != null) {
                return Arrays.stream(psiPackage.getFiles(GlobalSearchScope.allScope(project)))
                        .filter(psiFile -> psiFile instanceof PropertiesFile && psiFile.getVirtualFile().getNameWithoutExtension().equals(className))
                        .map(psiFile -> (PropertiesFile) psiFile)
                        .findFirst();
            }
        }
        return Optional.empty();
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

    private static class ConfigInfo {
        private final String path;
        private final String description;
        private String longDescription;

        private ConfigInfo(String path, String description) {
            this.path = path;
            this.description = description;
        }

        public String getPath() {
            return path;
        }

        private String getDescription() {
            return description;
        }

        public String getLongDescription() {
            return longDescription;
        }

        public void setLongDescription(String longDescription) {
            this.longDescription = longDescription;
        }
    }
}
