package com.github.ptest.runConfiguration;

import com.github.ptest.element.*;
import com.github.ptest.toolWindow.PTestStructureViewElement;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerTestTreeView;
import com.intellij.ide.dnd.aware.DnDAwareTree;
import com.intellij.ide.util.treeView.smartTree.TreeElementWrapper;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.ui.treeStructure.Tree;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.run.RunnableScriptFilter;
import com.jetbrains.python.testing.AbstractPythonTestConfigurationProducer;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;
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
        // is project folder
        if (element instanceof PsiDirectory
                && ((PsiDirectory) element).getVirtualFile().equals(element.getProject().getBaseDir()))
            return false;
        // get context component
        Component component = PlatformDataKeys.CONTEXT_COMPONENT.getData(context.getDataContext());
        // is in test runner 
        if (component instanceof SMTRunnerTestTreeView) {
            return setupConfigurationInSMTRunner((SMTRunnerTestTreeView) component, config);
        }
        // is in tool window
        if (component instanceof DnDAwareTree) {
            return setupConfigurationInToolWindow((Tree) component, config);
        }
        // is in editor / project / structure view
        PTestMethod pTestMethod = PTestMethod.createFrom(element);
        if (pTestMethod != null) {
            return pTestMethod.setupConfiguration(config);
        }
        PTestClass pTestClass = PTestClass.createFrom(element);
        if (pTestClass != null) {
            return pTestClass.setupConfiguration(config);
        }
        PTestModule pTestModule = PTestModule.createFrom(element);
        if (pTestModule != null) {
            return pTestModule.setupConfiguration(config);
        }
        PTestPackage pTestPackage = PTestPackage.createFrom(element);
        if (pTestPackage != null) {
            return pTestPackage.setupConfiguration(config);
        }
        // is XML
        PTestXML pTestXML = PTestXML.createFrom(element);
        if (pTestXML != null) {
            return pTestXML.setupConfiguration(config);
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

    public boolean setupConfigurationInSMTRunner(@NotNull SMTRunnerTestTreeView component, @NotNull PTestRunConfiguration configuration) {
        try {
            AbstractTestProxy test = component.getSelectedTest();
            configuration.setValueForEmptyWorkingDirectory();
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
    
    public boolean setupConfigurationInToolWindow(@NotNull Tree component, @NotNull PTestRunConfiguration configuration) {
        try {
            List<PTestStructureViewElement> selectedElements = new ArrayList<>();
            for (DefaultMutableTreeNode selectedNode : component.getSelectedNodes(DefaultMutableTreeNode.class, null)) {
                TreeElementWrapper elementWrapper = (TreeElementWrapper) selectedNode.getUserObject();
                PTestStructureViewElement structureViewElement = (PTestStructureViewElement) elementWrapper.getValue(); 
                selectedElements.add(structureViewElement);
            }
            
            if (selectedElements.size() == 1) {
                return selectedElements.get(0).getElement().setupConfiguration(configuration);
            }
            configuration.setValueForEmptyWorkingDirectory();
            configuration.setRunTest(true);
            List<String> testTargetTexts = new ArrayList<>();
            for (PTestStructureViewElement selectedElement : selectedElements) {
                PTestRunConfiguration tempConfig = new PTestRunConfiguration(configuration.getProject(), configuration.getFactory());
                selectedElement.getElement().setupConfiguration(tempConfig);
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
}
