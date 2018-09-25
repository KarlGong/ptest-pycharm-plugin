package com.github.ptest.toolWindow;

import com.github.ptest.element.PTestModule;
import com.intellij.ide.structureView.StructureViewModelBase;
import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PTestStructureViewModel extends StructureViewModelBase {
    public PTestStructureViewModel(@NotNull PsiFile psiFile, @Nullable Editor editor) {
        super(psiFile, editor, new PTestStructureViewElement(null, PTestModule.createFrom(psiFile)));
        withSorters(Sorter.ALPHA_SORTER);
        withSuitableClasses(PyFunction.class, PyClass.class); // auto scroll from source
    }
}
