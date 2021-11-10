/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.infra.database.metadata.dialect;

import lombok.Getter;
import org.apache.shardingsphere.infra.database.metadata.DataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.MemorizedDataSourceMetaData;
import org.apache.shardingsphere.infra.database.metadata.UnrecognizedDatabaseURLException;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data source meta data for H2.
 */
@Getter
public final class H2DataSourceMetaData implements MemorizedDataSourceMetaData {
    
    private static final int DEFAULT_PORT = -1;
    
    private static final String DEFAULT_HOST_NAME = "";
    
    private final String hostName;
    
    private final int port;
    
    private final String catalog;
    
    private final String schema;
    
    private final Pattern pattern = Pattern.compile("jdbc:h2:((mem|~)[:/](?<catalog>[\\w\\-]+)|"
            + "(ssl:|tcp:)(//)?(?<hostName>[\\w\\-.]+)(:(?<port>[0-9]{1,4})/)?[/~\\w\\-.]+/(?<name>[\\-\\w]*)|"
            + "file:[/~\\w\\-]+/(?<fileName>[\\-\\w]*));?\\S*", Pattern.CASE_INSENSITIVE);
    
    public H2DataSourceMetaData(final String url) {
        Matcher matcher = pattern.matcher(url);
        if (!matcher.find()) {
            throw new UnrecognizedDatabaseURLException(url, pattern.pattern());
        }
        String portFromMatcher = matcher.group("port");
        String catalogFromMatcher = matcher.group("catalog");
        String nameFromMatcher = matcher.group("name");
        String fileNameFromMatcher = matcher.group("fileName");
        String hostNameFromMatcher = matcher.group("hostName");
        boolean setPort = null != portFromMatcher && !portFromMatcher.isEmpty();
        String name = null == nameFromMatcher ? fileNameFromMatcher : nameFromMatcher;
        hostName = null == hostNameFromMatcher ? DEFAULT_HOST_NAME : hostNameFromMatcher;
        port = setPort ? Integer.parseInt(portFromMatcher) : DEFAULT_PORT;
        catalog = null == catalogFromMatcher ? name : catalogFromMatcher;
        schema = null;
    }
    
    @Override
    public boolean isInSameDatabaseInstance(final DataSourceMetaData dataSourceMetaData) {
        return DEFAULT_HOST_NAME.equals(hostName) && DEFAULT_PORT == port ? Objects.equals(schema, dataSourceMetaData.getSchema())
                : MemorizedDataSourceMetaData.super.isInSameDatabaseInstance(dataSourceMetaData);
    }
}
