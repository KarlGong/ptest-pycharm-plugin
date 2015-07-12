package karl.gong.ptest;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.ParamsGroup;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.python.testing.PythonTestCommandLineStateBase;
import org.jetbrains.annotations.NotNull;

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
    }

    @NotNull
    protected ConsoleView createAndAttachConsole(Project project, ProcessHandler processHandler, Executor executor)
            throws ExecutionException {
        final ConsoleView consoleView = super.createAndAttachConsole(project, processHandler, executor);
        addTracebackFilter(project, consoleView, processHandler);
        return consoleView;
    }
}
