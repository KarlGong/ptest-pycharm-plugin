package com.github.ptest.element;

import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;

import java.nio.file.Paths;

public class PTestXML extends PTestElement<PsiFile> {
    
    public PTestXML(PsiFile xmlFile) {
        super(xmlFile);
    }

    @Override
    public boolean setupConfiguration(PTestRunConfiguration configuration) {
        try {
            configuration.setValueForEmptyWorkingDirectory();
            configuration.setRunFailed(true);
            String xmlPath = getValue().getVirtualFile().getCanonicalPath();
            String xmlRelativePath = Paths.get(configuration.getWorkingDirectory()).relativize(Paths.get(xmlPath)).toString();
            configuration.setXunitXML(xmlRelativePath);
            configuration.setSuggestedName("ptests in " + xmlRelativePath);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static PTestXML createFrom(PsiElement element) {
        if (element instanceof PsiFile) {
            PsiFile xmlFile = (PsiFile) element;
            VirtualFile file = xmlFile.getVirtualFile();
            return file.getExtension() != null && file.getExtension().equalsIgnoreCase("xml") ? new PTestXML(xmlFile) : null;
        }
        return null;
    }
}
