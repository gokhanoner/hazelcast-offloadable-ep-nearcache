package com.oner.hazelcastoffoadablecachedemo;

import com.hazelcast.config.Config;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.core.Hazelcast;

public class HazelcastServer {
    public static void main(String[] args) {
        Config config = new Config();
        config.getGroupConfig().setName(TestConstants.GROUP_NAME).setPassword(TestConstants.GROUP_PASWD);
        config.getMapConfig(TestConstants.TEST_MAP).setInMemoryFormat(InMemoryFormat.BINARY);
        //config.getManagementCenterConfig().setEnabled(true).setUrl("http://localhost:8080/mancenter/");
        Hazelcast.newHazelcastInstance(config);
    }
}
