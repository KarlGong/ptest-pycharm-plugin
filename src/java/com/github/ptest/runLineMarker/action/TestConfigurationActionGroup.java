package com.github.ptest.runLineMarker.action;

import com.github.ptest.PTestUtil;
import com.github.ptest.element.PTestClass;
import com.github.ptest.element.PTestConfiguration;
import com.github.ptest.element.PTestElement;
import com.github.ptest.element.PTestMethod;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyExpression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class TestConfigurationActionGroup extends ActionGroup {
    final private PsiElement myElement;

    public TestConfigurationActionGroup(PsiElement element) {
        super("Test Configurations", null, AllIcons.Css.Atrule);
        this.setPopup(true);
        myElement = element;
    }

    public boolean hideIfNoVisibleChildren() {
        return true;
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        Map<String, AnAction> actions = new LinkedHashMap<>();
        actions.put("BeforeMethod", null);
        actions.put("AfterMethod", null);
        actions.put("BeforeGroup", null);
        actions.put("AfterGroup", null);
        actions.put("BeforeClass", null);
        actions.put("AfterClass", null);
        actions.put("BeforeSuite", null);
        actions.put("AfterSuite", null);

        PTestMethod pTestMethod = PTestMethod.createFrom(myElement);
        String group = pTestMethod.getGroup();

        for (PTestElement pTestElement : pTestMethod.getParent().getChildren()) {
            if (pTestElement instanceof PTestConfiguration) {
                PTestConfiguration pTestConfiguration = (PTestConfiguration) pTestElement;
                if (Objects.equals(group, pTestConfiguration.getGroup())) {
                    actions.put(pTestConfiguration.getName(), new NavigateAction(pTestConfiguration.getValue(),
                            pTestConfiguration.getName(), null, AllIcons.Css.Atrule));
                }
            }
        }
        
        return actions.values().toArray(new AnAction[actions.size()]);
    }
}
