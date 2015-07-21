package karl.gong.ptest.inspection;

import com.intellij.codeInspection.InspectionToolProvider;

public class PTestRedeclarationProvider implements InspectionToolProvider {
  public Class[] getInspectionClasses() {
    return new Class[] { PTestRedeclarationInspection.class};
  }
}
