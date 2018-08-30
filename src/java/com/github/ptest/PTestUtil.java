package com.github.ptest;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerTestTreeView;
import com.intellij.ide.util.treeView.smartTree.TreeElementWrapper;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import com.intellij.ui.treeStructure.Tree;
import com.github.ptest.toolWindow.PTestStructureViewElement;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.QualifiedNameFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

public class PTestUtil {
    public static List<PTestStructureViewElement> getSelectedPTestTargetsInTW(@NotNull ConfigurationContext context) {
        List<PTestStructureViewElement> pTestTargets = new ArrayList<>();
        try {
            TreePath[] treePaths = ((Tree) PlatformDataKeys.CONTEXT_COMPONENT.getData(context.getDataContext())).getSelectionPaths();
            for (TreePath treePath : treePaths) {
                PTestStructureViewElement element = (PTestStructureViewElement) ((TreeElementWrapper) ((DefaultMutableTreeNode) treePath.getLastPathComponent()).getUserObject()).getValue();
                pTestTargets.add(element);
            }
        } catch (Exception ignored) { }
        return pTestTargets;
    }

    public static AbstractTestProxy getSelectedPTestMethodInSMT(@NotNull ConfigurationContext context) {
        try {
            return ((SMTRunnerTestTreeView) PlatformDataKeys.CONTEXT_COMPONENT.getData(context.getDataContext())).getSelectedTest();
        } catch (Exception e) {
            return null;
        }
    }

    public static PyFunction getPTestMethod(@NotNull final PsiElement element) {
        final PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class, false);
        if (pyFunction == null) return null;

        final PyClass containingClass = pyFunction.getContainingClass();
        if (containingClass == null) return null;

        return hasDecorator(pyFunction, "Test") ? pyFunction : null;
    }

    public static PyClass getPTestClass(@NotNull final PsiElement element) {
        final PyClass pyClass = PsiTreeUtil.getParentOfType(element, PyClass.class, false);
        if (pyClass == null) return null;

        if (hasDecorator(pyClass, "TestClass")) return pyClass;
        for (PyClass ancestorClass : pyClass.getAncestorClasses(null)) {
            if (hasDecorator(ancestorClass, "TestClass")) {
                return pyClass;
            }
        }
        return null;
    }

    public static PyFile getPTestModule(@NotNull final PsiElement element) {
        if (element instanceof PyFile) {
            PyFile pyFile = (PyFile) element;
            VirtualFile file = pyFile.getVirtualFile();
            return !file.getName().equals(PyNames.INIT_DOT_PY) ? pyFile : null;
        }
        return null;
    }

    public static PsiDirectory getPTestPackage(@NotNull final PsiElement element) {
        if (element instanceof PsiDirectory) {
            PsiDirectory pyDirectory = (PsiDirectory) element;
            return hasAnyPyFile(pyDirectory) ? pyDirectory : null;
        }
        return null;
    }

    public static PsiFile getPTestXML(@NotNull final PsiElement element) {
        if (element instanceof PsiFile) {
            PsiFile xmlFile = (PsiFile) element;
            VirtualFile file = xmlFile.getVirtualFile();
            return file.getExtension() != null && file.getExtension().equalsIgnoreCase("xml") ? xmlFile : null;
        }
        return null;
    }

    public static boolean hasDecorator(PyDecoratable py, String name) {
        return py.getDecoratorList() != null
                && py.getDecoratorList().findDecorator(name) != null;
    }

    public static boolean hasDecoratorWithParam(PyDecoratable py, String decoratorName, String paramName) {
        return py.getDecoratorList() != null
                && py.getDecoratorList().findDecorator(decoratorName) != null
                && py.getDecoratorList().findDecorator(decoratorName).getKeywordArgument(paramName) != null;
    }

    public static String findShortestImportableName(PsiFileSystemItem file) {
        QualifiedName qName = QualifiedNameFinder.findShortestImportableQName(file);
        if (qName != null) return qName.toString();

        VirtualFile projectDir = file.getProject().getBaseDir();
        List<String> importableNames = new ArrayList<>();
        PsiFileSystemItem currentFile = file;

        while (!currentFile.getVirtualFile().equals(projectDir)) {
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
    
    @Nullable
    public static PsiElement getPsiElement(@Nullable ConfigurationContext context) {
        // no context
        if (context == null) return null;
        // location is invalid
        final Location location = context.getLocation();
        if (location == null) return null;
        // location is white space
        PsiElement element = location.getPsiElement();
        if (element instanceof PsiWhiteSpace) {
            PsiFile containingFile = element.getContainingFile();
            int textOffset = element.getTextOffset();
            element = PyUtil.findNonWhitespaceAtOffset(containingFile, textOffset);
            if (element == null) {
                element = PyUtil.findNonWhitespaceAtOffset(containingFile, textOffset - 1);
                if (element == null) {
                    element = PyUtil.findNonWhitespaceAtOffset(containingFile, textOffset + 1);
                }
            }
        }
        return element;
    }
    
    @Nullable
    public static PsiElement getPsiElement(@Nullable AnActionEvent e) {
        if (e == null) return null;
        DataContext dataContext = e.getDataContext();
        ConfigurationContext context = ConfigurationContext.getFromContext(dataContext);
        return getPsiElement(context);
    }
}
