package com.github.ptest.loadPropertyFile;

import com.github.ptest.runConfiguration.PTestConfigurationType;
import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Path;
import java.nio.file.Paths;

public class LoadPropertyFileAction extends AnAction {

    @Override
    public void update(AnActionEvent e) {
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file.getExtension() != null && file.getExtension().equalsIgnoreCase("ini")) {
            e.getPresentation().setVisible(true);
            e.getPresentation().setText(String.format("Load Property File '%s' for PTest", file.getName()));
        } else {
            e.getPresentation().setVisible(false);
        }
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        RunManager runManager = RunManager.getInstance(e.getProject());

        Path configFilePath = Paths.get(e.getData(PlatformDataKeys.VIRTUAL_FILE).getPath());

        // update existing run configuration
        for (RunConfiguration config : runManager.getConfigurationsList(new PTestConfigurationType())) {
            PTestRunConfiguration ptestConfig = (PTestRunConfiguration) config;
            ptestConfig.setUsePropertyFile(true);
            if (StringUtil.isEmptyOrSpaces(ptestConfig.getWorkingDirectory())) {
                ptestConfig.setPropertyFile(Paths.get(e.getProject().getBasePath()).relativize(configFilePath).toString());
            } else {
                ptestConfig.setPropertyFile(Paths.get(ptestConfig.getWorkingDirectory()).relativize(configFilePath).toString());
            }
            ptestConfig.setPropertyFile(Paths.get(ptestConfig.getWorkingDirectory()).relativize(configFilePath).toString());
        }

        // update template run configuration
        RunConfiguration templateConfig = runManager.getConfigurationTemplate(new PTestConfigurationType().PTEST_FACTORY).getConfiguration();
        PTestRunConfiguration ptestTemplateConfig = (PTestRunConfiguration) templateConfig;
        ptestTemplateConfig.setUsePropertyFile(true);
        if (StringUtil.isEmptyOrSpaces(ptestTemplateConfig.getWorkingDirectory())) {
            ptestTemplateConfig.setPropertyFile(Paths.get(e.getProject().getBasePath()).relativize(configFilePath).toString());   
        } else {
            ptestTemplateConfig.setPropertyFile(Paths.get(ptestTemplateConfig.getWorkingDirectory()).relativize(configFilePath).toString());
        }
    }
}
