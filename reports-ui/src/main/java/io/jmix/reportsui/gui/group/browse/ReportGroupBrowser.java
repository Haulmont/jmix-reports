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

package io.jmix.reportsui.gui.group.browse;

import com.haulmont.cuba.core.global.LoadContext;
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.ui.Notifications;
import io.jmix.ui.action.list.CreateAction;
import io.jmix.ui.action.list.EditAction;
import io.jmix.ui.action.list.RemoveAction;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ListComponent;
import io.jmix.ui.component.Table;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Named;

@UiController("report_ReportGroup.browse")
@UiDescriptor("group-browse.xml")
public class ReportGroupBrowser extends StandardLookup<ReportGroup> {

    @Autowired
    protected Table reportGroupsTable;

    @Named("reportGroupsTable.create")
    protected CreateAction createAction;

    @Named("reportGroupsTable.edit")
    protected EditAction editAction;

    @Autowired
    protected DataManager dataManager;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected Notifications notifications;

    @Autowired
    protected Messages messages;

    @Subscribe
    protected void onInit(InitEvent event) {
        createAction.setOpenMode(OpenMode.DIALOG);
        editAction.setOpenMode(OpenMode.DIALOG);

        reportGroupsTable.addAction(new RemoveReportGroupAction(reportGroupsTable));
    }

    protected class RemoveReportGroupAction extends RemoveAction {

        public RemoveReportGroupAction(ListComponent owner) {
            super(owner.getId());
        }

        @Override
        public void actionPerform(Component component) {
            if (!isEnabled()) {
                return;
            }

            ReportGroup group = (ReportGroup) target.getSingleSelected();
            if (group != null) {
                if (group.getSystemFlag()) {
                    notifications.create(Notifications.NotificationType.WARNING)
                            .withCaption(messages.getMessage("unableToDeleteSystemReportGroup"))
                            .show();
                } else {
                    LoadContext<Report> loadContext = new LoadContext<>(Report.class);
                    loadContext.setFetchPlan("report.view");
                    LoadContext.Query query =
                            new LoadContext.Query("select r from report_Report r where r.group.id = :groupId");
                    query.setMaxResults(1);
                    query.setParameter("groupId", group.getId());
                    loadContext.setQuery(query);

                    Report report = dataManager.load(loadContext);
                    if (report != null) {
                        notifications.create(Notifications.NotificationType.WARNING)
                                .withCaption(messages.getMessage("unableToDeleteNotEmptyReportGroup"))
                                .show();
                    } else {
                        super.actionPerform(component);
                    }
                }
            }
        }
    }
}