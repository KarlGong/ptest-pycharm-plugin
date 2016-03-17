package com.iselsoft.ptest.runconfiguration;

import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.PanelWithAnchor;
import com.intellij.ui.RawCommandLineEditor;
import com.jetbrains.python.run.AbstractPyCommonOptionsForm;
import com.jetbrains.python.run.AbstractPythonRunConfiguration;
import com.jetbrains.python.run.PyCommonOptionsFormFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PTestRunConfigurationEditor extends SettingsEditor<PTestRunConfiguration> implements PanelWithAnchor {
    private JPanel rootPanel;
    private JPanel mainPanel;
    private JPanel commonOptionsPlaceholder;
    private AbstractPyCommonOptionsForm commonOptionsForm;
    
    private JRadioButton runTestRadioButton;
    private JTextField testTargetsTextField;

    private JRadioButton runFailedRadioButton;
    private TextFieldWithBrowseButton xunitXMLTextField;

    private JCheckBox optionsCheckBox;
    private RawCommandLineEditor optionsTextField;

    private JCheckBox variablesCheckBox;
    private RawCommandLineEditor variablesTextField;
    private JCheckBox verboseCheckBox;
    private JCheckBox disableScreenshotCheckBox;

    private final Project project;
    private JComponent anchor;

    public PTestRunConfigurationEditor(final Project project, PTestRunConfiguration configuration) {
        this.project = project;
        commonOptionsForm = PyCommonOptionsFormFactory.getInstance().createForm(configuration.getCommonOptionsFormData());
        commonOptionsPlaceholder.add(commonOptionsForm.getMainPanel());

        final FileChooserDescriptor fileChooserDescriptor = FileChooserDescriptorFactory
                .createSingleFileDescriptor("xml");
        fileChooserDescriptor.setTitle("Select XML Path");
        xunitXMLTextField.addBrowseFolderListener("Select XML Path", "Select xunit xml to run with failed/skipped tests", project, fileChooserDescriptor);

        runTestRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                testTargetsTextField.setEnabled(runTestRadioButton.isSelected());
                xunitXMLTextField.setEnabled(!runTestRadioButton.isSelected());
            }
        });
        
        runFailedRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                xunitXMLTextField.setEnabled(runFailedRadioButton.isSelected());
                testTargetsTextField.setEnabled(!runFailedRadioButton.isSelected());
            }
        });

        optionsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                optionsTextField.setEnabled(optionsCheckBox.isSelected());
            }
        });
        variablesCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                variablesTextField.setEnabled(variablesCheckBox.isSelected());
            }
        });
        
    }
    
    @Override
    public JComponent getAnchor() {
        return anchor;
    }

    @Override
    public void setAnchor(JComponent anchor) {
        this.anchor = anchor;
        commonOptionsForm.setAnchor(anchor);
    }

    @Override
    protected void resetEditorFrom(PTestRunConfiguration config) {
        AbstractPythonRunConfiguration.copyParams(config, commonOptionsForm);
        runTestRadioButton.setSelected(config.isRunTest());
        testTargetsTextField.setEnabled(config.isRunTest());
        testTargetsTextField.setText(config.getTestTargets());
        
        runFailedRadioButton.setSelected(config.isRunFailed());
        xunitXMLTextField.setEnabled(config.isRunFailed());
        xunitXMLTextField.setText(config.getXunitXML());
        
        optionsCheckBox.setSelected(config.isUseOptions());
        optionsTextField.setEnabled(config.isUseOptions());
        optionsTextField.setText(config.getOptions());
        
        variablesCheckBox.setSelected(config.isUseVariables());
        variablesTextField.setEnabled(config.isUseVariables());
        variablesTextField.setText(config.getVariables());
        
        verboseCheckBox.setSelected(config.isVerbose());
        disableScreenshotCheckBox.setSelected(config.isDisableScreenshot());
    }

    @Override
    protected void applyEditorTo(PTestRunConfiguration config) throws ConfigurationException {
        AbstractPythonRunConfiguration.copyParams(commonOptionsForm, config);
        config.setRunTest(runTestRadioButton.isSelected());
        config.setTestTargets(testTargetsTextField.getText().trim());
        
        config.setRunFailed(runFailedRadioButton.isSelected());
        config.setXunitXML(xunitXMLTextField.getText().trim());
        
        config.setUseOptions(optionsCheckBox.isSelected());
        config.setOptions(optionsTextField.getText().trim());
        
        config.setUseVariables(variablesCheckBox.isSelected());
        config.setVariables(variablesTextField.getText().trim());
        
        config.setVerbose(verboseCheckBox.isSelected());
        config.setDisableScreenshot(disableScreenshotCheckBox.isSelected());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return rootPanel;
    }
}
