package karl.gong.ptest;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.Location;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.TestFrameworkRunningModel;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentContainer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.run.AbstractPythonRunConfiguration;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class PythonPTestRerunFailedTestsAction extends AbstractRerunFailedTestsAction {
    protected PythonPTestRerunFailedTestsAction(@NotNull ComponentContainer componentContainer) {
        super(componentContainer);
    }

    @Override
    @Nullable
    protected MyRunProfile getRunProfile(@NotNull ExecutionEnvironment environment) {
        final TestFrameworkRunningModel model = getModel();
        if (model == null) {
            return null;
        }
        return new MyTestRunProfile((AbstractPythonRunConfiguration) model.getProperties().getConfiguration());
    }

    private class MyTestRunProfile extends MyRunProfile {

        public MyTestRunProfile(RunConfigurationBase configuration) {
            super(configuration);
        }

        @NotNull
        @Override
        public Module[] getModules() {
            return ((AbstractPythonRunConfiguration) getPeer()).getModules();
        }

        @Nullable
        @Override
        public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
            final AbstractPythonRunConfiguration configuration = ((AbstractPythonRunConfiguration) getPeer());

            return new FailedPythonTestCommandLineState((PythonPTestRunConfiguration) configuration, env,
                    (PythonPTestCommandLineState) configuration.getState(executor, env));
        }
    }

    private class FailedPythonTestCommandLineState extends PythonPTestCommandLineState {

        private final PythonPTestCommandLineState state;
        private final PythonPTestRunConfiguration configuration;
        private final Project project;

        public FailedPythonTestCommandLineState(PythonPTestRunConfiguration configuration,
                                                ExecutionEnvironment env,
                                                PythonPTestCommandLineState state) {
            super(configuration, env);
            this.state = state;
            this.configuration = configuration;
            project = configuration.getProject();
        }

        @Override
        protected String getRunner() {
            return state.getRunner();
        }

        @Override
        protected void addAfterParameters(GeneralCommandLine cmd) throws ExecutionException {
            ParamsGroup script_params = cmd.getParametersList().getParamsGroup(GROUP_SCRIPT);
            assert script_params != null;
            String options = configuration.getOptions();
            if (configuration.isUseOptions() && !StringUtil.isEmptyOrSpaces(options)) {
                script_params.addParametersString(options);
            }
            String variables = configuration.getVariables();
            if (configuration.isUseVariables() && !StringUtil.isEmptyOrSpaces(variables)) {
                script_params.addParametersString(variables);
            }

            List<String> rerunTestTargets = new ArrayList<String>();
            for (AbstractTestProxy failedTest : getFailedTests(project)) {
                if (failedTest.isLeaf()) {
                    rerunTestTargets.add(failedTest.getName());
                }
            }
            script_params.addParameter("-t");
            script_params.addParameter(StringUtils.join(rerunTestTargets, ","));

            if (configuration.isVerbose()) {
                script_params.addParameter("-v");
            }
            if (configuration.isDisableScreenshot()) {
                script_params.addParameter("--disablescreenshot");
            }
        }
    }
}