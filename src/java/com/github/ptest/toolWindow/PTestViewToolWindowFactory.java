package com.github.ptest.toolWindow;

import com.github.ptest.element.PTestModule;
import com.intellij.ide.structureView.StructureView;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBPanelWithEmptyText;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class PTestViewToolWindowFactory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        updateToolWindow(project, toolWindow);
        project.getMessageBus().connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) { }

            @Override
            public void fileClosed(@NotNull FileEditorManager source, @NotNull VirtualFile file) { }

            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                updateToolWindow(project, toolWindow);
            }
        });
    }

    private void updateToolWindow(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ContentManager contentManager = toolWindow.getContentManager();
        contentManager.removeAllContents(true);

        FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
        Editor selectedEditor = fileEditorManager.getSelectedTextEditor();
        if (selectedEditor != null) {
            PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(selectedEditor.getDocument());
            if (psiFile != null && PTestModule.createFrom(psiFile) != null) {
                FileEditor fileEditor = fileEditorManager.getSelectedEditor(psiFile.getVirtualFile());
                StructureView structureView = new PTestStructureViewFactory().getStructureViewBuilder(psiFile).createStructureView(fileEditor, project);

                Content content = ContentFactory.SERVICE.getInstance().createContent(structureView.getComponent(), "", false);
                contentManager.addContent(content);
                return;
            }
        }
        JBPanelWithEmptyText panel = new JBPanelWithEmptyText() {
            @Override
            public Color getBackground() {
                return UIUtil.getTreeBackground();
            }
        };
        panel.getEmptyText().setText("Not available");
        Content content = ContentFactory.SERVICE.getInstance().createContent(panel, "", false);
        contentManager.addContent(content);
    }
}
