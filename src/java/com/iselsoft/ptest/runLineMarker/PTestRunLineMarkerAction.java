package com.iselsoft.ptest.runLineMarker;

import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.iselsoft.ptest.runConfiguration.PTestConfigurationProducer;
import com.iselsoft.ptest.runConfiguration.PTestConfigurationType;
import com.iselsoft.ptest.runConfiguration.PTestRunConfiguration;
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
        RunnerAndConfigurationSettings setting = context.getConfiguration();
        if (setting == null || setting.getConfiguration() == null || !(setting.getConfiguration() instanceof PTestRunConfiguration)) {
            RunConfiguration config = CONFIG_PRODUCER.createLightConfiguration(context);
            if (config == null) return;
            removeOldTemporaryConfigurations(runManager);
            setting = runManager.createConfiguration(config, config.getFactory());
            runManager.setTemporaryConfiguration(setting);
        } else {
            boolean hasExistingSetting = false;
            for (RunnerAndConfigurationSettings existingSetting : runManager.getConfigurationSettingsList(new PTestConfigurationType())) {
                if (existingSetting.equals(setting)) {
                    hasExistingSetting = true;
                    break;
                }
            }
            if (!hasExistingSetting) {
                removeOldTemporaryConfigurations(runManager);
                runManager.addConfiguration(setting, runManager.isConfigurationShared(setting));
                runManager.setTemporaryConfiguration(setting);
            }
        }
        runManager.setSelectedConfiguration(setting);

        ExecutionUtil.runConfiguration(setting, myExecutor);
    }
    
    private void removeOldTemporaryConfigurations(RunManagerEx runManager) {
        int tempSettingsLimit = runManager.getConfig().getRecentsLimit();
        while (true) {
            List<RunnerAndConfigurationSettings> tempSettings = runManager.getTempConfigurationsList();
            if (tempSettings.size() >= tempSettingsLimit) {
                runManager.removeConfiguration(tempSettings.get(0));
            } else {
                break;
            }
        } 
    }
    
    private String getActionName(DataContext dataContext, @NotNull Executor executor) {
        final ConfigurationContext context = ConfigurationContext.getFromContext(dataContext);
        if (context.getLocation() == null) return null;
        RunnerAndConfigurationSettings setting = context.getConfiguration();
        if (setting == null || setting.getConfiguration() == null || !(setting.getConfiguration() instanceof PTestRunConfiguration)) {
            RunConfiguration runConfiguration = CONFIG_PRODUCER.createLightConfiguration(context);
            if (runConfiguration == null) return null;
            PTestRunConfiguration config = (PTestRunConfiguration) runConfiguration;
            return executor.getStartActionText(config.getActionName());
        } else {
            PTestRunConfiguration config = (PTestRunConfiguration) setting.getConfiguration();
            return executor.getStartActionText(config.getActionName());
        }
    }
}
