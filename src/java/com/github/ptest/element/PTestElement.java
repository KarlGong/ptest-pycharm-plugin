package com.github.ptest.element;

import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.List;

public abstract class PTestElement<E extends PsiElement> {
    private E myElement;
    private List<String> myErrors = new ArrayList<>();

    public PTestElement(E element) {
        myElement = element;
    }

    public E getValue() {
        return myElement;
    }
    
    public void addError(String error) {
        myErrors.add(error);
    }
    
    public List<String> getErrors() {
        return myErrors;
    }

    public abstract boolean setupConfiguration(PTestRunConfiguration configuration);

    public List<PTestElement> getChildren() {
        return null;
    }

    public ItemPresentation getPresentation() {
        return null;
    }
}
