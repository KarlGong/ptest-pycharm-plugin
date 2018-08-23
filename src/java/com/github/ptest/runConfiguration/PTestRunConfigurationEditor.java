package com.github.ptest.runConfiguration;

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
    
    private JCheckBox propertyFileCheckBox;
    private TextFieldWithBrowseButton propertyFileTextField;
    
    private JCheckBox propertiesCheckBox;
    private RawCommandLineEditor propertiesTextField;
    
    private JCheckBox verboseCheckBox;
    private JCheckBox disableScreenshotCheckBox;

    private final Project project;
    private JComponent anchor;

    public PTestRunConfigurationEditor(final Project project, PTestRunConfiguration configuration) {
        this.project = project;
        commonOptionsForm = PyCommonOptionsFormFactory.getInstance().createForm(configuration.getCommonOptionsFormData());
        commonOptionsPlaceholder.add(commonOptionsForm.getMainPanel());

        final FileChooserDescriptor xmlChooserDescriptor = FileChooserDescriptorFactory
                .createSingleFileDescriptor("xml");
        xmlChooserDescriptor.setTitle("Select xunit XML");
        xunitXMLTextField.addBrowseFolderListener("Select xunit XML", "Select xunit xml to run with failed/skipped tests", project, xmlChooserDescriptor);

        final FileChooserDescriptor propertyFileChooserDescriptor = FileChooserDescriptorFactory
                .createSingleFileDescriptor("ini");
        propertyFileChooserDescriptor.setTitle("Select Property File");
        propertyFileTextField.addBrowseFolderListener("Select Property File", "Select .ini Property File to be used by PTest", project, propertyFileChooserDescriptor);


        runTestRadioButton.addActionListener(e -> {
            testTargetsTextField.setEnabled(runTestRadioButton.isSelected());
            xunitXMLTextField.setEnabled(!runTestRadioButton.isSelected());
        });
        
        runFailedRadioButton.addActionListener(e -> {
            xunitXMLTextField.setEnabled(runFailedRadioButton.isSelected());
            testTargetsTextField.setEnabled(!runFailedRadioButton.isSelected());
        });

        optionsCheckBox.addActionListener(e -> optionsTextField.setEnabled(optionsCheckBox.isSelected()));
        propertyFileCheckBox.addActionListener(e -> propertyFileTextField.setEnabled(propertyFileCheckBox.isSelected()));
        propertiesCheckBox.addActionListener(e -> propertiesTextField.setEnabled(propertiesCheckBox.isSelected()));
        
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
        optionsTextField.setText(config.getOptiones());
        
        propertyFileCheckBox.setSelected(config.isUsePropertyFile());
        propertyFileTextField.setEnabled(config.isUsePropertyFile());
        propertyFileTextField.setText(config.getPropertyFile());
        
        propertiesCheckBox.setSelected(config.isUseProperties());
        propertiesTextField.setEnabled(config.isUseProperties());
        propertiesTextField.setText(config.getProperties());
        
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
        
        config.setUsePropertyFile(propertyFileCheckBox.isSelected());
        config.setPropertyFile(propertyFileTextField.getText().trim());
        
        config.setUseProperties(propertiesCheckBox.isSelected());
        config.setProperties(propertiesTextField.getText().trim());
        
        config.setVerbose(verboseCheckBox.isSelected());
        config.setDisableScreenshot(disableScreenshotCheckBox.isSelected());
    }

    @NotNull
    @Override
    protected JComponent createEditor() {
        return rootPanel;
    }
}
