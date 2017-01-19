package com.iselsoft.ptest.runLineMarker;

import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.util.Function;
import com.iselsoft.ptest.PTestUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.Nullable;

public class PTestRunLineMarkerContributor extends RunLineMarkerContributor {
    private static final Function<PsiElement, String> TOOLTIP_PROVIDER = psiElement -> "Run ptest";

    @Nullable
    @Override
    public Info getInfo(PsiElement psiElement) {
        if (psiElement instanceof PyFunction && PTestUtil.getPTestMethod(psiElement) != null && PTestUtil.getPTestClass(psiElement) != null) {
            return new Info(AllIcons.RunConfigurations.TestState.Run, TOOLTIP_PROVIDER, PTestRunLineMarkerAction.getActions());
        } else if (psiElement instanceof PyClass && PTestUtil.getPTestClass(psiElement) != null) {
            return new Info(AllIcons.RunConfigurations.TestState.Run_run, TOOLTIP_PROVIDER, PTestRunLineMarkerAction.getActions());
        }
        return null;
    }
}
