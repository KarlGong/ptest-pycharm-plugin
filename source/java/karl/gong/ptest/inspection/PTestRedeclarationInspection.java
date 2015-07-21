package karl.gong.ptest.inspection;

import com.intellij.codeInsight.controlflow.ControlFlowUtil;
import com.intellij.codeInsight.controlflow.Instruction;
import com.intellij.codeInsight.daemon.GroupNames;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Function;
import com.jetbrains.python.PyBundle;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.codeInsight.controlflow.ControlFlowCache;
import com.jetbrains.python.codeInsight.controlflow.ReadWriteInstruction;
import com.jetbrains.python.codeInsight.controlflow.ScopeOwner;
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil;
import com.jetbrains.python.inspections.PyInspection;
import com.jetbrains.python.inspections.PyInspectionVisitor;
import com.jetbrains.python.inspections.quickfix.PyRenameElementQuickFix;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PTestRedeclarationInspection extends PyInspection {
    @Nls
    @NotNull
    public String getDisplayName() {
        return "Redeclared ptest names without usage";
    }

    @NotNull
    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    @NotNull
    public String getShortName() {
        return "RedeclaredPTestNames";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                          boolean isOnTheFly,
                                          @NotNull LocalInspectionToolSession session) {
        return new Visitor(holder, session);
    }

    private static class Visitor extends PyInspectionVisitor {
        public Visitor(@Nullable ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
            super(holder, session);
        }

        @Override
        public void visitPyFunction(final PyFunction node) {
            if (isDecorated(node)) {
                processElement(node);
            }
        }

        @Override
        public void visitPyClass(final PyClass node) {
            if (isDecorated(node)) {
                processElement(node);
            }
        }

        private static boolean isConditional(@NotNull PsiElement node) {
            return PsiTreeUtil.getParentOfType(node, PyIfStatement.class, PyConditionalExpression.class, PyTryExceptStatement.class) != null;
        }

        private static boolean isDecorated(@NotNull PyDecoratable node) {
            boolean isDecorated = false;
            final PyDecoratorList decoratorList = node.getDecoratorList();
            if (decoratorList != null) {
                final PyDecorator[] decorators = decoratorList.getDecorators();
                if (decorators.length > 0) {
                    isDecorated = true;
                }
            }
            return isDecorated;
        }

        private void processElement(@NotNull final PsiNameIdentifierOwner element) {
            if (isConditional(element)) {
                return;
            }
            final String name = element.getName();
            final ScopeOwner owner = ScopeUtil.getScopeOwner(element);
            if (owner != null && name != null) {
                final Instruction[] instructions = ControlFlowCache.getControlFlow(owner).getInstructions();
                PsiElement elementInControlFlow = element;
                if (element instanceof PyTargetExpression) {
                    final PyImportStatement importStatement = PsiTreeUtil.getParentOfType(element, PyImportStatement.class);
                    if (importStatement != null) {
                        elementInControlFlow = importStatement;
                    }
                }
                final int startInstruction = ControlFlowUtil.findInstructionNumberByElement(instructions, elementInControlFlow);
                if (startInstruction < 0) {
                    return;
                }
                final Ref<PsiElement> readElementRef = Ref.create(null);
                final Ref<PsiElement> writeElementRef = Ref.create(null);
                ControlFlowUtil.iteratePrev(startInstruction, instructions, new Function<Instruction, ControlFlowUtil.Operation>() {
                    @Override
                    public ControlFlowUtil.Operation fun(Instruction instruction) {
                        if (instruction instanceof ReadWriteInstruction && instruction.num() != startInstruction) {
                            final ReadWriteInstruction rwInstruction = (ReadWriteInstruction) instruction;
                            if (name.equals(rwInstruction.getName())) {
                                final PsiElement originalElement = rwInstruction.getElement();
                                if (originalElement != null) {
                                    if (rwInstruction.getAccess().isReadAccess()) {
                                        readElementRef.set(originalElement);
                                    }
                                    if (rwInstruction.getAccess().isWriteAccess()) {
                                        if (originalElement != element) {
                                            writeElementRef.set(originalElement);
                                        }
                                    }
                                }
                                return ControlFlowUtil.Operation.CONTINUE;
                            }
                        }
                        return ControlFlowUtil.Operation.NEXT;
                    }
                });
                final PsiElement writeElement = writeElementRef.get();
                if (writeElement != null && readElementRef.get() == null) {
                    final List<LocalQuickFix> quickFixes = new ArrayList<LocalQuickFix>();
                    if (suggestRename(element, writeElement)) {
                        quickFixes.add(new PyRenameElementQuickFix());
                    }
                    final PsiElement identifier = element.getNameIdentifier();
                    registerProblem(identifier != null ? identifier : element,
                            String.format("Redeclared ptest ''%s'' defined above without usage", name),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                            null,
                            quickFixes.toArray(new LocalQuickFix[quickFixes.size()]));
                }
            }
        }

        private static boolean suggestRename(@NotNull PsiNameIdentifierOwner element, @NotNull PsiElement originalElement) {
            // Target expressions in the same scope are treated as the same variable
            if ((element instanceof PyTargetExpression) && originalElement instanceof PyTargetExpression) {
                return false;
            }
            // Renaming an __init__ method results in renaming its class
            else if (element instanceof PyFunction && PyNames.INIT.equals(element.getName()) &&
                    ((PyFunction) element).getContainingClass() != null) {
                return false;
            }
            return true;
        }
    }
}