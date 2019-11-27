package com.github.ptest.runConfiguration;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.run.PythonConfigurationFactoryBase;
import icons.PythonIcons;

import javax.swing.*;

public class PTestConfigurationType extends ConfigurationTypeBase {
    
    public final PTestConfigurationFactory PTEST_FACTORY = new PTestConfigurationFactory(this);

    public PTestConfigurationType() {
        super("com/github/ptest", "com/github/ptest", "run com.github.ptest", PythonIcons.Python.PythonTests);
    }

    public static PTestConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(PTestConfigurationType.class);
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{PTEST_FACTORY};
    }

    public class PTestConfigurationFactory extends PythonConfigurationFactoryBase {
        protected PTestConfigurationFactory(ConfigurationType configurationType) {
            super(configurationType);
        }

        @Override
        public RunConfiguration createTemplateConfiguration(Project project) {
            return new PTestRunConfiguration(project, this);
        }

        @Override
        public String getName() {
            return "com/github/ptest";
        }

        @Override
        public Icon getIcon() {
            return PythonIcons.Python.PythonTests;
        }
    }
}
