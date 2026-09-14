package org.test;/*
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.drools.core.command.SetActiveAgendaGroup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.api.runtime.ExecutionResults;
import org.kie.kogito.rules.a.Applicant;
import org.kie.kogito.rules.a.LoanApplication;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.RuleServicesClient;

@RunWith(Parameterized.class)
public class TestLoanDRL {

    private static final String CONTAINER_ID = "RULES-JAR-1.0";
    private static final long EXTENDED_TIMEOUT = 300000L;

    private KieServicesClient kieServicesClient;

    @Parameterized.Parameters
    public static Collection<Object> data() {
        Object[] obj = new Object[]{MarshallingFormat.JAXB,
                MarshallingFormat.JSON,
                MarshallingFormat.XSTREAM};
        return Arrays.asList(obj);
    }

    public TestLoanDRL(MarshallingFormat marshallingFormat) {
        kieServicesClient = getKieServicesClient(marshallingFormat);
    }

    @Test
    public void testDrl() {
        KieCommands commandsFactory = KieServices.Factory.get().getCommands();

        RuleServicesClient ruleClient = kieServicesClient.getServicesClient(RuleServicesClient.class);

        List<Command> kiebaseACommands = Arrays.asList(
                commandsFactory.newInsert(new Applicant("#0001", 20), "applicant"),
                commandsFactory.newInsert(new LoanApplication("#0001"), "application"),
                new SetActiveAgendaGroup("applicationGroup"),
                commandsFactory.newFireAllRules());

        final BatchExecutionCommand batchExecutionCommand = commandsFactory.newBatchExecution(kiebaseACommands);
        final ServiceResponse<ExecutionResults> results = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecutionCommand);
        Assertions.assertThat(results.getType()).isEqualTo(KieServiceResponse.ResponseType.SUCCESS);
       /* final PMML4Result pmml4Result = (PMML4Result) results.getResult().getValue("results");
        Assertions.assertThat(pmml4Result).isNotNull();
        Assertions.assertThat(pmml4Result.getResultCode()).isEqualTo("OK");

        Assertions.assertThat(pmml4Result.getResultVariables().get(TARGET_FIELD)).isNotNull();
        Assertions.assertThat(pmml4Result.getResultVariables().get(TARGET_FIELD)).isEqualTo(expectedResult);

        Assertions.assertThat((double) pmml4Result.getResultVariables().get(PROBABILITY_SETOSA_FIELD))
                .isCloseTo(setosaProbability(), TOLERANCE_PERCENTAGE);
        Assertions.assertThat((double) pmml4Result.getResultVariables().get(PROBABILITY_VERSICOLOR_FIELD))
                .isCloseTo(versicolorProbability(), TOLERANCE_PERCENTAGE);
        Assertions.assertThat((double) pmml4Result.getResultVariables().get(PROBABILITY_VIRGINICA_FIELD))
                .isCloseTo(virginicaProbability(), TOLERANCE_PERCENTAGE);*/
    }

    private KieServicesClient getKieServicesClient(MarshallingFormat marshallingFormat) {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration("http://localhost:8080/kie" +
                                                                                                 "-server/services" +
                                                                                                 "/rest/server"
                , "kie-server", "kie-server", EXTENDED_TIMEOUT);
        configuration.setMarshallingFormat(marshallingFormat);
        return KieServicesFactory.newKieServicesClient(configuration);
    }

}
