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

package io.jmix.reportsui.gui.template.edit.pivottable.property.edit;

import io.jmix.core.Messages;
import io.jmix.reports.entity.pivottable.PivotTableProperty;
import io.jmix.reports.entity.pivottable.PivotTablePropertyType;
import io.jmix.ui.Dialogs;
import io.jmix.ui.component.Form;
import io.jmix.ui.component.HasContextHelp;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.model.InstanceContainer;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

@UiController("report_PivotTableProperty.edit")
@UiDescriptor("pivottable-property-edit.xml")
public class PivotTablePropertyEdit extends StandardEditor<PivotTableProperty> {
    @Autowired
    protected Form propertyForm;
    @Autowired
    protected InstanceContainer<PivotTableProperty> propertyDc;
    @Autowired
    protected SourceCodeEditor sourceCodeEditor;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Dialogs dialogs;

    @Subscribe
    protected void onBeforeShow(BeforeShowEvent event) {
        initFunctionField();
    }

    @Subscribe(id = "propertyDc", target = Target.DATA_CONTAINER)
    protected void onPropertyDcItemPropertyChange(InstanceContainer.ItemPropertyChangeEvent<PivotTableProperty> event) {
        if ("type".equals(event.getProperty())) {
            initFunctionField();
        }
    }

    @Install(to = "sourceCodeEditor", subject = "contextHelpIconClickHandler")
    protected void sourceCodeEditorContextHelpIconClickHandler(HasContextHelp.ContextHelpIconClickEvent contextHelpIconClickEvent) {
        dialogs.createMessageDialog()
                .withCaption(messages.getMessage("pivotTable.functionHelpCaption"))
                .withMessage(messages.getMessage("pivotTable.propertyFunctionHelp"))
                .withModal(false)
                .withWidth("560px")
                .show();
    }

    protected void initFunctionField() {
        PivotTableProperty property = getEditedEntity();
        propertyForm.getComponent("function")
                .setVisible(property.getType() == PivotTablePropertyType.DERIVED);
    }
}
