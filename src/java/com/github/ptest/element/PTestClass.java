package com.github.ptest.element;

import com.github.ptest.PTestUtil;
import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyClass;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PTestClass extends PTestElement<PyClass> {

    public PTestClass(PyClass pyClass) {
        super(pyClass);
    }

    @Override
    public boolean setupConfiguration(PTestRunConfiguration configuration) {
        try {
            configuration.setValueForEmptyWorkingDirectory();
            configuration.setRunTest(true);
            String testTarget = PTestUtil.findShortestImportableName(myElement.getContainingFile()) + "."
                    + myElement.getName();
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + myElement.getName());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<PTestElement> getChildren() {
        List<PTestElement> children = new ArrayList<>();

        myElement.visitMethods(pyFunction -> {
            if (PTestUtil.hasDecorator(pyFunction, "Test", null, null)) {
                PTestMethod pTestMethod = new PTestMethod(this, pyFunction);
                // deal with duplicated & inherited tests
                if (children.contains(pTestMethod)) {
                    if (!pTestMethod.isInherited()) {
                        children.remove(pTestMethod);
                        children.add(pTestMethod);
                    }
                } else {
                    children.add(pTestMethod);
                }
            }
            return true;
        }, true, null);

        return children;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public String getPresentableText() {
                ItemPresentation presentation = myElement.getPresentation();
                return presentation != null ? presentation.getPresentableText() : PyNames.UNNAMED_ELEMENT;
            }

            @Override
            public String getLocationString() {
                int childrenCount = getChildren().size();
                if (childrenCount == 0) {
                    return "· no tests";
                }
                if (childrenCount == 1) {
                    return "· 1 test";
                }
                return "· " + childrenCount + " tests";
            }

            @Override
            public Icon getIcon(boolean open) {
                return myElement.getIcon(0);
            }
        };
    }

    @Override
    public boolean equals(Object other) {
        // deal with duplicated test classes, only consider in the same module
        return other instanceof PTestClass && Objects.equals(getValue().getName(), ((PTestClass) other).getValue().getName());
    }

    public static PTestClass createFrom(PsiElement element) {
        final PyClass pyClass = PsiTreeUtil.getParentOfType(element, PyClass.class, false);
        if (pyClass == null) return null;

        if (PTestUtil.hasDecorator(pyClass, "TestClass", null, null)) return new PTestClass(pyClass);
        for (PyClass ancestorClass : pyClass.getAncestorClasses(null)) {
            if (PTestUtil.hasDecorator(ancestorClass, "TestClass", null, null)) {
                return new PTestClass(pyClass);
            }
        }
        return null;
    }
}
