package com.oner.hazelcastoffoadablecachedemo;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.Date;
import java.util.Random;

public class HazelcastDataClient {
    private static HazelcastInstance hz;
    public static void main(String[] args) {
        ClientConfig config = new ClientConfig();
        config.getGroupConfig().setName(TestConstants.GROUP_NAME).setPassword(TestConstants.GROUP_PASWD);
        hz = HazelcastClient.newHazelcastClient(config);
        init();
        hz.shutdown();
    }

    private static void init() {
        Random random = new Random();
        IMap<Integer, HazelcastOffoadableCacheDemoApplication.MyData> testMap = hz.getMap(TestConstants.TEST_MAP);
        for (int i = 0; i < TestConstants.TEST_MAP_DATA; i++) {
            HazelcastOffoadableCacheDemoApplication.MyData data = new HazelcastOffoadableCacheDemoApplication.MyData(
                    RandomStringUtils.randomAlphabetic(1024),
                    RandomStringUtils.randomAlphabetic(2048),
                    RandomStringUtils.randomAlphabetic(2048),
                    random.nextInt(100),
                    new Date(random.nextLong()));
            testMap.put(i, data);
        }
        System.out.println("data size:" + testMap.size());
    }

}
