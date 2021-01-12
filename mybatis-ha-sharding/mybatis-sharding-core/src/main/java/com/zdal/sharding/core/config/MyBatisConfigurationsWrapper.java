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
package com.zdal.sharding.core.config;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * Name: MyBatisConfigurationsWrapper.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.config.MyBatisConfigurationsWrapper.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年7月14日 下午6:15:42
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
public class MyBatisConfigurationsWrapper extends Configuration {
	
	private final Configuration configuration;

	private final List<SqlSessionFactory> sqlSessionFactories;

	public MyBatisConfigurationsWrapper(Configuration configuration,
			List<SqlSessionFactory> sqlSessionFactories) {
		this.configuration = configuration;
		this.sqlSessionFactories = sqlSessionFactories;
	}

	@Override
	public Environment getEnvironment() {
		return configuration.getEnvironment();
	}

	@Override
	public ExecutorType getDefaultExecutorType() {
		return configuration.getDefaultExecutorType();
	}

	@Override
	public ObjectFactory getObjectFactory() {
		return configuration.getObjectFactory();
	}

	@Override
	public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
		for (SqlSessionFactory sqlSessionFactory : getSqlSessionFactories()) {
			T mapper = null;
			try {
				mapper = sqlSessionFactory.getConfiguration().getMapper(type, sqlSession);
			} catch (BindingException e) {
				// ignore exception
			}

			if (mapper != null) {
				return mapper;
			}
		}

		throw new BindingException("Type " + type + " is not known to the MapperRegistry.");

	}

	@Override
	public boolean hasMapper(Class<?> type) {
		for (SqlSessionFactory factory : getSqlSessionFactories()) {
			if (factory.getConfiguration().hasMapper(type)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public MappedStatement getMappedStatement(String id) {
		Exception exception = null;
		MappedStatement mappedStatement = null;
		for (SqlSessionFactory sqlSessionFactory : getSqlSessionFactories()) {
			try {
				mappedStatement = sqlSessionFactory.getConfiguration().getMappedStatement(id);
			} catch (Exception e) {
				// ignore exception
				exception = e;
			}

			if (mappedStatement != null) {
				return mappedStatement;
			}
		}

		throw new BindingException("Invalid bound statement (not found): " + id, exception);
	}

	@Override
	public boolean hasStatement(String statementName, boolean validateIncompleteStatements) {

		if (validateIncompleteStatements) {
			buildAllStatements();
		}

		for (SqlSessionFactory sqlSessionFactory : getSqlSessionFactories()) {
			try {
				boolean has = sqlSessionFactory.getConfiguration().getMappedStatementNames().contains(statementName);

				if(has) {
					return true;
				}
			} catch (Exception e) {
				// ignore exception
			}
		}

		return false;

	}

	@Override
	protected void buildAllStatements() {
		for (SqlSessionFactory sqlSessionFactory : getSqlSessionFactories()) {
			try {
				Configuration configuration = sqlSessionFactory.getConfiguration();

				Method m = Configuration.class.getDeclaredMethod("buildAllStatements");
				m.setAccessible(true);
				m.invoke(configuration);
			} catch (Exception e) {
				// ignore exception
			}
		}


	}

	private List<SqlSessionFactory> getSqlSessionFactories() {
		return sqlSessionFactories;
	}

}
