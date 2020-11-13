package com.github.ptest.runLineMarker;

import com.github.ptest.element.PTestClass;
import com.github.ptest.element.PTestMethod;
import com.github.ptest.runLineMarker.action.RunTestAction;
import com.github.ptest.runLineMarker.action.TestConfigurationActionGroup;
import com.intellij.execution.Executor;
import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import icons.PTestIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PTestRunLineMarkerContributor extends RunLineMarkerContributor {
    @Nullable
    @Override
    public Info getInfo(@NotNull PsiElement element) {
        if (element instanceof PyFunction && PTestMethod.createFrom(element) != null) {
            return new Info(PTestIcons.Test, e -> "PTest @Test", getTestActions(element));
        } else if (element instanceof PyClass && PTestClass.createFrom(element) != null) {
            return new Info(PTestIcons.TestClass, e -> "PTest @TestClass", getTestClassActions(element));
        }
        return null;
    }

    @NotNull
    private static AnAction[] getTestActions(PsiElement element) {
        List<AnAction> actions = new ArrayList<>();
        // runs
        for (Executor executor : Executor.EXECUTOR_EXTENSION_NAME.getExtensions()) {
            actions.add(new RunTestAction(executor, element));
        }
        // goto
        actions.add(new TestConfigurationActionGroup(element));
        return actions.toArray(new AnAction[0]);
    }

    @NotNull
    private static AnAction[] getTestClassActions(PsiElement element) {
        List<AnAction> actions = new ArrayList<>();
        for (Executor executor : Executor.EXECUTOR_EXTENSION_NAME.getExtensions()) {
            actions.add(new RunTestAction(executor, element));
        }
        return actions.toArray(new AnAction[0]);
    }
}
