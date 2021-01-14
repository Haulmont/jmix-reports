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

package io.jmix.reportsui.gui.template.edit;

import io.jmix.reports.entity.ReportOutputType;
import io.jmix.reports.entity.ReportTemplate;
import io.jmix.ui.component.BoxLayout;
import io.jmix.ui.component.Fragment;
import io.jmix.ui.screen.ScreenFragment;
import io.jmix.ui.screen.Subscribe;

public abstract class DescriptionEditFragment extends ScreenFragment {

    protected ReportTemplate reportTemplate;
    protected BoxLayout previewBox;
    protected boolean supportPreview;

    @Subscribe
    protected void onInit(InitEvent event) {
        previewBox = (BoxLayout) getFragment().getComponentNN("previewBox");
    }

    public void setItem(ReportTemplate reportTemplate) {
        this.reportTemplate = reportTemplate;
    }

    public ReportTemplate getReportTemplate() {
        return reportTemplate;
    }

    public void showPreview() {
        previewBox.setVisible(true);
        previewBox.setHeight("100%");
        previewBox.setWidth("100%");
        previewBox.removeAll();

        Fragment parent = getFragment();
        parent.setResponsive(true);
        parent.setWidth("1280px");

        initPreviewContent(previewBox);
    }

    public void hidePreview() {
        previewBox.setVisible(false);
        previewBox.removeAll();

        Fragment parent = getFragment();
        parent.setSizeAuto();
        parent.setResponsive(false);
    }

    public void setVisible(boolean visible) {
        getFragment().setVisible(visible);
    }

    public abstract boolean isSupportPreview();

    public abstract boolean isApplicable(ReportOutputType reportOutputType);

    public abstract boolean applyChanges();

    protected abstract void initPreviewContent(BoxLayout previewBox);
}
