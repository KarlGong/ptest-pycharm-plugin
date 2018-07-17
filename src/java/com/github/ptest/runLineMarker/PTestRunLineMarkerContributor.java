package com.github.ptest.runLineMarker;

import com.github.ptest.PTestUtil;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PTestRunLineMarkerContributor extends RunLineMarkerContributor {
    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement psiElement) {
        if (psiElement instanceof PyFunction && PTestUtil.getPTestMethod(psiElement) != null && PTestUtil.getPTestClass(psiElement) != null) {
            return new Info(AllIcons.RunConfigurations.TestState.Run, element -> "Run test", PTestRunLineMarkerAction.getActions());
        } else if (psiElement instanceof PyClass && PTestUtil.getPTestClass(psiElement) != null) {
            return new Info(AllIcons.RunConfigurations.TestState.Run_run, element -> "Run test class", PTestRunLineMarkerAction.getActions());
        }
        return null;
    }
}
