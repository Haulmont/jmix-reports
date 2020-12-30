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

package io.jmix.reportsui.gui.template.edit.pivottable.aggregation.edit;

import io.jmix.core.Messages;
import io.jmix.reports.entity.pivottable.PivotTableAggregation;
import io.jmix.ui.Dialogs;
import io.jmix.ui.WindowParam;
import io.jmix.ui.component.SourceCodeEditor;
import io.jmix.ui.component.ValidationErrors;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Objects;

@UiController("report_PivotTableAggregation.edit")
@UiDescriptor("pivottable-aggregation-edit.xml")
public class PivotTableAggregationEdit extends StandardEditor<PivotTableAggregation> {

    @WindowParam
    protected Collection<PivotTableAggregation> existingItems;
    @Autowired
    protected SourceCodeEditor sourceCodeEditor;
    @Autowired
    protected Dialogs dialogs;
    @Autowired
    protected Messages messages;
    @Autowired
    protected ScreenValidation screenValidation;

    @Subscribe
    protected void onAfterInit(AfterInitEvent event) {
        sourceCodeEditor.setContextHelpIconClickHandler(e ->
                dialogs.createMessageDialog()
                        .withCaption(messages.getMessage("pivotTable.functionHelpCaption"))
                        .withMessage(messages.getMessage("pivotTable.aggregationFunctionHelp"))
                        .withModal(false)
                        .withWidth("560px")
                        .show());
    }

    @Subscribe
    protected void onBeforeCommit(BeforeCommitChangesEvent event) {
        if (event.isCommitPrevented()) {
            PivotTableAggregation aggregation = getEditedEntity();
            boolean hasMatches = existingItems.stream().
                    anyMatch(e -> !Objects.equals(aggregation, e) &&
                            Objects.equals(aggregation.getCaption(), e.getCaption()));
            if (hasMatches) {
                ValidationErrors validationErrors = new ValidationErrors();
                validationErrors.add(messages.getMessage("pivotTableEdit.uniqueAggregationOptionCaption"));

                //todo
                screenValidation.showValidationErrors(this, validationErrors);
                event.preventCommit();
            }
            event.resume();
        }
        event.preventCommit();
    }
}
