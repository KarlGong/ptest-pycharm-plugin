package com.github.ptest.element;

import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.icons.AllIcons;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyFunction;
import org.apache.commons.lang.StringUtils;

import javax.swing.*;

public class PTestConfiguration extends PTestElement<PyFunction> {
    private PTestClass myParent;
    private String myName;

    public PTestConfiguration(PTestClass parent, PyFunction pyFunction, String name) {
        super(pyFunction);
        myParent = parent;
        myName = name;
    }

    public boolean isInherited() {
        return getValue().getContainingClass() != myParent.getValue();
    }

    public String getName() {
        return myName;
    }

    public String getGroup() {
        PyExpression valueExp = getValue().getDecoratorList().findDecorator(getName()).getKeywordArgument("group");
        return valueExp == null ? null : valueExp.getText();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new ColoredItemPresentation() {
            @Override
            public String getPresentableText() {
                return getName();
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
                return AllIcons.Css.Atrule;
            }
        };
    }

    @Override
    public boolean setupConfiguration(PTestRunConfiguration configuration) {
        throw new UnsupportedOperationException();
    }
}
