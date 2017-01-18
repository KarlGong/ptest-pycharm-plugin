package com.iselsoft.ptest.toolWindow;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.iselsoft.ptest.runConfiguration.PTestConfigurationProducer;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;

public class PTestStructureViewElement implements StructureViewTreeElement {
    private static final PTestConfigurationProducer CONFIG_PRODUCER = new PTestConfigurationProducer();

    private PTestStructureViewElement myParent;
    private PyElement myElement;

    public PTestStructureViewElement(PTestStructureViewElement parent, PyElement element) {
        myParent = parent;
        myElement = element;
    }

    protected StructureViewTreeElement createChild(PyElement element) {
        return new PTestStructureViewElement(this, element);
    }
    
    public PTestStructureViewElement getParent() {
        return myParent;
    }

    @Nullable
    @Override
    public PyElement getValue() {
        return myElement.isValid() ? myElement : null;
    }

    @Override
    public void navigate(boolean requestFocus) {
        final PyElement element = getValue();
        if (element != null) {
            myElement.navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return myElement.isValid() && myElement.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return myElement.isValid() && myElement.canNavigateToSource();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof StructureViewTreeElement) {
            final Object value = ((StructureViewTreeElement) o).getValue();
            final String name = myElement.getName();
            if (value instanceof PyElement && name != null) {
                return name.equals(((PyElement) value).getName());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        final String name = myElement.getName();
        return name != null ? name.hashCode() : 0;
    }

    @NotNull
    public StructureViewTreeElement[] getChildren() {
        final PyElement element = getValue();
        if (element == null) {
            return EMPTY_ARRAY;
        }
        final Collection<StructureViewTreeElement> children = new ArrayList<>();

        if (element instanceof PyFile) {
            for (PsiElement psiElement : element.getChildren()) {
                PyClass pTestClass = CONFIG_PRODUCER.getPTestClass(psiElement);
                StructureViewTreeElement child = createChild(pTestClass);
                if (pTestClass != null && !children.contains(child)) {
                    children.add(child);
                }
            }
        } else if (element instanceof PyClass) {
            PyClass pTestClass = CONFIG_PRODUCER.getPTestClass(element);
            pTestClass.visitMethods(pyFunction -> {
                PyFunction pTestMethod = CONFIG_PRODUCER.getPTestMethod(pyFunction);
                StructureViewTreeElement child = createChild(pTestMethod);
                if (pTestMethod != null && !children.contains(child)) {
                    children.add(child);
                }
                return true;
            }, true, null);
        }

        return children.toArray(new StructureViewTreeElement[children.size()]);
    }


    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        final PyElement element = getValue();

        return new ColoredItemPresentation() {
            @Nullable
            @Override
            public String getPresentableText() {
                if (element == null) return PyNames.UNNAMED_ELEMENT;
                
                if (element instanceof PyFile || element instanceof PyFunction) {
                    return element.getName();
                }
                ItemPresentation presentation = element.getPresentation();
                if (presentation == null) return PyNames.UNNAMED_ELEMENT;
                
                return presentation.getPresentableText();
            }

            @Nullable
            @Override
            public TextAttributesKey getTextAttributesKey() {
                return null;
            }

            @Nullable
            @Override
            public String getLocationString() {
                return null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean open) {
                return element != null ? element.getIcon(0) : null;
            }
        };
    }
}
