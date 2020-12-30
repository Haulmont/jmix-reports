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

package io.jmix.reportsui.gui.report.run;

import com.haulmont.yarg.reporting.ReportOutputDocument;
import io.jmix.core.Messages;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reportsui.gui.ReportGuiManager;
import io.jmix.ui.Fragments;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.UiComponents;
import io.jmix.ui.WindowConfig;
import io.jmix.ui.component.*;
import io.jmix.ui.screen.*;
import io.jmix.ui.theme.ThemeConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@UiController("chart_ShowChartController")
@UiDescriptor("show-chart.xml")
public class ShowChartController extends StandardLookup {
    public static final String JSON_CHART_SCREEN_ID = "chart$jsonChart";

    public static final String CHART_JSON_PARAMETER = "chartJson";
    public static final String REPORT_PARAMETER = "report";
    public static final String TEMPLATE_CODE_PARAMETER = "templateCode";
    public static final String PARAMS_PARAMETER = "reportParams";

    @Autowired
    protected GroupBoxLayout reportParamsBox;

    @Autowired
    protected GroupBoxLayout chartBox;

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected ThemeConstants themeConstants;

    @Autowired
    protected ComboBox<Report> reportLookup;

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected Button printReportBtn;

    @Autowired
    protected BoxLayout parametersFrameHolder;

    @Autowired
    protected HBoxLayout reportSelectorBox;

    @Autowired
    protected WindowConfig windowConfig;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Fragments fragments;

    protected InputParametersFrame inputParametersFrame;

    protected Report report;

    protected String templateCode;

    @Subscribe
    protected void onInit(InitEvent event) {
        //TODO get dialog options
//        getDialogOptions()
//                .setWidth(themeConstants.get("cuba.gui.report.ShowChartController.width"))
//                .setHeight(themeConstants.get("cuba.gui.report.ShowChartController.height"))
//                .setResizable(true);

        String chartJson = (String) params.get(CHART_JSON_PARAMETER);
        report = (Report) params.get(REPORT_PARAMETER);
        templateCode = (String) params.get(TEMPLATE_CODE_PARAMETER);
        @SuppressWarnings("unchecked")
        Map<String, Object> reportParameters = (Map<String, Object>) params.get(PARAMS_PARAMETER);

        if (!windowConfig.hasWindow(JSON_CHART_SCREEN_ID)) {
            showChartsNotIncluded();
            return;
        }

        if (report != null) {
            reportSelectorBox.setVisible(false);

            initFrames(chartJson, reportParameters);
        } else {
            showDiagramStubText();
        }

        reportLookup.addValueChangeListener(e -> {
            report = (Report) e.getValue();
            initFrames(null, null);
        });
    }

    protected void initFrames(String chartJson, Map<String, Object> reportParameters) {
        openChart(chartJson);
        openReportParameters(reportParameters);
    }

    private void openReportParameters(Map<String, Object> reportParameters) {
        parametersFrameHolder.removeAll();

        if (report != null) {
            Map<String, Object> params = ParamsMap.of(
                    InputParametersFrame.REPORT_PARAMETER, report,
                    InputParametersFrame.PARAMETERS_PARAMETER, reportParameters
            );

            inputParametersFrame = (InputParametersFrame) openFrame(parametersFrameHolder,
                    "report_inputParametersFrame", params);

            inputParametersFrame = fragments.create(this, InputParametersFrame.class);
            inputParametersFrame.set
            parametersFrameHolder.add(inputParametersFrame.getFragment());
            reportParamsBox.setVisible(true);
        } else {
            reportParamsBox.setVisible(false);
        }
    }

    protected void openChart(String chartJson) {
        chartBox.removeAll();
        if (chartJson != null) {
            openFrame(chartBox, JSON_CHART_SCREEN_ID, ParamsMap.of(CHART_JSON_PARAMETER, chartJson));
        }

        showDiagramStubText();
    }

    protected void showDiagramStubText() {
        if (chartBox.getOwnComponents().isEmpty()) {
            Label label = uiComponents.create(Label.class);
            label.setValue(messages.getMessage("showChart.caption"));
            label.setAlignment(Alignment.MIDDLE_CENTER);
            label.setStyleName("h1");
            chartBox.add(label);
        }
    }

    protected void showChartsNotIncluded() {
        reportLookup.setEditable(false);
        chartBox.removeAll();
        Label label = uiComponents.create(Label.class);
        label.setValue(messages.getMessage("showChart.noChartComponent"));
        label.setAlignment(Alignment.MIDDLE_CENTER);
        label.setStyleName("h1");
        chartBox.add(label);
    }

    @Subscribe("printReportBtn")
    public void printReport() {
        if (inputParametersFrame != null && inputParametersFrame.getReport() != null) {
            if (validateAll()) {
                Map<String, Object> parameters = inputParametersFrame.collectParameters();
                Report report = inputParametersFrame.getReport();

                if (templateCode == null) {
                    templateCode = report.getTemplates().stream()
                            .filter(template -> template.getReportOutputType() == ReportOutputType.CHART)
                            .findFirst()
                            .map(ReportTemplate::getCode).orElse(null);
                }

                ReportOutputDocument reportResult = reportGuiManager.getReportResult(report, parameters, templateCode);
                openChart(new String(reportResult.getContent(), StandardCharsets.UTF_8));
            }
        }
    }
}