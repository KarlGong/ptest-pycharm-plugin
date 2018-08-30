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
    public Info getInfo(@NotNull PsiElement element) {
        if (element instanceof PyFunction && PTestUtil.getPTestMethod(element) != null && PTestUtil.getPTestClass(element) != null) {
            return new Info(AllIcons.RunConfigurations.TestState.Run, e -> "Run test", PTestRunLineMarkerAction.getActions());
        } else if (element instanceof PyClass && PTestUtil.getPTestClass(element) != null) {
            return new Info(AllIcons.RunConfigurations.TestState.Run_run, e -> "Run test class", PTestRunLineMarkerAction.getActions());
        }
        return null;
    }
}
