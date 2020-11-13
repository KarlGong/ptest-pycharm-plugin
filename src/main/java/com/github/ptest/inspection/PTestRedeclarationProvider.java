package com.github.ptest.inspection;

import com.intellij.codeInspection.InspectionToolProvider;

public class PTestRedeclarationProvider implements InspectionToolProvider {
  
  @Override
  public Class[] getInspectionClasses() {
    return new Class[] { PTestRedeclarationInspection.class};
  }
}
