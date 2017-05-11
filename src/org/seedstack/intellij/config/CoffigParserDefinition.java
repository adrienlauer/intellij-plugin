package org.seedstack.intellij.config;

import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.yaml.YAMLParserDefinition;

public class CoffigParserDefinition extends YAMLParserDefinition {
    private static final IFileElementType FILE = new IFileElementType(CoffigLanguage.INSTANCE);

    public IFileElementType getFileNodeType() {
        return FILE;
    }

    public PsiFile createFile(final FileViewProvider viewProvider) {
        return new CoffigYAMLFileImpl(viewProvider);
    }
}