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
package com.zdal.sharding.core.config.impl;

import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.springframework.core.io.Resource;
import com.google.common.collect.Lists;
import com.zdal.sharding.core.config.ClusterConfiguration;

/**
 * Name: ClusterConfigurationImpl.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.config.impl.ClusterConfigurationImpl.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年7月18日 上午11:23:44
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
public class ClusterConfigurationImpl implements ClusterConfiguration {
	
	private String shardName;
	
	private List<String> shardInfos;
	
	private DataSource shardDataSource;
	
	private SqlSessionFactory sqlSessionFactory;
	
	private Resource configLocation;

	private Resource[] mapperLocations;

	private Properties configurationProperties;

	private boolean failFast;
	
	private Interceptor[] plugins;

	private TypeHandler<?>[] typeHandlers;

	private String typeHandlersPackage;

	private Class<?>[] typeAliases;

	private String typeAliasesPackage;
	
	
	//constructor
	public ClusterConfigurationImpl(){
	}
	
	/**
	 * 物理分区和逻辑分区一对一
	 * @param shardId	分区Id
	 * @param dataSource	数据源
	 * @param sqlSessionFactory	 mybaits中的{@link SqlSessionFactory}
	 */
	public ClusterConfigurationImpl(String shardName, DataSource dataSource, SqlSessionFactory sqlSessionFactory){
		this(shardName, Lists.newArrayList(shardName), dataSource, sqlSessionFactory);
	}
	
	public ClusterConfigurationImpl(String shardId, List<String> shardNames, DataSource dataSource, SqlSessionFactory sqlSessionFactory){
		this.shardName = shardId;
		this.shardDataSource = dataSource;
		this.sqlSessionFactory = sqlSessionFactory;
		this.shardInfos = shardNames;
	}


	/**
	 * @return the shardInfos
	 */
	public List<String> getShardInfos() {
		return shardInfos;
	}

	/**
	 * @param shardInfos the shardInfos to set
	 */
	public void setShardInfos(List<String> shardInfos) {
		this.shardInfos = shardInfos;
	}

	/**
	 * @return the sqlSessionFactory
	 */
	public SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}

	/**
	 * @param sqlSessionFactory the sqlSessionFactory to set
	 */
	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}

	/**
	 * @return the configLocation
	 */
	public Resource getConfigLocation() {
		return configLocation;
	}

	/**
	 * @param configLocation the configLocation to set
	 */
	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	/**
	 * @return the mapperLocations
	 */
	public Resource[] getMapperLocations() {
		return mapperLocations;
	}

	/**
	 * @param mapperLocations the mapperLocations to set
	 */
	public void setMapperLocations(Resource[] mapperLocations) {
		this.mapperLocations = mapperLocations;
	}

	/**
	 * @return the configurationProperties
	 */
	public Properties getConfigurationProperties() {
		return configurationProperties;
	}

	/**
	 * @param configurationProperties the configurationProperties to set
	 */
	public void setConfigurationProperties(Properties configurationProperties) {
		this.configurationProperties = configurationProperties;
	}

	/**
	 * @return the failFast
	 */
	public boolean isFailFast() {
		return failFast;
	}

	/**
	 * @param failFast the failFast to set
	 */
	public void setFailFast(boolean failFast) {
		this.failFast = failFast;
	}

	/**
	 * @return the plugins
	 */
	public Interceptor[] getPlugins() {
		return plugins;
	}

	/**
	 * @param plugins the plugins to set
	 */
	public void setPlugins(Interceptor[] plugins) {
		this.plugins = plugins;
	}

	/**
	 * @return the typeHandlers
	 */
	public TypeHandler<?>[] getTypeHandlers() {
		return typeHandlers;
	}

	/**
	 * @param typeHandlers the typeHandlers to set
	 */
	public void setTypeHandlers(TypeHandler<?>[] typeHandlers) {
		this.typeHandlers = typeHandlers;
	}

	/**
	 * @return the typeHandlersPackage
	 */
	public String getTypeHandlersPackage() {
		return typeHandlersPackage;
	}

	/**
	 * @param typeHandlersPackage the typeHandlersPackage to set
	 */
	public void setTypeHandlersPackage(String typeHandlersPackage) {
		this.typeHandlersPackage = typeHandlersPackage;
	}

	/**
	 * @return the typeAliases
	 */
	public Class<?>[] getTypeAliases() {
		return typeAliases;
	}

	/**
	 * @param typeAliases the typeAliases to set
	 */
	public void setTypeAliases(Class<?>[] typeAliases) {
		this.typeAliases = typeAliases;
	}

	/**
	 * @return the typeAliasesPackage
	 */
	public String getTypeAliasesPackage() {
		return typeAliasesPackage;
	}

	/**
	 * @param typeAliasesPackage the typeAliasesPackage to set
	 */
	public void setTypeAliasesPackage(String typeAliasesPackage) {
		this.typeAliasesPackage = typeAliasesPackage;
	}

	/**
	 * @return the shardName
	 */
	public String getShardName() {
		return shardName;
	}

	/**
	 * @param shardName the shardName to set
	 */
	public void setShardName(String shardName) {
		this.shardName = shardName;
	}

	/**
	 * @return the shardDataSource
	 */
	public DataSource getShardDataSource() {
		return shardDataSource;
	}

	/**
	 * @param shardDataSource the shardDataSource to set
	 */
	public void setShardDataSource(DataSource shardDataSource) {
		this.shardDataSource = shardDataSource;
	}

	
	

}
