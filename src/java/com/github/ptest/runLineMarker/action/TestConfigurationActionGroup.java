package com.github.ptest.runLineMarker.action;

import com.github.ptest.PTestUtil;
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
        PyExpression valueExp = pTestMethod.getValue().getDecoratorList().findDecorator("Test").getKeywordArgument("group");
        final String groupName = valueExp == null ? null : valueExp.getText();

        PyClass pyClass = PsiTreeUtil.getParentOfType(myElement, PyClass.class, false);
        pyClass.visitMethods(function -> {
            for (String configWithGroup : new String[]{"BeforeMethod", "AfterMethod", "BeforeGroup", "AfterGroup"}) {
                if (PTestUtil.hasDecorator(function, configWithGroup, groupName == null ? null : "group", groupName)) {
                    actions.put(configWithGroup, new NavigateAction(function, configWithGroup, null, AllIcons.Css.Atrule));
                }
            }

            for (String config : new String[]{"BeforeClass", "AfterClass", "BeforeSuite", "AfterSuite"}) {
                if (PTestUtil.hasDecorator(function, config, null, null)) {
                    actions.put(config, new NavigateAction(function, config, null, AllIcons.Css.Atrule));
                }
            }
            return true;
        }, true, null);

        return actions.values().toArray(new AnAction[actions.size()]);
    }
}
