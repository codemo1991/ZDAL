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
package com.zdal.sharding.core.shard.impl;

import static org.apache.ibatis.reflection.ExceptionUtil.unwrapThrowable;
import static org.mybatis.spring.SqlSessionUtils.closeSqlSession;
import static org.mybatis.spring.SqlSessionUtils.isSqlSessionTransactional;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Set;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.MyBatisExceptionTranslator;
import org.mybatis.spring.SqlSessionUtils;
import org.springframework.dao.support.PersistenceExceptionTranslator;

import com.zdal.sharding.core.shard.IShardInfo;

/**
 * Name: IShardInfoImpl.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.shard.impl.IShardInfoImpl.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年7月14日 下午5:00:26
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
public class ShardInfoImpl implements IShardInfo {
	
	private final SqlSessionFactory sqlSessionFactory;
	
	private final PersistenceExceptionTranslator exceptionTranslator;
	
	private final SqlSession sqlSessionProxy;
	
	public final ExecutorType executorType = ExecutorType.SIMPLE;
	
	public final Set<String> shardName;
	
	
	public ShardInfoImpl(Set<String> shardName, SqlSessionFactory sqlSessionFactory ){
		this.sqlSessionFactory = sqlSessionFactory;
		this.shardName = shardName;
		this.exceptionTranslator = new MyBatisExceptionTranslator(
	            sqlSessionFactory.getConfiguration().getEnvironment().getDataSource(), true);
		
		this.sqlSessionProxy = (SqlSession) Proxy.newProxyInstance(
		        SqlSessionFactory.class.getClassLoader(),
		        new Class[] { SqlSession.class },
		        new SqlSessionInterceptor());
	}
	
	private class SqlSessionInterceptor implements InvocationHandler {
	    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
	      final SqlSession sqlSession = SqlSessionUtils.getSqlSession(
	    		  ShardInfoImpl.this.sqlSessionFactory,
	    		  ShardInfoImpl.this.executorType ,
	    		  ShardInfoImpl.this.exceptionTranslator);
	      try {
	        Object result = method.invoke(sqlSession, args);
	        if (!isSqlSessionTransactional(sqlSession, ShardInfoImpl.this.sqlSessionFactory)) {
	          sqlSession.commit(true);
	        }
	        return result;
	      } catch (Throwable t) {
	        Throwable unwrapped = unwrapThrowable(t);
	        if (ShardInfoImpl.this.exceptionTranslator != null && unwrapped instanceof PersistenceException) {
	          Throwable translated = ShardInfoImpl.this.exceptionTranslator.translateExceptionIfPossible((PersistenceException) unwrapped);
	          if (translated != null) {
	            unwrapped = translated;
	          }
	        }
	        throw unwrapped;
	      } finally {
	        closeSqlSession(sqlSession, ShardInfoImpl.this.sqlSessionFactory);
	      }
	    }
	  }
	

	@Override
	public SqlSessionFactory getSqlSessionFactory() {
		return this.sqlSessionFactory;
	}

	@Override
	public SqlSession establishSqlSession() {
		return this.sqlSessionProxy;
	}

	@Override
	public Collection<String> getMappedStatementNames() {
		return getConfiguration().getMappedStatementNames();
	}

	@Override
	public boolean hasMapper(Class<?> type) {
		return getConfiguration().hasMapper(type);
	}

	@Override
	public Configuration getConfiguration() {
		return this.sqlSessionFactory.getConfiguration();
	}

	@Override
	public Set<String> getShardNames() {
		return shardName;
	}

}
