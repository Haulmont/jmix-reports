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

package io.jmix.reportsui.screen.report.validators;

import io.jmix.core.DevelopmentException;
import io.jmix.reports.entity.ReportInputParameter;
import io.jmix.reports.exception.ReportParametersValidationException;
import io.jmix.reportsui.screen.ReportParameterValidator;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.component.validation.AbstractValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;

@Component("report_ReportParamFieldValidator")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class ReportParamFieldValidator extends AbstractValidator<Object> {

    @Autowired
    protected ReportParameterValidator reportParameterValidator;

    private final ReportInputParameter inputParameter;

    public ReportParamFieldValidator(ReportInputParameter inputParameter) {
        if (inputParameter == null) {
            throw new DevelopmentException("ReportInputParameter is not defined");
        }

        this.inputParameter = inputParameter;
    }

    @Override
    public void accept(@Nullable Object o) {
        if (o != null) {
            try {
                reportParameterValidator.validateParameterValue(inputParameter, o);
            } catch (ReportParametersValidationException e) {
                throw new ValidationException(e.getMessage());
            }
        }
    }
}
