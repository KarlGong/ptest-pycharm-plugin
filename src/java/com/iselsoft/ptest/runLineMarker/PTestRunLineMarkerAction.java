package com.iselsoft.ptest.runLineMarker;

import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.BaseRunConfigurationAction;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.actions.ConfigurationFromContextImpl;
import com.intellij.execution.configurations.LocatableConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.RunManagerImpl;
import com.intellij.execution.impl.RunnerAndConfigurationSettingsImpl;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.iselsoft.ptest.runConfiguration.PTestConfigurationProducer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PTestRunLineMarkerAction extends AnAction {
    private static final PTestConfigurationProducer CONFIG_PRODUCER = new PTestConfigurationProducer();
    private final Executor myExecutor;

    private PTestRunLineMarkerAction(@NotNull Executor executor) {
        super(executor.getIcon());
        myExecutor = executor;
    }

    @NotNull
    public static AnAction[] getActions() {
        List<AnAction> actions = new ArrayList<AnAction>();
        for (Executor executor : ExecutorRegistry.getInstance().getRegisteredExecutors()) {
            actions.add(new PTestRunLineMarkerAction(executor));
        }
        return actions.toArray(new AnAction[actions.size()]);
    }

    @Override
    public void update(AnActionEvent e) {
        String name = getActionName(e.getDataContext(), myExecutor);
        e.getPresentation().setVisible(name != null);
        e.getPresentation().setText(name);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final DataContext dataContext = e.getDataContext();
        final ConfigurationContext context = ConfigurationContext.getFromContext(dataContext);
        if (context.getLocation() == null) return;
        final RunManagerEx runManager = (RunManagerEx) context.getRunManager();
        RunnerAndConfigurationSettings configuration = context.findExisting();
        if (configuration == null) {
            configuration = context.getConfiguration();
            if (configuration == null) {
                return;
            }
            runManager.setTemporaryConfiguration(configuration);
        }
        runManager.setSelectedConfiguration(configuration);

        ExecutionUtil.runConfiguration(configuration, myExecutor);
    }

    private String getActionName(DataContext dataContext, @NotNull Executor executor) {
        final ConfigurationContext context = ConfigurationContext.getFromContext(dataContext);
        if (context.getLocation() == null) return null;
        RunConfiguration configuration = CONFIG_PRODUCER.createLightConfiguration(context);
        if (configuration == null) return null;
        RunnerAndConfigurationSettingsImpl
                settings = new RunnerAndConfigurationSettingsImpl(RunManagerImpl.getInstanceImpl(context.getProject()), configuration, false);
        ConfigurationFromContext config = new ConfigurationFromContextImpl(CONFIG_PRODUCER, settings, context.getPsiLocation());
        String actionName = BaseRunConfigurationAction.suggestRunActionName((LocatableConfiguration) config.getConfiguration());
        return executor.getStartActionText(actionName);
    }
}
