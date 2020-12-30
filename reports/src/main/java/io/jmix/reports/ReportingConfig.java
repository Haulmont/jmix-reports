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

package io.jmix.reports;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

/**
 * Reporting configuration interface.
 */
@ConfigurationProperties(prefix = "jmix.reports")
@ConstructorBinding
public class ReportingConfig {

    String officePath;

    List<Integer> officePorts;

    int docFormatterTimeout;

    boolean displayDeviceAvailable;

    String pdfFontsDirectory;

    boolean putEmptyRowIfNoDataSelected;

    int parameterPrototypeQueryLimit;

    List<String> wizardEntitiesBlackList;

    List<String> wizardEntitiesWhiteList;

    List<String> wizardPropertiesBlackList;

    List<String> wizardPropertiesExcludedBlackList;

    int entityTreeModelMaxDeep;

    int htmlExternalResourcesTimeoutSec;

    String curlPath;

    String curlParams;

    int curlTimeout;

    boolean useReadOnlyTransactionForGroovy;

    boolean historyRecordingEnabled;

    boolean saveOutputDocumentsToHistory;

    int historyCleanupMaxDays;

    int historyCleanupMaxItemsPerReport;

    int countOfRetry;

    boolean useOfficeForDocumentConversion;

    public ReportingConfig(@DefaultValue("/") String officePath,
                           @DefaultValue("8100,8101,8102,8103") List<Integer> officePorts,
                           @DefaultValue("20") int docFormatterTimeout,
                           @DefaultValue("false") boolean displayDeviceAvailable,
                           String pdfFontsDirectory,
                           @DefaultValue("true") boolean putEmptyRowIfNoDataSelected,
                           @DefaultValue("1000") int parameterPrototypeQueryLimit,
                           @DefaultValue("") List<String> wizardEntitiesBlackList,
                           @DefaultValue("") List<String> wizardEntitiesWhiteList,
                           @DefaultValue("") List<String> wizardPropertiesBlackList,
                           @DefaultValue("") List<String> wizardPropertiesExcludedBlackList,
                           @DefaultValue("3") int entityTreeModelMaxDeep,
                           @DefaultValue("5") int htmlExternalResourcesTimeoutSec,
                           @DefaultValue("curl") String curlPath,
                           @DefaultValue("") String curlParams,
                           @DefaultValue("10") int curlTimeout,
                           @DefaultValue("true") boolean useReadOnlyTransactionForGroovy,
                           @DefaultValue("false") boolean historyRecordingEnabled,
                           @DefaultValue("false") boolean saveOutputDocumentsToHistory,
                           @DefaultValue("730") int historyCleanupMaxDays,
                           @DefaultValue("1000") int historyCleanupMaxItemsPerReport,
                           @DefaultValue("3") int countOfRetry,
                           @DefaultValue("false") boolean useOfficeForDocumentConversion) {
        this.officePath = officePath;
        this.officePorts = officePorts;
        this.docFormatterTimeout = docFormatterTimeout;
        this.displayDeviceAvailable = displayDeviceAvailable;
        this.pdfFontsDirectory = pdfFontsDirectory;
        this.putEmptyRowIfNoDataSelected = putEmptyRowIfNoDataSelected;
        this.parameterPrototypeQueryLimit = parameterPrototypeQueryLimit;
        this.wizardEntitiesBlackList = wizardEntitiesBlackList;
        this.wizardEntitiesWhiteList = wizardEntitiesWhiteList;
        this.wizardPropertiesBlackList = wizardPropertiesBlackList;
        this.wizardPropertiesExcludedBlackList = wizardPropertiesExcludedBlackList;
        this.entityTreeModelMaxDeep = entityTreeModelMaxDeep;
        this.htmlExternalResourcesTimeoutSec = htmlExternalResourcesTimeoutSec;
        this.curlPath = curlPath;
        this.curlParams = curlParams;
        this.curlTimeout = curlTimeout;
        this.useReadOnlyTransactionForGroovy = useReadOnlyTransactionForGroovy;
        this.historyRecordingEnabled = historyRecordingEnabled;
        this.saveOutputDocumentsToHistory = saveOutputDocumentsToHistory;
        this.historyCleanupMaxDays = historyCleanupMaxDays;
        this.historyCleanupMaxItemsPerReport = historyCleanupMaxItemsPerReport;
        this.countOfRetry = countOfRetry;
        this.useOfficeForDocumentConversion = useOfficeForDocumentConversion;
    }

    /**
     * @return Path to the installed OpenOffice
     */
    public String getOfficePath() {
        return officePath;
    }

    /**
     * @return The list of ports to start OpenOffice on.
     */
    public List<Integer> getOfficePorts() {
        return officePorts;
    }

