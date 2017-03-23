package com.iselsoft.ptest.runConfiguration;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.Filter;
import com.intellij.execution.testframework.TestFrameworkRunningModel;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentContainer;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.python.run.AbstractPythonRunConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class PTestRerunFailedTestsAction extends AbstractRerunFailedTestsAction {
    private static final Filter NOT_PASSED_FILTER = new Filter() {
        @Override
        public boolean shouldAccept(final AbstractTestProxy test) {
            return test.isInProgress() || test.isDefect() || test.isInterrupted() || test.isIgnored();
        }
    }; 
    
    protected PTestRerunFailedTestsAction(@NotNull ComponentContainer componentContainer) {
        super(componentContainer);
    }

    protected Filter<?> getFailuresFilter() {
        return NOT_PASSED_FILTER;
    }
    
    @Override
    @Nullable
    protected MyRunProfile getRunProfile(@NotNull ExecutionEnvironment environment) {
        final TestFrameworkRunningModel model = getModel();
        if (model == null) {
            return null;
        }
        return new MyPTestRunProfile((AbstractPythonRunConfiguration) model.getProperties().getConfiguration());
    }

    private class MyPTestRunProfile extends MyRunProfile {

        public MyPTestRunProfile(RunConfigurationBase configuration) {
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

            return new FailedPTestCommandLineState((PTestRunConfiguration) configuration, env,
                    (PTestCommandLineState) configuration.getState(executor, env));
        }
    }

    private class FailedPTestCommandLineState extends PTestCommandLineState {

        private final PTestCommandLineState state;
        private final PTestRunConfiguration configuration;
        private final Project project;

        public FailedPTestCommandLineState(PTestRunConfiguration configuration,
                                           ExecutionEnvironment env,
                                           PTestCommandLineState state) {
            super(configuration, env);
            this.state = state;
            this.configuration = configuration;
            project = configuration.getProject();
        }

        @Override
        protected List<String> getTestSpecs() {
            List<String> specs = new ArrayList<String>();
            List<String> rerunTestTargets = new ArrayList<String>();
            for (AbstractTestProxy failedTest : getFailedTests(project)) {
                if (failedTest.isLeaf()) {
                    if (failedTest.getParent().getParent().getName().equals("[root]")) {
                        rerunTestTargets.add(failedTest.getParent().getName() + "." + failedTest.getName());
                    } else {
                        rerunTestTargets.add(failedTest.getParent().getParent().getName() + "." + failedTest.getName());
                    }
                }
            }
            specs.add("-t");
            specs.add(StringUtil.join(rerunTestTargets, ","));
            return specs;
        }
    }
}