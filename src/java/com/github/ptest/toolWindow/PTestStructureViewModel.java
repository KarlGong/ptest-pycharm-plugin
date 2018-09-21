package com.github.ptest.toolWindow;

import com.github.ptest.element.PTestElement;
import com.github.ptest.element.PTestModule;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PTestStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider, StructureViewModel.ExpandInfoProvider {
    public PTestStructureViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor) {
        this(psiFile, editor, new PTestStructureViewElement(null, PTestModule.createFrom(psiFile)));
        withSorters(Sorter.ALPHA_SORTER);
    }

    public PTestStructureViewModel(@NotNull PsiFile file, @Nullable Editor editor, @NotNull StructureViewTreeElement element) {
        super(file, editor, element);
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        PTestElement value = ((PTestStructureViewElement) element).getElement();
        return !value.isLeaf();
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        PTestElement value = ((PTestStructureViewElement) element).getElement();
        return value.isLeaf();
    }

    @Override
    public boolean shouldEnterElement(Object element) {
        PTestElement e = (PTestElement) element;
        return !e.isLeaf();
    }

    @NotNull
    @Override
    public Filter[] getFilters() {
        return super.getFilters(); // return empty array
    }

    @Override
    public boolean isAutoExpand(@NotNull StructureViewTreeElement element) {
        PTestElement value = ((PTestStructureViewElement) element).getElement();
        return !value.isLeaf();
    }

    @Override
    public boolean isSmartExpand() {
        return false;
    }
}
