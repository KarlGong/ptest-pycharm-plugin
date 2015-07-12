package karl.gong.ptest;

import com.intellij.execution.configurations.*;
import com.jetbrains.python.run.PythonConfigurationFactoryBase;
import icons.PythonIcons;
import com.intellij.openapi.project.Project;

import javax.swing.*;

public class PythonPTestConfigurationType extends ConfigurationTypeBase {
    
    public final PythonPTestConfigurationFactory PY_PTEST_FACTORY = new PythonPTestConfigurationFactory(this);

    public PythonPTestConfigurationType() {
        super("ptest", "ptest", "run ptest", PythonIcons.Python.PythonTests);
        addFactory(new PythonPTestConfigurationFactory(this) {
            @Override
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new PythonPTestRunConfiguration(project, this);
            }
        });
    }

    public static PythonPTestConfigurationType getInstance() {
        return ConfigurationTypeUtil.findConfigurationType(PythonPTestConfigurationType.class);
    }


    public class PythonPTestConfigurationFactory extends PythonConfigurationFactoryBase {
        protected PythonPTestConfigurationFactory(ConfigurationType configurationType) {
            super(configurationType);
        }

        @Override
        public RunConfiguration createTemplateConfiguration(Project project) {
            return new PythonPTestRunConfiguration(project, this);
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

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{PY_PTEST_FACTORY};
    }
}
