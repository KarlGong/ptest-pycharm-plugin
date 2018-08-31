package com.github.ptest.runLineMarker.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.pom.Navigatable;

public class NavigateAction extends AnAction {
    private Navigatable myDest;
    
    public NavigateAction(String text, Navigatable dest) {
        super(text);
        myDest = dest;
    }
    
    @Override
    public void actionPerformed(AnActionEvent e) {
        myDest.navigate(true);
    }
}
