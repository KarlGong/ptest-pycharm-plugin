package com.iselsoft.ptest.toolWindow;

import com.intellij.ide.structureView.StructureView;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class PTestViewToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Editor selectedEditor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(selectedEditor.getDocument());
        FileEditor fileEditor = FileEditorManager.getInstance(project).getSelectedEditor(psiFile.getVirtualFile());
        StructureView structureView = new PTestStructureViewFactory().getStructureViewBuilder(psiFile).createStructureView(fileEditor, project);

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(structureView.getComponent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
