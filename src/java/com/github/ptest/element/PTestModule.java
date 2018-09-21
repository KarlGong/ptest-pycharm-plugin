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

public class PTestModule extends PTestElement<PyFile> {

    public PTestModule(PyFile pyFile) {
        super(pyFile);
    }

    @Override
    public boolean setupConfiguration(PTestRunConfiguration configuration) {
        try {
            configuration.setValueForEmptyWorkingDirectory();
            configuration.setRunTest(true);
            String testTarget = PTestUtil.findShortestImportableName(myElement);
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

        for (PyClass pyClass : myElement.getTopLevelClasses()) {
            if (PTestUtil.hasDecorator(pyClass, "TestClass", null, null)) {
                children.add(new PTestClass(pyClass));
            } else {
                for (PyClass ancestorClass : pyClass.getAncestorClasses(null)) {
                    if (PTestUtil.hasDecorator(ancestorClass, "TestClass", null, null)) {
                        children.add(new PTestClass(pyClass));
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
                return myElement.getName();
            }

            @Override
            public String getLocationString() {
                return null;
            }

            @Override
            public Icon getIcon(boolean b) {
                return myElement.getIcon(0);
            }
        };
    }

    @Override
    public boolean isLeaf() {
        return false;
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
