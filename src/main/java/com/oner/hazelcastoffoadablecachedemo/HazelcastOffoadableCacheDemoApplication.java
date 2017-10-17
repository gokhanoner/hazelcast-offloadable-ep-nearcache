package com.oner.hazelcastoffoadablecachedemo;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.config.NearCacheConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Offloadable;
import com.hazelcast.core.ReadOnly;
import com.hazelcast.map.EntryBackupProcessor;
import com.hazelcast.map.EntryProcessor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class HazelcastOffoadableCacheDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HazelcastOffoadableCacheDemoApplication.class, args);
	}

	@Bean
	ClientConfig clientConfig() {
		ClientConfig clientConfig = new ClientConfig();
		clientConfig.getGroupConfig().setName(TestConstants.GROUP_NAME).setPassword(TestConstants.GROUP_PASWD);

		NearCacheConfig nearCacheConfig = new NearCacheConfig();
		nearCacheConfig.setMaxSize(10000);
		nearCacheConfig.setInvalidateOnChange(true);
		nearCacheConfig.setTimeToLiveSeconds(6000);
		Map<String, NearCacheConfig> hcm = new HashMap<>();
		hcm.put(TestConstants.TEST_MAP_CACHE, nearCacheConfig);
		clientConfig.setNearCacheConfigMap(hcm);
		return clientConfig;
	}

	@Bean
	HazelcastInstance hazelcastInstance(ClientConfig clientConfig) {
		return HazelcastClient.newHazelcastClient(clientConfig);
	}

	@RestController
	@RequestMapping(value = "/data/hazelcast")
	static class MyRestController {
		private final HzService hzService;

		@Autowired
		MyRestController(HzService hzService) {
			this.hzService = hzService;
		}

		@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = {"application/json"})
		@ResponseStatus(HttpStatus.OK)
		public @ResponseBody MyDataDao getData(@PathVariable("id") Integer id) throws Exception {
			return hzService.getData(id);
		}
	}


	@Service
	static class HzService {
		private final HazelcastInstance hz;

		@Autowired
		HzService(HazelcastInstance hz) {
			this.hz = hz;
		}

		@Cacheable(value = TestConstants.TEST_MAP_CACHE)
		public MyDataDao getData(Integer id) {
			return (MyDataDao) hz.getMap(TestConstants.TEST_MAP).executeOnKey(id, new MyEP());
		}

		@CacheEvict(TestConstants.TEST_MAP_CACHE)
		public void removeData(Integer id) {
			hz.getMap(TestConstants.TEST_MAP).executeOnKey(id, new MyEP());
			hz.getPartitionService().getPartition(id).getOwner().getAddress();
		}

	}

	static class MyEP implements EntryProcessor<Integer, MyData>, Offloadable, ReadOnly {

		@Override
		public Object process(Map.Entry<Integer, MyData> entry) {
			MyData value = entry.getValue();
			Integer key = entry.getKey();
			key++;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				/*null*/
			}
			return value == null ? null : new MyDataDao(value.getName() + " " + value.getSurname(), value.getAge());
		}

		@Override
		public EntryBackupProcessor<Integer, MyData> getBackupProcessor() {
			return null;
		}

		@Override
		public String getExecutorName() {
			return OFFLOADABLE_EXECUTOR;
		}
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class MyData implements Serializable {
		private String name;
		private String surname;
		private String abc;
		private int age;
		private Date birthdate;
	}

	enum Modes {
		SYNC,
		ASYNC;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	static class MyDataDao implements Serializable {
		private String fullname;
		private int age;
	}
}
