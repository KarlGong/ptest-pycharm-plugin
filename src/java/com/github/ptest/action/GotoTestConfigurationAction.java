package com.github.ptest.action;

import com.github.ptest.PTestUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.Nullable;

public class GotoTestConfigurationAction extends AnAction {
    private PsiElement element;
    private String decoratorName;
    private boolean hasGroup;
    
    public GotoTestConfigurationAction(PsiElement element, @Nullable String decoratorName, boolean hasGroup) {
        super("@" + decoratorName);
        this.element = element;
        this.decoratorName = decoratorName;
        this.hasGroup = hasGroup;
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        if (hasGroup) {
            PyFunction pyFunction = PTestUtil.getPTestMethod(this.element);
            PyExpression pyExp = pyFunction.getDecoratorList().findDecorator("Test").getKeywordArgument("group");
            String groupName = pyExp == null ? "DEFAULT" : pyExp.getText().replace("group=", "");

            PyClass pyClass = PsiTreeUtil.getParentOfType(this.element, PyClass.class, false);
            pyClass.visitMethods(function -> {
                PyDecoratorList decoratorList = function.getDecoratorList();
                if (decoratorList == null) return true;
                PyDecorator decorator = decoratorList.findDecorator(this.decoratorName);
                if (decorator == null) return true;
                PyExpression exp = decorator.getKeywordArgument("group");
                if ((exp == null ? "DEFAULT" : exp.getText().replace("group=", "")).equals(groupName)) {
                    function.navigate(true);
                    return false;
                }
                return true;
            }, true, null);
        } else {
            PyClass pyClass = PsiTreeUtil.getParentOfType(this.element, PyClass.class, false);
            pyClass.visitMethods(function -> {
                PyDecoratorList decoratorList = function.getDecoratorList();
                if (decoratorList == null) return true;
                PyDecorator decorator = decoratorList.findDecorator(this.decoratorName);
                if (decorator == null) return true;
                function.navigate(true);
                return false;
            }, true, null);
        }
    }
}
