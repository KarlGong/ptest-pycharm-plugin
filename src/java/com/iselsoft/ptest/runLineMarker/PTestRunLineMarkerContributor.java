package com.iselsoft.ptest.runLineMarker;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.iselsoft.ptest.runConfiguration.PTestConfigurationProducer;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.Nullable;

public class PTestRunLineMarkerContributor extends RunLineMarkerContributor {
    private static final PTestConfigurationProducer CONFIG_PRODUCER = new PTestConfigurationProducer();
    private static final Function<PsiElement, String> TOOLTIP_PROVIDER = new Function<PsiElement, String>() {
        @Override
        public String fun(PsiElement psiElement) {
            return "Run ptest";
        }
    };

    @Nullable
    @Override
    public Info getInfo(PsiElement psiElement) {
        if (psiElement instanceof PyFunction && CONFIG_PRODUCER.isPTestMethod(psiElement)) {
            return new Info(AllIcons.RunConfigurations.TestState.Run, TOOLTIP_PROVIDER, PTestRunLineMarkerAction.getActions());
        } else if (psiElement instanceof PyClass && CONFIG_PRODUCER.isPTestClass(psiElement)) {
            return new Info(AllIcons.RunConfigurations.TestState.Run_run, TOOLTIP_PROVIDER, PTestRunLineMarkerAction.getActions());
        }
        return null;
    }
}
