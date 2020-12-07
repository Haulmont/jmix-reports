/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.reportsui.gui.template.edit;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.core.global.Security;
import com.haulmont.cuba.gui.upload.FileUploadingAPI;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.security.EntityOp;
import io.jmix.reports.ReportPrintHelper;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.CustomTemplateDefinedBy;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reportsui.gui.datasource.NotPersistenceDatasource;
import io.jmix.reportsui.gui.definition.edit.scripteditordialog.ScriptEditorDialog;
import io.jmix.reportsui.gui.report.run.ShowChartController;
import io.jmix.reportsui.gui.report.run.ShowPivotTableController;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@UiController("report_TemplateEditor")
@UiDescriptor("template-edit.xml")
public class TemplateEditor extends StandardEditor<ReportTemplate> {

    public static final String CUSTOM_DEFINE_BY = "customDefinedBy";
    public static final String CUSTOM = "custom";
    public static final String REPORT_OUTPUT_TYPE = "reportOutputType";

    @Autowired
    protected Label isCustomLabel;

    @Autowired
    protected CheckBox custom;

    @Autowired
    protected Label templateFileLabel;

    @Autowired
    protected FileStorageUploadField templateUploadField;

    @Autowired
    protected RadioButtonGroup<Boolean> isGroovyRadioButtonGroup;

    @Autowired
    protected Label<String> isGroovyLabel;

    @Autowired
    protected TextArea customDefinition;

    @Autowired
    protected LinkButton customDefinitionHelpLinkButton;

    @Autowired
    protected LinkButton fullScreenLinkButton;

    @Autowired
    protected Label customDefinitionLabel;

    @Autowired
    protected ComboBox customDefinedBy;

    @Autowired
    protected Label customDefinedByLabel;

    @Autowired
    protected CheckBox alterable;

    @Autowired
    protected Label alterableLabel;

    @Autowired
    protected ComboBox<ReportOutputType> outputType;

    @Autowired
    protected TextField outputNamePattern;

    @Autowired
    protected Label outputNamePatternLabel;

    @Autowired
    protected ChartEditFrame chartEdit;

    @Autowired
    protected PivotTableEditFrame pivotTableEdit;

    @Autowired
    protected TableEditFrame tableEdit;

    @Autowired
    protected NotPersistenceDatasource<ReportTemplate> templateDs;

    @Autowired
    protected BoxLayout descriptionEditBox;

    @Autowired
    protected BoxLayout previewBox;

    @Autowired
    protected SourceCodeEditor templateFileEditor;

    @Autowired
    protected WindowConfig windowConfig;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected Security security;

    @Autowired
    protected FileUploadingAPI fileUploading;

    @Autowired
    protected ScreenBuilders screenBuilders;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Notifications notifications;

    @Subscribe
    protected void onInit(InitEvent event) {
        outputNamePattern.setContextHelpIconClickHandler(e ->
                dialogs.createMessageDialog()
                        .withCaption(messages.getMessage("template.namePatternText"))
                        .withMessage(messages.getMessage("template.namePatternTextHelp"))
                        .withModal(false)
                        .withWidth("560px")
                        .show());

        Map<String, Boolean> groovyOptions = new HashMap<>();
        groovyOptions.put(messages.getMessage("template.freemarkerType"), Boolean.FALSE);
        groovyOptions.put(messages.getMessage("template.groovyType"), Boolean.TRUE);
        isGroovyRadioButtonGroup.setOptionsMap(groovyOptions);
    }

