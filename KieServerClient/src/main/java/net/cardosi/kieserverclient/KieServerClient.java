/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.cardosi.kieserverclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.drools.core.command.runtime.pmml.ApplyPmmlModelCommand;
import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.KieServices;
import org.kie.api.command.KieCommands;
import org.kie.api.pmml.PMML4Result;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.runtime.ExecutionResults;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.DMNServicesClient;
import org.kie.server.client.RuleServicesClient;

import static net.cardosi.kieserverclient.MyConfigurationObject.KIE_SERVICES_CLIENT;

public class KieServerClient {

    private static final String DMN_CONTAINER_ID ="DMN-JAR-1.0";
    private static final String PMML_CONTAINER_ID ="PMML-JAR-2.0";
    private static DMNServicesClient DMN_CLIENT = KIE_SERVICES_CLIENT.getServicesClient(DMNServicesClient.class);
    private static RuleServicesClient RULES_CLIENT = KIE_SERVICES_CLIENT.getServicesClient(RuleServicesClient.class);
    private static KieCommands COMMANDS_FACTORY = KieServices.Factory.get().getCommands();

    public static void main(String[] args) {
        System.out.println("Starting KieServerClient");
        getContainers();
        /*disposeAllContainers();
        getContainers();
        KieContainerResource toCreate = getPMMLKieContainerResource();
        createContainer(toCreate);
        getContainers();*/
       // executePMML();
/*        KieContainerResource toCreate = getDMNKieContainerResource();
        createContainer(toCreate);
        getContainers();*/
//        executeDMN();
    }

    private static void executePMML() {
        final PMMLRequestData request = new PMMLRequestData("123", "SimpleScorecardCategorical");
        request.setSource("SimpleScorecardCategorical.pmml");
        Map<String, Object> INPUT_DATA = new HashMap<>();
        INPUT_DATA.put("input1", "classA");
        INPUT_DATA.put("input2", "classB");
        INPUT_DATA.forEach(request::addRequestParam);

        final ApplyPmmlModelCommand command = (ApplyPmmlModelCommand) COMMANDS_FACTORY.newApplyPmmlModel(request);
        final ServiceResponse<ExecutionResults> response = RULES_CLIENT.executeCommandsWithResults(PMML_CONTAINER_ID, command);
        ExecutionResultImpl results = (ExecutionResultImpl) response.getResult();
        Map<String, Object> resultMap = results.getResults();
        PMML4Result retrieved = (PMML4Result) resultMap.get("results");
        System.out.println(retrieved);
    }

    private static void executeDMN() {
        DMNContext dmnContext = DMN_CLIENT.newContext();
        dmnContext.set("name", "John");

        ServiceResponse<DMNResult> evaluateAll = DMN_CLIENT.evaluateAll(DMN_CONTAINER_ID, dmnContext);


        DMNResult dmnResult = evaluateAll.getResult();
        System.out.printf("Result: %s%n", dmnResult);
        DMNContext retrieved = dmnResult.getContext();
        System.out.println(retrieved.get("greet"));
    }

    private static List<KieContainerResource> getContainers() {
        System.out.println("== List existing containers ==");
        // Retrieve list of KIE containers
        List<KieContainerResource> toReturn = KIE_SERVICES_CLIENT.listContainers().getResult().getContainers();
        if (toReturn.isEmpty()) {
            System.out.println("No containers available...");
        } else {
            toReturn.forEach(System.out::println);
        }
        return toReturn;
    }

    private static List<KieContainerResource> disposeAllContainers() {
        System.out.println("== Disposing all containers ==");

        // Retrieve list of KIE containers
        List<KieContainerResource> kieContainers = getContainers();
        if (kieContainers.isEmpty()) {
            return Collections.emptyList();
        }
        List<KieContainerResource> toReturn = new ArrayList<>();
        kieContainers.forEach(toDispose -> {
            KieContainerResource disposed = disposeContainer(toDispose);
            if (disposed != null)  {
                toReturn.add(disposed);
            }
        });
        return toReturn;
    }

    private static KieContainerResource disposeFirstContainer() {
        System.out.println("== Disposing container ==");
        // Retrieve list of KIE containers
        List<KieContainerResource> kieContainers = getContainers();
        if (kieContainers.isEmpty()) {
            return null;
        }
        // Dispose KIE container
        KieContainerResource toDispose = kieContainers.get(0);
        return disposeContainer(toDispose);
    }

    private static KieContainerResource disposeContainer(KieContainerResource toDispose) {
        String containerId = toDispose.getContainerId();
        ServiceResponse<Void> responseDispose = KIE_SERVICES_CLIENT.disposeContainer(containerId);
        if (responseDispose.getType() == KieServiceResponse.ResponseType.FAILURE) {
            System.out.println("Error disposing " + containerId + ". Message: ");
            System.out.println(responseDispose.getMsg());
            return null;
        }
        System.out.println("Success Disposing container " + containerId);
        return toDispose;
    }

    private static void createContainer() {
        System.out.println("== Create containers ==");

        // Dispose KIE container
        KieContainerResource container = disposeFirstContainer();
        if (container == null) {
            return;
        }
        System.out.println("Trying to recreate the container...");
        createContainer(container);
    }
    
    private static void createContainer(KieContainerResource toCreate) {
        System.out.println("== Create container ==");
        String containerId  = toCreate.getContainerId();
        ServiceResponse<KieContainerResource> createResponse = KIE_SERVICES_CLIENT.createContainer(containerId, toCreate);
        if(createResponse.getType() == KieServiceResponse.ResponseType.FAILURE) {
            System.out.println("Error creating " + containerId + ". Message: ");
            System.out.println(createResponse.getMsg());
        }
        System.out.println("Container created with success!");
    }

    private static KieContainerResource getDMNKieContainerResource() {
        System.out.println("== Instantiate KieContainerResource ==");
        KieContainerResource toReturn = new KieContainerResource();
        toReturn.setContainerId(DMN_CONTAINER_ID);
        ReleaseId releaseId = new ReleaseId();
        releaseId.setGroupId("org.kie");
        releaseId.setArtifactId("dmnjar");
        releaseId.setVersion("1.0");
        toReturn.setReleaseId(releaseId);
        return toReturn;
    }

    private static KieContainerResource getPMMLKieContainerResource() {
        System.out.println("== Instantiate KieContainerResource ==");
        KieContainerResource toReturn = new KieContainerResource();
        toReturn.setContainerId(PMML_CONTAINER_ID);
        ReleaseId releaseId = new ReleaseId();
        releaseId.setGroupId("org.kie");
        releaseId.setArtifactId("pmmljar");
        releaseId.setVersion("2.0");
        toReturn.setReleaseId(releaseId);
        return toReturn;
    }
}   