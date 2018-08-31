package com.github.ptest.runLineMarker.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.pom.Navigatable;

import javax.swing.*;

public class NavigateAction extends AnAction {
    private Navigatable myDest;
    
    public NavigateAction(Navigatable dest, String text, String description, Icon icon) {
        super(text, description, icon);
        myDest = dest;
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        myDest.navigate(true);
    }
}
