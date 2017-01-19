package com.iselsoft.ptest;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerTestTreeView;
import com.intellij.ide.util.treeView.smartTree.TreeElementWrapper;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.treeStructure.Tree;
import com.iselsoft.ptest.toolWindow.PTestStructureViewElement;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyDecoratable;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;

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
            if (file.getName().equals(PyNames.INIT_DOT_PY)) {
                return null;
            }
            return pyFile;
        }
        return null;
    }

    public static PsiDirectory getPTestPackage(@NotNull final PsiElement element) {
        if (element instanceof PsiDirectory) {
            PsiDirectory pyDirectory = (PsiDirectory) element;
            for (VirtualFile file : (pyDirectory).getVirtualFile().getChildren()) {
                if (file.getName().equals(PyNames.INIT_DOT_PY)) {
                    return pyDirectory;
                }
            }
            return null;
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

    private static boolean hasDecorator(PyDecoratable py, String name) {
        return py.getDecoratorList() != null && py.getDecoratorList().findDecorator(name) != null;
    }
}
