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
package io.jmix.reportsui.gui.report.browse;

import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.common.util.ParamsMap;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.reports.app.service.ReportService;
import io.jmix.reports.entity.Report;
import io.jmix.reportsui.gui.ReportGuiManager;
import io.jmix.reportsui.gui.report.edit.ReportEditor;
import io.jmix.reportsui.gui.report.history.ReportExecutionBrowser;
import io.jmix.reportsui.gui.report.importdialog.ReportImportDialog;
import io.jmix.reportsui.gui.report.run.InputParametersDialog;
import io.jmix.reportsui.gui.report.wizard.ReportWizardCreator;
import io.jmix.ui.Notifications;
import io.jmix.ui.ScreenBuilders;
import io.jmix.ui.Screens;
import io.jmix.ui.UiProperties;
import io.jmix.ui.action.Action;
import io.jmix.ui.component.GroupTable;
import io.jmix.ui.download.ByteArrayDataProvider;
import io.jmix.ui.download.DownloadFormat;
import io.jmix.ui.download.Downloader;
import io.jmix.ui.model.CollectionContainer;
import io.jmix.ui.model.CollectionLoader;
import io.jmix.ui.navigation.Route;
import io.jmix.ui.screen.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@UiController("report_Report.browse")
@UiDescriptor("report-browse.xml")
@LookupComponent("reportsTable")
@Route("reports")
public class ReportBrowser extends StandardLookup<Report> {

    @Autowired
    protected ReportGuiManager reportGuiManager;
    @Autowired
    protected ReportService reportService;
    @Autowired
    protected UiProperties uiProperties;
    @Autowired
    protected CollectionLoader<Report> reportDl;
    @Autowired
    protected CoreProperties coreProperties;
    @Autowired
    protected CollectionContainer<Report> reportDc;
    @Autowired
    protected GroupTable<Report> reportsTable;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected AccessManager accessManager;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected EntityStates entityStates;
    @Autowired
    protected Messages messages;
    @Autowired
    protected Screens screens;
    @Autowired
    protected FetchPlanRepository fetchPlanRepository;
    @Autowired
    protected Notifications notifications;
    @Autowired
    protected Downloader downloader;
    @Autowired
    protected ScreenBuilders screenBuilders;

    @Install(to = "reportsTable.create", subject = "afterCloseHandler")
    protected void reportsTableCreateAfterCloseHandler(AfterCloseEvent event) {
        if (event.closedWith(StandardOutcome.COMMIT)) {
            Report newReport = (Report) ((EditorScreen) event.getScreen()).getEditedEntity();

            if (!entityStates.isNew(newReport)) {
                Report reloadedReport = reloadReport(newReport, reportDl.getFetchPlan());
                reportDc.getMutableItems().add(reloadedReport);
            }
        }
    }

    @Subscribe("popupCreateBtn.wizard")
    protected void onPopupCreateBtnWizard(Action.ActionPerformedEvent event) {
        ReportWizardCreator wizard = screens.create(ReportWizardCreator.class, OpenMode.DIALOG);
        wizard.addAfterCloseListener(e -> {
            if (e.closedWith(StandardOutcome.COMMIT)) {
                Report item = wizard.getItem().getGeneratedReport();
                reportDc.getMutableItems().add(item);
                reportsTable.setSelected(item);
                ReportEditor reportEditor = (ReportEditor) screenBuilders.editor(reportsTable)
                        .withOpenMode(OpenMode.THIS_TAB)
                        .build();
                reportEditor.show()
                        .addAfterCloseListener(closeEvent -> {
                            if (closeEvent.closedWith(StandardOutcome.COMMIT)) {
                                Report item1 = reportEditor.getEditedEntity();
                                if (item1 != null) {
                                    reportDc.replaceItem(item1);
                                }
                            }
                            UUID newReportId = reportEditor.getEditedEntity().getId();
                            reportsTable.expandPath(reportDc.getItem(newReportId));
                        });
            }
        });
        wizard.show();
    }

    private boolean isPermissionsToCreateReports() {
        CrudEntityContext showScreenContext = new CrudEntityContext(metadata.getClass(Report.class));
        accessManager.applyRegisteredConstraints(showScreenContext);

        return showScreenContext.isCreatePermitted();
    }

    @Install(to = "reportsTable.import", subject = "enabledRule")
    protected boolean tableImportEnabledRule() {
        return isPermissionsToCreateReports();
    }

