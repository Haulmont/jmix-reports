/*
 * Copyright 2021 Haulmont.
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

package io.jmix.reportsui.runner;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.reports.runner.ReportRunContext;
import io.jmix.ui.screen.FrameOwner;

import javax.annotation.Nullable;
import java.util.Map;
/**
 * Class stores the information required for report running from UI. The instance of the class may be created using the
 * constructor or using the {@link FluentUiReportRunner} bean.
 * <p/>
 * In addition to options required to run a report, it is possible to control the following ones:
 * <ul>
 *     <li>Show dialog to input the report parameters before a run or not</li>
 *     <li>Execute report generation synchronously or in the background</li>
 * </ul>
 *
 * <br/>
 * Creation examples:
 * <pre>
 *  UiReportRunContext context = new UiReportRunContext(report)
 *                 .addParam("customers", customersList)
 *                 .setOutputNamePattern("Customers")
 *                 .setOriginFrameOwner(screen)
 *                 .setRunInBackgroundMode(RunInBackgroundMode.YES)
 *                 .setShowParametersDialogMode(ShowParametersDialogMode.NO);
 *
 *
 *  UiReportRunContext context = new UiReportRunContext(report)
 *                 .setReportTemplate(template)
 *                 .setOutputType(ReportOutputType.PDF)
 *                 .setParams(paramsMap);
 *
 *  UiReportRunContext context = new UiReportRunContext(report)
 *                 .setOutputNamePattern("Customers")
 *                 .setShowParametersDialogMode(ShowParametersDialogMode.IF_REQUIRED);
 * </pre>
 *
 * @see FluentUiReportRunner
 * @see UiReportRunner
 * @see ShowParametersDialogMode
 * @see RunInBackgroundMode
 */
public class UiReportRunContext {
    private ReportRunContext reportRunContext = new ReportRunContext();

    private FrameOwner originFrameOwner;
    private RunInBackgroundMode runInBackgroundMode;
    private ShowParametersDialogMode showParametersDialogMode;

    public UiReportRunContext() {
    }
    public UiReportRunContext(Report report) {
        this.reportRunContext.setReport(report);
    }

    public ReportRunContext getReportRunContext() {
        return reportRunContext;
    }

    public UiReportRunContext setReportRunContext(ReportRunContext reportRunContext) {
        this.reportRunContext = reportRunContext;
        return this;
    }

    @Nullable
    public FrameOwner getOriginFrameOwner() {
        return originFrameOwner;
    }

    public UiReportRunContext setOriginFrameOwner(FrameOwner originFrameOwner) {
        this.originFrameOwner = originFrameOwner;
        return this;
    }

    @Nullable
    public RunInBackgroundMode getRunInBackgroundMode() {
        return runInBackgroundMode;
    }

    public UiReportRunContext setRunInBackgroundMode(@Nullable RunInBackgroundMode runInBackgroundMode) {
        this.runInBackgroundMode = runInBackgroundMode;
        return this;
    }

    @Nullable
    public ShowParametersDialogMode getShowParametersDialogMode() {
        return showParametersDialogMode;
    }

    public UiReportRunContext setShowParametersDialogMode(@Nullable ShowParametersDialogMode showParametersDialogMode) {
        this.showParametersDialogMode = showParametersDialogMode;
        return this;
    }

    public UiReportRunContext setReport(Report report) {
        this.reportRunContext.setReport(report);
        return this;
    }

    public UiReportRunContext setParams(Map<String, Object> params) {
        this.reportRunContext.setParams(params);
        return this;
    }

    public UiReportRunContext addParam(String alias, Object value) {
        this.reportRunContext.addParam(alias, value);
        return this;
    }


    public UiReportRunContext setReportTemplate(ReportTemplate template) {
        this.reportRunContext.setReportTemplate(template);
        return this;
    }

    public UiReportRunContext setOutputType(ReportOutputType type) {
        this.reportRunContext.setOutputType(type);
        return this;
    }

    public UiReportRunContext setOutputNamePattern(String outputNamePattern) {
        this.reportRunContext.setOutputNamePattern(outputNamePattern);
        return this;
    }

    public Report getReport() {
        return reportRunContext.getReport();
    }

    @Nullable
    public ReportOutputType getOutputType() {
        return reportRunContext.getOutputType();
    }

    public Map<String, Object> getParams() {
        return reportRunContext.getParams();
    }

    @Nullable
    public String getOutputNamePattern() {
        return reportRunContext.getOutputNamePattern();
    }

    @Nullable
    public ReportTemplate getReportTemplate() {
        return reportRunContext.getReportTemplate();
    }

}
