package com.iselsoft.ptest.runConfiguration;

import com.intellij.execution.configurations.*;
import com.jetbrains.python.run.PythonConfigurationFactoryBase;
import icons.PythonIcons;
import com.intellij.openapi.project.Project;

import javax.swing.*;

public class PTestConfigurationType extends ConfigurationTypeBase {
    
    public final PythonPTestConfigurationFactory PY_PTEST_FACTORY = new PythonPTestConfigurationFactory(this);

    public PTestConfigurationType() {
        super("ptest", "ptest", "run ptest", PythonIcons.Python.PythonTests);
    }

    public static PTestConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(PTestConfigurationType.class);
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{PY_PTEST_FACTORY};
    }

    public class PythonPTestConfigurationFactory extends PythonConfigurationFactoryBase {
        protected PythonPTestConfigurationFactory(ConfigurationType configurationType) {
            super(configurationType);
        }

        @Override
        public RunConfiguration createTemplateConfiguration(Project project) {
            return new PTestRunConfiguration(project, this);
        }

        @Override
        public String getName() {
            return "ptest";
        }

        @Override
        public Icon getIcon() {
            return PythonIcons.Python.PythonTests;
        }
    }
}
