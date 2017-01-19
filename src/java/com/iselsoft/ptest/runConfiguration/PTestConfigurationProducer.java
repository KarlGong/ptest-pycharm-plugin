package com.iselsoft.ptest.runConfiguration;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerTestTreeView;
import com.intellij.ide.util.treeView.smartTree.TreeElementWrapper;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.treeStructure.Tree;
import com.iselsoft.ptest.toolWindow.PTestStructureViewElement;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.QualifiedNameFinder;
import com.jetbrains.python.testing.AbstractPythonTestRunConfiguration;
import com.jetbrains.python.testing.PythonTestConfigurationProducer;
import com.jetbrains.python.testing.PythonUnitTestRunnableScriptFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PTestConfigurationProducer extends PythonTestConfigurationProducer {

    public PTestConfigurationProducer() {
        super(PTestConfigurationType.getInstance().PY_PTEST_FACTORY);
    }

    @Override
    public boolean setupConfigurationFromContext(
            AbstractPythonTestRunConfiguration configuration,
            ConfigurationContext context,
            Ref<PsiElement> sourceElement) {
        // no context
        if (context == null) return false;
        // location is invalid
        final Location location = context.getLocation();
        if (location == null || !isAvailable(location)) return false;
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
        // element is invalid
        if (element == null) return false;
        // is in <if __name__ = "__main__"> section
        if (PythonUnitTestRunnableScriptFilter.isIfNameMain(location)) return false;
        // is ptest target
        PTestRunConfiguration config = (PTestRunConfiguration) configuration;
        // is in tool window
        List<PTestStructureViewElement> testTargets = getSelectedPTestTargetsInTW(context);
        if (!testTargets.isEmpty()) {
            if (testTargets.size() == 1) {
                return setupConfigurationForPTestTargetInTW(testTargets.get(0), config);
            } else {
                return setupConfigurationForPTestTargetsInTW(testTargets, config);
            }
        }
        // is in test runner 
        AbstractTestProxy smtPTestMethod = getSelectedPTestMethodInSMT(context);
        if (smtPTestMethod != null) {
            return setupConfigurationForPTestMethodInSMT(smtPTestMethod, config);
        }
        // is in editor
        PyFunction pTestMethod = getPTestMethod(element);
        if (pTestMethod != null && getPTestClass(element) != null) {
            return setupConfigurationForPTestMethod(pTestMethod, config);
        }
        PyClass pTestClass = getPTestClass(element);
        if (pTestClass != null) {
            return setupConfigurationForPTestClass(pTestClass, config);
        }
        PyFile pTestModule = getPTestModule(element);
        if (pTestModule != null) {
            return setupConfigurationForPTestModule(pTestModule, config);
        }
        PsiDirectory pTestPackage = getPTestPackage(element);
        if (pTestPackage != null) {
            return setupConfigurationForPTestPackage(pTestPackage, config);
        }
        // is XML
        PsiFile pTestXML = getPTestXML(element);
        if (pTestXML != null) {
            return setupConfigurationForPTestXML(pTestXML, config);
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(AbstractPythonTestRunConfiguration configuration, ConfigurationContext context) {
        PTestRunConfiguration config = (PTestRunConfiguration) configuration;
        PTestRunConfiguration newConfig = new PTestRunConfiguration(config.getProject(), config.getFactory());

        if (setupConfigurationFromContext(newConfig, context, null)) {
            if (newConfig.isRunTest()) {
                return config.isRunTest() && newConfig.getTestTargets().equals(config.getTestTargets());
            } else if (newConfig.isRunFailed()) {
                return config.isRunFailed() && newConfig.getXunitXML().equals(config.getXunitXML());
            }
        }
        return false;
    }

    public boolean isAvailable(@NotNull final Location location) {
        return true;
    }

    public List<PTestStructureViewElement> getSelectedPTestTargetsInTW(@NotNull ConfigurationContext context) {
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

    public boolean setupConfigurationForPTestTargetInTW(@NotNull final PTestStructureViewElement testTarget, @Nullable final PTestRunConfiguration configuration) {
        try {
            PyElement pyElement = testTarget.getValue();
            PyFunction pTestMethod = getPTestMethod(pyElement);
            if (pTestMethod != null) {
                setValueForEmptyWorkingDirectory(configuration);
                configuration.setRunTest(true);
                String testName = pTestMethod.getName();
                String testTargetText = QualifiedNameFinder.findShortestImportableQName((PsiFile) testTarget.getParent().getParent().getValue()).toString() + "."
                        + testTarget.getParent().getValue().getName() + "." + testName;
                configuration.setTestTargets(testTargetText);
                configuration.setSuggestedName("ptest " + testTarget.getParent().getValue().getName() + "." + testName);
                configuration.setActionName("ptest " + testName);
                return true;
            }
            PyClass pTestClass = getPTestClass(pyElement);
            if (pTestClass != null) {
                return setupConfigurationForPTestClass(pTestClass, configuration);
            }
            PyFile pTestModule = getPTestModule(pyElement);
            if (pTestModule != null) {
                return setupConfigurationForPTestModule(pTestModule, configuration);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setupConfigurationForPTestTargetsInTW(@NotNull final List<PTestStructureViewElement> testTargets, @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            List<String> testTargetTexts = new ArrayList<>();
            for (PTestStructureViewElement testTarget : testTargets) {
                PTestRunConfiguration tempConfig = new PTestRunConfiguration(configuration.getProject(), configuration.getFactory());
                setupConfigurationForPTestTargetInTW(testTarget, tempConfig);
                testTargetTexts.add(tempConfig.getTestTargets());
            }
            configuration.setTestTargets(String.join(",", testTargetTexts));
            configuration.setSuggestedName("ptests selected tests");
            configuration.setActionName("ptests selected tests");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public AbstractTestProxy getSelectedPTestMethodInSMT(@NotNull ConfigurationContext context) {
        try {
            return ((SMTRunnerTestTreeView) PlatformDataKeys.CONTEXT_COMPONENT.getData(context.getDataContext())).getSelectedTest();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean setupConfigurationForPTestMethodInSMT(@NotNull final AbstractTestProxy test, @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            AbstractTestProxy testClass;
            if (test.getParent().getParent().getName().equals("[root]")) {
                testClass = test.getParent();
            } else {
                testClass = test.getParent().getParent();
            }
            String testName = test.getName();
            configuration.setTestTargets(testClass.getName() + "." + testName);
            String[] splittedNames = testClass.getName().split("\\.");
            configuration.setSuggestedName("ptest " + splittedNames[splittedNames.length - 1] + "." + testName);
            configuration.setActionName("ptest " + testName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public PyFunction getPTestMethod(@NotNull final PsiElement element) {
        final PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class, false);
        if (pyFunction == null) return null;

        final PyClass containingClass = pyFunction.getContainingClass();
        if (containingClass == null) return null;

        return hasDecorator(pyFunction, "Test") ? pyFunction : null;
    }

    public boolean setupConfigurationForPTestMethod(@NotNull final PyFunction pyFunction,
                                                    @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            String testName = pyFunction.getName();
            String testTarget = QualifiedNameFinder.findShortestImportableQName(pyFunction.getContainingFile()).toString() + "."
                    + pyFunction.getContainingClass().getName() + "." + testName;
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptest " + pyFunction.getContainingClass().getName() + "." + testName);
            configuration.setActionName("ptest " + testName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public PyClass getPTestClass(@NotNull final PsiElement element) {
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

    public boolean setupConfigurationForPTestClass(@NotNull final PyClass pyClass,
                                                   @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            String testTarget = QualifiedNameFinder.findShortestImportableQName(pyClass.getContainingFile()).toString() + "."
                    + pyClass.getName();
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + pyClass.getName());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public PyFile getPTestModule(@NotNull final PsiElement element) {
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

    public boolean setupConfigurationForPTestModule(@NotNull final PyFile pyModule,
                                                    @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            String testTarget = QualifiedNameFinder.findShortestImportableQName(pyModule).toString();
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + testTarget);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public PsiDirectory getPTestPackage(@NotNull final PsiElement element) {
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

    public boolean setupConfigurationForPTestPackage(@NotNull final PsiDirectory pyPackage,
                                                     @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            String testTarget = QualifiedNameFinder.findShortestImportableQName(pyPackage).toString();
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + testTarget);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public PsiFile getPTestXML(@NotNull final PsiElement element) {
        if (element instanceof PsiFile) {
            PsiFile xmlFile = (PsiFile) element;
            VirtualFile file = xmlFile.getVirtualFile();
            return file.getExtension() != null && file.getExtension().equalsIgnoreCase("xml") ? xmlFile : null;
        }
        return null;
    }

    public boolean setupConfigurationForPTestXML(@NotNull final PsiFile xmlFile,
                                                 @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunFailed(true);
            String xml = xmlFile.getVirtualFile().getCanonicalPath();
            String xmlRelativePath = new File(configuration.getWorkingDirectory()).toURI().relativize(new File(xml).toURI()).getPath();
            configuration.setXunitXML(xmlRelativePath);
            configuration.setSuggestedName("ptests in " + xmlRelativePath);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void setValueForEmptyWorkingDirectory(@NotNull final PTestRunConfiguration configuration) {
        if (StringUtil.isEmptyOrSpaces(configuration.getWorkingDirectory())) {
            configuration.setWorkingDirectory(configuration.getProject().getBasePath());
        }
    }

    private boolean hasDecorator(PyDecoratable py, String name) {
        return py.getDecoratorList() != null && py.getDecoratorList().findDecorator(name) != null;
    }
}
