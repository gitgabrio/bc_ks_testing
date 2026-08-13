/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.test;

import java.util.Arrays;
import java.util.Collection;

import org.assertj.core.api.Assertions;
import org.drools.core.command.impl.CommandFactoryServiceImpl;
import org.drools.core.command.runtime.pmml.ApplyPmmlModelCommand;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.api.command.KieCommands;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;

@RunWith(Parameterized.class)
public class TestScorecardPMML {

    private static final String CONTAINER_ID = "PMML-JAR-2.0";
    private static final long EXTENDED_TIMEOUT = 300000L;
    private static final String FILE_NAME = "CompoundNestedPredicateScorecard.pmml";
    private static final String MODEL_NAME = "CompoundNestedPredicateScorecard";
    private static final String TARGET_FIELD = "Score";
    private static final String REASON_CODE1_FIELD = "Reason Code 1";
    private static final String REASON_CODE2_FIELD = "Reason Code 2";


    private double input1 = -7;
    private String input2 = "classC";
    private double score =  -15.5;
    private String reasonCode1 =  "characteristic1ReasonCode";
    private String reasonCode2 =  "characteristic2ReasonCode";

    private KieServicesClient kieServicesClient;

    @Parameterized.Parameters
    public static Collection<Object> data() {
        Object[] obj = new Object[]{MarshallingFormat.JAXB,
                MarshallingFormat.JSON,
                MarshallingFormat.XSTREAM};
        return Arrays.asList(obj);
    }

    public TestScorecardPMML(MarshallingFormat marshallingFormat) {
        kieServicesClient = getKieServicesClient(marshallingFormat);
    }

    @Test
    public void testPmml() {
        KieCommands commandsFactory = KieServices.Factory.get().getCommands();
        final PMMLRequestData request = new PMMLRequestData("123", MODEL_NAME);
        request.setSource(FILE_NAME);
        request.addRequestParam("input1", input1);
        request.addRequestParam("input2", input2);

        RuleServicesClient ruleClient = kieServicesClient.getServicesClient(RuleServicesClient.class);

        final ApplyPmmlModelCommand command = (ApplyPmmlModelCommand) ((CommandFactoryServiceImpl) commandsFactory)
                .newApplyPmmlModel(request);
        final ServiceResponse<ExecutionResults> results = ruleClient.executeCommandsWithResults(CONTAINER_ID, command);
        Assertions.assertThat(results.getType()).isEqualTo(KieServiceResponse.ResponseType.SUCCESS);
        final PMML4Result pmml4Result = (PMML4Result) results.getResult().getValue("results");
        Assertions.assertThat(pmml4Result).isNotNull();
        Assertions.assertThat(pmml4Result.getResultCode()).isEqualTo("OK");

        Assertions.assertThat(pmml4Result.getResultVariables().get(TARGET_FIELD)).isNotNull();
        Assertions.assertThat(pmml4Result.getResultVariables().get(TARGET_FIELD)).isNotNull();
        Assertions.assertThat(pmml4Result.getResultVariables().get(TARGET_FIELD)).isEqualTo(score);
//         TODO: Uncomment when reason codes are implemented
        Assertions.assertThat(pmml4Result.getResultVariables().get(REASON_CODE1_FIELD)).isEqualTo(reasonCode1);
        Assertions.assertThat(pmml4Result.getResultVariables().get(REASON_CODE2_FIELD)).isEqualTo(reasonCode2);
    }

    private KieServicesClient getKieServicesClient(MarshallingFormat marshallingFormat) {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration("http://localhost:8080/kie" +
                                                                                                 "-server/services" +
                                                                                                 "/rest/server"
                , "wbadmin", "wbadmin", EXTENDED_TIMEOUT);
        configuration.setMarshallingFormat(marshallingFormat);
        return KieServicesFactory.newKieServicesClient(configuration);
    }

}