    @Subscribe("reportsTable.runReport")
    protected void onTableRunReport(Action.ActionPerformedEvent event) {
        Report report = reportsTable.getSingleSelected();
        if (report != null) {
            report = reloadReport(report, fetchPlanRepository.findFetchPlan(
                    metadata.getClass(Report.class), "report.edit"));
            if (report.getInputParameters() != null && report.getInputParameters().size() > 0 ||
                    reportGuiManager.inputParametersRequiredByTemplates(report)) {
                screens.create(InputParametersDialog.class, OpenMode.DIALOG,
                        new MapScreenOptions(ParamsMap.of("report", report)))
                        .show()
                        .addAfterCloseListener(e -> {
                            reportsTable.focus();
                        });
            } else {
                reportGuiManager.printReport(report, Collections.emptyMap(), ReportBrowser.this);
            }
        }
    }

    @Subscribe("reportsTable.import")
    protected void onTableImport(Action.ActionPerformedEvent event) {
        ReportImportDialog reportImportDialog = screenBuilders.screen(this)
                .withScreenClass(ReportImportDialog.class)
                .withOpenMode(OpenMode.DIALOG)
                .build();
        reportImportDialog.addAfterCloseListener(e -> {
            if (e.closedWith(StandardOutcome.COMMIT)) {
                reportDl.load();
            }
        });
        reportImportDialog.show();
    }

    @Subscribe("reportsTable.export")
    protected void onTableExport(Action.ActionPerformedEvent event) {
        Set<Report> reports = reportsTable.getSelected();
        if (!reports.isEmpty()) {
            ByteArrayDataProvider provider = new ByteArrayDataProvider(reportService.exportReports(reports), uiProperties.getSaveExportedByteArrayDataThresholdBytes(), coreProperties.getTempDir());
            if (reports.size() > 1) {
                downloader.download(provider, "Reports", DownloadFormat.ZIP);
            } else if (reports.size() == 1) {
                downloader.download(provider, reports.iterator().next().getName(), DownloadFormat.ZIP);
            }
        }
    }

    @Subscribe("reportsTable.copy")
    protected void onTableCopy(Action.ActionPerformedEvent event) {
        Report report = reportsTable.getSingleSelected();
        if (report != null) {
            reportService.copyReport(report);
            reportDl.load();
        } else {
            notifications.create(Notifications.NotificationType.HUMANIZED)
                    .withCaption(messages.getMessage(getClass(), "notification.selectReport"))
                    .show();
        }
    }

    @Install(to = "reportsTable.copy", subject = "enabledRule")
    protected boolean tableCopyEnabledRule() {
        Report report = reportsTable.getSingleSelected();
        return report != null && isPermissionsToCreateReports();
    }


    @Subscribe("reportsTable.executions")
    protected void onTableExecutions(Action.ActionPerformedEvent event) {
        Set<Report> selectedReports = reportsTable.getSelected();
        screenBuilders.screen(ReportBrowser.this)
                .withScreenClass(ReportExecutionBrowser.class)
                .withOptions(new MapScreenOptions(
                        ParamsMap.of(ReportExecutionBrowser.REPORTS_PARAMETER, new ArrayList<>(selectedReports))
                ))
                .show();
    }

    private Report reloadReport(Report report, FetchPlan fetchPlan) {
        MetaClass metaClass = metadata.getClass(Report.class);
        LoadContext<Report> lc = new LoadContext<>(metaClass);
        lc.setId(report.getId());
        lc.setFetchPlan(fetchPlan);
        report = dataManager.load(lc);
        return report;
    }

    @Install(to = "reportsTable.edit", subject = "afterCloseHandler")
    protected void tableEditAfterCloseHandler(AfterCloseEvent event) {
        if (event.closedWith(StandardOutcome.COMMIT)) {
            Report editedReport = (Report) ((EditorScreen) event.getScreen()).getEditedEntity();
            Report currentItem = reportDc.getItem(editedReport.getId());

            if (!editedReport.getVersion().equals(currentItem.getVersion())) {
                Report reloadedReport = reloadReport(currentItem, reportDl.getFetchPlan());
                reportDc.replaceItem(reloadedReport);
            }
        }
    }

    @Install(to = "reportsTable.create", subject = "enabledRule")
    protected boolean reportsTableCreateEnabledRule() {
        return isPermissionsToCreateReports();
    }

    @Install(to = "popupCreateBtn.wizard", subject = "enabledRule")
    protected boolean popupCreateBtnWizardEnabledRule() {
        return isPermissionsToCreateReports();
    }
}