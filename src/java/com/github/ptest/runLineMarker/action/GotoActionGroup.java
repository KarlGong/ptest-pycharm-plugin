package com.github.ptest.runLineMarker.action;

import com.github.ptest.PTestUtil;
import com.intellij.openapi.actionSystem.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class GotoActionGroup extends ActionGroup {
    final private PsiElement myElement;

    public GotoActionGroup(PsiElement element) {
        super("Go To", true);
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

        PyFunction pyFunction = PTestUtil.getPTestMethod(myElement);
        PyExpression valueExp = pyFunction.getDecoratorList().findDecorator("Test").getKeywordArgument("group");
        final String groupName = valueExp == null ? null : valueExp.getText();

        PyClass pyClass = PsiTreeUtil.getParentOfType(myElement, PyClass.class, false);
        pyClass.visitMethods(function -> {
            for (String configWithGroup : new String[]{"BeforeMethod", "AfterMethod", "BeforeGroup", "AfterGroup"}) {
                if (actions.get(configWithGroup) == null
                        && PTestUtil.hasDecorator(function, configWithGroup, groupName == null ? null : "group", groupName)) {
                    actions.put(configWithGroup, new NavigateAction("@" + configWithGroup, function));
                }
            }

            for (String config : new String[]{"BeforeClass", "AfterClass", "BeforeSuite", "AfterSuite"}) {
                if (actions.get(config) == null
                        && PTestUtil.hasDecorator(function, config, null, null)) {
                    actions.put(config, new NavigateAction("@" + config, function));
                }
            }
            return true;
        }, true, null);

        return actions.values().toArray(new AnAction[actions.size()]);
    }
}
