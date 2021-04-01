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

package io.jmix.reportsui.screen.report.wizard.step;

import io.jmix.core.MessageTools;
import io.jmix.core.Messages;
import io.jmix.core.Metadata;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.Field;
import io.jmix.ui.component.Validatable;
import io.jmix.ui.component.ValidationException;
import io.jmix.ui.screen.ScreenFragment;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public abstract class StepFragment extends ScreenFragment {

    @Autowired
    protected Messages messages;

    @Autowired
    protected MessageTools messageTools;

    @Autowired
    protected Metadata metadata;

    protected InitStepFrameHandler initFrameHandler;
    protected BeforeHideStepFrameHandler beforeHideFrameHandler;
    protected BeforeShowStepFrameHandler beforeShowFrameHandler;

    protected boolean validateBeforeNext = true;
    protected boolean validateBeforePrev = false;

    protected FrameValidator frameValidator = new FrameValidator();
    protected boolean isInitialized;

    public void setInitFrameHandler(InitStepFrameHandler initFrameHandler) {
        this.initFrameHandler = initFrameHandler;
    }

    public void setBeforeHideFrameHandler(BeforeHideStepFrameHandler beforeHideFrameHandler) {
        this.beforeHideFrameHandler = beforeHideFrameHandler;
    }

    public void setBeforeShowFrameHandler(BeforeShowStepFrameHandler beforeShowFrameHandler) {
        this.beforeShowFrameHandler = beforeShowFrameHandler;
    }

    public void initFrame() {
        if (!isInitialized) {
            doDefaultInit();
            if (initFrameHandler != null) {
                initFrameHandler.initFrame();
            }
            isInitialized = true;
        }

    }

    private void doDefaultInit() {
        new DefaultFrameInitializer().initFrame();
    }

    public List<String> validateFrame() {
        return frameValidator.validateAllComponents();
    }

    public void beforeHide() {
        if (beforeHideFrameHandler == null) {
            return;
        }
        beforeHideFrameHandler.beforeHideFrame();
    }

    public void beforeShow() {
        if (beforeShowFrameHandler == null) {
            return;
        }
        beforeShowFrameHandler.beforeShowFrame();
    }

    public abstract String getCaption();

    public abstract boolean isLast();

    public abstract boolean isFirst();

    public boolean isValidateBeforeNext() {
        return validateBeforeNext;
    }

    public boolean isValidateBeforePrev() {
        return validateBeforePrev;
    }

    public interface BeforeHideStepFrameHandler {
        void beforeHideFrame();
    }

    public interface BeforeShowStepFrameHandler {
        void beforeShowFrame();
    }

    public interface InitStepFrameHandler {
        void initFrame();
    }

    protected class DefaultFrameInitializer implements InitStepFrameHandler {
        @Override
        public void initFrame() {
            for (Component c : getFragment().getComponents()) {
                if (c instanceof Field) {
                    Field field = (Field) c;
                    if (field.isRequired() && StringUtils.isBlank(field.getRequiredMessage()) && StringUtils.isBlank(field.getCaption())) {
                        field.setRequiredMessage(getDefaultRequiredMessage(messages.getMessage(field.getId())));
                    }
                }
            }
        }
    }

    protected class FrameValidator {
        public List<String> validateAllComponents() {
            List<String> errors = new ArrayList<>();
            for (Component c : getFragment().getComponents()) {
                if (c instanceof Validatable) {
                    Validatable validatable = (Validatable) c;
                    try {
                        validatable.validate();
                    } catch (ValidationException e) {
                        errors.add(e.getMessage());
                    }
                }
            }
            return errors;
        }
    }

    protected String getDefaultRequiredMessage(String name) {
        return messages.formatMessage("validation.required.defaultMsg", name);
    }
}