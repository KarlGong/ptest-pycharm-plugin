package com.github.ptest.toolWindow;

import com.github.ptest.element.PTestElement;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyElement;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PTestStructureViewElement implements StructureViewTreeElement {
    private PTestStructureViewElement myParent;
    private PTestElement myElement;

    public PTestStructureViewElement(PTestStructureViewElement parent, PTestElement element) {
        myParent = parent;
        myElement = element;
    }

    public PTestStructureViewElement getParent() {
        return myParent;
    }

    @Override
    public PyElement getValue() {
        PsiElement element = myElement.getElement();
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
        getValue().navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return getValue().canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return getValue().canNavigateToSource();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StructureViewTreeElement) {
            final Object value = ((StructureViewTreeElement) o).getValue();
            final String name = getValue().getName();
            if (value instanceof PyElement && name != null) {
                return name.equals(((PyElement) value).getName());
            }
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
                PTestStructureViewElement child = new PTestStructureViewElement(this, (PTestElement) pTestElement);
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
