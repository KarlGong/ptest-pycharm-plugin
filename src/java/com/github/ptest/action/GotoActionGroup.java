package com.github.ptest.action;

import com.github.ptest.PTestUtil;
import com.intellij.execution.Location;
import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.openapi.actionSystem.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GotoActionGroup extends ActionGroup {
    @Override
    public void update(AnActionEvent e) {
        PsiElement element = PTestUtil.getPsiElement(e);
        e.getPresentation().setVisible(element != null
                && PTestUtil.getPTestMethod(element) != null
                && PTestUtil.getPTestClass(element) != null);
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        PsiElement element = PTestUtil.getPsiElement(e);
        return new AnAction[]{
                new GotoTestConfigurationAction(element, "BeforeMethod", true),
                new GotoTestConfigurationAction(element, "AfterMethod", true),
                new GotoTestConfigurationAction(element, "BeforeGroup", true),
                new GotoTestConfigurationAction(element, "AfterGroup", true),
                new GotoTestConfigurationAction(element, "BeforeClass", false),
                new GotoTestConfigurationAction(element, "AfterClass", false),
                new GotoTestConfigurationAction(element, "BeforeSuite", false),
                new GotoTestConfigurationAction(element, "AfterSuite", false)
        };
    }
}
