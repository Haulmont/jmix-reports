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

package io.jmix.reports.libintegration;

//import com.haulmont.cuba.core.Persistence;
//import com.haulmont.cuba.core.Transaction;
//import com.haulmont.cuba.core.TransactionParams;
//import com.haulmont.cuba.core.global.Security;
//import com.haulmont.cuba.core.global.UserSessionSource;

import com.haulmont.yarg.exception.ValidationException;
import com.haulmont.yarg.structure.BandData;
import com.haulmont.yarg.structure.ReportQuery;
import io.jmix.core.DataManager;
import io.jmix.core.Metadata;
import io.jmix.core.TimeSource;
import io.jmix.core.session.SessionData;
import io.jmix.reports.ReportingConfig;
import org.codehaus.groovy.runtime.MethodClosure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component(GroovyScriptParametersProvider.NAME)
public class CubaGroovyScriptParametersProvider implements GroovyScriptParametersProvider {

    @Autowired
    protected ReportingConfig reportingConfig;

//    @Autowired
//    protected SessionData sessionData;

//    @Autowired
//    protected Persistence persistence;

    @Autowired
    protected Metadata metadata;

    @Autowired
    protected DataManager dataManager;

//    @Autowired
//    protected UserSessionSource sessionSource;
//
//    @Autowired
//    protected Security security;

    @Autowired
    protected TimeSource timeSource;

    @Override
    public Map<String, Object> prepareParameters(ReportQuery reportQuery, BandData parentBand, Map<String, Object> reportParameters) {

        Map<String, Object> scriptParams = new HashMap<>();
        scriptParams.put("reportQuery", reportQuery);
        scriptParams.put("parentBand", parentBand);
        scriptParams.put("params", reportParameters);
//        scriptParams.put("persistence", persistence);
        scriptParams.put("metadata", metadata);
        scriptParams.put("dataManager", dataManager);
//        scriptParams.put("security", security);
        scriptParams.put("timeSource", timeSource);
//        scriptParams.put("userSession", sessionData.getHttpSession());
//        scriptParams.put("sessionData", sessionData);
//        scriptParams.put("transactional", new MethodClosure(this, "transactional"));
        scriptParams.put("validationException", new MethodClosure(this, "validationException"));

        return scriptParams;
    }

    protected void validationException(String message) {
        throw new ValidationException(message);
    }

//    protected void transactional(Closure closure) {
//        Transaction tx;
//        if (!persistence.isInTransaction() && reportingConfig.getUseReadOnlyTransactionForGroovy()) {
//            tx = persistence.createTransaction(new TransactionParams().setReadOnly(true));
//        } else {
//            tx = persistence.getTransaction();
//        }
//        try {
//            closure.call(persistence.getEntityManager());
//            tx.commit();
//        } finally {
//            tx.end();
//        }
//    }
}
