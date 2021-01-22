/*
 * Copyright (c) 2008-2019 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package io.jmix.reports;

import com.google.common.collect.Sets;
import com.haulmont.yarg.reporting.ReportOutputDocument;
import com.haulmont.yarg.structure.ReportOutputType;
import io.jmix.core.*;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.localfs.LocalFileStorage;
import io.jmix.reports.entity.JmixReportOutputType;
import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportExecution;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Component(ReportExecutionHistoryRecorder.NAME)
public class ReportExecutionHistoryRecorderBean implements ReportExecutionHistoryRecorder {
    private static Logger log = LoggerFactory.getLogger(ReportExecutionHistoryRecorderBean.class);

    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DataManager dataManager;
    @Autowired
    protected CurrentAuthentication currentAuthentication;
    @Autowired
    protected TimeSource timeSource;
    //TODO Server info API
//    @Autowired
//    protected ServerInfoAPI serverInfoAPI;
    @Autowired
    protected ReportsProperties reportsProperties;
    @Autowired
    protected TransactionTemplate transaction;
    @PersistenceContext
    protected EntityManager entityManager;
    @Autowired
    protected LocalFileStorage localFileStorage;
    @Autowired
    protected EntityStates entityStates;

    @Override
    public ReportExecution startExecution(Report report, Map<String, Object> params) {
        ReportExecution execution = metadata.create(ReportExecution.class);

        execution.setReport(report);
        execution.setReportName(report.getName());
        execution.setReportCode(report.getCode());
        execution.setUser(currentAuthentication.getUser().getUsername());
        execution.setStartTime(timeSource.currentTimestamp());
        //TODO server info api
//        execution.setServerId(serverInfoAPI.getServerId());
        setParametersString(execution, params);
        handleNewReportEntity(execution);

        execution = dataManager.save(execution);
        return execution;
    }

    @Override
    public void markAsSuccess(ReportExecution execution, ReportOutputDocument document) {
        handleSessionExpired(() -> {
            execution.setSuccess(true);
            execution.setFinishTime(timeSource.currentTimestamp());
            if (shouldSaveDocument(execution, document)) {
                try {
                    URI reference = saveDocument(document);
                    execution.setFileUri(reference);
                } catch (FileStorageException e) {
                    log.error("Failed to save output document", e);
                }
            }
            dataManager.save(execution);
        });
    }

    @Override
    public void markAsCancelled(ReportExecution execution) {
        handleSessionExpired(() -> {
            execution.setCancelled(true);
            execution.setFinishTime(timeSource.currentTimestamp());
            dataManager.save(execution);
        });
    }

    @Override
    public void markAsError(ReportExecution execution, Exception e) {
        handleSessionExpired(() -> {
            execution.setSuccess(false);
            execution.setFinishTime(timeSource.currentTimestamp());
            execution.setErrorMessage(e.getMessage());

            dataManager.save(execution);
        });
    }

    protected void setParametersString(ReportExecution reportExecution, Map<String, Object> params) {
        if (params.size() <= 0) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = (entry.getValue() instanceof Entity)
                    ? String.format("%s-%s", metadata.getClass(entry.getValue().getClass()), Id.of((Entity) entry.getValue()).getValue())
                    : entry.getValue();
            builder.append(String.format("key: %s, value: %s", entry.getKey(), value)).append("\n");
        }
        reportExecution.setParams(builder.toString());
    }

    private void handleNewReportEntity(ReportExecution entity) {
        Report report = entity.getReport();

        // handle case when user runs report that isn't saved yet from Report Editor
        if (entityStates.isNew(report)) {
            Report reloaded = dataManager.load(Id.of(report))
                    .fetchPlan(FetchPlan.INSTANCE_NAME)
                    .optional()
                    .orElse(null);
            entity.setReport(reloaded);
        }
    }

    // can be used as extension point
    @SuppressWarnings("unused")
    protected boolean shouldSaveDocument(ReportExecution execution, ReportOutputDocument document) {
        ReportOutputType type = document.getReportOutputType();
        Set<String> outputTypesWithoutDocument = Sets.newHashSet(
                JmixReportOutputType.chart.getId(),
                JmixReportOutputType.table.getId(),
                JmixReportOutputType.pivot.getId());
        return reportsProperties.isSaveOutputDocumentsToHistory() && !outputTypesWithoutDocument.contains(type.getId());
    }

    protected URI saveDocument(ReportOutputDocument document) throws FileStorageException {
        URI reference = localFileStorage.createReference(document.getDocumentName());
        localFileStorage.saveStream(reference, new ByteArrayInputStream(document.getContent()));
        return reference;
    }

    /**
     * It is not rare for large reports to execute longer than {@link //TODO fix javadoc ServerConfig#getUserSessionExpirationTimeoutSec()}.
     * In this case when report is finished - user session is already expired and can't be used to make changes to database.
     */
    protected void handleSessionExpired(Runnable action) {
        //TODO handle session expired
//        boolean userSessionIsValid = currentAuthentication.getAuthentication();
//        if (userSessionIsValid) {
//            action.run();
//        } else {
//            log.debug("No valid user session, record history under system user");
        //TODO with system user
//            authentication.withSystemUser(() -> {
//                action.run();
//                return null;
//            });
//        }
    }

    @Override
    public String cleanupHistory() {
        int deleted = 0;

        deleted += deleteHistoryByDays();
        deleted += deleteHistoryGroupedByReport();

        return deleted > 0 ? String.valueOf(deleted) : null;
    }

    private int deleteHistoryByDays() {
        int historyCleanupMaxDays = reportsProperties.getHistoryCleanupMaxDays();
        if (historyCleanupMaxDays <= 0) {
            return 0;
        }

        Date borderDate = DateUtils.addDays(timeSource.currentTimestamp(), -historyCleanupMaxDays);
        log.debug("Deleting report executions older than {}", borderDate);

        List<URI> paths = new ArrayList<>();

        int deleted = transaction.execute(status -> {
            List<URI> ids = entityManager.createQuery("select e.pathDocument from report_ReportExecution e"
                    + " where e.pathDocument is not null and e.startTime < :borderDate", URI.class)
                    .setParameter("borderDate", borderDate)
                    .getResultList();
            paths.addAll(ids);

            //todo
            //entityManager.setSoftDeletion(false);
            return entityManager.createQuery("delete from report_ReportExecution e where e.startTime < :borderDate")
                    .setParameter("borderDate", borderDate)
                    .executeUpdate();
        });

        deleteFileAndFiles(paths);
        return deleted;
    }

    private void deleteFileAndFiles(List<URI> paths) {
        if (!paths.isEmpty()) {
            log.debug("Deleting {} output document files", paths.size());
        }

        for (URI path : paths) {
            try {
                localFileStorage.removeFile(path);
            } catch (FileStorageException e) {
                log.error("Failed to remove document from storage " + path, e);
            }
        }
    }

    private int deleteHistoryGroupedByReport() {
        int maxItemsPerReport = reportsProperties.getHistoryCleanupMaxItemsPerReport();
        if (maxItemsPerReport <= 0) {
            return 0;
        }

        List<UUID> allReportIds = dataManager.loadValues("select r.id from report_Report r")
                .properties("id")
                .list()
                .stream()
                .map(kve -> (UUID) kve.getValue("id"))
                .collect(Collectors.toList());

        log.debug("Deleting report executions for every report, older than {}th execution", maxItemsPerReport);
        int total = 0;
        for (UUID reportId : allReportIds) {
            int deleted = deleteForOneReport(reportId, maxItemsPerReport);
            total += deleted;
        }
        return total;
    }

    private int deleteForOneReport(UUID reportId, int maxItemsPerReport) {
        List<URI> paths = new ArrayList<>();
        int deleted = transaction.execute(status -> {
            //em.setSoftDeletion(false);
            int rows = 0;
            Date borderStartTime = entityManager.createQuery(
                    "select e.startTime from report_ReportExecution e"
                            + " where e.report.id = :reportId"
                            + " order by e.startTime desc", Date.class)
                    .setParameter("reportId", reportId)
                    .setFirstResult(maxItemsPerReport)
                    .setMaxResults(1)
                    .getSingleResult();

            if (borderStartTime != null) {
                List<URI> ids = entityManager.createQuery("select e.pathDocument from report_ReportExecution e"
                        + " where e.outputDocument is not null and e.report.id = :reportId and e.startTime <= :borderTime", URI.class)
                        .setParameter("reportId", reportId)
                        .setParameter("borderTime", borderStartTime)
                        .getResultList();
                paths.addAll(ids);

                rows = entityManager.createQuery("delete from report_ReportExecution e"
                        + " where e.report.id = :reportId and e.startTime <= :borderTime")
                        .setParameter("reportId", reportId)
                        .setParameter("borderTime", borderStartTime)
                        .executeUpdate();
            }
            return rows;
        });
        deleteFileAndFiles(paths);
        return deleted;
    }
}
