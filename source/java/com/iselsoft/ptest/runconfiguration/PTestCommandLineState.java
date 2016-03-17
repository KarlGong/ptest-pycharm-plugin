package com.iselsoft.ptest.runconfiguration;

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
import com.jetbrains.python.HelperPackage;
import com.jetbrains.python.run.CommandLinePatcher;
import com.jetbrains.python.testing.PythonTestCommandLineStateBase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PTestCommandLineState extends PythonTestCommandLineStateBase {
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
        List<String> specs = new ArrayList<String>();
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
        String variables = configuration.getVariables();
        if (configuration.isUseVariables() && !StringUtil.isEmptyOrSpaces(variables)) {
            script_params.addParametersString(variables);
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
        script_params.addParameter(new File(getJarDir(getClass()), "ptestrunner.py").getAbsolutePath());
        addBeforeParameters(cmd);
        script_params.addParameters(getTestSpecs());
        addAfterParameters(cmd);
    }

    @Override
    public ExecutionResult execute(Executor executor, CommandLinePatcher... patchers) throws ExecutionException {
        final ProcessHandler processHandler = startProcess(patchers);
        final ConsoleView console = createAndAttachConsole(configuration.getProject(), processHandler, executor);

        List<AnAction> actions = Lists
                .newArrayList(createActions(console, processHandler));

        DefaultExecutionResult executionResult =
                new DefaultExecutionResult(console, processHandler, actions.toArray(new AnAction[actions.size()]));

        PTestRerunFailedTestsAction rerunFailedTestsAction = new PTestRerunFailedTestsAction(console);
        if (console instanceof SMTRunnerConsoleView) {
            rerunFailedTestsAction.init(((BaseTestsOutputConsoleView) console).getProperties());
            rerunFailedTestsAction.setModelProvider(new Getter<TestFrameworkRunningModel>() {
                @Override
                public TestFrameworkRunningModel get() {
                    return ((SMTRunnerConsoleView) console).getResultsViewer();
                }
            });
        }

        executionResult.setRestartActions(rerunFailedTestsAction, new ToggleAutoTestAction());
        return executionResult;
    }

    private static File getJarDir(Class<?> cls) {
        ClassLoader loader = cls.getClassLoader();
        String clsName = cls.getName();
        // convert class name to path
        String clsPath = clsName.replace(".", "/") + ".class";
        java.net.URL url = loader.getResource(clsPath);
        String realPath = url.getPath();
        int pos = realPath.indexOf("file:");
        if (pos > -1) {
            realPath = realPath.substring(pos + 5);
        }
        pos = realPath.indexOf(clsPath);
        realPath = realPath.substring(0, pos - 1);
        // it is a jar file
        if (realPath.endsWith("!")) {
            realPath = realPath.substring(0, realPath.lastIndexOf("/"));
        }
        // decode
        try {
            realPath = java.net.URLDecoder.decode(realPath, "utf-8");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new File(realPath);
    }
}
