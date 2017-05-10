package org.seedstack.intellij.config;

import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.seedstack.intellij.SeedStackIcons;

import javax.swing.*;

public class CoffigFileType extends LanguageFileType implements FileTypeIdentifiableByVirtualFile {
    public static final CoffigFileType INSTANCE = new CoffigFileType();

    private CoffigFileType() {
        super(CoffigLanguage.YAMLINSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Coffig file";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Coffig YAML file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "yaml";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return SeedStackIcons.FILE;
    }

    @Override
    public boolean isMyFileType(@NotNull VirtualFile virtualFile) {
        // TODO: implement detection of location
        return false;
    }
}