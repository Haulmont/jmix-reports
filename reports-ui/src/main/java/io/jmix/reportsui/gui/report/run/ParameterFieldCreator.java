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

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import io.jmix.core.*;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.ParameterType;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.entity.ReportType;
import io.jmix.ui.Actions;
import io.jmix.ui.UiComponents;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.entitypicker.EntityClearAction;
import io.jmix.ui.action.entitypicker.LookupAction;
import io.jmix.ui.action.tagpicker.TagLookupAction;
import io.jmix.ui.action.valuepicker.ValueClearAction;
import io.jmix.ui.action.valuespicker.SelectAction;
import io.jmix.ui.component.*;
import io.jmix.ui.component.data.options.ContainerOptions;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.model.DataComponents;
import io.jmix.ui.screen.MapScreenOptions;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@org.springframework.stereotype.Component("report_ParameterFieldCreator")
public class ParameterFieldCreator {

    public static final String COMMON_LOOKUP_SCREEN_ID = "commonLookup";

    @Autowired
    protected UiComponents uiComponents;

    @Autowired
    protected Messages messages;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected ClassManager classManager;

    @Autowired
    protected ReportService reportService;

    @Autowired
    protected QueryTransformerFactory queryTransformerFactory;

    @Autowired
    protected DatatypeRegistry datatypeRegistry;

    @Autowired
    protected DataComponents factory;

    @Autowired
    protected FetchPlans fetchPlans;

    @Autowired
    protected Actions actions;

    protected Map<ParameterType, FieldCreator> fieldCreationMapping = new ImmutableMap.Builder<ParameterType, FieldCreator>()
            .put(ParameterType.BOOLEAN, new CheckBoxCreator())
            .put(ParameterType.DATE, new DateFieldCreator())
            .put(ParameterType.ENTITY, new SingleFieldCreator())
            .put(ParameterType.ENUMERATION, new EnumFieldCreator())
            .put(ParameterType.TEXT, new TextFieldCreator())
            .put(ParameterType.NUMERIC, new NumericFieldCreator())
            .put(ParameterType.ENTITY_LIST, new MultiFieldCreator())
            .put(ParameterType.DATETIME, new DateTimeFieldCreator())
            .put(ParameterType.TIME, new TimeFieldCreator())
            .build();

    public Label<String> createLabel(ReportInputParameter parameter, Field field) {
        Label<String> label = uiComponents.create(Label.class);
        label.setAlignment(field instanceof TagPicker ? Component.Alignment.TOP_LEFT : Component.Alignment.MIDDLE_LEFT);
        label.setWidth(Component.AUTO_SIZE);
        label.setValue(parameter.getLocName());
        return label;
    }

    public Field createField(ReportInputParameter parameter) {
        Field field = fieldCreationMapping.get(parameter.getType()).createField(parameter);
        field.setRequiredMessage(messages.formatMessage(this.getClass(), "error.paramIsRequiredButEmpty", parameter.getLocName()));

        field.setId("param_" + parameter.getAlias());
        field.setWidth("100%");
        field.setEditable(true);

        field.setRequired(parameter.getRequired());
        return field;
    }

    protected void setCurrentDateAsNow(ReportInputParameter parameter, Field dateField) {
        Date now = reportService.currentDateOrTime(parameter.getType());
        dateField.setValue(now);
        parameter.setDefaultValue(reportService.convertToString(now.getClass(), now));
    }

    protected interface FieldCreator {
        Field createField(ReportInputParameter parameter);
    }

