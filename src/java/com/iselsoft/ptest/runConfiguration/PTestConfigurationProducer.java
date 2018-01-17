package com.iselsoft.ptest.runConfiguration;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.iselsoft.ptest.PTestUtil;
import com.iselsoft.ptest.toolWindow.PTestStructureViewElement;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.run.RunnableScriptFilter;
import com.jetbrains.python.testing.AbstractPythonTestConfigurationProducer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PTestConfigurationProducer extends AbstractPythonTestConfigurationProducer<PTestRunConfiguration> {

    public PTestConfigurationProducer() {
        super(PTestConfigurationType.getInstance().PTEST_FACTORY);
    }

    @NotNull
    @Override
    public Class<? super PTestRunConfiguration> getConfigurationClass() {
        return PTestRunConfiguration.class;
    }

    @Override
    public boolean setupConfigurationFromContext(PTestRunConfiguration config, ConfigurationContext context, Ref<PsiElement> sourceElement) {
        // no context
        if (context == null) return false;
        // location is invalid
        final Location location = context.getLocation();
        if (location == null) return false;
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
        if (RunnableScriptFilter.isIfNameMain(location)) return false;
        // is in tool window
        List<PTestStructureViewElement> testTargets = PTestUtil.getSelectedPTestTargetsInTW(context);
        if (!testTargets.isEmpty()) {
            if (testTargets.size() == 1) {
                return setupConfigurationForPTestTargetInTW(testTargets.get(0), config);
            } else {
                return setupConfigurationForPTestTargetsInTW(testTargets, config);
            }
        }
        // is in test runner 
        AbstractTestProxy smtPTestMethod = PTestUtil.getSelectedPTestMethodInSMT(context);
        if (smtPTestMethod != null) {
            return setupConfigurationForPTestMethodInSMT(smtPTestMethod, config);
        }
        // is in editor
        PyFunction pTestMethod = PTestUtil.getPTestMethod(element);
        if (pTestMethod != null && PTestUtil.getPTestClass(element) != null) {
            return setupConfigurationForPTestMethod(pTestMethod, config);
        }
        PyClass pTestClass = PTestUtil.getPTestClass(element);
        if (pTestClass != null) {
            return setupConfigurationForPTestClass(pTestClass, config);
        }
        PyFile pTestModule = PTestUtil.getPTestModule(element);
        if (pTestModule != null) {
            return setupConfigurationForPTestModule(pTestModule, config);
        }
        PsiDirectory pTestPackage = PTestUtil.getPTestPackage(element);
        if (pTestPackage != null) {
            return setupConfigurationForPTestPackage(pTestPackage, config);
        }
        // is XML
        PsiFile pTestXML = PTestUtil.getPTestXML(element);
        if (pTestXML != null) {
            return setupConfigurationForPTestXML(pTestXML, config);
        }
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(PTestRunConfiguration config, ConfigurationContext context) {
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

    public boolean setupConfigurationForPTestTargetInTW(@NotNull PTestStructureViewElement testTarget, @NotNull PTestRunConfiguration configuration) {
        try {
            PyElement pyElement = testTarget.getValue();
            PyFunction pTestMethod = PTestUtil.getPTestMethod(pyElement);
            if (pTestMethod != null) {
                setValueForEmptyWorkingDirectory(configuration);
                configuration.setRunTest(true);
                String testName = pTestMethod.getName();
                String testTargetText = PTestUtil.findShortestImportableName((PsiFile) testTarget.getParent().getParent().getValue()) + "."
                        + testTarget.getParent().getValue().getName() + "." + testName;
                configuration.setTestTargets(testTargetText);
                configuration.setSuggestedName("ptest " + testTarget.getParent().getValue().getName() + "." + testName);
                configuration.setActionName("ptest " + testName);
                return true;
            }
            PyClass pTestClass = PTestUtil.getPTestClass(pyElement);
            if (pTestClass != null) {
                return setupConfigurationForPTestClass(pTestClass, configuration);
            }
            PyFile pTestModule = PTestUtil.getPTestModule(pyElement);
            if (pTestModule != null) {
                return setupConfigurationForPTestModule(pTestModule, configuration);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setupConfigurationForPTestTargetsInTW(@NotNull List<PTestStructureViewElement> testTargets, @NotNull PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            List<String> testTargetTexts = new ArrayList<>();
            for (PTestStructureViewElement testTarget : testTargets) {
                PTestRunConfiguration tempConfig = new PTestRunConfiguration(configuration.getProject(), configuration.getFactory());
                setupConfigurationForPTestTargetInTW(testTarget, tempConfig);
                testTargetTexts.add(tempConfig.getTestTargets());
            }
            Collections.sort(testTargetTexts);
            configuration.setTestTargets(String.join(",", testTargetTexts));
            configuration.setSuggestedName("ptests selected tests");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setupConfigurationForPTestMethodInSMT(@NotNull AbstractTestProxy test, @NotNull PTestRunConfiguration configuration) {
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

    public boolean setupConfigurationForPTestMethod(@NotNull PyFunction pyFunction, @NotNull PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            String testName = pyFunction.getName();
            String testTarget = PTestUtil.findShortestImportableName(pyFunction.getContainingFile()) + "."
                    + pyFunction.getContainingClass().getName() + "." + testName;
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptest " + pyFunction.getContainingClass().getName() + "." + testName);
            configuration.setActionName("ptest " + testName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setupConfigurationForPTestClass(@NotNull PyClass pyClass, @NotNull PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            String testTarget = PTestUtil.findShortestImportableName(pyClass.getContainingFile()) + "."
                    + pyClass.getName();
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + pyClass.getName());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setupConfigurationForPTestModule(@NotNull PyFile pyModule, @NotNull PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            String testTarget = PTestUtil.findShortestImportableName(pyModule);
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + testTarget);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setupConfigurationForPTestPackage(@NotNull PsiDirectory pyPackage, @NotNull PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            String testTarget = PTestUtil.findShortestImportableName(pyPackage);
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + testTarget);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean setupConfigurationForPTestXML(@NotNull PsiFile xmlFile, @NotNull PTestRunConfiguration configuration) {
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

    private void setValueForEmptyWorkingDirectory(@NotNull PTestRunConfiguration configuration) {
        if (StringUtil.isEmptyOrSpaces(configuration.getWorkingDirectory())) {
            configuration.setWorkingDirectory(configuration.getProject().getBasePath());
        }
    }
}
