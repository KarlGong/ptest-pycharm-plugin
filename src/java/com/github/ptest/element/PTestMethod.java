package com.github.ptest.element;

import com.github.ptest.PTestUtil;
import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.LayeredIcon;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import icons.RemoteServersIcons;

import javax.swing.*;

public class PTestMethod extends PTestElement<PyFunction> {
    private PTestClass myParent;

    public PTestMethod(PTestClass parent, PyFunction pyFunction) {
        super(pyFunction);
        myParent = parent;
    }

    public boolean isInherited() {
        return myElement.getContainingClass() != myParent.getElement();
    }

    public boolean isDataProvided() {
        return PTestUtil.hasDecorator(myElement, "Test", "data_provider", null);
    }

    @Override
    public boolean setupConfiguration(PTestRunConfiguration configuration) {
        try {
            configuration.setValueForEmptyWorkingDirectory();
            configuration.setRunTest(true);
            String testName = myElement.getName();
            String testTarget;
            if (isInherited()) {
                testTarget = PTestUtil.findShortestImportableName(myParent.getElement().getContainingFile()) + "."
                        + myParent.getElement().getName() + "." + testName;
            } else {
                testTarget = PTestUtil.findShortestImportableName(myElement.getContainingFile()) + "."
                        + myElement.getContainingClass().getName() + "." + testName;
            }
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptest " + myElement.getContainingClass().getName() + "." + testName);
            configuration.setActionName("ptest " + testName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public String getPresentableText() {
                return myElement.getName();
            }

            @Override
            public String getLocationString() {
                return null;
            }

            @Override
            public Icon getIcon(boolean open) {
                Icon normalIcon = myElement.getIcon(0);
                if (isInherited()) {
                    normalIcon = AllIcons.Nodes.MethodReference;
                }
                if (isDataProvided()) {
                    LayeredIcon icon = new LayeredIcon(2);
                    icon.setIcon(normalIcon, 0);
                    icon.setIcon(RemoteServersIcons.ResumeScaled, 1);
                    return icon;
                }
                return normalIcon;
            }
        };
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    public static PTestMethod createFrom(PsiElement element) {
        final PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class, false);
        if (pyFunction == null) return null;

        final PyClass containingClass = pyFunction.getContainingClass();
        if (containingClass == null) return null;

        if (!PTestUtil.hasDecorator(pyFunction, "Test", null, null)) return null;

        PTestClass pTestClass = PTestClass.createFrom(element);
        if (pTestClass == null) return null;

        return new PTestMethod(pTestClass, pyFunction);
    }
}
