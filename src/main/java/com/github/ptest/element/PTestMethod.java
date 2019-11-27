package com.github.ptest.element;

import com.github.ptest.PTestUtil;
import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.LayeredIcon;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyFunction;
import icons.RemoteServersIcons;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;

public class PTestMethod extends PTestElement<PyFunction> {
    private PTestClass myParent;

    public PTestMethod(PTestClass parent, PyFunction pyFunction) {
        super(pyFunction);
        myParent = parent;
    }

    public boolean isInherited() {
        return getValue().getContainingClass() != getParent().getValue();
    }

    public boolean isDataProvided() {
        return PTestUtil.hasDecorator(getValue(), "Test", "data_provider", null);
    }
    
    public PTestClass getParent() {
        return myParent;
    }

    public String getGroup() {
        PyExpression valueExp = getValue().getDecoratorList().findDecorator("Test").getKeywordArgument("group");
        return valueExp == null ? null : valueExp.getText();
    }
    
    @Override
    public boolean setupConfiguration(PTestRunConfiguration configuration) {
        try {
            configuration.setValueForEmptyWorkingDirectory();
            configuration.setRunTest(true);
            String testName = getValue().getName();
            String testTarget = PTestUtil.findShortestImportableName(getParent().getValue().getContainingFile()) + "."
                    + getParent().getValue().getName() + "." + testName;
            String suggestedName = getParent().getValue().getName() + "." + testName;
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptest " + suggestedName);
            configuration.setActionName("ptest " + testName);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ColoredItemPresentation() {
            @Override
            public String getPresentableText() {
                return getValue().getName();
            }

            @Override
            public TextAttributesKey getTextAttributesKey() {
                if (getErrors().size() > 0) {
                    return CodeInsightColors.WRONG_REFERENCES_ATTRIBUTES;
                }
                return isInherited() ? CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES : null;
            }

            @Override
            public String getLocationString() {
                if (getErrors().size() > 0) {
                    return getErrors().get(0);
                }
                String group = getGroup();
                return group != null ? StringUtils.strip(group, "\"") : null;
            }

            @Override
            public Icon getIcon(boolean open) {
                Icon normalIcon = getValue().getIcon(0);
                if (isDataProvided()) {
                    LayeredIcon icon = new LayeredIcon(2);
                    icon.setIcon(normalIcon, 0);
                    icon.setIcon(RemoteServersIcons.ResumeScaled, 1);
                    return icon;
                }
                return normalIcon;
            }
        };
    }

    public static PTestMethod createFrom(PsiElement element) {
        final PyFunction pyFunction = PsiTreeUtil.getParentOfType(element, PyFunction.class, false);
        if (pyFunction == null) return null;

        final PyClass containingClass = pyFunction.getContainingClass();
        if (containingClass == null) return null;

        if (!PTestUtil.hasDecorator(pyFunction, "Test", null, null)) return null;

        PTestClass pTestClass = PTestClass.createFrom(element);
        if (pTestClass == null) return null;

        return new PTestMethod(pTestClass, pyFunction);
    }
}
