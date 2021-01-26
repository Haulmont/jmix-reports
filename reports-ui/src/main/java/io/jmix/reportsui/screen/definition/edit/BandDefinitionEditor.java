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
package io.jmix.reportsui.screen.definition.edit;

import io.jmix.core.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.app.service.ReportsWizard;
import io.jmix.reports.entity.*;
import io.jmix.reports.util.DataSetFactory;
import io.jmix.reportsui.action.list.EditViewAction;
import io.jmix.reportsui.screen.ReportsClientProperties;
import io.jmix.reportsui.screen.definition.edit.crosstab.CrossTabTableDecorator;
import io.jmix.reportsui.screen.definition.edit.scripteditordialog.ScriptEditorDialog;
import io.jmix.security.constraint.PolicyStore;
import io.jmix.security.constraint.SecureOperations;
import io.jmix.ui.Actions;
import io.jmix.ui.Dialogs;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.*;
import io.jmix.ui.component.autocomplete.AutoCompleteSupport;
import io.jmix.ui.component.autocomplete.JpqlSuggestionFactory;
import io.jmix.ui.component.autocomplete.Suggester;
import io.jmix.ui.component.autocomplete.Suggestion;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.util.*;

@UiController("report_BandDefinitionEditor.fragment")
@UiDescriptor("definition-edit.xml")
public class BandDefinitionEditor extends ScreenFragment implements Suggester {

    @Autowired
    protected CollectionContainer<BandDefinition> bandsDc;
    @Autowired
    protected CollectionContainer<DataSet> dataSetsDc;
    @Autowired
    protected InstanceContainer<Report> reportDc;
    @Autowired
    protected CollectionContainer<ReportInputParameter> parametersDc;
    @Autowired
    protected Table<DataSet> dataSetsTable;
    @Autowired
    protected SourceCodeEditor dataSetScriptField;
    @Autowired
    protected SourceCodeEditor jsonGroovyCodeEditor;
    @Autowired
    protected BoxLayout dataSetScriptBox;
    @Autowired
    protected Label<String> entitiesParamLabel;
    @Autowired
    protected Label<String> entityParamLabel;
    @Autowired
    protected GridLayout commonEntityGrid;
    @Autowired
    protected ComboBox<JsonSourceType> jsonSourceTypeField;
    @Autowired
    protected VBoxLayout jsonDataSetTypeVBox;
    @Autowired
    protected Label<String> jsonPathQueryLabel;
    @Autowired
    protected VBoxLayout jsonSourceGroovyCodeVBox;
    @Autowired
    protected VBoxLayout jsonSourceURLVBox;
    @Autowired
    protected VBoxLayout jsonSourceParameterCodeVBox;
    @Autowired
    protected HBoxLayout textParamsBox;
    @Autowired
    protected Label<String> viewNameLabel;
    @Autowired
    protected ComboBox<Orientation> orientationField;
    @Autowired
    protected ComboBox<BandDefinition> parentBandField;
    @Autowired
    protected TextField<String> nameField;
    @Autowired
    protected ComboBox<String> viewNameField;
    @Autowired
    protected ComboBox entitiesParamField;
    @Autowired
    protected ComboBox entityParamField;
    @Autowired
    protected ComboBox dataStoreField;
    @Autowired
    protected CheckBox isProcessTemplateField;
    @Autowired
    protected CheckBox isUseExistingViewField;
    @Autowired
    protected Button viewEditButton;
    @Autowired
    protected Label<String> buttonEmptyElement;
    @Autowired
    protected Label<String> checkboxEmptyElement;
    @Autowired
    protected Label<String> spacerLabel;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected ReportsWizard reportWizardService;
    @Autowired
    protected BoxLayout editPane;
    @Autowired
    protected DataSetFactory dataSetFactory;
    @Autowired
    protected CrossTabTableDecorator tabOrientationTableDecorator;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected SecureOperations secureOperations;
    @Autowired
    protected PolicyStore policyStore;
    @Autowired
    protected TextArea<String> jsonPathQueryTextAreaField;
    @Autowired
    protected JpqlSuggestionFactory jpqlSuggestionFactory;
    @Autowired
    protected Stores stores;
    @Autowired
    protected ReportsClientProperties reportsClientProperties;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected ScreenBuilders screenBuilders;
    @Autowired
    protected Actions actions;

