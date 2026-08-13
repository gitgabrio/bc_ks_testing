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
package net.cardosi.kieserverclient.custom;

import java.util.Set;
import org.kie.server.api.marshalling.BaseMarshallerBuilder;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomMarshallerBuilder extends BaseMarshallerBuilder {

    private static final Logger logger = LoggerFactory.getLogger(CustomMarshallerBuilder.class);

    @Override
    public Marshaller build(Set<Class<?>> classes, MarshallingFormat format, ClassLoader classLoader) {
        switch (format) {
            case JSON:
                logger.debug("About to build default instance of JSON marshaller with classes {} and class loader {}", classes, classLoader);
                return new CustomJSONMarshaller(classes, classLoader);
            case JAXB:
                logger.debug("About to build default instance of JAXB marshaller with classes {} and class loader {}", classes, classLoader);
                return new CustomJaxbMarshaller(classes, classLoader);
            case XSTREAM:
            default:
                super.build(classes, format, classLoader);
        }
        return null;
    }

}