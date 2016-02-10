/*
 * Copyright (c) 2008-2016, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.session;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.config.Config;
import com.hazelcast.config.ConfigLoader;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.BuildInfo;
import com.hazelcast.instance.BuildInfoProvider;
import com.hazelcast.instance.GroupProperty;
import com.hazelcast.license.domain.LicenseType;
import com.hazelcast.license.util.LicenseHelper;

import java.io.IOException;

import static com.hazelcast.util.Preconditions.checkNotNull;

/**
 * Utility class for Jetty Session Replication modules.
 */
final class JettySessionUtils {

    static final String DEFAULT_INSTANCE_NAME = "SESSION-REPLICATION-INSTANCE";
    static final String DEFAULT_MAP_NAME = "session-replication-map";

    static final int DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000;
    static final int HOUR_IN_MILLISECONDS = 60 * 60 * 1000;

    private JettySessionUtils() {
    }

    /**
     * Create a Hazelcast client instance to connect an existing cluster
     */
    static HazelcastInstance createHazelcastClientInstance(String configLocation) {
        ClientConfig config;
        try {
            XmlClientConfigBuilder builder;
            if (configLocation == null) {
                builder = new XmlClientConfigBuilder();
            } else {
                builder = new XmlClientConfigBuilder(configLocation);
            }
            config = builder.build();
            String licenseKey = config.getLicenseKey();
            if (licenseKey == null) {
                licenseKey = config.getProperty(GroupProperty.ENTERPRISE_LICENSE_KEY);
            }
            BuildInfo buildInfo = BuildInfoProvider.getBuildInfo();
            LicenseHelper.checkLicenseKey(licenseKey, buildInfo.getVersion(),
                    LicenseType.ENTERPRISE, LicenseType.ENTERPRISE_HD);
        } catch (IOException e) {
            throw new RuntimeException("failed to load config", e);
        }

        checkNotNull(config, "failed to find configLocation: " + configLocation);

        return HazelcastClient.newHazelcastClient(config);
    }

    static HazelcastInstance createHazelcastFullInstance(String configLocation) {
        Config config;
        try {
            if (configLocation == null) {
                config = new XmlConfigBuilder().build();
            } else {
                config = ConfigLoader.load(configLocation);
            }
            String licenseKey = config.getLicenseKey();

            BuildInfo buildInfo = BuildInfoProvider.getBuildInfo();
            LicenseHelper.checkLicenseKey(licenseKey, buildInfo.getVersion(), LicenseType.ENTERPRISE);
        } catch (IOException e) {
            throw new RuntimeException("failed to load config", e);
        }

        checkNotNull(config, "failed to find configLocation: " + configLocation);

        config.setInstanceName(DEFAULT_INSTANCE_NAME);
        return Hazelcast.getOrCreateHazelcastInstance(config);
    }
}
