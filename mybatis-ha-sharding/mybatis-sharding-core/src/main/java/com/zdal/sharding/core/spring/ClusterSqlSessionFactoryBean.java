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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import com.zdal.sharding.core.config.ClusterConfiguration;
import com.zdal.sharding.core.config.impl.ClusterConfigurationImpl;
import com.zdal.sharding.core.session.factory.IClusterSqlSessionFactory;
import com.zdal.sharding.core.utils.StringUtil;

/**
 * Name: ClusterSqlSessionFactoryBean.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.spring.ClusterSqlSessionFactoryBean.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年7月18日 上午11:18:44
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
public class ClusterSqlSessionFactoryBean implements FactoryBean<IClusterSqlSessionFactory>, InitializingBean {
	
	private Resource configLocation;

	private Resource[] mapperLocations;

	private Map<String, DataSource> dataSources;
	
	private Properties configurationProperties;

	private String environment = IClusterSqlSessionFactory.class.getSimpleName();

	private Interceptor[] plugins;

	private TypeHandler<?>[] typeHandlers;

	private String typeHandlersPackage;

	private Class<?>[] typeAliases;

	private String typeAliasesPackage;
	
	private IClusterSqlSessionFactory clusterSqlSessionFactory;
	
	//直接配置ShardConfiguration
	private List<ClusterConfigurationImpl> clusterConfigurationImpls;

	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		List<ClusterConfiguration> shardConfigs = new ArrayList<ClusterConfiguration>();
		
		if(CollectionUtils.isEmpty(clusterConfigurationImpls)){
			for(Map.Entry<String, DataSource> entry : dataSources.entrySet()){
				String shardId = entry.getKey();	//虚拟分区ID
				DataSource dataSource = entry.getValue();	//虚拟分区所属数据源
				
				SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
				factoryBean.setConfigLocation(this.configLocation);
				factoryBean.setMapperLocations(this.mapperLocations);
				factoryBean.setDataSource(dataSource);
				factoryBean.setEnvironment(this.environment);
				factoryBean.setConfigurationProperties(this.configurationProperties);
				factoryBean.setPlugins(this.plugins);
				factoryBean.setTypeHandlers(this.typeHandlers);
				factoryBean.setTypeHandlersPackage(this.typeHandlersPackage);
				factoryBean.setTypeAliases(this.typeAliases);
				factoryBean.setTypeAliasesPackage(this.typeAliasesPackage);
				
				SqlSessionFactory sessionFacotry = factoryBean.getObject();
				
				shardConfigs.add(new ClusterConfigurationImpl(shardId, dataSource, sessionFacotry));
			}
		}else {
			for(ClusterConfigurationImpl shardConfiguration : clusterConfigurationImpls){
				

				if(shardConfiguration.getConfigLocation() == null) {
					shardConfiguration.setConfigLocation(this.configLocation);
				}

				if(shardConfiguration.getMapperLocations() == null || shardConfiguration.getMapperLocations().length == 0) {
					shardConfiguration.setMapperLocations(this.mapperLocations);
				}

				SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
				factoryBean.setConfigLocation(shardConfiguration.getConfigLocation());
				factoryBean.setMapperLocations(shardConfiguration.getMapperLocations());

				factoryBean.setDataSource(shardConfiguration.getShardDataSource());
				factoryBean.setEnvironment(this.environment);
				factoryBean.setConfigurationProperties(this.configurationProperties);
				factoryBean.setPlugins(this.plugins);
				factoryBean.setTypeHandlers(this.typeHandlers);

				if(StringUtil.isEmptyOrNullOrStringNull(shardConfiguration.getTypeHandlersPackage())) {
					shardConfiguration.setTypeHandlersPackage(this.typeHandlersPackage);
				}

				factoryBean.setTypeHandlersPackage(shardConfiguration.getTypeHandlersPackage());
				factoryBean.setTypeAliases(this.typeAliases);

				if(StringUtil.isEmptyOrNullOrStringNull(shardConfiguration.getTypeAliasesPackage())) {
					shardConfiguration.setTypeAliasesPackage(this.typeAliasesPackage);
				}

				factoryBean.setTypeAliasesPackage(shardConfiguration.getTypeAliasesPackage());

				SqlSessionFactory sessionFacotry = factoryBean.getObject();
				shardConfiguration.setSqlSessionFactory(sessionFacotry);

				shardConfigs.add(shardConfiguration);
			}
			
		}
		
		DALConfiguration configuration = new DALConfiguration(shardConfigs);
		clusterSqlSessionFactory = configuration.buildShardedSessionFactory();

	}

	@Override
	public IClusterSqlSessionFactory getObject() throws Exception {
		if( this.clusterSqlSessionFactory == null )
			this.afterPropertiesSet();
		return this.clusterSqlSessionFactory;
	}

	@Override
	public Class<?> getObjectType() {
		return this.clusterSqlSessionFactory == null ? IClusterSqlSessionFactory.class : this.clusterSqlSessionFactory.getClass();
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public Resource getConfigLocation() {
		return configLocation;
	}

	public void setConfigLocation(Resource configLocation) {
		this.configLocation = configLocation;
	}

	public Resource[] getMapperLocations() {
		return mapperLocations;
	}

	public void setMapperLocations(Resource[] mapperLocations) {
		this.mapperLocations = mapperLocations;
	}

	public Map<String, DataSource> getDataSources() {
		return dataSources;
	}

	public void setDataSources(Map<String, DataSource> dataSources) {
		this.dataSources = dataSources;
	}

	public Properties getConfigurationProperties() {
		return configurationProperties;
	}

	public void setConfigurationProperties(Properties configurationProperties) {
		this.configurationProperties = configurationProperties;
	}

	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

	public Interceptor[] getPlugins() {
		return plugins;
	}

	public void setPlugins(Interceptor[] plugins) {
		this.plugins = plugins;
	}

	public TypeHandler<?>[] getTypeHandlers() {
		return typeHandlers;
	}

	public void setTypeHandlers(TypeHandler<?>[] typeHandlers) {
		this.typeHandlers = typeHandlers;
	}

	public String getTypeHandlersPackage() {
		return typeHandlersPackage;
	}

	public void setTypeHandlersPackage(String typeHandlersPackage) {
		this.typeHandlersPackage = typeHandlersPackage;
	}

	public Class<?>[] getTypeAliases() {
		return typeAliases;
	}

	public void setTypeAliases(Class<?>[] typeAliases) {
		this.typeAliases = typeAliases;
	}

	public String getTypeAliasesPackage() {
		return typeAliasesPackage;
	}

	public void setTypeAliasesPackage(String typeAliasesPackage) {
		this.typeAliasesPackage = typeAliasesPackage;
	}

	public IClusterSqlSessionFactory getClusterSqlSessionFactory() {
		return clusterSqlSessionFactory;
	}
	public void setClusterSqlSessionFactory(IClusterSqlSessionFactory clusterSqlSessionFactory) {
		this.clusterSqlSessionFactory = clusterSqlSessionFactory;
	}

	public List<ClusterConfigurationImpl> getClusterConfigurationImpls() {
		return clusterConfigurationImpls;
	}

	public void setClusterConfigurationImpls(List<ClusterConfigurationImpl> clusterConfigurationImpls) {
		this.clusterConfigurationImpls = clusterConfigurationImpls;
	}
	
	
	

}
