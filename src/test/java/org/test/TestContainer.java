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

import org.junit.Assume;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;


public class TestContainer {

    private static final String CONTAINER_ID = "PMML-JAR-2.0";
    private static final ReleaseId RELEASE_ID = new ReleaseId("org.kie", "pmmljar", "2.0");
    private static final KieServerConfigItem CONFIG_ITEM = new KieServerConfigItem(KieServerConstants.KIE_DROOLS_FILTER_REMOTEABLE_CLASSES, "true", Boolean.class.getName());

    private static final long EXTENDED_TIMEOUT = 300000L;

    @Test
    public void createContainer() {
        KieServicesClient client = getKieServicesClient();
        KieContainerResource containerResource = new KieContainerResource(CONTAINER_ID, RELEASE_ID);
        containerResource.setConfigItems(Arrays.asList(CONFIG_ITEM));
        ServiceResponse<KieContainerResource> reply = client.createContainer(CONTAINER_ID, containerResource);
        Assume.assumeTrue(reply.getType().equals(ServiceResponse.ResponseType.SUCCESS));
    }

    public static KieServicesClient getKieServicesClient() {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(
                "http://localhost:8080/kie-server/services/rest/server",
                "wbadmin", "wbadmin", EXTENDED_TIMEOUT);
        configuration.setMarshallingFormat(MarshallingFormat.JSON);

        KieServicesClient kieServicesClient = KieServicesFactory.newKieServicesClient(configuration);
        return kieServicesClient;
    }

}