    protected SourceCodeEditor.Mode dataSetScriptFieldMode = SourceCodeEditor.Mode.Text;

    @Subscribe("jsonSourceGroovyCodeLinkBtn")
    protected void showJsonScriptEditorDialog(Button.ClickEvent event) {
        ScriptEditorDialog editorDialog = (ScriptEditorDialog) screenBuilders.screen(this)
                .withScreenId("report_Editor.dialog")
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        "scriptValue", jsonGroovyCodeEditor.getValue(),
                        "helpHandler", jsonGroovyCodeEditor.getContextHelpIconClickHandler()
                ))).build();
        editorDialog.setCaption(getScriptEditorDialogCaption());
        editorDialog.addAfterCloseListener(actionId -> {
            if (Window.COMMIT_ACTION_ID.equals(((StandardCloseAction) actionId.getCloseAction()).getActionId())) {
                jsonGroovyCodeEditor.setValue(editorDialog.getValue());
            }
        });

        editorDialog.show();
    }

    protected String getScriptEditorDialogCaption() {
        ReportGroup group = reportDc.getItem().getGroup();
        String report = reportDc.getItem().getName();

        if (ObjectUtils.isNotEmpty(group) && ObjectUtils.isNotEmpty(report)) {
            return messages.formatMessage(getClass(), "scriptEditorDialog.captionFormat", report, bandsDc.getItem().getName());
        }
        return null;
    }

    @Subscribe("dataSetTextLinkBtn")
    protected void showDataSetScriptEditorDialog(Button.ClickEvent event) {
        ScriptEditorDialog editorDialog = (ScriptEditorDialog) screenBuilders.screen(this)
                .withScreenId("report_Editor.dialog")
                .withOpenMode(OpenMode.DIALOG)
                .withOptions(new MapScreenOptions(ParamsMap.of(
                        "mode", dataSetScriptFieldMode,
                        "suggester", dataSetScriptField.getSuggester(),
                        "scriptValue", dataSetScriptField.getValue(),
                        "helpHandler", dataSetScriptField.getContextHelpIconClickHandler()
                ))).build();
        editorDialog.setCaption(getScriptEditorDialogCaption());
        editorDialog.addAfterCloseListener(actionId -> {
            if (Window.COMMIT_ACTION_ID.equals(((StandardCloseAction) actionId.getCloseAction()).getActionId())) {
                dataSetScriptField.setValue(editorDialog.getValue());
            }
        });

        editorDialog.show();
    }

    public void setBandDefinition(BandDefinition bandDefinition) {
        if (bandDefinition != null) {
            bandsDc.setItem(bandDefinition);
        }
        nameField.setEditable((bandDefinition == null || bandDefinition.getParent() != null)
                && isUpdatePermitted());
    }

    public InstanceContainer<BandDefinition> getBandDefinitionDs() {
        return bandsDc;
    }

    public void setEnabled(boolean enabled) {
        //Desktop Component containers doesn't apply disable flags for child components
        for (Component component : getFragment().getComponents()) {
            component.setEnabled(enabled);
        }
    }

    @Override
    public List<Suggestion> getSuggestions(AutoCompleteSupport source, String text, int cursorPosition) {
        if (StringUtils.isBlank(text)) {
            return Collections.emptyList();
        }
        int queryPosition = cursorPosition - 1;

        return jpqlSuggestionFactory.requestHint(text, queryPosition, source, cursorPosition);
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        initDataSetListeners();

        initActions();

        initDataStoreField();

        initSourceCodeOptions();
    }

    @Install(to = "jsonGroovyCodeEditor", subject = "contextHelpIconClickHandler")
    protected void jsonGroovyCodeEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage(getClass(), "dataSet.text"))
                .withMessage(messages.getMessage(getClass(), "dataSet.jsonSourceGroovyCodeHelp"))
                .withModal(false)
                .withWidth("700px")
                .show();
    }

    @Install(to = "jsonPathQueryTextAreaField", subject = "contextHelpIconClickHandler")
    protected void jsonPathQueryTextAreaFieldContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage(getClass(), "dataSet.text"))
                .withMessage(messages.getMessage(getClass(), "dataSet.jsonPathQueryHelp"))
                .withModal(false)
                .withWidth("700px")
                .show();
    }

    protected void initSourceCodeOptions() {
        boolean enableTabSymbolInDataSetEditor = reportsClientProperties.getEnableTabSymbolInDataSetEditor();
        jsonGroovyCodeEditor.setHandleTabKey(enableTabSymbolInDataSetEditor);
        dataSetScriptField.setHandleTabKey(enableTabSymbolInDataSetEditor);
    }

    protected void initJsonDataSetOptions(DataSet dataSet) {
        jsonDataSetTypeVBox.removeAll();
        jsonDataSetTypeVBox.add(jsonSourceTypeField);
        jsonDataSetTypeVBox.add(jsonPathQueryLabel);
        jsonDataSetTypeVBox.add(jsonPathQueryTextAreaField);

        if (dataSet.getJsonSourceType() == null) {
            dataSet.setJsonSourceType(JsonSourceType.GROOVY_SCRIPT);
        }

        switch (dataSet.getJsonSourceType()) {
            case GROOVY_SCRIPT:
                jsonDataSetTypeVBox.add(jsonSourceGroovyCodeVBox);
                jsonDataSetTypeVBox.expand(jsonSourceGroovyCodeVBox);
                break;
            case URL:
                jsonDataSetTypeVBox.add(jsonSourceURLVBox);
                jsonDataSetTypeVBox.expand(jsonSourceURLVBox);
                break;
            case PARAMETER:
                jsonDataSetTypeVBox.add(jsonSourceParameterCodeVBox);
                jsonDataSetTypeVBox.add(spacerLabel);
                jsonDataSetTypeVBox.expand(spacerLabel);
                break;
        }
    }

    protected void initDataStoreField() {
        Map<String, Object> all = new HashMap<>();
        all.put(messages.getMessage(getClass(), "dataSet.dataStoreMain"), Stores.MAIN);
        for (String additional : stores.getAdditional()) {
            all.put(additional, additional);
        }
        dataStoreField.setOptionsMap(all);
    }

    @Subscribe("dataSetsTable.create")
    protected void onDataSetsCreate(Action.ActionPerformedEvent event) {
        BandDefinition selectedBand = bandsDc.getItem();
        if (selectedBand != null) {
            DataSet dataset = dataSetFactory.createEmptyDataSet(selectedBand);
            selectedBand.getDataSets().add(dataset);
            dataSetsDc.getMutableItems().add(dataset);
            dataSetsDc.setItem(dataset);
            dataSetsTable.setSelected(dataset);
        }
    }

    @Install(to = "dataSetsTable.create", subject = "enabledRule")
    protected boolean dataSetsCreateEnabledRule() {
        return isUpdatePermitted();
    }

    protected void initActions() {
        EditViewAction editViewAction = (EditViewAction) actions.create(EditViewAction.ID);
        editViewAction.setDataSetsTable(dataSetsTable);
        editViewAction.setBandsDc(bandsDc);
        viewEditButton.setAction(editViewAction);

        viewNameField.setOptionsMap(new HashMap<>());

        entitiesParamField.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDc, "listEntitiesParamName"));
        entityParamField.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDc, "entityParamName"));
        viewNameField.setNewOptionHandler(LinkedWithPropertyNewOptionHandler.handler(dataSetsDc, "viewName"));
    }

    @Subscribe(id = "parametersDc", target = Target.DATA_CONTAINER)
    protected void onParametersDcCollectionChange(CollectionContainer.CollectionChangeEvent<ReportInputParameter> event) {

        Map<String, Object> paramAliases = new HashMap<>();

        for (ReportInputParameter item : event.getSource().getItems()) {
            paramAliases.put(item.getName(), item.getAlias());
        }
        entitiesParamField.setOptionsMap(paramAliases);
        entityParamField.setOptionsMap(paramAliases);
    }

    @Subscribe(id = "bandsDc", target = Target.DATA_CONTAINER)
    protected void onBandsDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<BandDefinition> event) {
        if ("name".equals(event.getProperty()) && StringUtils.isBlank((String) event.getValue())) {
            event.getItem().setName("*");
        }
    }

    @Subscribe(id = "bandsDc", target = Target.DATA_CONTAINER)
    protected void onBandsDcItemChange(InstanceContainer.ItemChangeEvent<BandDefinition> event) {
        updateRequiredIndicators(event.getItem());
        selectFirstDataSet();
    }

    @Subscribe(id = "dataSetsDc", target = Target.DATA_CONTAINER)
    protected void onDataSetsDcItemChange(InstanceContainer.ItemChangeEvent<DataSet> event) {
        DataSet dataSet = event.getItem();

        if (dataSet != null) {
            applyVisibilityRules(event.getItem());

            if (dataSet.getType() == DataSetType.SINGLE) {
                refreshViewNames(findParameterByAlias(dataSet.getEntityParamName()));
            } else if (dataSet.getType() == DataSetType.MULTI) {
                refreshViewNames(findParameterByAlias(dataSet.getListEntitiesParamName()));
            }

            dataSetScriptField.resetEditHistory();
        } else {
            hideAllDataSetEditComponents();
        }
    }

    @Subscribe(id = "dataSetsDc", target = Target.DATA_CONTAINER)
    protected void onDataSetsDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<DataSet> event) {
        applyVisibilityRules(event.getItem());
        if ("entityParamName".equals(event.getProperty()) || "listEntitiesParamName".equals(event.getProperty())) {
            ReportInputParameter linkedParameter = findParameterByAlias(String.valueOf(event.getValue()));
            refreshViewNames(linkedParameter);
        }

        if ("processTemplate".equals(event.getProperty())) {
            applyVisibilityRulesForType(event.getItem());
        }
    }

    protected void initDataSetListeners() {
        tabOrientationTableDecorator.decorate(dataSetsTable, dataSetsDc, bandsDc);
        dataSetScriptField.resetEditHistory();
        hideAllDataSetEditComponents();
    }

    protected boolean isUpdatePermitted() {
        return secureOperations.isEntityUpdatePermitted(metadata.getClass(Report.class), policyStore);
    }

    protected void updateRequiredIndicators(BandDefinition item) {
        boolean required = !(item == null || reportDc.getItem().getRootBandDefinition().equals(item));
        parentBandField.setRequired(required);
        orientationField.setRequired(required);
        nameField.setRequired(item != null);
    }

    @Nullable
    protected ReportInputParameter findParameterByAlias(String alias) {
        for (ReportInputParameter reportInputParameter : parametersDc.getItems()) {
            if (reportInputParameter.getAlias().equals(alias)) {
                return reportInputParameter;
            }
        }
        return null;
    }

    protected void refreshViewNames(@Nullable ReportInputParameter reportInputParameter) {
        if (reportInputParameter != null) {
            if (StringUtils.isNotBlank(reportInputParameter.getEntityMetaClass())) {
                MetaClass parameterMetaClass = metadata.getClass(reportInputParameter.getEntityMetaClass());
                Collection<String> fetchPlanNames = fetchPlanRepository.getFetchPlanNames(parameterMetaClass);
                Map<String, String> fetchPlans = new HashMap<>();
                for (String fetchPlanName : fetchPlanNames) {
                    fetchPlans.put(fetchPlanName, fetchPlanName);
                }
                fetchPlans.put(FetchPlan.LOCAL, FetchPlan.LOCAL);
                fetchPlans.put(FetchPlan.INSTANCE_NAME, FetchPlan.INSTANCE_NAME);
                viewNameField.setOptionsMap(fetchPlans);
                return;
            }
        }

        viewNameField.setOptionsMap(new HashMap<>());
    }

    protected void applyVisibilityRules(DataSet item) {
        applyVisibilityRulesForType(item);
        if (item.getType() == DataSetType.SINGLE || item.getType() == DataSetType.MULTI) {
            applyVisibilityRulesForEntityType(item);
        }
    }

    protected void applyVisibilityRulesForType(DataSet dataSet) {
        hideAllDataSetEditComponents();

        if (dataSet.getType() != null) {
            switch (dataSet.getType()) {
                case SQL:
                case JPQL:
                    textParamsBox.add(dataStoreField);
                    dataSetScriptBox.add(isProcessTemplateField);
                case GROOVY:
                    editPane.add(dataSetScriptBox);
                    break;
                case SINGLE:
                    editPane.add(commonEntityGrid);
                    setCommonEntityGridVisiblity(true, false);
                    editPane.add(spacerLabel);
                    editPane.expand(spacerLabel);
                    break;
                case MULTI:
                    editPane.add(commonEntityGrid);
                    setCommonEntityGridVisiblity(false, true);
                    editPane.add(spacerLabel);
                    editPane.expand(spacerLabel);
                    break;
                case JSON:
                    initJsonDataSetOptions(dataSet);
                    editPane.add(jsonDataSetTypeVBox);
                    break;
            }

            switch (dataSet.getType()) {
                case SQL:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.SQL;
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.SQL);
                    dataSetScriptField.setSuggester(null);
                    dataSetScriptField.setContextHelpIconClickHandler(null);
                    break;

                case GROOVY:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.Groovy;
                    dataSetScriptField.setSuggester(null);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Groovy);
                    dataSetScriptField.setContextHelpIconClickHandler(e ->
                            dialogs.createMessageDialog()
                                    .withCaption(messages.getMessage(getClass(), "dataSet.text"))
                                    .withMessage(messages.getMessage(getClass(), "dataSet.textHelp"))
                                    .withModal(false)
                                    .withWidth("700px")
                                    .withContentMode(ContentMode.HTML)
                                    .show());
                    break;

                case JPQL:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.Text;
                    dataSetScriptField.setSuggester(isProcessTemplateField.isChecked() ? null : this);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Text);
                    dataSetScriptField.setContextHelpIconClickHandler(null);
                    break;

                default:
                    dataSetScriptFieldMode = SourceCodeEditor.Mode.Text;
                    dataSetScriptField.setSuggester(null);
                    dataSetScriptField.setMode(SourceCodeEditor.Mode.Text);
                    dataSetScriptField.setContextHelpIconClickHandler(null);
                    break;
            }
        }
    }

    protected void applyVisibilityRulesForEntityType(DataSet item) {
        commonEntityGrid.remove(viewNameLabel);
        commonEntityGrid.remove(viewNameField);
        commonEntityGrid.remove(viewEditButton);
        commonEntityGrid.remove(buttonEmptyElement);
        commonEntityGrid.remove(isUseExistingViewField);
        commonEntityGrid.remove(checkboxEmptyElement);

        if (Boolean.TRUE.equals(item.getUseExistingView())) {
            commonEntityGrid.add(viewNameLabel);
            commonEntityGrid.add(viewNameField);
        } else {
            commonEntityGrid.add(viewEditButton);
            commonEntityGrid.add(buttonEmptyElement);
        }

        commonEntityGrid.add(isUseExistingViewField);
        commonEntityGrid.add(checkboxEmptyElement);
    }

    protected void hideAllDataSetEditComponents() {
        // do not use setVisible(false) due to web legacy (Vaadin 6) layout problems #PL-3916
        textParamsBox.remove(dataStoreField);
        dataSetScriptBox.remove(isProcessTemplateField);
        editPane.remove(dataSetScriptBox);
        editPane.remove(commonEntityGrid);
        editPane.remove(jsonDataSetTypeVBox);
        editPane.remove(spacerLabel);
    }

    protected void selectFirstDataSet() {
//        dataSetDc.refresh();
        if (!dataSetsDc.getItems().isEmpty()) {
            DataSet item = dataSetsDc.getItems().iterator().next();
            dataSetsTable.setSelected(item);
        } else {
            dataSetsTable.setSelected((DataSet) null);
        }
    }

    // This is a stub for using set in some DataSet change listener
    protected void setViewEditVisibility(DataSet dataSet) {
        if (isViewEditAllowed(dataSet)) {
            viewEditButton.setVisible(true);
        } else {
            viewEditButton.setVisible(false);
        }
    }

    protected boolean isViewEditAllowed(DataSet dataSet) {
        return true;
    }

    protected void setCommonEntityGridVisiblity(boolean visibleEntityGrid, boolean visibleEntitiesGrid) {
        entityParamLabel.setVisible(visibleEntityGrid);
        entityParamField.setVisible(visibleEntityGrid);
        entitiesParamLabel.setVisible(visibleEntitiesGrid);
        entitiesParamField.setVisible(visibleEntitiesGrid);
    }
}