    protected class DateFieldCreator implements FieldCreator {
        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = uiComponents.create(DateField.class);
            dateField.setResolution(DateField.Resolution.DAY);
            dateField.setDateFormat(messages.getMessage("dateFormat"));
            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, dateField);
            }
            return dateField;
        }
    }

    protected class DateTimeFieldCreator implements FieldCreator {
        @Override
        public Field createField(ReportInputParameter parameter) {
            DateField dateField = uiComponents.create(DateField.class);
            dateField.setResolution(DateField.Resolution.MIN);
            dateField.setDateFormat(messages.getMessage("dateTimeFormat"));
            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, dateField);
            }
            return dateField;
        }
    }

    protected class TimeFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            Field timeField = uiComponents.create(TimeField.class);
            if (BooleanUtils.isTrue(parameter.getDefaultDateIsCurrent())) {
                setCurrentDateAsNow(parameter, timeField);
            }
            return uiComponents.create(TimeField.class);
        }
    }

    protected class CheckBoxCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            CheckBox checkBox = uiComponents.create(CheckBox.class);
            checkBox.setAlignment(Component.Alignment.MIDDLE_LEFT);
            return checkBox;
        }
    }

    protected class TextFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            return uiComponents.create(TextField.class);
        }
    }

    protected class NumericFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            TextField textField = uiComponents.create(TextField.class);
            textField.setDatatype(datatypeRegistry.get(Double.class));
            return textField;
        }
    }

    protected class EnumFieldCreator implements FieldCreator {

        @Override
        public Field createField(ReportInputParameter parameter) {
            ComboBox lookupField = uiComponents.create(ComboBox.class);
            String enumClassName = parameter.getEnumerationClass();
            if (StringUtils.isNotBlank(enumClassName)) {
                Class enumClass = classManager.loadClass(enumClassName);

                if (enumClass != null) {
                    Object[] constants = enumClass.getEnumConstants();
                    List<Object> optionsList = new ArrayList<>();
                    Collections.addAll(optionsList, constants);

                    lookupField.setOptionsList(optionsList);
                    if (optionsList.size() < 10) {
                        lookupField.setTextInputAllowed(false);
                    }
                }
            }
            return lookupField;
        }
    }

    protected class SingleFieldCreator implements FieldCreator {
        @Override
        public Field createField(ReportInputParameter parameter) {
            boolean isLookup = Boolean.TRUE.equals(parameter.getLookup());
            EntityPicker field;
            MetaClass entityMetaClass = metadata.getClass(parameter.getEntityMetaClass());

            if (isLookup) {
                field = createEntityComboBox(parameter, entityMetaClass);
            } else {
                field = createEntityPicker();
            }
            field.setMetaClass(entityMetaClass);

            LookupAction pickerLookupAction = (LookupAction) actions.create(LookupAction.ID);
            field.addAction(pickerLookupAction);

            String parameterScreen = parameter.getScreen();

            if (StringUtils.isNotEmpty(parameterScreen)) {
                pickerLookupAction.setScreenId(parameterScreen);
            }

            Action entityClearAction = actions.create(EntityClearAction.ID);
            field.addAction(entityClearAction);

            return field;
        }

        protected EntityPicker createEntityPicker() {
            return uiComponents.create(EntityPicker.class);
        }

        protected EntityPicker createEntityComboBox(ReportInputParameter parameter, MetaClass entityMetaClass) {
            EntityComboBox field = uiComponents.create(EntityComboBox.class);

            FetchPlan fetchPlan = fetchPlans.builder(entityMetaClass.getJavaClass())
                    .addFetchPlan(FetchPlan.INSTANCE_NAME)
                    .build();

            CollectionContainer collectionContainer = factory.createCollectionContainer(entityMetaClass.getJavaClass());
            collectionContainer.setFetchPlan(fetchPlan);

            CollectionLoader collectionLoader = factory.createCollectionLoader();
            collectionLoader.setContainer(collectionContainer);

            String whereClause = parameter.getLookupWhere();
            String joinClause = parameter.getLookupJoin();
            if (!Strings.isNullOrEmpty(whereClause)) {
                String query = String.format("select e from %s e", entityMetaClass.getName());
                QueryTransformer queryTransformer = queryTransformerFactory.transformer(query);
                queryTransformer.addWhere(whereClause);
                if (!Strings.isNullOrEmpty(joinClause)) {
                    queryTransformer.addJoin(joinClause);
                }
                query = queryTransformer.getResult();
                collectionLoader.setQuery(query);
            }
            field.setOptionsContainer(collectionContainer);
            return field;
        }
    }

    protected class MultiFieldCreator implements FieldCreator {

        @Override
        public Field createField(final ReportInputParameter parameter) {
            TagPicker tagPicker = uiComponents.create(TagPicker.class);
            MetaClass entityMetaClass = metadata.getClass(parameter.getEntityMetaClass());

            CollectionContainer collectionContainer = factory.createCollectionContainer(entityMetaClass.getJavaClass());
            FetchPlan fetchPlan = fetchPlans.builder(entityMetaClass.getJavaClass())
                    .addFetchPlan(FetchPlan.LOCAL)
                    .build();

            collectionContainer.setFetchPlan(fetchPlan);
            factory.createCollectionLoader().setContainer(collectionContainer);

            ContainerOptions options = new ContainerOptions(collectionContainer);

            tagPicker.setOptions(options);
            tagPicker.setEditable(true);
            tagPicker.setHeight("120px");

            TagLookupAction tagLookupAction = (TagLookupAction) actions.create(TagLookupAction.ID);
            tagLookupAction.setTagPicker(tagPicker);
            tagLookupAction.setMultiSelect(true);

            String screen = parameter.getScreen();
            if (StringUtils.isNotEmpty(screen)) {
                tagLookupAction.setScreenId(screen);
            }
            tagPicker.addAction(tagLookupAction);

            ValueClearAction valueClearAction = (ValueClearAction) actions.create(ValueClearAction.ID);
            tagPicker.addAction(valueClearAction);

            tagPicker.setInlineTags(true);

            return tagPicker;
        }
    }
}