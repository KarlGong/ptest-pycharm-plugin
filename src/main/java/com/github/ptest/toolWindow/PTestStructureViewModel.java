package com.github.ptest.toolWindow;

import com.github.ptest.element.PTestModule;
import com.intellij.ide.structureView.StructureViewModel.ElementInfoProvider;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.util.treeView.smartTree.Filter;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class PTestStructureViewModel extends StructureViewModelBase implements ElementInfoProvider {

    public PTestStructureViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor) {
        super(psiFile, editor, new PTestStructureViewElement(PTestModule.createFrom(psiFile)));
        withSorters(Sorter.ALPHA_SORTER);
        withSuitableClasses(PyFunction.class, PyClass.class); // auto scroll from source
    }

    @NotNull
    @Override
    public Filter[] getFilters() {
        return new Filter[]{new PTestConfigurationFilter()};
    }

    @Override
    public boolean isAlwaysShowsPlus(StructureViewTreeElement element) {
        return !(element.getValue() instanceof PyFunction);
    }

    @Override
    public boolean isAlwaysLeaf(StructureViewTreeElement element) {
        return element.getValue() instanceof PyFunction;
    }
}
