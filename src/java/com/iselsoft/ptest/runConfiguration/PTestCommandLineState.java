package com.iselsoft.ptest.runConfiguration;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.autotest.ToggleAutoTestAction;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.python.HelperPackage;
import com.jetbrains.python.run.CommandLinePatcher;
import com.jetbrains.python.testing.PythonTestCommandLineStateBase;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class PTestCommandLineState extends PythonTestCommandLineStateBase<PTestRunConfiguration> {
    private final PTestRunConfiguration configuration;

    public PTestCommandLineState(PTestRunConfiguration configuration, ExecutionEnvironment env) {
        super(configuration, env);
        this.configuration = configuration;
    }

    @Override
    protected HelperPackage getRunner() {
        return null;
    }

    @Override
    protected List<String> getTestSpecs() {
        List<String> specs = new ArrayList<>();
        if (configuration.isRunTest()) {
            specs.add("-t");
            specs.add(configuration.getTestTargets());
        } else if (configuration.isRunFailed()) {
            specs.add("-R");
            specs.add(configuration.getXunitXML());
        }
        return specs;
    }

    @Override
    protected void addAfterParameters(GeneralCommandLine cmd) {
        ParamsGroup script_params = cmd.getParametersList().getParamsGroup(GROUP_SCRIPT);
        assert script_params != null;
        String options = configuration.getOptions();
        if (configuration.isUseOptions() && !StringUtil.isEmptyOrSpaces(options)) {
            script_params.addParametersString(options);
        }
        String properties = configuration.getProperties();
        if (configuration.isUseProperties() && !StringUtil.isEmptyOrSpaces(properties)) {
            script_params.addParametersString(properties);
        }
        if (configuration.isVerbose()) {
            script_params.addParameter("-v");
        }
        if (configuration.isDisableScreenshot()) {
            script_params.addParameter("--disablescreenshot");
        }
    }

    @Override
    protected void addTestRunnerParameters(GeneralCommandLine cmd) {
        ParamsGroup script_params = cmd.getParametersList().getParamsGroup(GROUP_SCRIPT);
        assert script_params != null;
        String ptestPluginPath = PluginManager.getPlugin(PluginId.getId("karl.gong.plugin.ptest")).getPath().getAbsolutePath();
        script_params.addParameter(Paths.get(ptestPluginPath, "lib", "ptest_runner.py").toAbsolutePath().toString());
        addBeforeParameters(cmd);
        script_params.addParameters(getTestSpecs());
        addAfterParameters(cmd);
    }

    @Override
    public ExecutionResult execute(Executor executor, CommandLinePatcher... patchers) throws ExecutionException {
        final ProcessHandler processHandler = startProcess(getDefaultPythonProcessStarter(), patchers);
        final ConsoleView console = createAndAttachConsole(configuration.getProject(), processHandler, executor);

        DefaultExecutionResult executionResult = new DefaultExecutionResult(console, processHandler, createActions(console, processHandler));

        PTestRerunFailedTestsAction rerunFailedTestsAction = new PTestRerunFailedTestsAction(console);
        if (console instanceof SMTRunnerConsoleView) {
            rerunFailedTestsAction.init(((BaseTestsOutputConsoleView) console).getProperties());
            rerunFailedTestsAction.setModelProvider(() -> ((SMTRunnerConsoleView) console).getResultsViewer());
        }

        executionResult.setRestartActions(rerunFailedTestsAction, new ToggleAutoTestAction());
        return executionResult;
    }
}
