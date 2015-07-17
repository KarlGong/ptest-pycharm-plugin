package karl.gong.ptest;

import com.google.common.collect.Lists;
import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestFrameworkRunningModel;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.util.Getter;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.python.run.CommandLinePatcher;
import com.jetbrains.python.testing.PythonTestCommandLineStateBase;

import java.util.ArrayList;
import java.util.List;

public class PythonPTestCommandLineState extends PythonTestCommandLineStateBase {
    private final PythonPTestRunConfiguration configuration;

    public PythonPTestCommandLineState(PythonPTestRunConfiguration configuration, ExecutionEnvironment env) {
        super(configuration, env);
        this.configuration = configuration;
    }

    @Override
    protected String getRunner() {
        return "pycharm/ptestrunner.py";
    }

    @Override
    protected List<String> getTestSpecs() {
        return new ArrayList<String>();
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
        if (configuration.isRunTest()) {
            script_params.addParameter("-t");
            script_params.addParameter(configuration.getTestTargets());
        } else if (configuration.isRunFailed()) {
            script_params.addParameter("-R");
            script_params.addParameter(configuration.getXunitXML());
        }
        if (configuration.isVerbose()) {
            script_params.addParameter("-v");
        }
        if (configuration.isDisableScreenshot()) {
            script_params.addParameter("--disablescreenshot");
        }
    }

    @Override
    public ExecutionResult execute(Executor executor, CommandLinePatcher... patchers) throws ExecutionException {
        final ProcessHandler processHandler = startProcess(patchers);
        final ConsoleView console = createAndAttachConsole(configuration.getProject(), processHandler, executor);

        List<AnAction> actions = Lists
                .newArrayList(createActions(console, processHandler));

        DefaultExecutionResult executionResult =
                new DefaultExecutionResult(console, processHandler, actions.toArray(new AnAction[actions.size()]));

        PythonPTestRerunFailedTestsAction rerunFailedTestsAction = new PythonPTestRerunFailedTestsAction(console);
        if (console instanceof SMTRunnerConsoleView) {
            rerunFailedTestsAction.init(((BaseTestsOutputConsoleView)console).getProperties());
            rerunFailedTestsAction.setModelProvider(new Getter<TestFrameworkRunningModel>() {
                @Override
                public TestFrameworkRunningModel get() {
                    return ((SMTRunnerConsoleView)console).getResultsViewer();
                }
            });
        }

        executionResult.setRestartActions(rerunFailedTestsAction, new ToggleAutoTestAction());
        return executionResult;
    }
}
