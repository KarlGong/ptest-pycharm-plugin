package com.github.ptest.runConfiguration;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.Project;
import com.jetbrains.python.run.PythonConfigurationFactoryBase;
import icons.PTestIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PTestConfigurationType extends ConfigurationTypeBase {
    
    public final PTestConfigurationFactory PTEST_FACTORY = new PTestConfigurationFactory(this);

    public PTestConfigurationType() {
        super("ptest", "ptest", "run ptest", PTestIcons.PTest);
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
        public String getId() {
            return "ptest";
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
            return PTestIcons.PTest;
        }
    }
}
