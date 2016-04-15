package com.iselsoft.ptest.runconfiguration;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.QualifiedNameFinder;
import com.jetbrains.python.testing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class PTestConfigurationProducer extends PythonTestConfigurationProducer {

    public PTestConfigurationProducer() {
        super(PTestConfigurationType.getInstance().PY_PTEST_FACTORY);
    }

    @Override
    protected boolean setupConfigurationFromContext(
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
        if (isPTestMethod(element, config)) {
            return setupConfigurationForPTestMethod(element, config);
        }
        if (isPTestClass(element, config)) {
            return setupConfigurationForPTestClass(element, config);
        }
        if (isPTestModule(element, config)) {
            return setupConfigurationForPTestModule(element, config);
        }
        if (isPTestPackage(element, config)) {
            return setupConfigurationForPTestPackage(element, config);
        }
        // is XML
        if (isXML(element, config)) {
            return setupConfigurationForXML(element, config);
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

    protected boolean isAvailable(@NotNull final Location location) {
        return true;
    }

    protected boolean isPTestMethod(@NotNull final PsiElement element,
                                    @Nullable final PTestRunConfiguration configuration) {
        final PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class, false);
        if (pyFunction == null) return false;

        final PyClass containingClass = pyFunction.getContainingClass();
        if (containingClass == null) return false;

        return hasDecorator(pyFunction, "Test") && isPTestClass(containingClass, configuration);
    }

    protected boolean setupConfigurationForPTestMethod(@NotNull final PsiElement element,
                                                       @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            final PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class, false);
            configuration.setRunTest(true);
            String testTarget = QualifiedNameFinder.findShortestImportableQName(element.getContainingFile()).toString() + "."
                    + pyFunction.getContainingClass().getName() + "." + pyFunction.getName();
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptest " + pyFunction.getContainingClass().getName() + "." + pyFunction.getName());
            configuration.setActionName("ptest " + pyFunction.getName());
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    protected boolean isPTestClass(@NotNull final PsiElement element,
                                   @Nullable final PTestRunConfiguration configuration) {
        final PyClass pyClass = PsiTreeUtil.getParentOfType(element, PyClass.class, false);
        if (pyClass == null) return false;

        return hasDecorator(pyClass, "TestClass");
    }

    protected boolean setupConfigurationForPTestClass(@NotNull final PsiElement element,
                                                      @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            final PyClass pyClass = PsiTreeUtil.getParentOfType(element, PyClass.class, false);
            configuration.setRunTest(true);
            String testTarget = QualifiedNameFinder.findShortestImportableQName(element.getContainingFile()).toString() + "."
                    + pyClass.getName();
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + pyClass.getName());
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    protected boolean isPTestModule(@NotNull final PsiElement element,
                                    @Nullable final PTestRunConfiguration configuration) {
        if (element instanceof PyFile) {
            VirtualFile file = ((PyFile) element).getVirtualFile();
            if (file.getName().equals(PyNames.INIT_DOT_PY)) return false;
            return true;
        }
        return false;
    }

    protected boolean setupConfigurationForPTestModule(@NotNull final PsiElement element,
                                                       @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            String testTarget = QualifiedNameFinder.findShortestImportableQName((PyFile) element).toString();
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + testTarget);
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    protected boolean isPTestPackage(@NotNull final PsiElement element,
                                     @Nullable final PTestRunConfiguration configuration) {
        if (element instanceof PsiDirectory) {
            boolean isPackage = false;
            for (VirtualFile file : ((PsiDirectory) element).getVirtualFile().getChildren()) {
                if (file.getName().equals(PyNames.INIT_DOT_PY))
                    isPackage = true;
            }
            return isPackage;
        }
        return false;
    }

    protected boolean setupConfigurationForPTestPackage(@NotNull final PsiElement element,
                                                        @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunTest(true);
            String testTarget = QualifiedNameFinder.findShortestImportableQName((PsiDirectory) element).toString();
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + testTarget);
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    protected boolean isXML(@NotNull final PsiElement element,
                            @Nullable final PTestRunConfiguration configuration) {
        if (element instanceof PsiFile) {
            VirtualFile file = ((PsiFile) element).getVirtualFile();
            return file.getExtension() != null && file.getExtension().equals("xml");
        }
        return false;
    }

    protected boolean setupConfigurationForXML(@NotNull final PsiElement element,
                                               @Nullable final PTestRunConfiguration configuration) {
        try {
            setValueForEmptyWorkingDirectory(configuration);
            configuration.setRunFailed(true);
            String xml = ((PsiFile) element).getVirtualFile().getCanonicalPath();
            String xmlRelativePath = new File(configuration.getWorkingDirectory()).toURI().relativize(new File(xml).toURI()).getPath();
            configuration.setXunitXML(xmlRelativePath);
            configuration.setSuggestedName("ptests in " + xmlRelativePath);
        } catch (NullPointerException e) {
            return false;
        }
        return true;
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
