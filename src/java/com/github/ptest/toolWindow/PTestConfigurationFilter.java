package com.github.ptest.toolWindow;

import com.github.ptest.element.PTestConfiguration;
import com.github.ptest.element.PTestElement;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.smartTree.ActionPresentation;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PTestConfigurationFilter implements Filter {
    @NotNull
    @Override
    public ActionPresentation getPresentation() {
        return new ActionPresentation() {
            @NotNull
            @Override
            public String getText() {
                return "Show Test Configurations";
            }

            @Override
            public String getDescription() {
                return "Show Test Configurations";
            }

            @Override
            public Icon getIcon() {
                return AllIcons.Css.Atrule;
            }
        };
    }

    @NotNull
    @Override
    public String getName() {
        return "SHOW_TEST_CONFIGURATIONS";
    }

    @Override
    public boolean isVisible(TreeElement treeElement) {
        if (treeElement instanceof PTestStructureViewElement) {
            PTestElement element = ((PTestStructureViewElement) treeElement).getElement();
            return !(element instanceof PTestConfiguration);
        }
        return false;
    }

    @Override
    public boolean isReverted() {
        return true;
    }
}
