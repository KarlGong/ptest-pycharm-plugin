package com.github.ptest.runLineMarker;

import com.github.ptest.PTestUtil;
import com.github.ptest.runLineMarker.action.TestConfigurationActionGroup;
import com.github.ptest.runLineMarker.action.RunTestAction;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PTestRunLineMarkerContributor extends RunLineMarkerContributor {
    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {
        if (element instanceof PyFunction && PTestUtil.getPTestMethod(element) != null && PTestUtil.getPTestClass(element) != null) {
            return new Info(AllIcons.RunConfigurations.TestState.Run, e -> "PTest @Test", getTestActions(element));
        } else if (element instanceof PyClass && PTestUtil.getPTestClass(element) != null) {
            return new Info(AllIcons.RunConfigurations.TestState.Run_run, e -> "PTest @TestClass", getTestClassActions(element));
        }
        return null;
    }

    @NotNull
    private static AnAction[] getTestActions(PsiElement element) {
        List<AnAction> actions = new ArrayList<>();
        // runs
        for (Executor executor : ExecutorRegistry.getInstance().getRegisteredExecutors()) {
            actions.add(new RunTestAction(executor, element));
        }
        // goto
        actions.add(new TestConfigurationActionGroup(element));
        return actions.toArray(new AnAction[actions.size()]);
    }

    @NotNull
    private static AnAction[] getTestClassActions(PsiElement element) {
        List<AnAction> actions = new ArrayList<>();
        for (Executor executor : ExecutorRegistry.getInstance().getRegisteredExecutors()) {
            actions.add(new RunTestAction(executor, element));
        }
        return actions.toArray(new AnAction[actions.size()]);
    }
}
