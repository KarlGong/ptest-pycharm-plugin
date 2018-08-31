package com.github.ptest.runLineMarker.action;

import com.github.ptest.runConfiguration.PTestConfigurationProducer;
import com.github.ptest.runConfiguration.PTestConfigurationType;
import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.actions.ConfigurationFromContext;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class RunTestAction extends AnAction {
    private static final PTestConfigurationProducer CONFIG_PRODUCER = new PTestConfigurationProducer();
    private final Executor myExecutor;
    private final PsiElement myElement;

    public RunTestAction(@NotNull Executor executor, @NotNull PsiElement element) {
        super(executor.getIcon());
        myExecutor = executor;
        myElement = element;
    }

    @Override
    public void update(AnActionEvent e) {
        String name = getActionName(e.getDataContext(), myExecutor);
        e.getPresentation().setVisible(name != null);
        e.getPresentation().setText(name);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final ConfigurationContext context = new ConfigurationContext(myElement);
        if (context.getLocation() == null) return;
        final RunManagerEx runManager = (RunManagerEx) context.getRunManager();
        RunnerAndConfigurationSettings setting = context.getConfiguration();
        if (setting == null || !(setting.getConfiguration() instanceof PTestRunConfiguration)) {
            ConfigurationFromContext config = CONFIG_PRODUCER.createConfigurationFromContext(context);
            if (config == null) return;
            setting = config.getConfigurationSettings();
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
                runManager.setTemporaryConfiguration(setting);
            }
        }
        runManager.setSelectedConfiguration(setting);

        ExecutionUtil.runConfiguration(setting, myExecutor);
    }

    private String getActionName(DataContext dataContext, @NotNull Executor executor) {
        final ConfigurationContext context = new ConfigurationContext(myElement);
        if (context.getLocation() == null) return null;
        RunnerAndConfigurationSettings setting = context.getConfiguration();
        if (setting == null || !(setting.getConfiguration() instanceof PTestRunConfiguration)) {
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
