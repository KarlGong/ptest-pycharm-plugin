package com.iselsoft.ptest.runConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.python.packaging.PyPackage;
import com.jetbrains.python.packaging.PyPackageManager;
import com.jetbrains.python.sdk.PythonSdkType;
import com.jetbrains.python.testing.AbstractPythonTestRunConfiguration;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PTestRunConfiguration extends AbstractPythonTestRunConfiguration {
    protected String title = "ptest";
    protected String pluralTitle = "ptests";
    
    private String suggestedName = "";
    private String actionName = "";

    private boolean runTest = true;
    private String testTargets = "";
    private boolean runFailed = false;
    private String xunitXML = "";
    private boolean useOptions = false;
    private String options = "";
    private boolean useVariables = false;
    private String variables = "";
    private boolean verbose = true;
    private boolean disableScreenshot = false;

    protected PTestRunConfiguration(final Project project, final ConfigurationFactory factory) {
        super(project, factory);
    }

    public boolean isRunTest() {
        return runTest;
    }

    public void setRunTest(boolean runTest) {
        this.runTest = runTest;
        this.runFailed = !runTest;
    }

    public String getTestTargets() {
        return testTargets;
    }

    public void setTestTargets(String testTargets) {
        this.testTargets = testTargets;
    }

    public boolean isRunFailed() {
        return runFailed;
    }

    public void setRunFailed(boolean runFailed) {
        this.runFailed = runFailed;
        this.runTest = !runFailed;
    }

    public String getXunitXML() {
        return xunitXML;
    }

    public void setXunitXML(String xunitXML) {
        this.xunitXML = xunitXML;
    }

    public boolean isUseOptions() {
        return useOptions;
    }

    public void setUseOptions(boolean useOptions) {
        this.useOptions = useOptions;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public boolean isUseVariables() {
        return useVariables;
    }

    public void setUseVariables(boolean useVariables) {
        this.useVariables = useVariables;
    }

    public String getVariables() {
        return variables;
    }

    public void setVariables(String variables) {
        this.variables = variables;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isDisableScreenshot() {
        return disableScreenshot;
    }

    public void setDisableScreenshot(boolean disableScreenshot) {
        this.disableScreenshot = disableScreenshot;
    }

    @Override
    public String suggestedName() {
        return suggestedName;
    }

    public void setSuggestedName(String suggestedName) {
        this.suggestedName = suggestedName;
        setGeneratedName(); // use the suggested name as name
    }

    @Override
    public String getActionName() {
        if (!StringUtil.isEmptyOrSpaces(actionName)) {
            return actionName;
        }
        return suggestedName();
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    @Override
    protected String getTitle() {
        return title;
    }

    @Override
    protected String getPluralTitle() {
        return pluralTitle;
    }

    @Override
    protected SettingsEditor<? extends RunConfiguration> createConfigurationEditor() {
        return new PTestRunConfigurationEditor(getProject(), this);
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
        return new PTestCommandLineState(this, env);
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        suggestedName = JDOMExternalizerUtil.readField(element, "SUGGESTED_NAME");
        actionName = JDOMExternalizerUtil.readField(element, "ACTION_NAME");
        
        runTest = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "RUN_TEST"));
        testTargets = JDOMExternalizerUtil.readField(element, "TEST_TARGETS");
        runFailed = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "RUN_FAILED"));
        xunitXML = JDOMExternalizerUtil.readField(element, "XUNIT_XML");
        useOptions = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "USE_OPTIONS"));
        options = JDOMExternalizerUtil.readField(element, "OPTIONS");
        useVariables = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "USE_VARIABLES"));
        variables = JDOMExternalizerUtil.readField(element, "VARIABLES");
        verbose = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "VERBOSE"));
        disableScreenshot = Boolean.parseBoolean(JDOMExternalizerUtil.readField(element, "DISABLE_SCREENSHOT"));
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        JDOMExternalizerUtil.writeField(element, "SUGGESTED_NAME", suggestedName);
        JDOMExternalizerUtil.writeField(element, "ACTION_NAME", actionName);
        
        JDOMExternalizerUtil.writeField(element, "RUN_TEST", String.valueOf(runTest));
        JDOMExternalizerUtil.writeField(element, "TEST_TARGETS", testTargets);
        JDOMExternalizerUtil.writeField(element, "RUN_FAILED", String.valueOf(runFailed));
        JDOMExternalizerUtil.writeField(element, "XUNIT_XML", xunitXML);
        JDOMExternalizerUtil.writeField(element, "USE_OPTIONS", String.valueOf(useOptions));
        JDOMExternalizerUtil.writeField(element, "OPTIONS", options);
        JDOMExternalizerUtil.writeField(element, "USE_VARIABLES", String.valueOf(useVariables));
        JDOMExternalizerUtil.writeField(element, "VARIABLES", variables);
        JDOMExternalizerUtil.writeField(element, "VERBOSE", String.valueOf(verbose));
        JDOMExternalizerUtil.writeField(element, "DISABLE_SCREENSHOT", String.valueOf(disableScreenshot));
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {
        if (runTest) {
            if (StringUtil.isEmptyOrSpaces(testTargets)) {
                throw new RuntimeConfigurationError("Please specify test targets");
            }
        } else if (runFailed) {
            if (StringUtil.isEmptyOrSpaces(xunitXML)) {
                throw new RuntimeConfigurationError("Please specify xunit XML");
            }
        }
        String workingDirectory = getWorkingDirectory();
        if (!StringUtil.isEmptyOrSpaces(workingDirectory)) {
            File workingDirectoryFile = new File(workingDirectory);
            if (!workingDirectoryFile.isDirectory()) {
                throw new RuntimeConfigurationWarning("Working directory is not a valid directory");
            }
        } else {
            throw new RuntimeConfigurationWarning("Please specify working directory");
        }
        String interpreterPath = getInterpreterPath();
        if (interpreterPath == null) {
            throw new RuntimeConfigurationWarning("No interpreter is specified.");
        }
        Sdk sdkPath = PythonSdkType.findSdkByPath(getInterpreterPath());
        if (sdkPath == null) {
            throw new RuntimeConfigurationWarning("No sdk found.");
        }
        PyPackage ptest = null;
        try {
            ptest = PyPackageManager.getInstance(sdkPath).findPackage("ptest", false);
        } catch (ExecutionException e) {
            return;
        }
        if (ptest == null)
            throw new RuntimeConfigurationWarning("No ptest module found in selected interpreter");
    }
}
