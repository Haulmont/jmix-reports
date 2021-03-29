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

package io.jmix.reportsui.screen.report.wizard;

import io.jmix.core.CoreProperties;
import io.jmix.core.Messages;
import io.jmix.reportsui.screen.report.wizard.step.StepFragment;
import io.jmix.ui.Dialogs;
import io.jmix.ui.Fragments;
import io.jmix.ui.Notifications;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.HasContextHelp;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.screen.Install;
import io.jmix.ui.screen.Subscribe;
import io.jmix.ui.screen.UiController;
import io.jmix.ui.screen.UiDescriptor;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("report_Save.fragment")
@UiDescriptor("save-fragment.xml")
public class SaveStepFragment extends StepFragment {

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected CoreProperties coreProperties;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Dialogs dialogs;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Fragments fragments;

    @Autowired
    protected Downloader downloader;

    @Subscribe
    public void onInit(InitEvent event) {

    }

    @Install(to = "outputFileFormat", subject = "contextHelpIconClickHandler")
    private void outputFileFormatContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage("template.namePatternText"))
                .withMessage(messages.getMessage("template.namePatternTextHelp"))
                .withModal(false)
                .withWidth("560px")
                .show();
    }


    public SaveStepFragment(ReportWizardCreator wizard) {
        //super(wizard, "", "saveStep");
        //beforeShowFrameHandler = new BeforeShowSaveStepFrameHandler();

        //beforeHideFrameHandler = new BeforeHideSaveStepFrameHandler();
    }

    @Override
    public boolean isLast() {
        return true;
    }

    @Override
    public boolean isFirst() {
        return false;
    }

//    protected class BeforeShowSaveStepFrameHandler implements BeforeShowStepFrameHandler {
//        @Override
//        public void beforeShowFrame() {
//            initSaveAction();
//            initDownloadAction();
//
//            if (StringUtils.isEmpty(wizard.outputFileName.getValue())) {
//                Object value = wizard.templateFileFormat.getValue();
//                wizard.outputFileName.setValue(wizard.generateOutputFileName(value.toString().toLowerCase()));
//            }
//            wizard.setCorrectReportOutputType();
//
//            initChartPreview();
//        }

//        protected void initChartPreview() {
//            if (wizard.outputFileFormat.getValue() == ReportOutputType.CHART) {
//                wizard.chartPreviewBox.setVisible(true);
//                wizard.diagramTypeLabel.setVisible(true);
//                wizard.diagramType.setVisible(true);
//
//                showChart();
//
////TODO dialog options
////                wizard.getDialogOptions()
////                        .setHeight(wizard.wizardHeight + 400).setHeightUnit(SizeUnit.PIXELS)
////                        .center();
//
//                wizard.diagramType.setRequired(true);
//                wizard.diagramType.setOptionsList(Arrays.asList(ChartType.values()));
//                wizard.diagramType.setValue(ChartType.SERIAL);
//
//                wizard.diagramType.addValueChangeListener(e -> {
//                    wizard.getItem().setChartType((ChartType) e.getValue());
//                    wizard.chartPreviewBox.removeAll();
//                    showChart();
//                });
//            } else {
//                wizard.chartPreviewBox.setVisible(false);
//                wizard.diagramTypeLabel.setVisible(false);
//                wizard.diagramType.setVisible(false);
//            }
//        }

//        protected void initDownloadAction() {
//            wizard.downloadTemplateFile.setCaption(wizard.generateTemplateFileName(wizard.templateFileFormat.getValue().toString().toLowerCase()));
//            wizard.downloadTemplateFile.setAction(new AbstractAction("generateNewTemplateAndGet") {
//                @Override
//                public void actionPerform(Component component) {
//                    byte[] newTemplate = null;
//                    try {
//                        wizard.getItem().setName(wizard.reportName.getValue().toString());
//                        newTemplate = wizard.reportWizardService.generateTemplate(wizard.getItem(), wizard.templateFileFormat.getValue());
//                        downloader.download(new ByteArrayDataProvider(newTemplate, uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir()),
//                                wizard.downloadTemplateFile.getCaption(), DownloadFormat.getByExtension(wizard.templateFileFormat.getValue().toString().toLowerCase()));
//                    } catch (TemplateGenerationException e) {
//                        notifications.create(Notifications.NotificationType.WARNING)
//                                .withCaption(messages.getMessage("templateGenerationException"))
//                                .show();
//                    }
//                    if (newTemplate != null) {
//                        wizard.lastGeneratedTemplate = newTemplate;
//                    }
//                }
//            });
//        }

//        protected void initSaveAction() {
//            wizard.saveBtn.setVisible(true);
//            wizard.saveBtn.setAction(new AbstractAction("saveReport") {
//                @Override
//                public void actionPerform(Component component) {
//                    try {
//                        //wizard.outputFileName.validate();
//                    } catch (ValidationException e) {
//                        notifications.create(Notifications.NotificationType.TRAY)
//                                .withCaption(messages.getMessage("validationFail.caption"))
//                                .withDescription(e.getMessage())
//                                .show();
//                        return;
//                    }
//                    if (wizard.getItem().getReportRegions().isEmpty()) {
//                        dialogs.createOptionDialog()
//                                .withCaption(messages.getMessage("dialogs.Confirmation"))
//                                .withMessage(messages.getMessage("confirmSaveWithoutRegions"))
//                                .withActions(
//                                        new DialogAction(DialogAction.Type.OK).withHandler(handle ->
//                                                convertToReportAndForceCloseWizard()
//                                        ),
//                                        new DialogAction(DialogAction.Type.NO)
//                                ).show();
//                    } else {
//                        convertToReportAndForceCloseWizard();
//                    }
//                }
//
//                private void convertToReportAndForceCloseWizard() {
//                    Report r = wizard.buildReport(false);
//                    //todo
////                    if (r != null) {
////                        wizard.close(Window.COMMIT_ACTION_ID); //true is ok cause it is a save btn
////                    }
//                }
//            });
//        }
//
//        protected void showChart() {
//            byte[] content = wizard.buildReport(true).getDefaultTemplate().getContent();
//            String chartDescriptionJson = new String(content, StandardCharsets.UTF_8);
//            AbstractChartDescription chartDescription = AbstractChartDescription.fromJsonString(chartDescriptionJson);
//            RandomChartDataGenerator randomChartDataGenerator = new RandomChartDataGenerator();
//            List<Map<String, Object>> randomChartData = randomChartDataGenerator.generateRandomChartData(chartDescription);
//            ChartToJsonConverter chartToJsonConverter = new ChartToJsonConverter();
//            String chartJson = null;
//            if (chartDescription instanceof PieChartDescription) {
//                chartJson = chartToJsonConverter.convertPieChart((PieChartDescription) chartDescription, randomChartData);
//            } else if (chartDescription instanceof SerialChartDescription) {
//                chartJson = chartToJsonConverter.convertSerialChart((SerialChartDescription) chartDescription, randomChartData);
//            }
//
//            //todo
////            wizard.openFrame(wizard.chartPreviewBox, ShowChartController.JSON_CHART_SCREEN_ID,
////                    ParamsMap.of(ShowChartController.CHART_JSON_PARAMETER, chartJson));
//        }
//    }

    protected class BeforeHideSaveStepFrameHandler implements BeforeHideStepFrameHandler {
        @Override
        public void beforeHideFrame() {
            wizard.saveBtn.setVisible(false);
        }
    }
}