    /**
     * @return Request to OpenOffice timeout in seconds.
     */
    public Integer getDocFormatterTimeout() {
        return docFormatterTimeout;
    }

    /**
     * @return Has to be false if using OpenOffice reporting formatter on a *nix server without X server running
     */
    public boolean getDisplayDeviceAvailable() {
        return displayDeviceAvailable;
    }

    /**
     * @return Directory with fonts for generate PDF from HTML
     */
    public String getPdfFontsDirectory() {
        return pdfFontsDirectory;
    }

    /**
     * @return The option which enforces standard data extractor to put empty row in each band if no data has been selected
     * In summary this option says - would table linked with empty band have at least one empty row or not.
     */
    public Boolean getPutEmptyRowIfNoDataSelected() {
        return putEmptyRowIfNoDataSelected;
    }

    /**
     * @return Default limit used if parameter prototype object does not specify limit itself
     */
    public Integer getParameterPrototypeQueryLimit() {
        return parameterPrototypeQueryLimit;
    }

    /**
     * Return entities that will not be available for report wizard.
     * Note that if {@code reporting.wizardEntitiesWhiteList} is not empty, this list will be ignored
     *
     * @return list of ignored entities
     */
    public List<String> getWizardEntitiesBlackList() {
        return wizardEntitiesBlackList;
    }

    /**
     * Entities that will be available for report wizard. All others entities will be ignored.
     * Note that even if {@code cuba.reporting.wizardEntitiesBlackList} is not empty, this list will be used anyway.
     *
     * @return list of entities that available for reportWizard
     */
    public List<String> getWizardEntitiesWhiteList() {
        return wizardEntitiesWhiteList;
    }

    /**
     * Entity properties that will not be available for report creation wizard. Format is like {@code BaseUuidEntity.id,BaseUuidEntity.createTs,ref$Car.id,...}<br>
     * Properties support inheritance, i.e. {@code BaseUuidEntity.id} will filter that field for all descendants, e.g. {@code ref$Car}.
     * To allow selection of a field for a concrete descendant (e.g. {@code ref$Car}), use
     * {@code reporting.wizardPropertiesExcludedBlackList} setting with value {@code ref$Car.id}.
     *
     * @return blacklisted properties that is not available
     */
    public List<String> getWizardPropertiesBlackList() {
        return wizardPropertiesBlackList;
    }

    /**
     * Entity properties that will not to be excluded by {@code reporting.wizardPropertiesBlackList} setting
     *
     * @see ReportingConfig#getWizardPropertiesBlackList()
     */
    public List<String> getWizardPropertiesExcludedBlackList() {
        return wizardPropertiesExcludedBlackList;
    }

    /**
     * Maximum depth of entity model that is used in report wizard and report dataset view editor.
     */
    public Integer getEntityTreeModelMaxDeep() {
        return entityTreeModelMaxDeep;
    }


    public Integer getHtmlExternalResourcesTimeoutSec() {
        return htmlExternalResourcesTimeoutSec;
    }

    /**
     * Reporting uses CURL tool to generate reports from URL. This is the system path to the tool.
     */
    public String getCurlPath() {
        return curlPath;
    }

    /**
     * Reporting uses CURL tool to generate reports from URL. This the string with parameters used while calling CURL.
     */
    public String getCurlParams() {
        return curlParams;
    }

    public Integer getCurlTimeout() {
        return curlTimeout;
    }

    /**
     * Toggle for Groovy dataset's transactions. If true, transactions are read-only.
     */
    public Boolean getUseReadOnlyTransactionForGroovy() {
        return useReadOnlyTransactionForGroovy;
    }

    /**
     * Flag to enable execution history recording.
     */
    public boolean isHistoryRecordingEnabled() {
        return historyRecordingEnabled;
    }

    /**
     * If enabled - then save all output documents to file storage, so they can be downloaded later.
     * Note that ReportExecution stores file that is independent from the one created by ReportingApi#createAndSaveReport methods.
     */
    public boolean isSaveOutputDocumentsToHistory() {
        return saveOutputDocumentsToHistory;
    }

    /**
     * Report execution history deletes all history items older than this number of days.
     * Value == 0 means no cleanup by this criteria.
     */
    public int getHistoryCleanupMaxDays() {
        return historyCleanupMaxDays;
    }

    /**
     * Report execution cleanup leaves only this number of execution history items for each report,
     * deleting all older items.
     * Value == 0 means no cleanup by this criteria.
     */
    public int getHistoryCleanupMaxItemsPerReport() {
        return historyCleanupMaxItemsPerReport;
    }

    public int getCountOfRetry() {
        return countOfRetry;
    }

    public boolean isUseOfficeForDocumentConversion() {
        return useOfficeForDocumentConversion;
    }
}