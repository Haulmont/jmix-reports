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

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.reports.ParameterClassResolver;
import io.jmix.reports.ReportPrintHelper;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reportsui.gui.ReportGuiManager;
import io.jmix.reportsui.gui.report.validators.ReportCollectionValidator;
import io.jmix.reportsui.gui.report.validators.ReportParamFieldValidator;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.screen.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@UiController("report_InputParameters.fragment")
@UiDescriptor("input-parameters-frame.xml")
public class InputParametersFragment extends ScreenFragment {
    public static final String REPORT_PARAMETER = "report";
    public static final String PARAMETERS_PARAMETER = "parameters";

    protected Report report;
    protected Map<String, Object> parameters;
    protected boolean bulkPrint;
    protected ReportInputParameter inputParameter;

    @Autowired
    protected ComboBox<ReportTemplate> templateField;

    @Autowired
    protected ComboBox<ReportOutputType> outputTypeField;

    @Autowired
    protected Label outputTypeLbl;

    @Autowired
    protected Label templateLbl;

    @Autowired
    protected GridLayout parametersGrid;

    @Autowired
    protected CollectionContainer<ReportTemplate> templateReportsDc;

    @Autowired
    protected CollectionLoader<ReportTemplate> templateReportsDl;

    @Autowired
    protected ReportService reportService;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected ParameterClassResolver parameterClassResolver;

    protected HashMap<String, Field> parameterComponents = new HashMap<>();

    @Autowired
    protected ParameterFieldCreator parameterFieldCreator;

    public void setReport(Report report) {
        this.report = report;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public void setBulkPrint(boolean bulkPrint) {
        this.bulkPrint = bulkPrint;
    }

    public void setInputParameter(ReportInputParameter inputParameter) {
        this.inputParameter = inputParameter;
    }

    @Subscribe
    public void onInit(InitEvent event) {
        ScreenOptions options = event.getOptions();

        if(options instanceof MapScreenOptions) {
            MapScreenOptions mapScreenOptions = (MapScreenOptions) options;

            report = (Report) mapScreenOptions.getParams().get(REPORT_PARAMETER);
            parameters = (Map<String, Object>) mapScreenOptions.getParams().get(PARAMETERS_PARAMETER);
//            bulkPrint = BooleanUtils.isTrue((Boolean) mapScreenOptions.getParams().get(BULK_PRINT));
//            inputParameter = (ReportInputParameter) mapScreenOptions.getParams().get(INPUT_PARAMETER);
        }

        if (report != null && !report.getIsTmp()) {
            report = dataManager.load(Id.of(report))
                    .fetchPlan(ReportService.MAIN_VIEW_NAME)
                    .one();
        }
        if (parameters == null) {
            parameters = Collections.emptyMap();
        }
        if (report != null) {
            if (CollectionUtils.isNotEmpty(report.getInputParameters())) {
                parametersGrid.setRows(report.getInputParameters().size() + 2);
                int currentGridRow = 2;
                for (ReportInputParameter parameter : report.getInputParameters()) {
                    if (bulkPrint && Objects.equals(inputParameter, parameter)) {
                        continue;
                    }
                    createComponent(parameter, currentGridRow, BooleanUtils.isNotTrue(parameter.getHidden()));
                    currentGridRow++;
                }
            }
            if (report.getTemplates() != null && report.getTemplates().size() > 1) {
                if (!report.getIsTmp()) {
                    templateReportsDl.setParameter("reportId", report.getId());
                    templateReportsDl.load();
                }
            }
        }
    }

    public Map<String, Object> collectParameters() {
        Map<String, Object> parameters = new HashMap<>();
        for (String paramName : parameterComponents.keySet()) {
            Field parameterField = parameterComponents.get(paramName);
            Object value = parameterField.getValue();
            parameters.put(paramName, value);
        }
        return parameters;
    }

    protected void createComponent(ReportInputParameter parameter, int currentGridRow, boolean visible) {
        Field field = parameterFieldCreator.createField(parameter);
        field.setWidth("400px");

        Object value = parameters.get(parameter.getAlias());

        if (value == null && parameter.getDefaultValue() != null) {
            Class parameterClass = parameterClassResolver.resolveClass(parameter);
            if (parameterClass != null) {
                value = reportService.convertFromString(parameterClass, parameter.getDefaultValue());
            }
        }

        if (!(field instanceof TagPicker)) {
            field.setValue(value);
        } else {
//            CollectionDatasource datasource = (CollectionDatasource) field.getDatasource();
//            if (value instanceof Collection) {
//                Collection collection = (Collection) value;
//                for (Object selected : collection) {
//                    datasource.includeItem((Entity) selected);
//                }
//            }
        }

        if (BooleanUtils.isTrue(parameter.getValidationOn())) {
            field.addValidator(new ReportParamFieldValidator(parameter));
        }

        if (BooleanUtils.isTrue(field.isRequired())) {
            field.addValidator(new ReportCollectionValidator(field));
        }

        Label<String> label = parameterFieldCreator.createLabel(parameter, field);
        label.setStyleName("c-report-parameter-caption");

        if (currentGridRow == 0) {
            //TODO request focus
//            field.requestFocus();
        }

        label.setVisible(visible);
        field.setVisible(visible);

        parameterComponents.put(parameter.getAlias(), field);
        parametersGrid.add(label, 0, currentGridRow);
        parametersGrid.add(field, 1, currentGridRow);
    }

    public void initTemplateAndOutputSelect() {
        if (report != null) {
            if (report.getTemplates() != null && report.getTemplates().size() > 1) {
                templateField.setValue(report.getDefaultTemplate());
                setTemplateVisible(true);
            }
            templateField.addValueChangeListener(e -> updateOutputTypes());
            updateOutputTypes();
        }
    }

    protected void updateOutputTypes() {
        if (!reportGuiManager.containsAlterableTemplate(report)) {
            setOutputTypeVisible(false);
            return;
        }

        ReportTemplate template;
        if (report.getTemplates() != null && report.getTemplates().size() > 1) {
            template = templateField.getValue();
        } else {
            template = report.getDefaultTemplate();
        }

        if (template != null && reportGuiManager.supportAlterableForTemplate(template)) {
            List<ReportOutputType> outputTypes = ReportPrintHelper.getInputOutputTypesMapping().get(template.getExt());
            if (outputTypes != null && !outputTypes.isEmpty()) {
                outputTypeField.setOptionsList(outputTypes);
                if (outputTypeField.getValue() == null) {
                    outputTypeField.setValue(template.getReportOutputType());
                }
                setOutputTypeVisible(true);
            } else {
                outputTypeField.setValue(null);
                setOutputTypeVisible(false);
            }
        } else {
            outputTypeField.setValue(null);
            setOutputTypeVisible(false);
        }
    }

    protected void setOutputTypeVisible(boolean visible) {
        outputTypeLbl.setVisible(visible);
        outputTypeField.setVisible(visible);
    }

    protected void setTemplateVisible(boolean visible) {
        templateLbl.setVisible(visible);
        templateField.setVisible(visible);
    }

    public Report getReport() {
        return report;
    }

    public ReportTemplate getReportTemplate() {
        return templateField.getValue();
    }

    public ReportOutputType getOutputType() {
        return outputTypeField.getValue();
    }
}