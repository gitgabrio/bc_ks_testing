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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Set;
import org.kie.server.api.marshalling.json.JSONMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomJSONMarshaller extends JSONMarshaller {

    private static final Logger logger = LoggerFactory.getLogger(CustomJSONMarshaller.class);

    public CustomJSONMarshaller(boolean formatDate) {
        super(formatDate);
    }

    public CustomJSONMarshaller(Set<Class<?>> classes, ClassLoader classLoader) {
        super(classes, classLoader);
    }

    public CustomJSONMarshaller(Set<Class<?>> classes, ClassLoader classLoader, boolean formatDate) {
        super(classes, classLoader, formatDate);
    }

    public CustomJSONMarshaller(Set<Class<?>> classes, ClassLoader classLoader, boolean formatDate, boolean useStrictJavaBeans) {
        super(classes, classLoader, formatDate, useStrictJavaBeans);
    }

    @Override
    protected void configureMarshaller(Set<Class<?>> classes, final ClassLoader classLoader) {
        super.configureMarshaller(classes, classLoader);
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    }

}