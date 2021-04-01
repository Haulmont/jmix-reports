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

import io.jmix.ui.component.Button;
import io.jmix.ui.screen.Screen;

public interface MainWizardScreen<T extends Screen> {

    T getMainWizardFrame();

    Button getBackwardBtn();

    Button getForwardBtn();

    void removeBtns();

    void addForwardBtn();

    void addBackwardBtn();

    void addSaveBtn();
}
