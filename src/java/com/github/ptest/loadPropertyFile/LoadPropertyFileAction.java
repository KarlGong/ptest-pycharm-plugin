package com.github.ptest.loadPropertyFile;

import com.github.ptest.runConfiguration.PTestConfigurationType;
import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;

import java.io.File;
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

        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        String path = Paths.get(e.getProject().getBasePath()).relativize(Paths.get(file.getPath())).toString();
        
        // update existing run configuration
        for (RunConfiguration config : runManager.getConfigurationsList(new PTestConfigurationType())) {
            PTestRunConfiguration ptestConfig = (PTestRunConfiguration) config;
            ptestConfig.setUseOptions(true);
            ptestConfig.setOptions("-p " + path);
        }

        // update template run configuration
        RunConfiguration templateConfig = runManager.getConfigurationTemplate(new PTestConfigurationType().PTEST_FACTORY).getConfiguration();
        PTestRunConfiguration ptestTemplateConfig = (PTestRunConfiguration) templateConfig;
        ptestTemplateConfig.setUseOptions(true);
        ptestTemplateConfig.setOptions("-p " + path);
    }


}
