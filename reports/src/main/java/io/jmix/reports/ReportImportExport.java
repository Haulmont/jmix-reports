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

package io.jmix.reports;

import io.jmix.reports.entity.Report;
import io.jmix.reports.entity.ReportImportOption;
import io.jmix.reports.entity.ReportImportResult;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;

public interface ReportImportExport {

    byte[] exportReports(Collection<Report> reports);

    Collection<Report> importReports(byte[] zipBytes);

    Collection<Report> importReports(byte[] zipBytes, EnumSet<ReportImportOption> importOptions);

    ReportImportResult importReportsWithResult(byte[] zipBytes, @Nullable EnumSet<ReportImportOption> importOptions);

    Collection<Report> importReportsFromPath(String path) throws IOException;
}