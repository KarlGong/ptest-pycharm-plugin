package com.github.ptest.element;

import com.github.ptest.PTestUtil;
import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyClass;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PTestClass extends PTestElement<PyClass> {
    private boolean myIsRedeclared = false;

    public PTestClass(PyClass pyClass) {
        super(pyClass);
    }

    public void setRedeclared(boolean isRedeclared) {
        myIsRedeclared = isRedeclared;
    }

    public boolean isRedeclared() {
        return myIsRedeclared;
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
                // deal with redeclared tests
                boolean foundDuplicated = false;
                for (PTestElement c : children) {
                    if (c instanceof PTestMethod) {
                        PTestMethod child = (PTestMethod) c;
                        if (Objects.equals(pTestMethod.getValue().getName(), child.getValue().getName())) {
                            if (!pTestMethod.isInherited()) {
                                pTestMethod.setRedeclared(true);
                                children.add(pTestMethod);
                            }
                            foundDuplicated = true;
                            break;
                        }
                    }
                }
                if (!foundDuplicated) {
                    children.add(pTestMethod);
                }
            }

            for (String configName : new String[]{"BeforeMethod", "AfterMethod", "BeforeGroup", "AfterGroup",
                    "BeforeClass", "AfterClass", "BeforeSuite", "AfterSuite"}) {
                if (PTestUtil.hasDecorator(pyFunction, configName, null, null)) {
                    PTestConfiguration pTestConfiguration = new PTestConfiguration(this, pyFunction, configName);
                    // deal with redeclared test configurations
                    boolean foundDuplicated = false;
                    for (PTestElement c : children) {
                        if (c instanceof PTestConfiguration) {
                            PTestConfiguration child = (PTestConfiguration) c;
                            if (Objects.equals(pTestConfiguration.getGroup(), child.getGroup())
                                    && Objects.equals(pTestConfiguration.getName(), child.getName())) {
                                if (!(pTestConfiguration.isInherited()
                                        && Objects.equals(pTestConfiguration.getValue().getName(), child.getValue().getName()))) {
                                    pTestConfiguration.setRedeclared(true);
                                    children.add(pTestConfiguration);
                                }
                                foundDuplicated = true;
                                break;
                            }
                        }
                    }
                    if (!foundDuplicated) {
                        children.add(pTestConfiguration);
                    }
                }
            }
            return true;
        }, true, null);

        return children;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ColoredItemPresentation() {
            @Override
            public String getPresentableText() {
                ItemPresentation presentation = myElement.getPresentation();
                return presentation != null ? presentation.getPresentableText() : PyNames.UNNAMED_ELEMENT;
            }

            @Override
            public TextAttributesKey getTextAttributesKey() {
                return isRedeclared() ? CodeInsightColors.GENERIC_SERVER_ERROR_OR_WARNING : null;
            }

            @Override
            public String getLocationString() {
                long childrenCount = getChildren().stream().filter(pTestElement -> pTestElement instanceof PTestMethod).count();
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
