package com.github.ptest.element;

import com.github.ptest.PTestUtil;
import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PTestModule extends PTestElement<PyFile> {

    public PTestModule(PyFile pyFile) {
        super(pyFile);
    }

    @Override
    public boolean setupConfiguration(PTestRunConfiguration configuration) {
        try {
            configuration.setValueForEmptyWorkingDirectory();
            configuration.setRunTest(true);
            String testTarget = PTestUtil.findShortestImportableName(getValue());
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + testTarget);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<PTestElement> getChildren() {
        List<PTestElement> children = new ArrayList<>();

        for (PyClass pyClass : getValue().getTopLevelClasses()) {
            if (PTestUtil.hasDecorator(pyClass, "TestClass", null, null)) {
                PTestClass pTestClass = new PTestClass(pyClass);
                // deal with redeclared test classes
                for (PTestElement c : children) {
                    if (c instanceof PTestClass) {
                        PTestClass child = (PTestClass) c;
                        if (Objects.equals(pTestClass.getValue().getName(), child.getValue().getName())) {
                            pTestClass.addError("Redeclared test class " + pTestClass.getValue().getName());
                            break;
                        }
                    }
                }
                children.add(pTestClass);
            } else {
                for (PyClass ancestorClass : pyClass.getAncestorClasses(null)) {
                    if (PTestUtil.hasDecorator(ancestorClass, "TestClass", null, null)) {
                        PTestClass pTestClass = new PTestClass(pyClass);
                        // deal with redeclared test classes
                        for (PTestElement c : children) {
                            if (c instanceof PTestClass) {
                                PTestClass child = (PTestClass) c;
                                if (Objects.equals(pTestClass.getValue().getName(), child.getValue().getName())) {
                                    pTestClass.addError("Redeclared test class " + pTestClass.getValue().getName());
                                    break;
                                }
                            }
                        }
                        children.add(pTestClass);
                        break;
                    }
                }
            }
        }
        return children;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @Override
            public String getPresentableText() {
                return getValue().getName();
            }

            @Override
            public String getLocationString() {
                return null;
            }

            @Override
            public Icon getIcon(boolean b) {
                return getValue().getIcon(0);
            }
        };
    }

    public static PTestModule createFrom(PsiElement element) {
        if (element instanceof PyFile) {
            PyFile pyFile = (PyFile) element;
            VirtualFile file = pyFile.getVirtualFile();
            return !file.getName().equals(PyNames.INIT_DOT_PY) ? new PTestModule(pyFile) : null;
        }
        return null;
    }
}
