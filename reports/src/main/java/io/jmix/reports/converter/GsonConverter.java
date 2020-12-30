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

package io.jmix.reports.converter;

import io.jmix.reports.entity.Report;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 */
//todo support versions
@Component("report_gsonConverter")
public class GsonConverter {

    @Autowired
    protected BeanFactory beanFactory;

    public String convertToString(Report report) {
        return new ReportGsonSerializationSupport(beanFactory).convertToString(report);
    }

    public Report convertToReport(String json) {
        return new ReportGsonSerializationSupport(beanFactory).convertToReport(json, Report.class);
    }
}
