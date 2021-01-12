/**
 * Copyright (C) 2016 mybatis-sharding-core Project
 *               Author: Administrator
 *               Date: 2016年7月14日
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
package com.zdal.sharding.core.session.factory.impl;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.TransactionIsolationLevel;
import org.springframework.util.Assert;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.zdal.sharding.core.config.MyBatisConfigurationsWrapper;
import com.zdal.sharding.core.session.IClusterSqlSession;
import com.zdal.sharding.core.session.factory.IClusterSqlSessionFactory;
import com.zdal.sharding.core.session.impl.ClusterSqlSessionImpl;

/**
 * Name: ClusterSqlSessionFactoryImpl.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.session.factory.impl.ClusterSqlSessionFactoryImpl.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年7月14日 下午5:24:11
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
public class ClusterSqlSessionFactoryImpl implements IClusterSqlSessionFactory {

	private final List<SqlSessionFactory> sqlSessionFactories;
	private final List<String> shardNames;
	
	private final Map<SqlSessionFactory, Set<String>> sqlSessionFactoryShardNameMap;

	@SuppressWarnings("unused")
	private final Map<SqlSessionFactory, Set<String>> fullSqlSessionFactoryShardNameMap;
	
	private final IClusterSqlSession clusterSqlSession;
	
	private final Configuration configurationsWrapper;
	
	public ClusterSqlSessionFactoryImpl(
			Map<SqlSessionFactory, Set<String>> sessionFactoryShardNameMap
			 ) {

		this.sqlSessionFactories = Lists.newArrayList(sessionFactoryShardNameMap.keySet());
		this.sqlSessionFactoryShardNameMap = Maps.newHashMap();
		this.fullSqlSessionFactoryShardNameMap = sessionFactoryShardNameMap;
		this.shardNames = Lists.newArrayList(Iterables.concat(sessionFactoryShardNameMap.values()));

		Set<String> uniqueShardIds = Sets.newHashSet();
		for (Map.Entry<SqlSessionFactory, Set<String>> entry : sessionFactoryShardNameMap.entrySet()) {
			SqlSessionFactory implementor = entry.getKey();
			Assert.notNull(implementor);
			
			Set<String> shardNameSet = entry.getValue();
			Assert.notNull(shardNameSet);
			Assert.notNull(!shardNameSet.isEmpty());
			
			for (String shardName : shardNameSet) {
				// TODO(tomislav): we should change it so we specify control shard in configuration
		        if(!uniqueShardIds.add(shardName)) {
		        	final String msg = String.format("Cannot have more than one shard with shard id %d.", shardName);
		        	throw new RuntimeException(msg);
		        }
		        if (shardNames.contains(shardName)) {
		        	if (!this.sqlSessionFactoryShardNameMap.containsKey(implementor)) {
		        		this.sqlSessionFactoryShardNameMap.put(implementor, Sets.<String>newHashSet());
		        	}
		        	this.sqlSessionFactoryShardNameMap.get(implementor).add(shardName);
		        }
			}
	    }
		
		this.clusterSqlSession = new ClusterSqlSessionImpl(this);
		
		this.configurationsWrapper = new MyBatisConfigurationsWrapper(sqlSessionFactories.get(0).getConfiguration(), this.getSqlSessionFactories());
		
	}
	
	
	@Override
	public IClusterSqlSession openSession() {
		return this.openSession(false);
	}

	@Override
	public IClusterSqlSession openSession(boolean autoCommit) {
		return this.openSession(ExecutorType.SIMPLE, autoCommit);
	}

	@Override
	public IClusterSqlSession openSession(ExecutorType execType) {
		return this.openSession(execType, false);
	}
	
	@Override
	public IClusterSqlSession openSession(ExecutorType execType, boolean autoCommit) {
		return clusterSqlSession;
	}
	
	@Override
	public IClusterSqlSession openSession(TransactionIsolationLevel level) {
		return this.openSession(ExecutorType.SIMPLE, level);
	}
	
	@Override
	public IClusterSqlSession openSession(ExecutorType execType,
			TransactionIsolationLevel level) {
		return clusterSqlSession;
	}
	
	@Override
	public ClusterSqlSessionImpl openSession(Connection connection) {
		throw new UnsupportedOperationException(
				"Cannot open a sharded session with a user provided connection.");
	}

	@Override
	public ClusterSqlSessionImpl openSession(ExecutorType execType,
			Connection connection) {
		throw new UnsupportedOperationException(
				"Cannot open a sharded session with a user provided connection.");
	}

	@Override
	public Configuration getConfiguration() {
		return this.configurationsWrapper;
	}


	@Override
	public List<SqlSessionFactory> getSqlSessionFactories() {
		return Collections.<SqlSessionFactory> unmodifiableList(sqlSessionFactories);
	}

	
	@Override
	public Map<SqlSessionFactory, Set<String>> getSqlSessionFactoryShardNameMap() {
		return this.sqlSessionFactoryShardNameMap;
	}

}
