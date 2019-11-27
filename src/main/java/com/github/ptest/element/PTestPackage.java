package com.github.ptest.element;

import com.github.ptest.PTestUtil;
import com.github.ptest.runConfiguration.PTestRunConfiguration;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;

public class PTestPackage extends PTestElement<PsiDirectory> {

    public PTestPackage(PsiDirectory directory) {
        super(directory);
    }

    @Override
    public boolean setupConfiguration(PTestRunConfiguration configuration) {
        try {
            configuration.setValueForEmptyWorkingDirectory();
            configuration.setRunTest(true);
            String testTarget = PTestUtil.findShortestImportableName(getValue());
            configuration.setTestTargets(testTarget);
            configuration.setSuggestedName("ptests in " + testTarget);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static PTestPackage createFrom(PsiElement element) {
        if (element instanceof PsiDirectory) {
            PsiDirectory pyDirectory = (PsiDirectory) element;
            return PTestUtil.hasAnyPyFile(pyDirectory) ? new PTestPackage(pyDirectory) : null;
        }
        return null;
    }
}
