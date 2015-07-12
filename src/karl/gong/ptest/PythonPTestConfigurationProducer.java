package karl.gong.ptest;

import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.QualifiedNameFinder;
import com.jetbrains.python.testing.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PythonPTestConfigurationProducer extends PythonTestConfigurationProducer {

    public PythonPTestConfigurationProducer() {
        super(PythonPTestConfigurationType.getInstance().PY_PTEST_FACTORY);
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
            element = PyUtil.findNonWhitespaceAtOffset(element.getContainingFile(), element.getTextOffset());
        }
        // element is invalid
        if (element == null) return false;
        // is in <if __name__ = "__main__"> section
        if (PythonUnitTestRunnableScriptFilter.isIfNameMain(location)) return false;
        // is ptest target
        PythonPTestRunConfiguration config = (PythonPTestRunConfiguration) configuration;
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
        return false;
    }

    @Override
    public boolean isConfigurationFromContext(AbstractPythonTestRunConfiguration configuration, ConfigurationContext context) {
        // no context
        if (context == null) return false;
        // location is invalid
        final Location location = context.getLocation();
        if (location == null || !isAvailable(location)) return false;
        // location is white space
        PsiElement element = location.getPsiElement();
        if (element instanceof PsiWhiteSpace) {
            element = PyUtil.findNonWhitespaceAtOffset(element.getContainingFile(), element.getTextOffset());
        }
        // element is invalid
        if (element == null) return false;
        // is in <if __name__ = "__main__"> section
        if (PythonUnitTestRunnableScriptFilter.isIfNameMain(location)) return false;
        // is ptest target
        PythonPTestRunConfiguration config = (PythonPTestRunConfiguration) configuration;
        PythonPTestRunConfiguration newConfig = new PythonPTestRunConfiguration(config.getProject(), config.getFactory());
        if (isPTestMethod(element, newConfig)) {
            setupConfigurationForPTestMethod(element, newConfig);
        } else if (isPTestClass(element, newConfig)) {
            setupConfigurationForPTestClass(element, newConfig);
        } else if (isPTestModule(element, newConfig)) {
            setupConfigurationForPTestModule(element, newConfig);
        } else if (isPTestPackage(element, newConfig)) {
            setupConfigurationForPTestPackage(element, newConfig);
        }
        if (newConfig.isRunTest()) {
            return config.isRunTest() && newConfig.getTestTargets().equals(config.getTestTargets());
        } else if (newConfig.isRunFailed()) {
            return config.isRunFailed() && newConfig.getXunitXML().equals(config.getXunitXML());
        }
        return false;
    }

    protected boolean isAvailable(@NotNull final Location location) {
        return true;
    }

    protected boolean isPTestMethod(@NotNull final PsiElement element,
                                    @Nullable final PythonPTestRunConfiguration configuration) {
        final PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class, false);
        if (pyFunction == null) return false;

        final PyClass containingClass = pyFunction.getContainingClass();
        if (containingClass == null) return false;

        return hasDecorator(pyFunction, "Test") && isPTestClass(containingClass, configuration);
    }

    protected boolean setupConfigurationForPTestMethod(@NotNull final PsiElement element,
                                                       @Nullable final PythonPTestRunConfiguration configuration) {
        try {
            final PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class, false);
            configuration.setRunTest(true);
            String testTarget = QualifiedNameFinder.findShortestImportableQName(element.getContainingFile()).toString() + "."
                    + pyFunction.getContainingClass().getName() + "." + pyFunction.getName();
            configuration.setTestTargets(testTarget);
            configuration.setName("ptest " + testTarget);
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    protected boolean isPTestClass(@NotNull final PsiElement element,
                                   @Nullable final PythonPTestRunConfiguration configuration) {
        final PyClass pyClass = PsiTreeUtil.getParentOfType(element, PyClass.class, false);
        if (pyClass == null) return false;

        return hasDecorator(pyClass, "TestClass");
    }

    protected boolean setupConfigurationForPTestClass(@NotNull final PsiElement element,
                                                      @Nullable final PythonPTestRunConfiguration configuration) {
        try {
            final PyClass pyClass = PsiTreeUtil.getParentOfType(element, PyClass.class, false);
            configuration.setRunTest(true);
            String testTarget = QualifiedNameFinder.findShortestImportableQName(element.getContainingFile()).toString() + "."
                    + pyClass.getName();
            configuration.setTestTargets(testTarget);
            configuration.setName("ptest " + testTarget);
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    protected boolean isPTestModule(@NotNull final PsiElement element,
                                    @Nullable final PythonPTestRunConfiguration configuration) {
        if (element instanceof PyFile) {
            VirtualFile file = ((PyFile) element).getVirtualFile();
            if (file.getName().equals("__init__.py")) return false;
            return true;
        }
        return false;
    }

    protected boolean setupConfigurationForPTestModule(@NotNull final PsiElement element,
                                                       @Nullable final PythonPTestRunConfiguration configuration) {
        try {
            configuration.setRunTest(true);
            String testTarget = QualifiedNameFinder.findShortestImportableQName((PyFile) element).toString();
            configuration.setTestTargets(testTarget);
            configuration.setName("ptest " + testTarget);
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    protected boolean isPTestPackage(@NotNull final PsiElement element,
                                     @Nullable final PythonPTestRunConfiguration configuration) {
        if (element instanceof PsiDirectory) {
            boolean isPackage = false;
            for (VirtualFile file : ((PsiDirectory) element).getVirtualFile().getChildren()) {
                if (file.getName().equals("__init__.py"))
                    isPackage = true;
            }
            return isPackage;
        }
        return false;
    }

    protected boolean setupConfigurationForPTestPackage(@NotNull final PsiElement element,
                                                        @Nullable final PythonPTestRunConfiguration configuration) {
        try {
            configuration.setRunTest(true);
            String testTarget = QualifiedNameFinder.findShortestImportableQName((PsiDirectory) element).toString();
            configuration.setTestTargets(testTarget);
            configuration.setName("ptest " + testTarget);
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    private boolean hasDecorator(PyDecoratable py, String name) {
        return py.getDecoratorList() != null && py.getDecoratorList().findDecorator(name) != null;
    }
}
