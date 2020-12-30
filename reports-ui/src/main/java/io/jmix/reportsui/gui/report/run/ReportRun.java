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
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportGroup;
import io.jmix.reportsui.gui.ReportGuiManager;
import io.jmix.ui.action.Action;
import io.jmix.ui.action.ItemTrackingAction;
import io.jmix.ui.component.*;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.screen.LookupComponent;
import io.jmix.ui.screen.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UiController("report_Report.run")
@UiDescriptor("report-run.xml")
@LookupComponent("reportsTable")
public class ReportRun extends StandardLookup<Report> {

    protected static final String RUN_ACTION_ID = "runReport";

    @Autowired
    protected Table<Report> reportsTable;

    @Autowired
    protected ReportGuiManager reportGuiManager;

    @Autowired
    protected CollectionContainer<Report> reportsDc;

    @Autowired
    protected CurrentAuthentication currentAuthentication;

    @Autowired
    protected TextField<String> nameFilter;

    @Autowired
    protected TextField<String> codeFilter;

    @Autowired
    protected EntityComboBox<ReportGroup> groupFilter;

    @Autowired
    protected DateField<Date> updatedDateFilter;

    @Autowired
    protected GridLayout gridFilter;

    @Autowired
    protected Messages messages;

    @Autowired
    protected DataManager dataManager;

    protected List<Report> reportsParameter;

    protected MetaClass metaClassParameter;

    protected String screenParameter;

    public void setReportsParameter(List<Report> reportsParameter) {
        this.reportsParameter = reportsParameter;
    }

    public void setMetaClassParameter(MetaClass metaClassParameter) {
        this.metaClassParameter = metaClassParameter;
    }

    public void setScreenParameter(String screenParameter) {
        this.screenParameter = screenParameter;
    }

    @Subscribe
    protected void onInit(InitEvent event) {
        List<Report> reports = reportsParameter;
        if (reports == null) {
            reports = reportGuiManager.getAvailableReports(screenParameter, currentAuthentication.getUser(),
                    metaClassParameter);
        }

        if (reportsParameter != null) {
            gridFilter.setVisible(false);
        }

        for (Report report : reports) {
            reportsDc.getItems().add(report);
        }

        Action runAction = new ItemTrackingAction(RUN_ACTION_ID)
                .withCaption(messages.getMessage(getClass(), "runReport"))
                .withHandler(e -> {
                    Report report = reportsTable.getSingleSelected();
                    if (report != null) {
                        report = dataManager.load(Id.of(report))
                                .fetchPlan(ReportService.MAIN_VIEW_NAME)
                                .one();
                        reportGuiManager.runReport(report, ReportRun.this);
                    }
                });

        reportsTable.addAction(runAction);
        reportsTable.setItemClickAction(runAction);

        //TODO shortcut
//        Action applyAction = new BaseAction("applyFilter")
//                .withShortcut(clientConfig.getFilterApplyShortcut())
//                .withHandler(e -> {
//                    filterReports();
//                });
//        reportsTable.addAction(applyAction);
    }

    @Subscribe("setFilterButton")
    public void filterReports() {
        String nameFilterValue = StringUtils.lowerCase(nameFilter.getValue());
        String codeFilterValue = StringUtils.lowerCase(codeFilter.getValue());
        ReportGroup groupFilterValue = groupFilter.getValue();
        Date dateFilterValue = updatedDateFilter.getValue();

        List<Report> reports =
                reportGuiManager.getAvailableReports(screenParameter, currentAuthentication.getUser(),
                        metaClassParameter)
                        .stream()
                        .filter(report -> {
                            if (nameFilterValue != null
                                    && !report.getName().toLowerCase().contains(nameFilterValue)) {
                                return false;
                            }

                            if (codeFilterValue != null) {
                                if (report.getCode() == null
                                        || (report.getCode() != null
                                        && !report.getCode().toLowerCase().contains(codeFilterValue))) {
                                    return false;
                                }
                            }

                            if (groupFilterValue != null && !Objects.equals(report.getGroup(), groupFilterValue)) {
                                return false;
                            }

                            if (dateFilterValue != null
                                    && report.getUpdateTs() != null
                                    && !report.getUpdateTs().after(dateFilterValue)) {
                                return false;
                            }

                            return true;
                        })
                        .collect(Collectors.toList());

        reportsDc.getItems().clear();
        for (Report report : reports) {
            reportsDc.getItems().add(report);
        }

        Table.SortInfo sortInfo = reportsTable.getSortInfo();
        if (sortInfo != null) {
            Table.SortDirection direction = sortInfo.getAscending() ? Table.SortDirection.ASCENDING : Table.SortDirection.DESCENDING;
            reportsTable.sort(sortInfo.getPropertyId().toString(), direction);
        }
    }

    @Subscribe("clearFilterButton")
    public void clearFilter() {
        nameFilter.setValue(null);
        codeFilter.setValue(null);
        updatedDateFilter.setValue(null);
        groupFilter.setValue(null);
        filterReports();
    }
}