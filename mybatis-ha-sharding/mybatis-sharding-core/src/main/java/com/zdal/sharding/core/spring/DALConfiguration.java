/**
 * Copyright (C) 2016 mybatis-sharding-core Project
 *               Author: Administrator
 *               Date: 2016年7月18日
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
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
package com.zdal.sharding.core.spring;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.session.SqlSessionFactory;

import com.zdal.sharding.core.config.ClusterConfiguration;
import com.zdal.sharding.core.config.impl.ClusterConfigurationImpl;
import com.zdal.sharding.core.session.factory.impl.ClusterSqlSessionFactoryImpl;

/**
 * Name: DALConfiguration.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.spring.DALConfiguration.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年7月18日 上午11:44:04
 *
 * Update-User: @author
 * Update-Time:
 * Update-Remark:
 * 
 * Check-User:
 * Check-Time:
 * Check-Remark:
 * 
 * Company: 
 * Copyright: 
 */
public class DALConfiguration {
	
	private List<ClusterConfiguration> configs;
	
	public DALConfiguration(List<ClusterConfiguration> shardConfigs
			) {

		configs = shardConfigs;
	}
	
	
	public ClusterSqlSessionFactoryImpl buildShardedSessionFactory() {
		Map<SqlSessionFactory, Set<String>> sqlSessionFactories = new HashMap<SqlSessionFactory, Set<String>>();

		for (ClusterConfiguration config : configs) {
			String shardName = config.getShardName();
			if (shardName == null) {
				final String msg = "Attempt to build a ShardedSessionFactory using a "
						+ "ShardConfiguration that has a null shard id.";
				throw new NullPointerException(msg);
			}
			Set<String> shardNames;
			shardNames = Collections.singleton(shardName);
			sqlSessionFactories.put(config.getSqlSessionFactory(), shardNames);
		}
		return new ClusterSqlSessionFactoryImpl(sqlSessionFactories);
	}

}