    @Subscribe
    protected void initNewItem(InitEntityEvent<ReportTemplate> event) {
        ReportTemplate template = event.getEntity();
        if (StringUtils.isEmpty(template.getCode())) {
            Report report = template.getReport();
            if (report != null) {
                if (report.getTemplates() == null || report.getTemplates().isEmpty())
                    template.setCode(ReportService.DEFAULT_TEMPLATE_CODE);
                else
                    template.setCode("Template_" + Integer.toString(report.getTemplates().size()));
            }
        }
    }

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        initUploadField();
        templateDs.addItemPropertyChangeListener(e -> {
            ReportTemplate reportTemplate = getEditedEntity();
            switch (e.getProperty()) {
                case REPORT_OUTPUT_TYPE: {
                    ReportOutputType prevOutputType = (ReportOutputType) e.getPrevValue();
                    ReportOutputType newOutputType = (ReportOutputType) e.getValue();
                    setupVisibility(reportTemplate.getCustom(), newOutputType);
                    if (hasHtmlCsvTemplateOutput(prevOutputType) && !hasTemplateOutput(newOutputType)) {
                        dialogs.createMessageDialog()
                                .withCaption(messages.getMessage("templateEditor.warning"))
                                .withMessage(messages.getMessage("templateEditor.clearTemplateMessage"))
                                .show();
                    }
                    break;
                }
                case CUSTOM: {
                    setupVisibility(Boolean.TRUE.equals(e.getValue()), reportTemplate.getReportOutputType());
                    break;
                }
                case CUSTOM_DEFINE_BY: {
                    boolean isGroovyScript = hasScriptCustomDefinedBy(reportTemplate.getCustomDefinedBy());
                    fullScreenLinkButton.setVisible(isGroovyScript);
                    customDefinitionHelpLinkButton.setVisible(isGroovyScript);
                    break;
                }
            }
        });
        initOutputTypeList();
    }

    protected boolean hasScriptCustomDefinedBy(CustomTemplateDefinedBy customTemplateDefinedBy) {
        return CustomTemplateDefinedBy.SCRIPT == customTemplateDefinedBy;
    }

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        ReportTemplate reportTemplate = getEditedEntity();
        initTemplateEditor(reportTemplate);
        getDescriptionEditFrames().forEach(controller -> controller.setItem(reportTemplate));
        setupVisibility(reportTemplate.getCustom(), reportTemplate.getReportOutputType());
    }

    protected Collection<DescriptionEditFrame> getDescriptionEditFrames() {
        return Arrays.asList(chartEdit, pivotTableEdit, tableEdit);
    }

    protected boolean hasTemplateOutput(ReportOutputType reportOutputType) {
        return reportOutputType != ReportOutputType.CHART
                && reportOutputType != ReportOutputType.TABLE
                && reportOutputType != ReportOutputType.PIVOT_TABLE;
    }

    protected boolean hasChartTemplateOutput(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.CHART;
    }

    protected boolean hasPdfTemplateOutput(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.PDF;
    }

    protected boolean hasHtmlCsvTemplateOutput(ReportOutputType reportOutputType) {
        return reportOutputType == ReportOutputType.CSV || reportOutputType == ReportOutputType.HTML;
    }

    protected void setupVisibility(boolean customEnabled, ReportOutputType reportOutputType) {
        boolean templateOutputVisibility = hasTemplateOutput(reportOutputType);
        boolean enabled = templateOutputVisibility && customEnabled;
        boolean groovyScriptVisibility = enabled && hasScriptCustomDefinedBy(getEditedEntity().getCustomDefinedBy());

        custom.setVisible(templateOutputVisibility);
        isCustomLabel.setVisible(templateOutputVisibility);

        customDefinedBy.setVisible(enabled);
        customDefinition.setVisible(enabled);
        customDefinedByLabel.setVisible(enabled);
        customDefinitionLabel.setVisible(enabled);

        customDefinitionHelpLinkButton.setVisible(groovyScriptVisibility);
        fullScreenLinkButton.setVisible(groovyScriptVisibility);

        customDefinedBy.setRequired(enabled);
        customDefinedBy.setRequiredMessage(messages.getMessage("templateEditor.customDefinedBy"));
        customDefinition.setRequired(enabled);
        customDefinition.setRequiredMessage(messages.getMessage("templateEditor.classRequired"));

        boolean supportAlterableForTemplate = templateOutputVisibility && !enabled;
        alterable.setVisible(supportAlterableForTemplate);
        alterableLabel.setVisible(supportAlterableForTemplate);

        templateUploadField.setVisible(templateOutputVisibility);
        templateFileLabel.setVisible(templateOutputVisibility);
        outputNamePattern.setVisible(templateOutputVisibility);
        outputNamePatternLabel.setVisible(templateOutputVisibility);

        setupTemplateTypeVisibility(templateOutputVisibility);
        visibleTemplateEditor(reportOutputType);
        setupVisibilityDescriptionEdit(enabled, reportOutputType);
    }

    protected void setupTemplateTypeVisibility(boolean visibility) {
        String extension = "";
        if (getEditedEntity().getDocumentName() != null) {
            extension = FilenameUtils.getExtension(getEditedEntity().getDocumentName()).toUpperCase();
        }
        isGroovyRadioButtonGroup.setVisible(visibility
                && ReportOutputType.HTML.equals(ReportOutputType.getTypeFromExtension(extension)));
        isGroovyLabel.setVisible(visibility
                && ReportOutputType.HTML.equals(ReportOutputType.getTypeFromExtension(extension)));
    }

    protected void setupVisibilityDescriptionEdit(boolean customEnabled, ReportOutputType reportOutputType) {
        DescriptionEditFrame applicableFrame =
                getDescriptionEditFrames().stream()
                        .filter(c -> c.isApplicable(reportOutputType))
                        .findFirst().orElse(null);
        if (applicableFrame != null) {
            descriptionEditBox.setVisible(!customEnabled);
            //todo
            //applicableFrame.setVisible(!customEnabled);
            applicableFrame.setItem(getEditedEntity());

            if (!customEnabled && applicableFrame.isSupportPreview()) {
                applicableFrame.showPreview();
            } else {
                applicableFrame.hidePreview();
            }
        }

        for (DescriptionEditFrame frame : getDescriptionEditFrames()) {
            if (applicableFrame != frame) {
                //todo
                //frame.setVisible(false);
            }
            if (applicableFrame == null) {
                frame.hidePreview();
                descriptionEditBox.setVisible(false);
            }
        }
    }

    protected void updateOutputType() {
        if (outputType.getValue() == null) {
            String extension = FilenameUtils.getExtension(templateUploadField.getFileName()).toUpperCase();
            ReportOutputType reportOutputType = ReportOutputType.getTypeFromExtension(extension);
            if (reportOutputType != null) {
                outputType.setValue(reportOutputType);
            }
        }
    }

    protected void initOutputTypeList() {
        ArrayList<ReportOutputType> outputTypes = new ArrayList<>(Arrays.asList(ReportOutputType.values()));

        if (!windowConfig.hasWindow(ShowChartController.JSON_CHART_SCREEN_ID)) {
            outputTypes.remove(ReportOutputType.CHART);
        }
        if (!windowConfig.hasWindow(ShowPivotTableController.PIVOT_TABLE_SCREEN_ID)) {
            outputTypes.remove(ReportOutputType.PIVOT_TABLE);
        }

        outputType.setOptionsList(outputTypes);
    }

    protected void initUploadField() {
        templateUploadField.addFileUploadErrorListener(e ->
                notifications.create(Notifications.NotificationType.WARNING)
                        .withCaption(messages.getMessage("templateEditor.uploadUnsuccess"))
                        .show());
        templateUploadField.addFileUploadSucceedListener(e -> {
            String fileName = templateUploadField.getFileName();
            ReportTemplate reportTemplate = getEditedEntity();
            reportTemplate.setName(fileName);

            File file = fileUploading.getFile(templateUploadField.getFileId());
            try {
                byte[] data = FileUtils.readFileToByteArray(file);
                reportTemplate.setContent(data);
            } catch (IOException ex) {
                throw new RuntimeException(
                        String.format("An error occurred while uploading file for template [%s]", getEditedEntity().getCode()), ex);
            }
            initTemplateEditor(reportTemplate);
            setupTemplateTypeVisibility(hasTemplateOutput(reportTemplate.getReportOutputType()));
            updateOutputType();

            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.getMessage("templateEditor.uploadSuccess"))
                    .show();
        });

        ReportTemplate reportTemplate = getEditedEntity();
        byte[] templateFile = reportTemplate.getContent();
        if (templateFile != null && !hasChartTemplateOutput(reportTemplate.getReportOutputType())) {
            templateUploadField.setContentProvider(() -> new ByteArrayInputStream(templateFile));
            FileDescriptor fileDescriptor = metadata.create(FileDescriptor.class);
            fileDescriptor.setName(reportTemplate.getName());
            templateUploadField.setValue(fileDescriptor);
        }

        boolean updatePermitted = security.isEntityOpPermitted(metadata.getClass(reportTemplate), EntityOp.UPDATE)
                && security.isEntityAttrUpdatePermitted(metadata.getClass(reportTemplate), "content");

        templateUploadField.setEditable(updatePermitted);
    }

    protected void initTemplateEditor(ReportTemplate reportTemplate) {
        templateFileEditor.setMode(SourceCodeEditor.Mode.HTML);
        String extension = FilenameUtils.getExtension(templateUploadField.getFileName());
        if (extension == null) {
            visibleTemplateEditor(null);
            return;
        }
        ReportOutputType outputType = ReportOutputType.getTypeFromExtension(extension.toUpperCase());
        visibleTemplateEditor(outputType);
        if (hasHtmlCsvTemplateOutput(outputType)) {
            String templateContent = new String(reportTemplate.getContent(), StandardCharsets.UTF_8);
            templateFileEditor.setValue(templateContent);
        }
        templateFileEditor.setEditable(security.isEntityOpPermitted(metadata.getClass(reportTemplate), EntityOp.UPDATE));
    }

    protected void visibleTemplateEditor(ReportOutputType outputType) {
        String extension = FilenameUtils.getExtension(templateUploadField.getFileName());
        if (extension == null) {
            templateFileEditor.setVisible(false);
            return;
        }
        templateFileEditor.setVisible(hasHtmlCsvTemplateOutput(outputType) || hasPdfTemplateOutput(outputType));
    }

    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        if (!validateTemplateFile() || !validateInputOutputFormats()) {
            event.preventCommit();
        }
        ReportTemplate reportTemplate = getEditedEntity();
        for (DescriptionEditFrame frame : getDescriptionEditFrames()) {
            if (frame.isApplicable(reportTemplate.getReportOutputType())) {
                if (!frame.applyChanges()) {
                    event.preventCommit();
                }
            }
        }

        if (!Boolean.TRUE.equals(reportTemplate.getCustom())) {
            reportTemplate.setCustomDefinition("");
        }

        String extension = FilenameUtils.getExtension(templateUploadField.getFileName());
        if (extension != null) {
            ReportOutputType outputType = ReportOutputType.getTypeFromExtension(extension.toUpperCase());
            if (hasHtmlCsvTemplateOutput(outputType)) {
                byte[] bytes = templateFileEditor.getValue() == null ?
                        new byte[0] :
                        templateFileEditor.getValue().getBytes(StandardCharsets.UTF_8);
                reportTemplate.setContent(bytes);
            }
        }
    }

    protected boolean validateInputOutputFormats() {
        ReportTemplate reportTemplate = getEditedEntity();
        String name = reportTemplate.getName();
        if (!Boolean.TRUE.equals(reportTemplate.getCustom())
                && hasTemplateOutput(reportTemplate.getReportOutputType())
                && name != null) {
            String inputType = name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : "";

            ReportOutputType outputTypeValue = outputType.getValue();
            if (!ReportPrintHelper.getInputOutputTypesMapping().containsKey(inputType) ||
                    !ReportPrintHelper.getInputOutputTypesMapping().get(inputType).contains(outputTypeValue)) {
                notifications.create(Notifications.NotificationType.TRAY)
                        .withCaption(messages.getMessage("inputOutputTypesError"))
                        .show();
                return false;
            }
        }
        return true;
    }

    protected boolean validateTemplateFile() {
        ReportTemplate template = getEditedEntity();
        if (!Boolean.TRUE.equals(template.getCustom())
                && hasTemplateOutput(template.getReportOutputType())
                && template.getContent() == null) {
            StringBuilder notification = new StringBuilder(messages.getMessage("template.uploadTemplate"));

            if (StringUtils.isEmpty(template.getCode())) {
                notification.append("\n").append(messages.getMessage("template.codeMsg"));
            }

            if (template.getOutputType() == null) {
                notification.append("\n").append(messages.getMessage("template.outputTypeMsg"));
            }

            notifications.create(Notifications.NotificationType.TRAY)
                    .withCaption(messages.getMessage("validationFail.caption"))
                    .show();

            return false;
        }
        return true;
    }

    @Subscribe("fullScreenLinkButton")
    public void showGroovyScriptEditorDialog() {
        ScriptEditorDialog editorDialog = (ScriptEditorDialog) screenBuilders.screen(this)
                .withScreenId("scriptEditorDialog")
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        "mode", SourceCodeEditor.Mode.Groovy,
                        "scriptValue", customDefinition.getValue(),
                        "helpVisible", customDefinitionHelpLinkButton.isVisible(),
                        "helpMsgKey", "templateEditor.textHelpGroovy"
                )))
                .build();
        editorDialog.addAfterCloseListener(actionId -> {
            StandardCloseAction closeAction = (StandardCloseAction) actionId.getCloseAction();
            if (Window.COMMIT_ACTION_ID.equals(closeAction.getActionId())) {
                customDefinition.setValue(editorDialog.getValue());
            }
        });
        editorDialog.show();
    }

    @Subscribe("customDefinitionHelpLinkButton")
    public void showCustomDefinitionHelp() {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage("templateEditor.titleHelpGroovy"))
                .withMessage(messages.getMessage("templateEditor.textHelpGroovy"))
                .withModal(false)
                .withWidth("700px")
                .show();
    }
}