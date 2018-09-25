package com.github.ptest.toolWindow;

import com.github.ptest.element.PTestConfiguration;
import com.github.ptest.element.PTestElement;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PTestStructureViewElement implements StructureViewTreeElement {
    private PTestElement myElement;

    public PTestStructureViewElement(PTestElement element) {
        myElement = element;
    }

    @Override
    public PyElement getValue() {
        PsiElement element = myElement.getValue();
        if (element instanceof PyElement) {
            return (PyElement) element;
        }
        return null;
    }

    public PTestElement getElement() {
        return myElement;
    }

    @Override
    public void navigate(boolean requestFocus) {
        PyElement element = getValue();
        if (element != null) {
            element.navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return getValue() != null && getValue().canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return getValue() != null && getValue().canNavigateToSource();
    }

    @Override
    public boolean equals(Object otherObj) {
        if (otherObj instanceof PTestStructureViewElement) {
            PyElement element = ((PTestStructureViewElement) otherObj).getValue();
            return Objects.equals(getValue().getName(), element.getName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        final String name = getValue().getName();
        return name != null ? name.hashCode() : 0;
    }

    @NotNull
    public PTestStructureViewElement[] getChildren() {
        final List<PTestStructureViewElement> children = new ArrayList<>();

        if (myElement.getChildren() != null) {
            for (Object pTestElement : myElement.getChildren()) {
                PTestStructureViewElement child = new PTestStructureViewElement((PTestElement) pTestElement);
                children.add(child);
            }
        }

        return children.toArray(new PTestStructureViewElement[children.size()]);
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        return myElement.getPresentation();
    }
}
