package com.github.ptest;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyDecoratable;
import com.jetbrains.python.psi.PyDecorator;
import com.jetbrains.python.psi.PyDecoratorList;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.resolve.QualifiedNameFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PTestUtil {
    public static boolean hasDecorator(@NotNull PyDecoratable py, @Nullable String decoratorName,
                                       @Nullable String paramName, @Nullable String paramValue) {
        PyDecoratorList decoratorList = py.getDecoratorList();
        if (decoratorList == null) return false;
        if (decoratorName == null) return true;
        PyDecorator decorator = decoratorList.findDecorator(decoratorName);
        if (decorator == null) return false;
        if (paramName == null) return true;
        PyExpression valueExp = decorator.getKeywordArgument(paramName);
        if (valueExp == null) return false;
        if (paramValue == null) return true;
        return valueExp.getText().equals(paramValue);
    }

    public static String findShortestImportableName(PsiFileSystemItem file) {
        QualifiedName qName = QualifiedNameFinder.findShortestImportableQName(file);
        if (qName != null) return qName.toString();

        String projectDir = file.getProject().getBasePath();
        List<String> importableNames = new ArrayList<>();
        PsiFileSystemItem currentFile = file;

        while (!currentFile.getVirtualFile().getPath().equals(projectDir)) {
            importableNames.add(0, currentFile.getVirtualFile().getNameWithoutExtension());
            currentFile = currentFile.getParent();
        }

        return String.join(".", importableNames);
    }

    public static boolean hasAnyPyFile(PsiDirectory directory) {
        for (PsiFile file : directory.getFiles()) {
            String fileName = file.getVirtualFile().getName();
            String extension = file.getVirtualFile().getExtension();
            if (extension != null && extension.equalsIgnoreCase("py") && !fileName.equals(PyNames.INIT_DOT_PY)) {
                return true;
            }
        }
        for (PsiDirectory subDirectory : directory.getSubdirectories()) {
            if (hasAnyPyFile(subDirectory)) {
                return true;
            }
        }
        return false;
    }
}
