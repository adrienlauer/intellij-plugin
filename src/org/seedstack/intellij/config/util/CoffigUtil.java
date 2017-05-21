package org.seedstack.intellij.config.util;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLFile;
import org.seedstack.intellij.config.yaml.CoffigYAMLFileType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class CoffigUtil {
    private CoffigUtil() {
        // no instantiation allowed
    }

    public static List<YAMLDocument> findCoffigKey(Project project, String path) {
        return null;
    }


    public static List<YAMLDocument> findCoffigDocuments(Project project) {
        List<YAMLDocument> result = new ArrayList<>();
        Collection<VirtualFile> virtualFiles = FileBasedIndex.getInstance().getContainingFiles(
                FileTypeIndex.NAME,
                CoffigYAMLFileType.INSTANCE,
                GlobalSearchScope.allScope(project));
        for (VirtualFile virtualFile : virtualFiles) {
            YAMLFile coffigFile = (YAMLFile) PsiManager.getInstance(project).findFile(virtualFile);
            if (coffigFile != null) {
                result.addAll(coffigFile.getDocuments());
            }
        }
        return result;
    }
}
