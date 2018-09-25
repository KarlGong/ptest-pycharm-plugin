package com.github.ptest.element;

import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;

import java.util.List;

public abstract class PTestElement<E extends PsiElement> {
    protected E myElement;

    public PTestElement(E element) {
        myElement = element;
    }

    public E getValue() {
        return myElement;
    }

    public abstract boolean setupConfiguration(PTestRunConfiguration configuration);

    public List<PTestElement> getChildren() {
        return null;
    }

    public ItemPresentation getPresentation() {
        return null;
    }
}
