/**
 * Copyright (C) 2016 mybatis-sharding-core Project
 *               Author: Administrator
 *               Date: 2016年6月12日
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
package com.zdal.sharding.core.plugins;

import java.sql.Connection;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

import com.zdal.sharding.core.converter.ISqlConverter;
import com.zdal.sharding.core.converter.impl.SqlConverter;
import com.zdal.sharding.core.helper.RouterHelper;
import com.zdal.sharding.core.utils.ReflectUtil;
import com.zdal.sharding.core.utils.StringUtil;

/**
 * Name: ZDalPlugin.java
 * ProjectName: [mybatis-sharding-core]
 * Package: [com.zdal.sharding.core.plugins.ZDalPlugin.java]
 * Description: TODO  
 * 
 * @since JDK1.7
 * @see
 *
 * Author: @author: Chris
 * Date: 2016年6月12日 下午2:13:27
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
@Intercepts({ @Signature(type = StatementHandler.class, method = "prepare", args = { Connection.class, Integer.class }) })
public class ZDalPlugin implements Interceptor {
	
	private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();  
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();  
    private static final ReflectorFactory DEFAULT_REFLECTOR_FACTORY = new DefaultReflectorFactory();
	
	private ISqlConverter sqlConverter = new SqlConverter();
	
	public ISqlConverter getSqlConverter() {
		return sqlConverter;
	}

	public void setSqlConverter(ISqlConverter sqlConverter) {
		this.sqlConverter = sqlConverter;
	}

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
		Object obj = statementHandler.getParameterHandler().getParameterObject();
		final String sql = tryGetSql(statementHandler);
		RouterHelper.setRouterInThread(sql);
		
		String targetSql = sqlConverter.conver(sql,obj);

		if (!StringUtil.isEmptyOrNull(targetSql) && !sql.equals(targetSql)) {
			ReflectUtil.setFieldValue(statementHandler.getBoundSql(), "sql", targetSql);
		}
		return invocation.proceed();
	}
	
	
	private String tryGetSql(StatementHandler handler) {
		String sql = handler.getBoundSql().getSql();
		
		if (StringUtil.isEmptyOrNull(sql)) {
			MetaObject metaStatementHandler = MetaObject.forObject(handler, DEFAULT_OBJECT_FACTORY,  
	                DEFAULT_OBJECT_WRAPPER_FACTORY,DEFAULT_REFLECTOR_FACTORY ); 
			
			 // 分离代理对象链(由于目标类可能被多个拦截器拦截，从而形成多次代理，通过下面的两次循环可以分离出最原始的的目标类)
	        while (metaStatementHandler.hasGetter("h")) {  
	            Object object = metaStatementHandler.getValue("h");  
	            metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, 
	            		DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);  
	        }  
	        // 分离最后一个代理对象的目标类  
	        while (metaStatementHandler.hasGetter("target")) {  
	            Object object = metaStatementHandler.getValue("target");  
	            metaStatementHandler = MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, 
	            		DEFAULT_OBJECT_WRAPPER_FACTORY, DEFAULT_REFLECTOR_FACTORY);  
	        }  
	        sql = (String) metaStatementHandler.getValue("delegate.boundSql.sql");  
		}
		return sql;
	}

	@Override
	public Object plugin(Object target) {
		return Plugin.wrap(target, this);
	}

	@Override
	public void setProperties(Properties arg0) {
	}

}
