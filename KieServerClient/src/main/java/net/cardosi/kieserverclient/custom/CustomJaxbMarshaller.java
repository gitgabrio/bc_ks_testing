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
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.jaxb.JaxbMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomJaxbMarshaller extends JaxbMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(CustomJaxbMarshaller.class);

    public CustomJaxbMarshaller(Set<Class<?>> classes, ClassLoader classLoader) {
        super(classes, classLoader);
    }


    @Override
    protected void buildMarshaller(Set<Class<?>> classes, final ClassLoader classLoader) {
        try {
            super.buildMarshaller(classes, classLoader);
        } catch (Throwable e) {
            logger.error("Error while creating JAXB Marshaller due to {}", e.getMessage(), e);
            throw new MarshallingException("Error while creating JAXB context from default classes! " + e.getMessage(), e);
        }
    }


}