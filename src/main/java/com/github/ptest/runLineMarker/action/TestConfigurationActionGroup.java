package com.github.ptest.runLineMarker.action;

import com.github.ptest.element.PTestConfiguration;
import com.github.ptest.element.PTestElement;
import com.github.ptest.element.PTestMethod;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;
import icons.PTestIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TestConfigurationActionGroup extends ActionGroup {
    final private PsiElement myElement;

    public TestConfigurationActionGroup(PsiElement element) {
        super("Test Configurations", null, PTestIcons.TestConfiguration);
        this.setPopup(true);
        myElement = element;
    }

    public boolean hideIfNoVisibleChildren() {
        return true;
    }

    @NotNull
    @Override
    public AnAction[] getChildren(@Nullable AnActionEvent e) {
        Map<String, AnAction> actionsMap = new LinkedHashMap<>();
        // for sorting the test configurations
        actionsMap.put("BeforeSuite", null);
        actionsMap.put("AfterSuite", null);
        actionsMap.put("BeforeClass", null);
        actionsMap.put("AfterClass", null);
        actionsMap.put("BeforeGroup", null);
        actionsMap.put("AfterGroup", null);
        actionsMap.put("BeforeMethod", null);
        actionsMap.put("AfterMethod", null);

        PTestMethod pTestMethod = PTestMethod.createFrom(myElement);
        String group = pTestMethod.getGroup();

        for (PTestElement pTestElement : pTestMethod.getParent().getChildren()) {
            if (pTestElement instanceof PTestConfiguration) {
                PTestConfiguration pTestConfiguration = (PTestConfiguration) pTestElement;
                if (!Arrays.asList("BeforeGroup", "AfterGroup", "BeforeMethod", "AfterMethod").contains(pTestConfiguration.getName())
                        || Objects.equals(group, pTestConfiguration.getGroup())) {
                    actionsMap.put(pTestConfiguration.getName(), new NavigateAction(pTestConfiguration.getValue(),
                            pTestConfiguration.getName(), null, PTestIcons.TestConfiguration));
                }
            }
        }

        List<AnAction> actions = new LinkedList<>();
        for(Map.Entry<String, AnAction> entry: actionsMap.entrySet()) {
            if (entry.getValue() != null) {
                actions.add(entry.getValue());
            }
        }
        
        return actions.toArray(new AnAction[0]);
    }
}
